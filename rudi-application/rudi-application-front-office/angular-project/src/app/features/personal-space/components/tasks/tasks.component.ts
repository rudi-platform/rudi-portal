import {DatePipe} from '@angular/common';
import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {MatIcon} from '@angular/material/icon';
import {MatSort, MatSortHeader, SortDirection} from '@angular/material/sort';
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
    MatTable,
    MatTableDataSource
} from '@angular/material/table';
import {RouterLink} from '@angular/router';
import {ProcessDefinitionsKeyIconRegistryService} from '@core/services/process-definitions-key-icon-registry.service';
import {RequestToStudy} from '@core/services/tasks-aggregator/request-to-study.interface';
import {ProcessDefinitionEnum} from '@core/services/tasks/process-definition.enum';
import {TranslatePipe} from '@ngx-translate/core';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {PaginatorComponent, PaginatorComponent as PaginatorComponent_1} from '@shared/core/common/paginator/paginator.component';
import {SearchCountComponent} from '@shared/core/search/search-count/search-count.component';
import {PROCESS_DEFINITION_KEY_TYPES} from '@shared/models/title-icon-type';
import {compareDates, compareIgnoringCase} from '@shared/utils/comparators-utils';
import {ProcessDefinitionKeyTranslatePipe} from '@shared/utils/pipes/process-definition-key-translate.pipe';

@Component({
    selector: 'app-tasks',
    templateUrl: './tasks.component.html',
    styleUrls: ['./tasks.component.scss'],
    imports: [LoaderComponent, SearchCountComponent, MatTable, MatSort, MatColumnDef, MatHeaderCellDef, MatHeaderCell, MatSortHeader, MatCellDef, MatCell, MatIcon, MatHeaderRowDef, MatHeaderRow, MatRowDef, MatRow, RouterLink, PaginatorComponent_1, DatePipe, TranslatePipe, ProcessDefinitionKeyTranslatePipe]
})
export class TasksComponent implements OnInit {

    requestsToStudyDisplayedColumns: string[] = ['receivedDate', 'processDefinitionKey', 'description', 'status'];

    /**
     * Source de données du tableau.
     * Initialisée une seule fois pour maintenir la cohérence des tris personnalisés
     * lors de la destruction/recréation du composant par les onglets.
     */
    dataSource: MatTableDataSource<RequestToStudy> = new MatTableDataSource<RequestToStudy>([]);

    restrictedDatasetIcon = 'key_icon_88_blue_definition_key';
    selfdataIcon = 'self_data_icon_definition_key';
    newDatasetRequestIcon = 'nouvelles_donnees_definition_key';
    projectIcon = 'project_definition_key';
    organizationIcon = 'organization_definition_key';

    @Input() loading = false;

    /**
     * Émet un signal pour demander au parent de rafraîchir les données.
     * Utile pour garantir la fraîcheur des données à chaque ré-affichage de l'onglet.
     */
    @Output() reload = new EventEmitter<void>();

    @Input()
    set requestsToStudy(requestsToStudy: RequestToStudy[]) {
        this._requestsToStudy = requestsToStudy;
        // Mise à jour des données de la source existante au lieu de recréer une dataSource
        this.dataSource.data = requestsToStudy || [];
    }


    /**
     * Utilisation d'un setter pour ViewChild : garantit que le paginateur est lié
     * à la dataSource dès qu'Angular l'instancie dans le DOM, même après un switch d'onglet.
     */
    @ViewChild(PaginatorComponent) set paginator(paginator: PaginatorComponent) {
        this.dataSource.paginator = paginator;
    }

    /**
     * Utilisation d'un setter pour ViewChild : garantit que le moteur de tri est lié
     * à la dataSource dès qu'il est disponible dans la vue.
     */
    @ViewChild(MatSort) set sort(sort: MatSort) {
        this.dataSource.sort = sort;
    }

    private _requestsToStudy: RequestToStudy[];

    get requestsToStudy(): RequestToStudy[] {
        return this._requestsToStudy;
    }

    constructor(
        processDefinitionsKeyIconRegistryService: ProcessDefinitionsKeyIconRegistryService,
        private readonly processDefinitionKeyTranslatePipe: ProcessDefinitionKeyTranslatePipe
    ) {
        processDefinitionsKeyIconRegistryService.addAllSvgIcons(PROCESS_DEFINITION_KEY_TYPES);
        this.sortData();
    }

    /**
     * Déclenche la demande de rechargement des données à l'initialisation du composant.
     * Comme le composant est recréé à chaque clic sur l'onglet, cela assure un rafraîchissement constant.
     */
    ngOnInit(): void {
        this.reload.emit();
    }

    /**
     * Définit la logique de tri personnalisé pour les colonnes spécifiques (dates, énumérations traduites).
     */
    private sortData(): void {
        this.dataSource.sortData = (data: RequestToStudy[], sort: MatSort) => {
            return data.sort((a: RequestToStudy, b: RequestToStudy) => {
                if (!sort.active || sort.direction === '') {
                    return 0;
                }
                if (sort.active === 'processDefinitionKey') {
                    return this.compareProcessDefinitions(a[sort.active], b[sort.active], sort.direction);
                } else if (sort.active === 'receivedDate') {
                    return compareDates(a[sort.active], b[sort.active], sort.direction);
                }
                return compareIgnoringCase(a[sort.active], b[sort.active], sort.direction);
            });
        };
    }

    // tslint:disable-next-line:no-any type de retour any[] obligatoire
    getRouterLinkUrl(request: RequestToStudy): any[] {
        if (request.url) {
            return ['..', request.url, request.taskId];
        }

        return null;
    }

    getIcon(value: RequestToStudy): string {
        let icon: string;
        if (value.processDefinitionKey === ProcessDefinitionEnum.LINKED_DATASET_PROCESS) {
            icon = this.restrictedDatasetIcon;
        } else if (value.processDefinitionKey === ProcessDefinitionEnum.NEW_DATASET_REQUEST_PROCESS) {
            icon = this.newDatasetRequestIcon;
        } else if (value.processDefinitionKey === ProcessDefinitionEnum.SELFDATA_INFORMATION_REQUEST_Process) {
            icon = this.selfdataIcon;
        } else if (value.processDefinitionKey === ProcessDefinitionEnum.PROJECT_PROCESS) {
            icon = this.projectIcon;
        } else if (value.processDefinitionKey === ProcessDefinitionEnum.ORGANIZATION_PROCESS) {
            icon = this.organizationIcon;
        } else if (value.processDefinitionKey === ProcessDefinitionEnum.LINKED_PRODUCER_PROCESS) {
            icon = this.organizationIcon;
        }

        return icon;
    }

    private compareProcessDefinitions(a: string, b: string, isAsc: SortDirection): number {
        const aString = this.processDefinitionKeyTranslatePipe.transform(a);
        const bString = this.processDefinitionKeyTranslatePipe.transform(b);
        return compareIgnoringCase(aString, bString, isAsc);
    }
}
