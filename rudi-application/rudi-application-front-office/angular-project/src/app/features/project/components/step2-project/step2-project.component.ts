import {Component, DestroyRef, inject, Input, OnInit} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {FormGroup, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatOption} from '@angular/material/core';

import {MatError, MatFormField} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatRadioButton, MatRadioChange, MatRadioGroup} from '@angular/material/radio';
import {MatSelect} from '@angular/material/select';
import {MatSlideToggle} from '@angular/material/slide-toggle';
import {TranslatePipe} from '@ngx-translate/core';
import {MultiSelectAutocompleteComponent} from '@shared/core/search/multi-select-autocomplete/multi-select-autocomplete.component';
import {SearchAutocompleteItem} from '@shared/core/search/search-autocomplete/search-autocomplete-item.interface';
import {OwnerType} from 'micro_service_modules/projekt/projekt-model';
import {OrganizationService, PagedOrganizationBeanList} from 'micro_service_modules/strukture/api-strukture';
import {OrganizationItem} from '../../model/organization-item';

@Component({
    selector: 'app-step2-project',
    templateUrl: './step2-project.component.html',
    styleUrls: ['./step2-project.component.scss'],
    imports: [FormsModule, ReactiveFormsModule, MatRadioGroup, MatRadioButton,
        MatSlideToggle, MatFormField, MatInput, MatSelect, MatOption, MatError, TranslatePipe, MultiSelectAutocompleteComponent]
})
export class Step2ProjectComponent implements OnInit {

    private readonly organizationService = inject(OrganizationService);
    private readonly destroyRef = inject(DestroyRef);

    @Input()
    public step2FormGroup: FormGroup;

    @Input()
    public organizationItems: OrganizationItem[];

    hasNoOrganizationError: boolean;

    /** Organisations partenaires : checkbox activée ? */
    relatedOrganizationsEnabled = false;

    /** Items affichés dans le multi-select (résultats filtrés localement) */
    relatedOrganizationsAutocompleteItems: SearchAutocompleteItem<string>[] = [];

    /** Chargement en cours */
    relatedOrganizationsLoading = false;

    /** Toutes les organisations chargées depuis l'API (une seule fois) */
    private allOrganizations: SearchAutocompleteItem<string>[] = [];

    /** Indique si les organisations ont déjà été chargées */
    private organizationsLoaded = false;

    /** UUIDs des organisations partenaires sélectionnées */
    selectedRelatedOrganizationUuids: string[] = [];

    /** UUIDs à exclure du multi-select (organisation owner) */
    ownerExcludedUuids: string[] = [];

    /** UUID de l'organisation owner à exclure des partenaires */
    private get ownerOrganizationUuid(): string | null {
        if (this.step2FormGroup.get('ownerType')?.value === OwnerType.Organization) {
            return this.step2FormGroup.get('organizationUuid')?.value || null;
        }
        return null;
    }

    ngOnInit(): void {
        this.hasNoOrganizationError = false;

        // Surveiller les changements d'organisation owner pour mettre à jour l'exclusion
        this.step2FormGroup.get('organizationUuid')?.valueChanges
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(() => this.updateOwnerExclusion());
        this.step2FormGroup.get('ownerType')?.valueChanges
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(() => this.updateOwnerExclusion());
    }

    onChangeOwnerType($event: MatRadioChange): void {
        const ownerType = $event.value as OwnerType;
        this.hasNoOrganizationError = ownerType === OwnerType.Organization && !this.organizationItems?.length;
    }

    onToggleRelatedOrganizations(): void {
        this.relatedOrganizationsEnabled = !this.relatedOrganizationsEnabled;
        if (!this.relatedOrganizationsEnabled) {
            this.selectedRelatedOrganizationUuids = [];
            this.step2FormGroup.get('relatedOrganizationUuids')?.setValue([]);
        }
    }

    onSearchRelatedOrganizations(searchText: string): void {
        if (!this.organizationsLoaded) {
            // Premier appel : charger toutes les organisations depuis l'API
            this.relatedOrganizationsLoading = true;
            this.organizationService.searchPublicOrganizationsBeans(
                undefined,                                    // name (toutes)
                [],                                           // uuids
                [],                                           // excludedUuids
                false,                                        // full
                undefined,                                    // active
                0,                                            // offset
                10000,                                        // limit (toutes)
                'name',                                       // order (tri alphabétique)
            ).subscribe({
                next: (result: PagedOrganizationBeanList) => {
                    this.allOrganizations = (result.elements || []).map(org => ({
                        label: org.name,
                        value: org.uuid,
                    }));
                    this.organizationsLoaded = true;
                    this.relatedOrganizationsAutocompleteItems = this.filterItems(searchText);
                    this.relatedOrganizationsLoading = false;
                },
                error: () => {
                    this.relatedOrganizationsLoading = false;
                },
            });
        } else {
            // Filtrage local uniquement
            this.relatedOrganizationsAutocompleteItems = this.filterItems(searchText);
        }
    }

    onRelatedOrganizationsChanged(uuids: string[]): void {
        this.selectedRelatedOrganizationUuids = uuids;
        this.step2FormGroup.get('relatedOrganizationUuids')?.setValue(uuids);
    }

    /** Met à jour l'exclusion de l'organisation owner dans la liste des partenaires */
    private updateOwnerExclusion(): void {
        const uuid = this.ownerOrganizationUuid;
        this.ownerExcludedUuids = uuid ? [uuid] : [];

        // Re-filtrer la liste affichée
        this.relatedOrganizationsAutocompleteItems = this.filterItems('');
    }

    /** Filtre les organisations par texte de recherche et exclut l'owner */
    private filterItems(searchText: string): SearchAutocompleteItem<string>[] {
        const excludeUuid = this.ownerOrganizationUuid;
        const lowerSearch = searchText?.toLowerCase() || '';
        return this.allOrganizations.filter(item => {
            if (excludeUuid && item.value === excludeUuid) {
                return false;
            }
            return !lowerSearch || item.label.toLowerCase().includes(lowerSearch);
        });
    }

}
