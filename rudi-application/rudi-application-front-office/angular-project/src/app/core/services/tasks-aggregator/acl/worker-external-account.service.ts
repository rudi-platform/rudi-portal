import {Injectable} from '@angular/core';
import {
    ExternalAccountRequestDependencies,
    ExternalAccountRequestTask,
    ExternalAccountRequestTaskDependenciesService,
    ExternalAccountRequestTaskDependencyFetchers
} from '@core/services/tasks/acl/external-account-request-task-dependencies.service';
import {
    ExternalAccountRequestTaskSearchCriteria
} from '@core/services/tasks/acl/external-account-request-task-search-criteria.interface';
import {TranslateService} from '@ngx-translate/core';
import {ExternalAccountRequest} from 'micro_service_modules/acl/acl-model';
import {Task} from 'micro_service_modules/api-bpmn';
import {RequestToStudy} from 'src/app/core/services/tasks-aggregator/request-to-study.interface';
import {WorkerService} from 'src/app/core/services/tasks-aggregator/worker.service';

@Injectable({
    providedIn: 'root'
})
export class WorkerExternalAccountService extends WorkerService
    <ExternalAccountRequestTask, ExternalAccountRequest, ExternalAccountRequestDependencies, ExternalAccountRequestTaskSearchCriteria> {

    constructor(
        externalAccountRequestTaskDependenciesService: ExternalAccountRequestTaskDependenciesService,
        externalAccountRequestTaskDependencyFetchers: ExternalAccountRequestTaskDependencyFetchers,
        private readonly translateService: TranslateService) {
        super(externalAccountRequestTaskDependenciesService, externalAccountRequestTaskDependencyFetchers);
    }

    mapToRequestToStudy(task: Task, assetDescription: ExternalAccountRequest, dependencies: ExternalAccountRequestDependencies): RequestToStudy {
        const requestToStudy = super.mapToRequestToStudy(task, assetDescription, dependencies);
        // on override le champ pour forcer la banette à afficher le titre de la réutilisation dans la colonne description.
        requestToStudy.description = assetDescription.email; 
        requestToStudy.url = 'external-account-request-task-detail';

        return requestToStudy;
    }
}
