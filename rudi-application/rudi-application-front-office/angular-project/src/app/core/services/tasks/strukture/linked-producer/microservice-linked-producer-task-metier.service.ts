import {
    LinkedProducerTaskSearchCriteria
} from '@core/services/tasks/strukture/linked-producer/linked-producer-task-search-criteria.interface';
import {TaskMetierService} from '@core/services/tasks/task-metier.service';
import {Task} from 'micro_service_modules/api-bpmn';
import {LinkedProducerStatus, TaskService as LinkedProducerTaskService} from 'micro_service_modules/strukture/api-strukture';
import {Observable} from 'rxjs';

export abstract class MicroserviceLinkedProducerTaskMetierService<T> extends TaskMetierService<T> {

    protected constructor(readonly linkedProducerTaskService: LinkedProducerTaskService) {
        super();
    }

    searchMicroserviceTasks(searchCriteria: LinkedProducerTaskSearchCriteria): Observable<Task[]> {
        return this.linkedProducerTaskService.searchTasks(
            searchCriteria.title,
            searchCriteria.description,
            searchCriteria.processDefinitionKeys,
            searchCriteria.status,
            searchCriteria.fonctionalStatus,
            LinkedProducerStatus.Draft,
            false
        );
    }

    getTask(taskId): Observable<Task> {
        return this.linkedProducerTaskService.getTask(taskId);
    }
}
