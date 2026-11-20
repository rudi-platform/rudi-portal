import {Injectable} from '@angular/core';
import {
    ExternalAccountRequestTaskDependencyFetchers
} from '@core/services/tasks/acl/external-account-request-task-dependencies.service';
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
export const EXTERNAL_ACCOUNT_REQUEST_PROCESS_KEY_DEFINITION: string = 'external-account-request-process';

@Injectable({
    providedIn: 'root'
})
export class TaskDependencyFetcherFactory {

    constructor(
        private readonly organizationTaskDependencyFetchers: OrganizationTaskDependencyFetchers,
        private readonly selfdataInformationRequestTaskDependencyFetchers: SelfdataInformationRequestTaskDependencyFetchers,
        private readonly projectTaskDependencyFetcher: ProjectTaskDependencyFetcher,
        private readonly externalAccountRequestTaskDependencyFetchers: ExternalAccountRequestTaskDependencyFetchers
    ) {
    }

    getService(processDefinitionKey: string): TaskDependencyFetchers<any, any, any> {
        switch (processDefinitionKey) {
            case ORGANIZATION_PROCESS_KEY_DEFINITION:
                return this.organizationTaskDependencyFetchers;
            case PROJECT_PROCESS_KEY_DEFINITION:
                return this.projectTaskDependencyFetcher;
            case EXTERNAL_ACCOUNT_REQUEST_PROCESS_KEY_DEFINITION:
                return this.externalAccountRequestTaskDependencyFetchers;
            case SELFDATA_PROCESS_KEY_DEFINITION:
            default:
                return this.selfdataInformationRequestTaskDependencyFetchers;
        }
    }

}
