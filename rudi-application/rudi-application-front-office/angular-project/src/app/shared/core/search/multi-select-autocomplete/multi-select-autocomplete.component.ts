import {LiveAnnouncer} from '@angular/cdk/a11y';
import {Component, ElementRef, EventEmitter, HostListener, Inject, Input, NgZone, OnChanges, OnDestroy, OnInit, Output, QueryList, SimpleChanges, ViewChild, ViewChildren, DOCUMENT} from '@angular/core';

import {FormBuilder, FormControl, ReactiveFormsModule} from '@angular/forms';
import {MatAutocomplete, MatAutocompleteSelectedEvent, MatAutocompleteTrigger} from '@angular/material/autocomplete';
import {MatOption} from '@angular/material/core';
import {MatFormField, MatSuffix} from '@angular/material/form-field';
import {MatChip, MatChipRemove} from '@angular/material/chips';
import {MatIcon} from '@angular/material/icon';
import {MatInput} from '@angular/material/input';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {SearchAutocompleteItem} from '@shared/core/search/search-autocomplete/search-autocomplete-item.interface';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {Subject, Subscription} from 'rxjs';
import {debounceTime, distinctUntilChanged, tap} from 'rxjs/operators';

const DEFAULT_MAX_SELECTION = 9;
// Pour surcharger si besoin
const DEFAULT_MIN_SEARCH_LENGTH = 1;

@Component({
    selector: 'app-multi-select-autocomplete',
    templateUrl: 'multi-select-autocomplete.component.html',
    styleUrls: ['multi-select-autocomplete.component.scss'],
    imports: [
        ReactiveFormsModule,
        MatAutocompleteTrigger,
        MatAutocomplete,
        MatOption,
        MatFormField,
        MatSuffix,
        MatInput,
        MatChip,
        MatChipRemove,
        MatIcon,
        MatProgressSpinner,
        TranslatePipe,
    ],
})
export class MultiSelectAutocompleteComponent<T> implements OnInit, OnChanges, OnDestroy {

    @ViewChild(MatAutocompleteTrigger) autocompleteTrigger: MatAutocompleteTrigger;
    @ViewChild('chipInput') chipInputRef: ElementRef<HTMLInputElement>;
    @ViewChildren(MatChipRemove, {read: ElementRef}) removeButtons: QueryList<ElementRef<HTMLButtonElement>>;

    formControl: FormControl;

    /** Items affichés dans le dropdown (fournis par le parent) */
    @Input() autocompleteItems: SearchAutocompleteItem<T>[] = [];

    @Input() placeholder = '';

    /** Libellé affiché au-dessus du champ */
    @Input() label = '';

    @Input() maxSelection = DEFAULT_MAX_SELECTION;

    @Input() minimumAutocompleteLength = DEFAULT_MIN_SEARCH_LENGTH;

    @Input() loading = false;

    /** Clé i18n pour le message de limite atteinte */
    @Input() maxSelectionMessageKey = 'multiSelect.maxSelectionReached';

    /** Valeurs à exclure de la sélection (retirées automatiquement si déjà sélectionnées) */
    @Input() excludedValues: T[] = [];

    /** Émet la chaîne de recherche quand l'utilisateur tape >= minLength caractères */
    @Output() searchTriggered = new EventEmitter<string>();

    /** Émet la liste des valeurs sélectionnées à chaque changement */
    @Output() selectionChanged = new EventEmitter<T[]>();

    /** Émet quand l'utilisateur scrolle en bas de la liste (pour lazy loading) */
    @Output() loadMore = new EventEmitter<void>();

    /** Items actuellement sélectionnés */
    selectedItems: SearchAutocompleteItem<T>[] = [];

    /** Subject dédié à la recherche */
    private readonly searchInput$ = new Subject<string>();
    private searchSubscription: Subscription;
    private scrollListener: (() => void) | null = null;

    constructor(
        private readonly formBuilder: FormBuilder,
        private readonly ngZone: NgZone,
        @Inject(DOCUMENT) private readonly doc: Document,
        private readonly liveAnnouncer: LiveAnnouncer,
        private readonly translateService: TranslateService,
    ) {}

