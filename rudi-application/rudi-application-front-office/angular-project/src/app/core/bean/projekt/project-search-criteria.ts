import {SearchCriteria} from '@core/bean/search-criteria';
import {ProjectStatus, TargetAudience} from 'micro_service_modules/projekt/projekt-model';

export class ProjectSearchCriteria extends SearchCriteria {
    dataset_uuids?: string[];
    linked_dataset_uuids?: string[];
    owner_uuids?: string[];
    project_uuids?: string[];
    status?: ProjectStatus[];
    themes?: string[];
    keywords?: string[];
    targetAudiences?: TargetAudience[];
}
