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

@Component({
    selector: 'app-my-tasks-histories-table',
    templateUrl: './my-tasks-histories-table.component.html',
    styleUrls: ['./my-tasks-histories-table.component.scss'],
    standalone: true,
    imports: [
        MatTable,
        MatSort,
        DatePipe,
        ...MaterialModules,
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
    public searchIsRunning = false;

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

        data.sort((a, b) => {
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

        this.elements = data;
    }

    private compare(a: number | string | undefined, b: number | string | undefined, isAsc: boolean) {
        return ((a ?? '') < (b ?? '') ? -1 : 1) * (isAsc ? 1 : -1);
    }

    initElements() {
        this.ELEMENTS = this.entries.map(entry => {
            this.total = this.entries.length;

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
            if (entry.processDefinitionKey) {
                item.processDefinitionKey = entry.processDefinitionKey;
            }

            // On conserve l'objet complet pour alimenter la page détail via Router state
            item.processHistoricInformation = entry;

            // Navigation vers la page de détail d'un historique
            // Route attendue: my-task-history-detail/:historicId
            if (item.id) {
                item.taskId = item.id;
                item.url = 'my-task-history-detail';
            }

            return item;
        });

        this.elements = this.ELEMENTS.slice();
        this.sortData(this.DEFAULT_SORT);
    }

    // tslint:disable-next-line:no-any type de retour any[] obligatoire
    getRouterLinkUrl(row: TaskHistoryItem): any[] {
        const url = row?.url;
        const taskId = row?.taskId ?? row?.id;

        if (url && taskId) {
            // Supporte les URLs avec segments (ex: 'task-history-detail/organization-process')
            const segments = url.split('/').filter(Boolean);
            return ['..', ...segments, taskId];
        }

        // Fallback (sécurité) : route explicite
        if (row?.processDefinitionKey && row?.id) {
            return ['..', 'task-history-detail', row.processDefinitionKey, row.id];
        }

        return null;
    }

    // tslint:disable-next-line:no-any type de retour any obligatoire
    getRouterLinkState(row: TaskHistoryItem): any {
        return {
            processHistoricInformation: row?.processHistoricInformation
        };
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
