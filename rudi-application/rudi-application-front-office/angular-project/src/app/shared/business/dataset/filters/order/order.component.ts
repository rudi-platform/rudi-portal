import {Component} from '@angular/core';
import {FiltersService} from '@core/services/filters.service';
import {TranslateService} from '@ngx-translate/core';
import {OrderFilterFormComponent} from '@shared/business/dataset/filters/filter-forms/order-filter-form/order-filter-form.component';

@Component({
    selector: 'app-order',
    templateUrl: './order.component.html',
    styleUrl: './order.component.scss',
    standalone: false,
})
export class OrderComponent extends OrderFilterFormComponent {
    menuIsOpened = false;

    constructor(
        protected readonly filtersService: FiltersService,
        protected readonly translateService: TranslateService
    ) {
        super(filtersService, translateService);
    }

    toggleMenu(): void {
        this.menuIsOpened = !this.menuIsOpened;
    }
}
