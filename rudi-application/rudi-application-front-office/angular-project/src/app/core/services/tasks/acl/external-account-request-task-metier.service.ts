import {Injectable} from '@angular/core';
import {
    MicroserviceExternalAccountRequestTaskMetierService
} from '@core/services/tasks/acl/microservice-external-account-request-task-metier.service';
import {ProcessHistoricInformation, Task} from 'micro_service_modules/api-bpmn';
import {TaskService as ExternalAccountRequestTaskService} from 'micro_service_modules/acl/acl-api';
import {ExternalAccountRequest} from 'micro_service_modules/acl/acl-model';
import {Observable} from 'rxjs';
import {ObjectType} from 'src/app/core/services/tasks/object-type.enum';

@Injectable({
    providedIn: 'root'
})
export class ExternalAccountRequestTaskMetierService extends MicroserviceExternalAccountRequestTaskMetierService<ExternalAccountRequest> {

    protected objectType: ObjectType = ObjectType.EXTERNAL_ACCOUNT_REQUEST;

    constructor(externalAccountRequestTaskService: ExternalAccountRequestTaskService) {
        super(externalAccountRequestTaskService);
    }

    claimTask(taskId: string): Observable<Task> {
        return this.externalAccountRequestTaskService.claimExternalAccountRequestTask(taskId);
    }

    doIt(taskId: string, actionName: string): Observable<Task> {
        return this.externalAccountRequestTaskService.doItExternalAccountRequest(taskId, actionName);
    }

    unclaimTask(taskId: string): Observable<Task> {
        return this.externalAccountRequestTaskService.unclaimExternalAccountRequestTask(taskId);
    }

    updateTask(task: Task): Observable<Task> {
        return this.externalAccountRequestTaskService.updateExternalAccountRequestTask(task);
    }

    createDraft(asset: ExternalAccountRequest): Observable<Task> {
        return this.externalAccountRequestTaskService.createExternalAccountRequestDraft(asset);
    }

    startTask(task: Task): Observable<Task> {
        return this.externalAccountRequestTaskService.startExternalAccountRequestTask(task);
    }

    getMyHistoricInformations(): Observable<ProcessHistoricInformation[]> {
        return this.externalAccountRequestTaskService.getMyHistoricInformations();
    }
}
