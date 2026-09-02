import {StampedSearchCriteria} from '@core/bean/stamped-search-criteria';
import {AddressType} from 'micro_service_modules/strukture/strukture-model';

export class AddressRoleSearchCriteria extends StampedSearchCriteria {
    code?: string;
    type?: AddressType;
}
