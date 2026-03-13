import {Component, EventEmitter, Input, Output} from '@angular/core';
import {BreakpointObserverService, NgClassObject} from '@core/services/breakpoint-observer.service';
import {OrganizationBean} from 'micro_service_modules/strukture/strukture-model';
import { NgClass } from '@angular/common';
import {OrganizationCardComponent} from '../organization-card/organization-card.component';
import {NgxPaginationModule} from 'ngx-pagination';
import {ExtendedModule} from '@angular/flex-layout/extended';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'app-list-organization-card',
    templateUrl: './list-organization-card.component.html',
    styleUrls: ['./list-organization-card.component.scss'],
    imports: [OrganizationCardComponent, NgxPaginationModule, NgClass, ExtendedModule, TranslatePipe]
})
export class ListOrganizationCardComponent {
    @Input() organizations: OrganizationBean[];
    @Input() totalItems: number;
    @Input() itemsPerPage: number;
    @Input() currentPage: number;
    @Input() datasetCountLoading: boolean;
    @Input() projectCountLoading: boolean;

    @Output() pageChangeEvent: EventEmitter<number>;

    constructor(
        private readonly breakpointObserver: BreakpointObserverService
    ) {
        this.organizations = [];
        this.totalItems = 0;
        this.itemsPerPage = 10;
        this.currentPage = 1;

        this.pageChangeEvent = new EventEmitter();
    }

    get paginationControlsNgClass(): NgClassObject {
        return this.breakpointObserver.getNgClassFromMediaSize('pagination-spacing');
    }

    onPageChange($event: number): void {
        this.currentPage = $event;
        this.pageChangeEvent.emit($event);
    }
}
