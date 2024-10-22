import {Injectable} from '@angular/core';
import {LinkedProducerMetierService} from '@core/services/linked-producer/linked-producer-metier.service';
import {LinkedProducerTaskMetierService} from '@core/services/tasks/strukture/linked-producer/linked-producer-task-metier.service';
import {
    LinkedProducerTaskSearchCriteria
} from '@core/services/tasks/strukture/linked-producer/linked-producer-task-search-criteria.interface';
import {DependencyFetcher} from '@shared/utils/dependencies-utils';
import {TaskWithDependencies} from '@shared/utils/task-utils';
import {AclService} from 'micro_service_modules/acl/acl-api';
import {Task} from 'micro_service_modules/api-bpmn';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';
import {LinkedProducer} from 'micro_service_modules/strukture/strukture-model';
import {TaskDependencies} from '../../task-dependencies.interface';
import {TaskDependencyFetchers, TaskWithDependenciesService} from '../../task-with-dependencies-service';

export interface LinkedProducerDependencies extends TaskDependencies {
    linkedProducer?: LinkedProducer;
}

export class LinkedProducerTask extends TaskWithDependencies<LinkedProducer, LinkedProducerDependencies> {
    constructor(task: Task) {
        super(task, {});
    }
}

@Injectable({
    providedIn: 'root'
})
export class LinkedProducerTaskDependenciesService extends TaskWithDependenciesService
    <LinkedProducerTask, LinkedProducerTaskSearchCriteria, LinkedProducer> {

    constructor(readonly linkedProducerTaskMetierService: LinkedProducerTaskMetierService) {
        super(linkedProducerTaskMetierService);
    }

    defaultSearchCriteria(): LinkedProducerTaskSearchCriteria {
        return {};
    }

    newTaskWithDependencies(task: Task): LinkedProducerTask {
        return new LinkedProducerTask(task);
    }

}

@Injectable({
    providedIn: 'root'
})
export class LinkedProducerTaskDependencyFetcher extends TaskDependencyFetchers<LinkedProducerTask, LinkedProducer, LinkedProducerDependencies> {

    constructor(organizationService: OrganizationService,
                readonly aclService: AclService,
                private readonly producerLinkMetierService: LinkedProducerMetierService
    ) {
        super(organizationService, aclService);
    }

    get linkedProducer(): DependencyFetcher<LinkedProducerTask, LinkedProducer> {
        return {
            hasPrerequisites: (input: LinkedProducerTask) => LinkedProducerTaskDependencyFetcher.hasLinkedProducerUuid(input),
            getKey: taskWithDependencies => taskWithDependencies.asset.uuid,
            getValue: uuid => this.producerLinkMetierService.getLinkedProducerByUuid(uuid)
        };
    }

    /**
     * Check les entrées des dépendances pour savoir s'il y a bien un uuid sur la task
     * @param input l'entrée à checker
     * @private
     */
    private static hasLinkedProducerUuid(input: LinkedProducerTask): boolean {
        return input != null && input.task != null && input.task.asset != null && input.task.asset.uuid != null;
    }
}
