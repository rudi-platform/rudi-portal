import {AsyncPipe} from '@angular/common';
import {Component, ElementRef, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FiltersService} from '@core/services/filters.service';
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
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {SearchBoxComponent} from '@shared/core/search/search-box/search-box.component';
import {OrganizationBean} from 'micro_service_modules/strukture/api-strukture';
import {Observable, Subscription} from 'rxjs';

@Component({
    selector: 'app-list-container-organization',
    templateUrl: './list-container.component.html',
    styleUrls: ['./list-container.component.scss'],
    imports: [
        MatToolbar,
        OrganizationOrderComponent,
        ListOrganizationCardComponent,
        AsyncPipe,
        TranslatePipe,
        SearchBoxComponent,
        LoaderComponent
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

    searchedTerm = '';
    isLoading = true;
    displayedTotal: number | null = null;

    @Input() mediaSize: MediaSize;
    @ViewChild('resultsCount') resultsCount?: ElementRef<HTMLElement>;

    private pendingSearchTerm = '';
    private pageChangePending = false;
    private loadingSubscription: Subscription;

    get resultsForParams(): { term: string } {
        return {term: this.searchedTerm};
    }

    constructor(
        private readonly searchOrganizationsService: SearchOrganizationsService,
        private readonly filtersService: FiltersService,
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
        this.pendingSearchTerm = $event?.trim() ?? '';
        this.searchOrganizationsService.onFreeTextChange($event);
    }

    onOrderChange($event: Order): void {
        this.searchOrganizationsService.currentSortOrder$.next($event);
    }

    onPageChange(page: number): void {
        this.pageChangePending = true;
        this.searchOrganizationsService.currentPage$.next(page);
    }

    ngOnDestroy(): void {
        this.loadingSubscription?.unsubscribe();
        this.searchOrganizationsService.complete();
    }

    ngOnInit(): void {
        // Le terme de recherche est partagé via FiltersService (singleton) et persiste d'une page à l'autre.
        // On le réinitialise à l'arrivée sur la page Organisations pour repartir d'un champ vide.
        this.filtersService.searchFilter.clear();
        this.searchedTerm = '';
        this.pendingSearchTerm = '';
        this.searchOrganizationsService.initSubscriptions();
        this.loadingSubscription = this.isLoadingCatalogue$.subscribe(isLoading => this.onLoadingChange(isLoading));
    }

    private onLoadingChange(isLoading: boolean): void {
        this.isLoading = isLoading;
        if (!isLoading) {
            // Captured with the total so the live region never pairs a new search term with the previous count
            this.displayedTotal = this.searchOrganizationsService.totalOrganizations$.value;
            this.searchedTerm = this.pendingSearchTerm;
            if (this.pageChangePending) {
                this.pageChangePending = false;
                this.resultsCount?.nativeElement.focus();
            }
        }
    }
}
