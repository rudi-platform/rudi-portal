import {Injectable} from '@angular/core';
import {RequestToStudy} from '@core/services/tasks-aggregator/request-to-study.interface';
import {WorkerService} from '@core/services/tasks-aggregator/worker.service';
import {
    LinkedProducerDependencies,
    LinkedProducerTask,
    LinkedProducerTaskDependenciesService,
    LinkedProducerTaskDependencyFetcher
} from '@core/services/tasks/strukture/linked-producer/linked-producer-task-dependencies.service';
import {
    LinkedProducerTaskSearchCriteria
} from '@core/services/tasks/strukture/linked-producer/linked-producer-task-search-criteria.interface';
import {Task} from 'micro_service_modules/api-bpmn';
import {LinkedProducer} from 'micro_service_modules/strukture/api-strukture';

@Injectable({
    providedIn: 'root'
})
export class WorkerLinkedProducerService extends WorkerService
    <LinkedProducerTask, LinkedProducer, LinkedProducerDependencies, LinkedProducerTaskSearchCriteria> {

    constructor(
        linkedProducerTaskDependenciesService: LinkedProducerTaskDependenciesService,
        linkedProducerTaskDependencyFetcher: LinkedProducerTaskDependencyFetcher) {
        super(linkedProducerTaskDependenciesService, linkedProducerTaskDependencyFetcher);
    }

    mapToRequestToStudy(task: Task, assetDescription: LinkedProducer, dependencies: LinkedProducerDependencies): RequestToStudy {
        const requestToStudy = super.mapToRequestToStudy(task, assetDescription, dependencies);
        // on override le champ pour forcer la banette à afficher le titre de la réutilisation dans la colonne description.
        requestToStudy.description = assetDescription.organization.name;
        requestToStudy.url = 'linked-producer-task-detail';

        return requestToStudy;
    }
}
