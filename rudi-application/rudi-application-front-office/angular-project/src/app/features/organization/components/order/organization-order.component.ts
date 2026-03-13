import { NgClass, UpperCasePipe } from '@angular/common';
import {Component, EventEmitter, Output} from '@angular/core';
import {ExtendedModule} from '@angular/flex-layout/extended';
import {FlexModule} from '@angular/flex-layout/flex';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatMenu, MatMenuItem, MatMenuTrigger} from '@angular/material/menu';
import {Order, OrderItem} from '@features/organization/components/order/type';
import {TranslatePipe} from '@ngx-translate/core';

const LIST_ORDER: OrderItem[] = [
    {libelle: 'sortBox.producer.organization_name', order: 'name'},
    {libelle: 'sortBox.-producer.organization_name', order: '-name'},
    {libelle: 'sortBox.dataset_dates.updated', order: 'openingDate'},
    {libelle: 'sortBox.-dataset_dates.updated', order: '-openingDate'},
];

@Component({
    selector: 'app-organization-order',
    templateUrl: './organization-order.component.html',
    styleUrls: ['./organization-order.component.scss'],
    imports: [MatButton, MatMenuTrigger, MatIcon, MatMenu, FlexModule, MatMenuItem, NgClass, ExtendedModule, UpperCasePipe, TranslatePipe]
})
export class OrganizationOrderComponent {
    listOrder: OrderItem[];
    menuIsOpened: boolean;
    selectedItem: OrderItem;

    @Output() orderChangeEvent: EventEmitter<Order>;

    constructor() {
        this.orderChangeEvent = new EventEmitter();
        this.menuIsOpened = false;
        this.listOrder = LIST_ORDER;
        this.selectedItem = this.listOrder[3];
    }

    onSelectedItemChange($event: OrderItem) {
        this.selectedItem = $event;
        this.orderChangeEvent.emit($event.order);
    }

    toggleMenu(): void {
        this.menuIsOpened = !this.menuIsOpened;
    }
}
