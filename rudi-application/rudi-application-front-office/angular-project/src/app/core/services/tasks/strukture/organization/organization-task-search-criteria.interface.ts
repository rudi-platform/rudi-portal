import {TaskSearchCriteria} from '@core/services/tasks/task-search-criteria.interface';

export interface OrganizationTaskSearchCriteria extends TaskSearchCriteria {
    title?: string;
}
