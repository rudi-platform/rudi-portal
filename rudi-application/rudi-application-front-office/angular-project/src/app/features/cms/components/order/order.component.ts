import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TranslateService, TranslatePipe} from '@ngx-translate/core';
import {Item} from '@shared/business/dataset/filters/filter-forms/item';
import {MatButton} from '@angular/material/button';
import {MatMenuTrigger, MatMenu, MatMenuItem} from '@angular/material/menu';
import { NgClass, UpperCasePipe } from '@angular/common';
import {MatIcon} from '@angular/material/icon';

interface OrderItem extends Item {
    libelle: string;
    value: string;
}

@Component({
    selector: 'cms-order',
    templateUrl: './order.component.html',
    styleUrl: './order.component.scss',
    imports: [MatButton, MatMenuTrigger, MatIcon, MatMenu, MatMenuItem, NgClass, UpperCasePipe, TranslatePipe]
})
export class CmsOrderComponent {

    @Input() items: OrderItem[] = [];
    @Output() orderChange = new EventEmitter<string>();
    menuIsOpened = false;

    constructor(
        protected readonly translateService: TranslateService
    ) {
    }


    private _selectedItem: OrderItem;

    get selectedItem(): OrderItem {
        if (!this._selectedItem) {
            return this.items[0];
        }
        return this._selectedItem;
    }

    set selectedItem(item: OrderItem) {
        this._selectedItem = item;
        this.orderChange.emit(item.value);
    }

    toggleMenu(): void {
        this.menuIsOpened = !this.menuIsOpened;
    }
}
