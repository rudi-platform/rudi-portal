import {
    ExternalAccountRequestTaskSearchCriteria
} from '@core/services/tasks/acl/external-account-request-task-search-criteria.interface';
import {Task} from 'micro_service_modules/api-bpmn';
import {TaskService as ExternalAccountRequestTaskService} from 'micro_service_modules/acl/acl-api';
import {Observable} from 'rxjs';
import {TaskMetierService} from 'src/app/core/services/tasks/task-metier.service';

export abstract class MicroserviceExternalAccountRequestTaskMetierService<T> extends TaskMetierService<T> {

    protected constructor(readonly externalAccountRequestTaskService: ExternalAccountRequestTaskService) {
        super();
    }

    searchMicroserviceTasks(searchCriteria: ExternalAccountRequestTaskSearchCriteria): Observable<Task[]> {
        return this.externalAccountRequestTaskService.searchTasks(
            searchCriteria.description,
            searchCriteria.processDefinitionKeys,
            searchCriteria.status,
            searchCriteria.fonctionalStatus,
            searchCriteria.asAdmin,
            searchCriteria.userLogin
        );
    }

    getTask(taskId): Observable<Task> {
        return this.externalAccountRequestTaskService.getTask(taskId);
    }
}
