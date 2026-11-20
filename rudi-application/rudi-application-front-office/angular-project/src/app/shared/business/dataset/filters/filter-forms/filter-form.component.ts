import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {FiltersService} from '@core/services/filters.service';
import {Filter} from '@core/services/filters/filter';
import {FilterPredicate} from '@shared/business/dataset/filters/filter-forms/filter-predicate';
import {Item} from '@shared/business/dataset/filters/filter-forms/item';
import {Observable, of, Subscription} from 'rxjs';
import {switchMap} from 'rxjs/operators';

@Component({
    template: '' // required by Angular
    ,
})
export abstract class FilterFormComponent<T, F extends Filter<T>, I extends Item> implements OnInit, OnDestroy {
    private static nextApplyButtonId = 0;

    @Input() ulClass?: string;
    @Input() formClass?: string;

    /**
     * Force une valeur pour ce filtre, pour éviter qu'on puisse le modifier.
     */
    @Input() forcedValue?: T;

    /**
     * Valeurs qui ne doivent pas être visibles dans la sélection.
     * Elles doivent être filtrées avec le prédicat {@link #filterItemsWithoutHiddenValuesPredicate} dans le ngOnInit du composant réel.
     */
    @Input() hiddenValues?: T[];

    readonly counter$: Observable<number>;
    @Output() submitFormEvent = new EventEmitter<void>();
    @Output() selectedItemsChange = new EventEmitter<I[]>();
    private valueSubscription?: Subscription;

    /**
     * Id unique du bouton "Appliquer", utilisé comme cible du lien d'évitement en tête de panneau
     * (RGAA : permettre de contourner la liste de cases à cocher/boutons radio pour atteindre directement
     * l'action de validation, sans avoir à tabuler à travers tous les items).
     */
    readonly applyButtonId = `filter-apply-button-${FilterFormComponent.nextApplyButtonId++}`;

    protected constructor(
        protected readonly filtersService: FiltersService
    ) {
        this.counter$ = this.filter.value$.pipe(
            switchMap(value => of(this.count(value) || null))
        );
    }

    _formGroup?: FormGroup;

    get formGroup(): FormGroup {
        return this._formGroup;
    }

    protected get filter(): F {
        return this.getFilterFrom(this.filtersService);
    }

    ngOnInit(): void {
        this.valueSubscription = this.filter.value$.subscribe(() => {
            if (this.formGroup) {
                this.revert();
                this.selectedItemsChange.emit(this.selectedItems);
            }
        });
        if (this.forcedValue !== undefined) {
            this.filter.forcedValue = this.forcedValue;
        }
    }

    ngOnDestroy(): void {
        if (this.forcedValue !== undefined) {
            this.filter.forcedValue = undefined;
        }
        this.valueSubscription?.unsubscribe();
    }

    submitForm(): void {
        this.filter.value = this.getValueFromFormGroup();
        this.submitFormEvent.emit();
    }

    /**
     * Lien d'évitement : place le focus directement sur le bouton "Appliquer" sans activer la navigation
     * native de l'ancre (même principe que {@link HeaderComponent#handleSkipLinkClick}).
     */
    handleSkipToApplyClick(event: Event): void {
        event.preventDefault();
        document.getElementById(this.applyButtonId)?.focus();
    }

    protected abstract get selectedItems(): I[];

    /**
     * Revert current value to value from filter. <b>Please verify, in each implementation, that this.formGroup is defined !</b>
     */
    abstract revert(): void;

    protected abstract getFilterFrom(filtersService: FiltersService): F;

    protected initFormGroup(): void {
        this._formGroup = this.buildFormGroup();
        this.selectedItemsChange.emit(this.selectedItems);
    }

    protected abstract buildFormGroup(): FormGroup;

    protected abstract getValueFromFormGroup(): T;

    protected abstract count(value: T): number;

    get submitIsEnabled(): boolean {
        return this.someItemsAreBeingSelected || this.filter.active;
    }

    /**
     * @returns ture if some items are being selected even if they have not been submitted yet
     */
    get someItemsAreBeingSelected(): boolean {
        return !this.filter.isEmptyValue(this.getValueFromFormGroup());
    }

    /**
     * Prédicat pour filter les items qui ne sont pas cachés.
     */
    get filterItemsWithoutHiddenValuesPredicate(): FilterPredicate<I> {
        return item => !this.hiddenValues?.find(hiddenValue => hiddenValue === item.value);
    }
}
