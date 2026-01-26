import {DatePipe, NgIf} from '@angular/common';
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
import {PagedLinkedDatasetList} from 'micro_service_modules/projekt/projekt-model';
import {SelfdataInformationRequestSearchCriteria, SelfdataInformationRequestStatus} from 'micro_service_modules/selfdata/selfdata-api';
import {SelfdataInformationRequest} from 'micro_service_modules/selfdata/selfdata-model';
import {NgxPaginationModule} from 'ngx-pagination';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {AbstractMyRequestTableComponent} from '../abstract-my-request-table.component';
import {RequestItem} from '../request-item';

@Component({
    selector: 'app-my-selfdata-requests',
    templateUrl: './my-selfdata-requests.component.html',
    styleUrls: ['./my-selfdata-requests.component.scss'],
    imports: [SearchCountComponent, MatTable, MatSort, MatColumnDef, MatHeaderCellDef, MatHeaderCell, MatSortHeader, MatCellDef, MatCell, MatIcon, MatHeaderRowDef, MatHeaderRow, MatRowDef, MatRow, NgIf, LoaderComponent, BackPaginationComponent, DatePipe, TranslatePipe, NgxPaginationModule]
})
export class MySelfdataRequestsComponent extends AbstractMyRequestTableComponent implements OnInit {

    static SELFDATA_INFORMATION_REQUEST_PAGINATOR_ID = 'selfdataInformationRequestPaginator';

    constructor(myRequestsService: MyRequestsService,
                processDefinitionsKeyIconRegistryService: ProcessDefinitionsKeyIconRegistryService) {
        super(myRequestsService, processDefinitionsKeyIconRegistryService);
        this.backPaginatorId = MySelfdataRequestsComponent.SELFDATA_INFORMATION_REQUEST_PAGINATOR_ID;
    }

    ngOnInit(): void {
        const defaultSortTable: SortTableInterface = {order: this.DEFAULT_SORT_ORDER, page: 1};
        super.loadContent(defaultSortTable);
    }

    protected getMyElements(offset: number, limit: number, order: string): Observable<RequestItem[]> {
        const selfdataInformationRequestSearchCriteria: SelfdataInformationRequestSearchCriteria = {};
        selfdataInformationRequestSearchCriteria.offset = offset;
        selfdataInformationRequestSearchCriteria.limit = limit;
        selfdataInformationRequestSearchCriteria.order = order;
        selfdataInformationRequestSearchCriteria.status = [
            SelfdataInformationRequestStatus.Completed,
            SelfdataInformationRequestStatus.Cancelled,
            SelfdataInformationRequestStatus.Rejected
        ];
        return this.myRequestsService.searchMyFinishedSelfdataInformationRequests(selfdataInformationRequestSearchCriteria).pipe(
            map((page: PagedLinkedDatasetList) => {
                this.total = page.total;
                return page.elements.map((selfdataRequest: SelfdataInformationRequest) => {
                    return {
                        updatedDate: new Date(selfdataRequest.updated_date),
                        initiator: selfdataRequest.initiator,
                        title: selfdataRequest.description,
                        functionalStatus: selfdataRequest.functional_status
                    } as RequestItem;
                });
            })
        );
    }
}
