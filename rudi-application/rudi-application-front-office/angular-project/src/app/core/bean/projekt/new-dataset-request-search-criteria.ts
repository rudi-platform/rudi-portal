import {SearchCriteria} from '@core/bean/search-criteria';
import {NewDatasetRequestStatus} from 'micro_service_modules/projekt/projekt-model';

export class NewDatasetRequestSearchCriteria extends SearchCriteria {
    status?: NewDatasetRequestStatus[];
}

