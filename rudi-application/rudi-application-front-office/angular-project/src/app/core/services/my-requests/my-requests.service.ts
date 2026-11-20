import {Injectable} from '@angular/core';
import {LinkedDatasetSearchCriteria} from '@core/bean/projekt/linked-dataset-search-criteria';
import {NewDatasetRequestSearchCriteria} from '@core/bean/projekt/new-dataset-request-search-criteria';
import {SelfdataInformationRequestSearchCriteria} from '@core/bean/selfdata/selfdata-information-request-search-criteria';
import {ProjektService} from 'micro_service_modules/projekt/projekt-api';
import {PagedLinkedDatasetList} from 'micro_service_modules/projekt/projekt-model';
import {SelfdataService} from 'micro_service_modules/selfdata/selfdata-api';
import {Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class MyRequestsService {

    constructor(
        private readonly projektService: ProjektService,
        private readonly selfdataService: SelfdataService) {
    }

    /**
     * Recherche des linked datasets du user connecté qui ont finalisés leurs workflows
     * @param linkedDatasetSearchCriteria
     */
    public searchMyFinishedLinkedDatasets(linkedDatasetSearchCriteria: LinkedDatasetSearchCriteria): Observable<PagedLinkedDatasetList> {
        return this.projektService.searchMyLinkedDatasets(
            linkedDatasetSearchCriteria.datasetConfidentiality,
            linkedDatasetSearchCriteria.linkedDatasetStatuses,
            linkedDatasetSearchCriteria.offset,
            linkedDatasetSearchCriteria.limit,
            linkedDatasetSearchCriteria.order
        );
    }

    /**
     * Recherche des new datasets request du user connecté qui ont finalisés leurs workflows
     * @param newDatasetRequestSearchCriteria
     */
    public searchMyFinishedNewDatasetRequests(newDatasetRequestSearchCriteria: NewDatasetRequestSearchCriteria): Observable<PagedLinkedDatasetList> {
        return this.projektService.searchMyNewDatasetRequests(
            newDatasetRequestSearchCriteria.status
        );
    }

    /**
     * Recherche des demandes effectuées par l'utilisateur connecté qui ont finalisées leurs workflows
     * @param selfdataInformationRequestFrontendSearchCriteria
     */
    public searchMyFinishedSelfdataInformationRequests(selfdataInformationRequestFrontendSearchCriteria: SelfdataInformationRequestSearchCriteria) {
        return this.selfdataService.searchMySelfdataInformationRequests(
            selfdataInformationRequestFrontendSearchCriteria.datasetUuid,
            selfdataInformationRequestFrontendSearchCriteria.status,
            selfdataInformationRequestFrontendSearchCriteria.offset,
            selfdataInformationRequestFrontendSearchCriteria.limit,
            selfdataInformationRequestFrontendSearchCriteria.order
        );
    }
}
