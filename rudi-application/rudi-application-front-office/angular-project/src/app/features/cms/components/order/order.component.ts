import {Component, EventEmitter, Input, Output} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {Item} from '@shared/business/dataset/filters/filter-forms/item';

interface OrderItem extends Item {
    libelle: string;
    value: string;
}

@Component({
    selector: 'cms-order',
    templateUrl: './order.component.html',
    styleUrl: './order.component.scss',
    standalone: false
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
