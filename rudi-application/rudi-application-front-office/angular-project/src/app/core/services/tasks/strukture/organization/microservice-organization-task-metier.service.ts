import {OrganizationTaskSearchCriteria} from '@core/services/tasks/strukture/organization/organization-task-search-criteria.interface';
import {Task} from 'micro_service_modules/api-bpmn';
import {OrganizationStatus, TaskService as OrganizationTaskService} from 'micro_service_modules/strukture/api-strukture';
import {Observable} from 'rxjs';
import {TaskMetierService} from 'src/app/core/services/tasks/task-metier.service';

export abstract class MicroserviceOrganizationTaskMetierService<T> extends TaskMetierService<T> {

    protected constructor(readonly organizationTaskService: OrganizationTaskService) {
        super();
    }

    searchMicroserviceTasks(searchCriteria: OrganizationTaskSearchCriteria): Observable<Task[]> {
        return this.organizationTaskService.searchTasks(
            searchCriteria.title,
            searchCriteria.description,
            searchCriteria.processDefinitionKeys,
            searchCriteria.status,
            searchCriteria.fonctionalStatus,
            OrganizationStatus.Draft,
            false
        );
    }

    getTask(taskId): Observable<Task> {
        return this.organizationTaskService.getTask(taskId);
    }
}
