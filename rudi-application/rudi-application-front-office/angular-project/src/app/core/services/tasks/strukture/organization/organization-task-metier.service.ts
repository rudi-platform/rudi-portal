import {Injectable} from '@angular/core';
import {
    MicroserviceOrganizationTaskMetierService
} from '@core/services/tasks/strukture/organization/microservice-organization-task-metier.service';
import {Task} from 'micro_service_modules/api-bpmn';
import {TaskService as OrganizationTaskService} from 'micro_service_modules/strukture/api-strukture';
import {Organization} from 'micro_service_modules/strukture/strukture-model';
import {Observable} from 'rxjs';
import {ObjectType} from 'src/app/core/services/tasks/object-type.enum';

@Injectable({
    providedIn: 'root'
})
export class OrganizationTaskMetierService extends MicroserviceOrganizationTaskMetierService<Organization> {

    protected objectType: ObjectType = ObjectType.ORGANIZATION;

    constructor(organizationTaskService: OrganizationTaskService) {
        super(organizationTaskService);
    }

    claimTask(taskId: string): Observable<Task> {
        return this.organizationTaskService.claimOrganizationTask(taskId);
    }

    doIt(taskId: string, actionName: string): Observable<Task> {
        return this.organizationTaskService.doItOrganization(taskId, actionName);
    }

    unclaimTask(taskId: string): Observable<Task> {
        return this.organizationTaskService.unclaimOrganizationTask(taskId);
    }

    updateTask(task: Task): Observable<Task> {
        return this.organizationTaskService.updateOrganizationTask(task);
    }

    createDraft(asset: Organization): Observable<Task> {
        return this.organizationTaskService.createOrganizationDraft(asset);
    }

    startTask(task: Task): Observable<Task> {
        return this.organizationTaskService.startOrganizationTask(task);
    }
}
