import {Injectable} from '@angular/core';
import {
    MicroserviceLinkedProducerTaskMetierService
} from '@core/services/tasks/strukture/linked-producer/microservice-linked-producer-task-metier.service';
import {Task} from 'micro_service_modules/api-bpmn';
import {TaskService as LinkedProducerTaskService} from 'micro_service_modules/strukture/api-strukture';
import {LinkedProducer} from 'micro_service_modules/strukture/strukture-model';
import {Observable} from 'rxjs';
import {ObjectType} from '../../object-type.enum';

@Injectable({
    providedIn: 'root'
})
export class LinkedProducerTaskMetierService extends MicroserviceLinkedProducerTaskMetierService<LinkedProducer> {

    protected objectType: ObjectType = ObjectType.LINKED_PRODUCER;

    constructor(linkedProducerTaskService: LinkedProducerTaskService) {
        super(linkedProducerTaskService);
    }

    claimTask(taskId: string): Observable<Task> {
        return this.linkedProducerTaskService.claimLinkedProducerTask(taskId);
    }

    doIt(taskId: string, actionName: string): Observable<Task> {
        return this.linkedProducerTaskService.doItLinkedProducer(taskId, actionName);
    }

    unclaimTask(taskId: string): Observable<Task> {
        return this.linkedProducerTaskService.unclaimLinkedProducerTask(taskId);
    }

    updateTask(task: Task): Observable<Task> {
        return this.linkedProducerTaskService.updateLinkedProducerTask(task);
    }

    createDraft(asset: LinkedProducer): Observable<Task> {
        return this.linkedProducerTaskService.createLinkedProducerDraft(asset);
    }

    startTask(task: Task): Observable<Task> {
        return this.linkedProducerTaskService.startLinkedProducerTask(task);
    }
}
