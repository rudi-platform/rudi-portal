import {SearchCriteria} from '@core/bean/search-criteria';
import {DatasetConfidentiality, LinkedDatasetStatus} from 'micro_service_modules/projekt/projekt-model';

export class LinkedDatasetSearchCriteria extends SearchCriteria {
    datasetConfidentiality?: DatasetConfidentiality;
    linkedDatasetStatuses?: LinkedDatasetStatus[];
}

