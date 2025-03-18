import {Injectable} from '@angular/core';
import {
    SelfdataInformationRequestTaskDependencyFetchers
} from '@core/services/tasks/selfdata/selfdata-information-request-task-dependencies.service';
import {OrganizationTaskDependencyFetchers} from '@core/services/tasks/strukture/organization/organization-task-dependencies.service';
import {TaskDependencyFetchers} from '@core/services/tasks/task-with-dependencies-service';

export const PROCESS_KEY_DEFINITION: string = 'processDefinitionKey';
export const SELFDATA_PROCESS_KEY_DEFINITION: string = 'selfdata-information-request-process';
export const ORGANIZATION_PROCESS_KEY_DEFINITION: string = 'organization-process';

@Injectable({
    providedIn: 'root'
})
export class TaskDependencyFetcherFactory {

    constructor(
        private readonly organizationTaskDependencyFetchers: OrganizationTaskDependencyFetchers,
        private readonly selfdataInformationRequestTaskDependencyFetchers: SelfdataInformationRequestTaskDependencyFetchers
    ) {
    }

    getService(processDefinitionKey: string): TaskDependencyFetchers<any, any, any> {
        switch (processDefinitionKey) {
            case ORGANIZATION_PROCESS_KEY_DEFINITION:
                return this.organizationTaskDependencyFetchers;
            case SELFDATA_PROCESS_KEY_DEFINITION:
            default:
                return this.selfdataInformationRequestTaskDependencyFetchers;
        }
    }

}
