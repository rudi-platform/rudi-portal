import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {DEFAULT_PROJECT_ORDER, Order} from '@core/services/asset/project/projekt-metier.service';
import {BreakpointObserverService, MediaSize, NgClassObject} from '@core/services/breakpoint-observer.service';
import {ProjectListService} from '@core/services/project-list.service';
import {ProjectCatalogItem, ProjectCatalogItemPage} from '@features/project/model/project-catalog-item';

const FIRST_PAGE = 1;

@Component({
    selector: 'app-project-list',
    templateUrl: './project-list.component.html',
    styleUrls: ['./project-list.component.scss'],
    standalone: false
})
export class ProjectListComponent implements OnInit {
    mediaSize: MediaSize;
    projectList: ProjectCatalogItemPage;
    @Input() maxResultsPerPage = 12;
    @Input() ownerId: string;
    @Input() producerUuid: string;
    /**
     * Set fixed number of cards to be displayed in a row.
     * maximum = 12 (current limit in SCSS rule : .project-card-container-*-cards).
     * Default : automatic (based on screen width).
     */
    @Input() resultsPerRow: number | undefined;
    @Input() disableScrollOnPageChange = false;
    @Input() linkedDatasetsGlobalIds: string[];
    @Output() clickProject = new EventEmitter<ProjectCatalogItem>();
    @Output() runningSearch = new EventEmitter<boolean>();
    @Output() projectListChange = new EventEmitter<ProjectCatalogItemPage>();
    @Output() reuseListTotal = new EventEmitter<number>();

    readonly maxPageDesktop = 9;
    orderValue: Order;
    /** minimum = 5 */
    readonly maxPageMobile = 5;
    private currentPage = FIRST_PAGE;
    private offset = 0;
    // Indique si on affiche le loader pendant l'édition du courrier
    public isLoading = false;

    constructor(
        private readonly projectListService: ProjectListService,
        private readonly breakpointObserver: BreakpointObserverService,
    ) {
    }

    get page(): number {
        return this.currentPage;
    }

    set page(value: number) {
        if (value < FIRST_PAGE) {
            console.warn('Page number cannot be less than ' + FIRST_PAGE);
            value = FIRST_PAGE;
        }
        this.currentPage = value;
        this.offset = (this.currentPage - 1) * this.maxResultsPerPage;

        this.searchProjects(this.orderValue);
    }

    @Input() set order(order: Order) {
        if (!this.projectList) {
            // component is not init
            return;
        }
        this.orderValue = order;
        this.searchProjects(order);
    }

    get paginationControlsNgClass(): NgClassObject {
        return this.breakpointObserver.getNgClassFromMediaSize('pagination-spacing');
    }

    /**
     * @return ne renvoie jamais null (obligatoire pour le pipe paginate)
     */
    get projectListItems(): ProjectCatalogItem[] {
        return this.projectList.items ?? [];
    }

    ngOnInit(): void {
        this.mediaSize = this.breakpointObserver.getMediaSize();
        this.page = FIRST_PAGE;
    }

    /**
     * Fonction permettant la gestion la pagination
     */
    handlePageChange(page: number): void {
        this.page = page;
        if (!this.disableScrollOnPageChange) {
            window.scroll(0, 0);
        }
    }

    /**
     * Appel du service
     */
    searchProjects(order: Order = DEFAULT_PROJECT_ORDER): void {
        this.isLoading = true;
        setTimeout(() => this.runningSearch.emit(true)); // setTimeout to avoid ExpressionChangedAfterItHasBeenCheckedError
        this.projectListService.searchProjectsCatalog(this.linkedDatasetsGlobalIds, this.offset, this.maxResultsPerPage, order, this.producerUuid)
            .subscribe({
                next: (data: ProjectCatalogItemPage) => {
                    this.isLoading = false;
                    this.projectList = data;
                    this.projectListChange.emit(data);
                    this.reuseListTotal.emit(data.total);
                },
                error: error => {
                    console.error('searchProjects failed', error);
                    this.isLoading = false;
                },
                complete: () => {
                    this.runningSearch.emit(false);
                    this.isLoading = false;

                }
            });
    }

}
