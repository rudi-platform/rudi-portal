import {Component} from '@angular/core';
import {FiltersService} from '@core/services/filters.service';
import {ArrayFilter} from '@core/services/filters/array-filter';
import {ArrayFilterFormComponent} from '@shared/business/dataset/filters/filter-forms/array-filter-form.component';
import {Item} from '@shared/business/dataset/filters/filter-forms/item';
import {NgIf, NgFor} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatButton} from '@angular/material/button';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'app-producer-names-filter-form',
    templateUrl: './producer-names-filter-form.component.html',
    styleUrls: ['./producer-names-filter-form.component.scss'],
    imports: [NgIf, FormsModule, ReactiveFormsModule, NgFor, MatCheckbox, MatButton, TranslatePipe]
})
export class ProducerNamesFilterFormComponent extends ArrayFilterFormComponent<string> {

    constructor(
        protected readonly filtersService: FiltersService
    ) {
        super(filtersService);
    }

    get formArrayName(): string {
        return 'prods';
    }

    protected get formGroupName(): string {
        return 'producer';
    }

    protected getItemFromValue(value: string): Item {
        return {
            name: value,
            value
        };
    }

    protected getFilterFrom(filtersService: FiltersService): ArrayFilter {
        return filtersService.producerNamesFilter;
    }

}
