import {Injectable} from '@angular/core';
import {
    SelfdataInformationRequestTaskDependencyFetchers
} from '@core/services/tasks/selfdata/selfdata-information-request-task-dependencies.service';
import {OrganizationTaskDependencyFetchers} from '@core/services/tasks/strukture/organization/organization-task-dependencies.service';
import {TaskDependencyFetchers} from '@core/services/tasks/task-with-dependencies-service';
import {ProjectTaskDependencyFetcher} from '@core/services/tasks/projekt/project-task-dependencies.service';

export const PROCESS_KEY_DEFINITION: string = 'processDefinitionKey';
export const SELFDATA_PROCESS_KEY_DEFINITION: string = 'selfdata-information-request-process';
export const ORGANIZATION_PROCESS_KEY_DEFINITION: string = 'organization-process';
export const PROJECT_PROCESS_KEY_DEFINITION: string = 'project-process';

@Injectable({
    providedIn: 'root'
})
export class TaskDependencyFetcherFactory {

    constructor(
        private readonly organizationTaskDependencyFetchers: OrganizationTaskDependencyFetchers,
        private readonly selfdataInformationRequestTaskDependencyFetchers: SelfdataInformationRequestTaskDependencyFetchers,
        private readonly projectTaskDependencyFetcher: ProjectTaskDependencyFetcher
    ) {
    }

    getService(processDefinitionKey: string): TaskDependencyFetchers<any, any, any> {
        switch (processDefinitionKey) {
            case ORGANIZATION_PROCESS_KEY_DEFINITION:
                return this.organizationTaskDependencyFetchers;
            case PROJECT_PROCESS_KEY_DEFINITION:
                return this.projectTaskDependencyFetcher;
            case SELFDATA_PROCESS_KEY_DEFINITION:
            default:
                return this.selfdataInformationRequestTaskDependencyFetchers;
        }
    }

}