    /** Empêche mat-autocomplete d'écrire la valeur sélectionnée dans l'input */
    displayFn = (): string => '';

    @HostListener('window:scroll')
    onWindowScroll(): void {
        if (this.autocompleteTrigger?.panelOpen) {
            this.autocompleteTrigger.updatePosition();
        }
    }

    ngOnInit(): void {
        this.formControl = this.formBuilder.control('', {updateOn: 'change'});
        this.setupAutocompleteEvent();
    }

    ngOnChanges(changes: SimpleChanges): void {
        // Retirer les items exclus de la sélection
        if (changes['excludedValues'] && this.excludedValues?.length) {
            const excluded = new Set(this.excludedValues);
            const before = this.selectedItems.length;
            this.selectedItems = this.selectedItems.filter(item => !excluded.has(item.value));
            if (this.selectedItems.length !== before) {
                this.updateDisplayValue();
                this.selectionChanged.emit(this.selectedItems.map(s => s.value));
            }
        }

        // Quand de nouveaux items arrivent et que le panel est ouvert
        if (changes['autocompleteItems'] && this.autocompleteTrigger?.panelOpen) {
            if (this.scrollListener) {
                this.loadMoreIfPanelNotFull();
            } else {
                // Attache le scroll listener si pas encore en place (cas où le panel était vide à l'ouverture)
                this.attachScrollListenerWhenReady();
            }
        }
    }

    /** Texte affiché dans le champ input : "nom, nom, nom" */
    get displayValue(): string {
        return this.selectedItems.map(item => item.label).join(', ');
    }

    /** Limite de sélection atteinte ? */
    get isMaxReached(): boolean {
        return this.selectedItems.length >= this.maxSelection;
    }

    /** Vérifie si un item est sélectionné */
    isSelected(item: SearchAutocompleteItem<T>): boolean {
        return this.selectedItems.some(selected => selected.value === item.value);
    }

    /** Toggle la sélection d'un item */
    toggleItem(item: SearchAutocompleteItem<T>, event: Event): void {
        if (typeof event?.stopPropagation === 'function') {
            event.stopPropagation();
            event.preventDefault();
        }

        const index = this.selectedItems.findIndex(selected => selected.value === item.value);
        if (index >= 0) {
            // Désélectionner
            this.selectedItems = this.selectedItems.filter((_, i) => i !== index);
            this.announce('multiSelect.itemRemoved', item);
        } else if (!this.isMaxReached) {
            // Sélectionner
            this.selectedItems = [...this.selectedItems, item];
            this.announce('multiSelect.itemAdded', item);
        }

        this.updateDisplayValue();
        this.selectionChanged.emit(this.selectedItems.map(s => s.value));

        // Garde le panel ouvert pour permettre la multi-sélection
        this.doc.defaultView.setTimeout(() => {
            this.autocompleteTrigger?.openPanel();
        });
    }

    /** Ouvre le panel et déclenche le chargement initial si l'input est vide */
    onInputFocus(): void {
        if (!this.autocompleteTrigger?.panelOpen) {
            this.autocompleteTrigger?.openPanel();
        }
        // Ne déclenche le chargement initial que si aucune donnée n'est déjà affichée
        if (!this.formControl.value && this.autocompleteItems.length === 0) {
            this.searchTriggered.emit('');
        }
    }

    /** Ouvre le panel via la flèche dropdown */
    onArrowClick(): void {
        this.autocompleteTrigger?.openPanel();
        if (!this.formControl.value && this.autocompleteItems.length === 0) {
            this.searchTriggered.emit('');
        }
    }

    /** Appelé quand le panel autocomplete s'ouvre */
    onPanelOpened(): void {
        // Attache un listener de scroll sur le panel pour l'infinite scroll
        this.attachScrollListenerWhenReady();
    }

    /** Appelé quand le panel autocomplete se ferme */
    onPanelClosed(): void {
        this.detachScrollListener();
    }

