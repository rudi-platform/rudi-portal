import {DatePipe} from '@angular/common';
import {Component, Input, OnInit} from '@angular/core';
import {MatSort, MatSortHeader, Sort} from '@angular/material/sort';
import {
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatHeaderCellDef,
    MatHeaderRow,
    MatHeaderRowDef,
    MatRow,
    MatRowDef,
    MatTable
} from '@angular/material/table';
import {TaskHistoryItem} from '@features/personal-space/components/my-tasks-histories-tables/task-history-item';
import {TranslateService} from '@ngx-translate/core';
import {BackPaginationSort} from '@shared/core/common/back-pagination/back-pagination-sort';
import {SortTableInterface} from '@shared/core/common/back-pagination/sort-table-interface';
import {MaterialModules} from '@shared/shared.constant';
import {SharedModule} from '@shared/shared.module';
import {ProcessHistoricInformation} from 'micro_service_modules/api-bpmn';
import {NgxPaginationModule} from 'ngx-pagination';

@Component({
    selector: 'app-my-tasks-histories-table',
    templateUrl: './my-tasks-histories-table.component.html',
    styleUrls: ['./my-tasks-histories-table.component.scss'],
    standalone: true,
    imports: [
        NgxPaginationModule,
        MatTable,
        MatSort,
        DatePipe,
        MaterialModules,
        SharedModule,
        MatHeaderCell,
        MatCell,
        MatColumnDef,
        MatHeaderRow,
        MatRow,
        MatSortHeader,
        MatHeaderRowDef,
        MatRowDef,
        MatCellDef,
        MatHeaderCellDef
    ]
})
export class MyTasksHistoriesTableComponent implements OnInit {

    /**
     * identifiant à définir pour chaque tableau à instancier pour permettre la présence
     * de plusieurs paginator sur une seule page
     */
    @Input({required: true}) backPaginatorId: string;
    @Input({required: true}) entries: ProcessHistoricInformation[];
    @Input({required: true}) icon: string;
    @Input({required: true}) tableTitleKey: string;
    @Input() url: string;

    DEFAULT_SORT: Sort = {active: 'endDate', direction: 'desc'};

    /**
     * Nombre d'éléments par page
     */
    public ITEMS_PER_PAGE = 5;

    /**
     * Nombre total d'éléments
     */
    public total: number;

    /**
     * éléments à afficher, objets de type : Demandes réalisées (purement visuel)
     */
    public elements: TaskHistoryItem[] = [];
    public ELEMENTS: TaskHistoryItem[] = [];

    /***
     * Booléen d'état : recherche en cours, pour le loader du tableau
     */
    public searchIsRunning: boolean = false;

    /**
     * ensemble des colonnes à afficher, toutes les mêmes car tous les composants affichent des demandes
     */
    public displayedColumns: string[] = ['endDate', 'description', 'initiator', 'functionalStatus'];

    /**
     * Objet de pagination avec le back-end
     */
    public backPaginationSort = new BackPaginationSort();

    constructor(public readonly translateService: TranslateService,) {
    }

    ngOnInit(): void {
        this.initElements();
    }

    onPageChange(event: SortTableInterface) {
        this.backPaginationSort.currentPage = event.page;
    }

    sortData(sort: Sort) {
        const data = this.ELEMENTS.slice();
        if (!sort.active || sort.direction === '') {
            this.elements = data;
            return;
        }

        this.elements = data.sort((a, b) => {
            const isAsc = sort.direction === 'asc';
            switch (sort.active) {
                case 'endDate':
                    return this.compare(a.endDate?.getTime(), b.endDate?.getTime(), isAsc);
                case 'description':
                    return this.compare(a.description, b.description, isAsc);
                case 'initiator':
                    return this.compare(a.initiator, b.initiator, isAsc);
                case 'functionalStatus':
                    return this.compare(a.functionalStatus, b.functionalStatus, isAsc);
                default:
                    return 0;
            }
        });
    }

    private compare(a: number | string | undefined, b: number | string | undefined, isAsc: boolean) {
        return ((a ?? '') < (b ?? '') ? -1 : 1) * (isAsc ? 1 : -1);
    }

    initElements() {
        this.ELEMENTS = this.entries!.map(entry => {
            this.total = this.entries!.length;

            let item = {} as TaskHistoryItem;
            if (entry.endTime) {
                item.endDate = new Date(entry.endTime);
            }
            if (entry.description) {
                item.description = entry.description;
            }
            if (entry.startUser) {
                item.initiator = entry.startUser;
            }
            if (entry.functionnalStatus) {
                item.functionalStatus = entry.functionnalStatus;
            }
            if (entry.id) {
                item.id = entry.id;
            }

            return item;
        });

        this.elements = this.ELEMENTS.slice();
        this.sortData(this.DEFAULT_SORT);
    }

    protected getRouterLink(item: TaskHistoryItem): any[] {
        if (this.url && item.id) {
            return ['..', this.url, item.id];
        }
        return undefined;
    }

    protected getResultsMessage(): string {
        if (this.entries?.length > 1) {
            return this.translateService.instant(this.tableTitleKey + '.plural');
        }
        if (this.entries?.length === 1) {
            return this.translateService.instant(this.tableTitleKey + '.singular');
        }
        return undefined;
    }

    get header(): string {
        return this.translateService.instant(this.tableTitleKey + '.header');
    }


}
