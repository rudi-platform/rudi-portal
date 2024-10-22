import {Injectable} from '@angular/core';
import {
    OrganizationDependencies,
    OrganizationTask,
    OrganizationTaskDependenciesService,
    OrganizationTaskDependencyFetcher
} from '@core/services/tasks/strukture/organization/organization-task-dependencies.service';
import {OrganizationTaskSearchCriteria} from '@core/services/tasks/strukture/organization/organization-task-search-criteria.interface';
import {Task} from 'micro_service_modules/api-bpmn';
import {Organization} from 'micro_service_modules/strukture/strukture-model';
import {RequestToStudy} from 'src/app/core/services/tasks-aggregator/request-to-study.interface';
import {WorkerService} from 'src/app/core/services/tasks-aggregator/worker.service';

@Injectable({
    providedIn: 'root'
})
export class WorkerOrganizationService extends WorkerService
    <OrganizationTask, Organization, OrganizationDependencies, OrganizationTaskSearchCriteria> {

    constructor(
        organizationTaskDependenciesService: OrganizationTaskDependenciesService,
        organizationDependenciyFetcher: OrganizationTaskDependencyFetcher) {
        super(organizationTaskDependenciesService, organizationDependenciyFetcher);
    }

    mapToRequestToStudy(task: Task, assetDescription: Organization, dependencies: OrganizationDependencies): RequestToStudy {
        const requestToStudy = super.mapToRequestToStudy(task, assetDescription, dependencies);
        // on override le champ pour forcer la banette à afficher le titre de la réutilisation dans la colonne description.
        requestToStudy.description = assetDescription.name;
        requestToStudy.url = 'organization-task-detail';

        return requestToStudy;
    }
}
