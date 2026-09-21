import {Component, Input} from '@angular/core';
import {FiltersService} from '@core/services/filters.service';
import {
    AccessStatusFilterItem
} from '@shared/business/dataset/filters/filter-forms/access-status-filter-form/access-status-filter-form.component';
import {Item} from '@shared/business/dataset/filters/filter-forms/item';

import {MatIcon} from '@angular/material/icon';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'app-filters-items-list',
    templateUrl: './filters-items-list.component.html',
    styleUrl: './filters-items-list.component.scss',
    imports: [
    MatIcon,
    TranslatePipe
],
})
export class FiltersItemsListComponent {
    @Input() selectedAccessStatusItems: AccessStatusFilterItem[];
    @Input() selectedProducerItems: Item[];
    @Input() selectedThemeItems: Item[];
    @Input() hasSelectedItems: boolean;

    constructor(
        public readonly filtersService: FiltersService
    ) {
    }

    /**
     * Filter not null value in selectedAccessStatusItems
     */
    get notNullSelectedAccessStatusItems(): AccessStatusFilterItem[] {
        this.selectedAccessStatusItems = this.selectedAccessStatusItems?.filter(
            selectedValue => selectedValue?.value != null
        ) ?? [];
        return this.selectedAccessStatusItems;
    }

    /**
     * Solution pour palier au cas particulier des filtres sur les status de restriction
     * A ameliorer plus tard, si d'autres filtres se rajoutent
     */
    hasSelectedAllOtherFiltre(): boolean {
        if (this.selectedThemeItems.length > 0) {
            return this.selectedThemeItems.some(value => value.value !== null);
        } else if (this.selectedProducerItems.length > 0) {
            return this.selectedProducerItems.some(value => value.value !== null);
        } else if (this.selectedAccessStatusItems.length > 0) {
            return this.selectedAccessStatusItems.some(value => value.value !== null);
        }
        return false;
    }


    deleteThemeFilter(theme: Item): void {
        this.filtersService.themesFilter.remove(theme.value);
    }

    deleteProducerFilter(producer: Item): void {
        this.filtersService.producerNamesFilter.remove(producer.name);
    }

    deleteAllFilters(): void {
        this.filtersService.deleteAllFilters();
    }

    deleteRestrictedAccessFilter(): void {
        this.filtersService.accessStatusFilter.clear();
    }
}