    /** Appelé à la sélection d'une option, à la souris comme au clavier (Entrée) */
    onOptionSelected(event: MatAutocompleteSelectedEvent): void {
        // Material only knows single selection: the real state is selectedItems, exposed by the option text
        event.option.deselect(false);
        this.toggleItem(event.option.value, undefined);
        // Remet l'input à vide : emitEvent:true pour que le Subject reçoive ''
        // et que distinctUntilChanged annule la recherche en cours
        this.formControl.setValue('');
        if (this.chipInputRef) {
            this.chipInputRef.nativeElement.value = '';
        }
    }

    ngOnDestroy(): void {
        this.detachScrollListener();
        this.searchSubscription?.unsubscribe();
    }

    /** Supprime un item sélectionné */
    removeItem(item: SearchAutocompleteItem<T>): void {
        const index = this.selectedItems.findIndex(selected => selected.value === item.value);
        this.selectedItems = this.selectedItems.filter(selected => selected.value !== item.value);
        this.updateDisplayValue();
        this.selectionChanged.emit(this.selectedItems.map(s => s.value));
        this.announce('multiSelect.itemRemoved', item);

        // The removed chip took the focus with it: move it to the next remove button, else back to the input
        this.doc.defaultView.setTimeout(() => {
            const buttons = this.removeButtons?.toArray() ?? [];
            const next = buttons[Math.min(index, buttons.length - 1)] ?? this.chipInputRef;
            next?.nativeElement.focus();
        });
    }

    private announce(key: string, item: SearchAutocompleteItem<T>): void {
        this.liveAnnouncer.announce(this.translateService.instant(key, {label: item.label}));
    }

    private updateDisplayValue(): void {
        // Les chips affichent les sélections, on vide l'input pour permettre une nouvelle recherche
        this.formControl.setValue('', {emitEvent: false});
        if (this.chipInputRef) {
            this.chipInputRef.nativeElement.value = '';
        }
    }

    private attachScrollListenerWhenReady(): void {
        // Petit délai pour laisser le CDK overlay rendre le panel dans le DOM
        this.ngZone.runOutsideAngular(() => {
            this.doc.defaultView.requestAnimationFrame(() => {
                const panel = this.doc.querySelector('.multi-select-panel') as HTMLElement;
                if (panel) {
                    this.scrollListener = this.handlePanelScroll.bind(this);
                    panel.addEventListener('scroll', this.scrollListener);
                    this.loadMoreIfPanelNotFull();
                }
            });
        });
    }

    private handlePanelScroll(event: Event): void {
        if (this.loading) {
            return;
        }
        const element = event.target as HTMLElement;
        const threshold = 50;
        if (element.scrollHeight - element.scrollTop - element.clientHeight < threshold) {
            this.ngZone.run(() => this.loadMore.emit());
        }
    }

    private detachScrollListener(): void {
        if (this.scrollListener) {
            const panel = this.doc.querySelector('.multi-select-panel') as HTMLElement;
            panel?.removeEventListener('scroll', this.scrollListener);
            this.scrollListener = null;
        }
    }

    /** Charge plus d'items si le panel ne scrolle pas encore */
    private loadMoreIfPanelNotFull(): void {
        this.ngZone.runOutsideAngular(() => {
            this.doc.defaultView.requestAnimationFrame(() => {
                const panel = this.doc.querySelector('.multi-select-panel') as HTMLElement;
                if (panel && panel.scrollHeight <= panel.clientHeight && this.autocompleteItems.length > 0) {
                    this.ngZone.run(() => this.loadMore.emit());
                }
            });
        });
    }

    private setupAutocompleteEvent(): void {
        // Subject dédié : seules les saisies explicites y sont poussées,
        // pas les écritures internes de mat-autocomplete
        this.searchSubscription = this.searchInput$.pipe(
            distinctUntilChanged(),
            tap(() => {
                this.autocompleteItems = [];
            }),
            debounceTime(300),
        ).subscribe((term: string) => {
            if (term.length >= this.minimumAutocompleteLength || term.length === 0) {
                this.searchTriggered.emit(term);
            }
        });

        // Pousse uniquement les saisies utilisateur dans le Subject
        this.formControl.valueChanges.subscribe((value) => {
            if (typeof value === 'string') {
                this.searchInput$.next(value);
            }
        });
    }
}
