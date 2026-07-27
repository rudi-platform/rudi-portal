import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MediaSize} from '@core/services/breakpoint-observer.service';
import {AccessStatusFiltersType} from '@core/services/filters/access-status-filters-type';
import {
    AccessStatusFilterItem
} from '@shared/business/dataset/filters/filter-forms/access-status-filter-form/access-status-filter-form.component';
import {Item} from '@shared/business/dataset/filters/filter-forms/item';
import {SimpleSkosConcept} from 'micro_service_modules/kos/kos-model';
import {MatToolbar} from '@angular/material/toolbar';
import {NgClass} from '@angular/common';
import {FilterMenuComponent} from '../../filters/filter-menu/filter-menu.component';
import {ThemesFilterFormComponent} from '../../filters/filter-forms/themes-filter-form/themes-filter-form.component';
import {ProducerNamesFilterFormComponent} from '../../filters/filter-forms/producer-names-filter-form/producer-names-filter-form.component';
import {DatesFilterFormComponent} from '../../filters/filter-forms/dates-filter-form/dates-filter-form.component';
import {AccessStatusFilterFormComponent} from '../../filters/filter-forms/access-status-filter-form/access-status-filter-form.component';

@Component({
    selector: 'app-dataset-list-banner',
    templateUrl: './dataset-list-banner.component.html',
    styleUrl: './dataset-list-banner.component.scss',
    imports: [
        MatToolbar,
        NgClass,
        FilterMenuComponent,
        ThemesFilterFormComponent,
        ProducerNamesFilterFormComponent,
        DatesFilterFormComponent,
        AccessStatusFilterFormComponent,
    ],
})
export class DatasetListBannerComponent {
    @Input() mediaSize: MediaSize;
    @Output() selectedDatesItemsChange = new EventEmitter<Item[]>();
    @Output() selectedProducerItemsChange = new EventEmitter<Item[]>();
    @Output() selectedThemeItemsChange = new EventEmitter<Item[]>();
    @Output() selectedAccessStatusItemsChange = new EventEmitter<AccessStatusFilterItem[]>();
    @Input() themes: SimpleSkosConcept[];
    @Input() producerNames: string[];
    @Input() accessStatusForcedValue: AccessStatusFiltersType;
    @Input() accessStatusHiddenValues: AccessStatusFiltersType[];
}

