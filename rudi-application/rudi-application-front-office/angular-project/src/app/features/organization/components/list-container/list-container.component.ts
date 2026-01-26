import {AsyncPipe, NgIf} from '@angular/common';
import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {MatToolbar} from '@angular/material/toolbar';
import {MediaSize} from '@core/services/breakpoint-observer.service';
import {OrganizationOrderComponent} from '@features/organization/components/order/organization-order.component';
import {Order} from '@features/organization/components/order/type';
import {TranslatePipe} from '@ngx-translate/core';
import {ListOrganizationCardComponent} from '@shared/business/organisation/list-organization-card/list-organization-card.component';
import {
    searchDefaultPageSize,
    SearchOrganizationsService
} from '@shared/business/organisation/list-organization-card/search-organizations.service';
import {OrganizationBean} from 'micro_service_modules/strukture/api-strukture';
import {Observable} from 'rxjs';
import {types} from 'sass';
import Error = types.Error;

@Component({
    selector: 'app-list-container-organization',
    templateUrl: './list-container.component.html',
    styleUrls: ['./list-container.component.scss'],
    imports: [
        NgIf,
        MatToolbar,
        OrganizationOrderComponent,
        ListOrganizationCardComponent,
        AsyncPipe,
        TranslatePipe
    ]
})
export class ListContainerComponent implements OnInit, OnDestroy {

    itemsPerPage: number;
    organizations$: Observable<OrganizationBean[]>;
    totalOrganizations$: Observable<number>;
    isLoadingCatalogue$: Observable<boolean>;
    datasetCountLoading$: Observable<boolean>;
    projectCountLoading$: Observable<boolean>;
    currentPage$: Observable<number>;

    @Input() mediaSize: MediaSize;

    constructor(
        private searchOrganizationsService: SearchOrganizationsService,
    ) {
        this.itemsPerPage = searchDefaultPageSize;
        this.organizations$ = searchOrganizationsService.organizations$;
        this.totalOrganizations$ = searchOrganizationsService.totalOrganizations$;
        this.isLoadingCatalogue$ = searchOrganizationsService.isLoadingCatalogue$;
        this.datasetCountLoading$ = searchOrganizationsService.datasetCountLoading$;
        this.projectCountLoading$ = searchOrganizationsService.projectsCountLoading$;
        this.currentPage$ = searchOrganizationsService.currentPage$;
    }

    onChangesSearchTerms($event: string): void {
        throw new Error('Search not implemented yet');
    }

    onOrderChange($event: Order): void {
        this.searchOrganizationsService.currentSortOrder$.next($event);
    }

    onPageChange(page: number): void {
        this.searchOrganizationsService.currentPage$.next(page);
    }

    ngOnDestroy(): void {
        this.searchOrganizationsService.complete();
    }

    ngOnInit(): void {
        this.searchOrganizationsService.initSubscriptions();
    }
}
