import {Component} from '@angular/core';
import {FiltersService} from '@core/services/filters.service';
import {TranslateService, TranslatePipe} from '@ngx-translate/core';
import {OrderFilterFormComponent} from '@shared/business/dataset/filters/filter-forms/order-filter-form/order-filter-form.component';
import {MatButton} from '@angular/material/button';
import {MatMenuTrigger, MatMenu, MatMenuItem} from '@angular/material/menu';
import { NgClass, UpperCasePipe } from '@angular/common';
import {MatIcon} from '@angular/material/icon';

@Component({
    selector: 'app-order',
    templateUrl: './order.component.html',
    styleUrl: './order.component.scss',
    imports: [
    MatButton,
    MatMenuTrigger,
    MatIcon,
    MatMenu,
    MatMenuItem,
    NgClass,
    UpperCasePipe,
    TranslatePipe
],
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
