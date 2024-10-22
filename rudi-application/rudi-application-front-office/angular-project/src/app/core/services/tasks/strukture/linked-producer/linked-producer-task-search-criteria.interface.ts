import {TaskSearchCriteria} from '@core/services/tasks/task-search-criteria.interface';

export interface LinkedProducerTaskSearchCriteria extends TaskSearchCriteria {
    title?: string;
}
