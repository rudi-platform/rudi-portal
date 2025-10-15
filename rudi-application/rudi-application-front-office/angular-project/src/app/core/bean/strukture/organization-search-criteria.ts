import {SearchCriteria} from '@core/bean/search-criteria';
import {Status} from 'micro_service_modules/api-bpmn';
import {OrganizationStatus} from 'micro_service_modules/strukture/strukture-model';

export class OrganizationSearchCriteria extends SearchCriteria {
    uuid?: string;
    name?: string;
    active?: boolean;
    userUuid?: string;
    organizationStatus?: OrganizationStatus;
    status?: Status;
}
