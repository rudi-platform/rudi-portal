import {TaskSearchCriteria} from '@core/services/tasks/task-search-criteria.interface';

export interface ExternalAccountRequestTaskSearchCriteria extends TaskSearchCriteria {
    userLogin?: string;
}
