import { DatePipe } from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {MatIcon} from '@angular/material/icon';
import {MatSort, MatSortHeader} from '@angular/material/sort';
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
import {MyRequestsService} from '@core/services/my-requests/my-requests.service';
import {ProcessDefinitionsKeyIconRegistryService} from '@core/services/process-definitions-key-icon-registry.service';
import {TranslatePipe} from '@ngx-translate/core';
import {BackPaginationComponent} from '@shared/core/common/back-pagination/back-pagination.component';
import {SortTableInterface} from '@shared/core/common/back-pagination/sort-table-interface';
import {LoaderComponent} from '@shared/core/common/loader/loader.component';
import {SearchCountComponent} from '@shared/core/search/search-count/search-count.component';
import {LinkedDataset, LinkedDatasetSearchCriteria, LinkedDatasetStatus} from 'micro_service_modules/projekt/projekt-api';
import {DatasetConfidentiality, PagedLinkedDatasetList} from 'micro_service_modules/projekt/projekt-model';
import {NgxPaginationModule} from 'ngx-pagination';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {AbstractMyRequestTableComponent} from '../abstract-my-request-table.component';
import {RequestItem} from '../request-item';

@Component({
    selector: 'app-my-linked-datasets',
    templateUrl: './my-linked-datasets.component.html',
    styleUrls: ['./my-linked-datasets.component.scss'],
    imports: [SearchCountComponent, MatTable, MatSort, MatColumnDef, MatHeaderCellDef, MatHeaderCell, MatSortHeader, MatCellDef, MatCell, MatIcon, MatHeaderRowDef, MatHeaderRow, MatRowDef, MatRow, LoaderComponent, BackPaginationComponent, DatePipe, TranslatePipe, NgxPaginationModule]
})
export class MyLinkedDatasetsComponent extends AbstractMyRequestTableComponent implements OnInit {

    static LINKED_DATASET_PAGINATOR_ID = 'linkedDatasetPaginator';

    constructor(myRequestsService: MyRequestsService,
                processDefinitionsKeyIconRegistryService: ProcessDefinitionsKeyIconRegistryService) {
        super(myRequestsService, processDefinitionsKeyIconRegistryService);
        this.backPaginatorId = MyLinkedDatasetsComponent.LINKED_DATASET_PAGINATOR_ID;
    }

    ngOnInit(): void {
        const defaultSortTable: SortTableInterface = {order: this.DEFAULT_SORT_ORDER, page: 1};
        super.loadContent(defaultSortTable);
    }

    protected getMyElements(offset: number, limit: number, order: string): Observable<RequestItem[]> {
        const linkedDatasetSearchCriteria: LinkedDatasetSearchCriteria = {};
        linkedDatasetSearchCriteria.offset = offset;
        linkedDatasetSearchCriteria.limit = limit;
        linkedDatasetSearchCriteria.order = order;
        linkedDatasetSearchCriteria.datasetConfidentiality = DatasetConfidentiality.Restricted;
        linkedDatasetSearchCriteria.status = [LinkedDatasetStatus.Validated, LinkedDatasetStatus.Cancelled];
        return this.myRequestsService.searchMyFinishedLinkedDatasets(linkedDatasetSearchCriteria).pipe(
            map((page: PagedLinkedDatasetList) => {
                this.total = page.total;
                return page.elements.map((link: LinkedDataset) => {
                    return {
                        updatedDate: new Date(link.updated_date),
                        initiator: link.initiator,
                        title: link.description,
                        functionalStatus: link.functional_status
                    } as RequestItem;
                });
            })
        );
    }
}
