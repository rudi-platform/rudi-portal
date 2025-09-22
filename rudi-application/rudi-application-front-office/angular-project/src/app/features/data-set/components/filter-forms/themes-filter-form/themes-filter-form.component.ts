import {Component} from '@angular/core';
import {FiltersService} from '@core/services/filters.service';
import {ArrayFilter} from '@core/services/filters/array-filter';
import {SimpleSkosConcept} from 'micro_service_modules/kos/kos-model';
import {ArrayFilterFormComponent, Item} from '../array-filter-form.component';

@Component({
    selector: 'app-themes-filter-form',
    templateUrl: './themes-filter-form.component.html',
    styleUrls: ['./themes-filter-form.component.scss'],
    standalone: false
})
export class ThemesFilterFormComponent extends ArrayFilterFormComponent<SimpleSkosConcept> {

    constructor(filtersService: FiltersService) {
        super(filtersService);
    }

    get formArrayName(): string {
        return 'themes';
    }

    protected get formGroupName(): string {
        return 'thematic';
    }

    protected getItemFromValue(simpleSkosConcept: SimpleSkosConcept): Item {
        return {
            name: simpleSkosConcept.text,
            value: simpleSkosConcept.concept_code
        };
    }

    protected getFilterFrom(filtersService: FiltersService): ArrayFilter {
        return filtersService.themesFilter;
    }

}
