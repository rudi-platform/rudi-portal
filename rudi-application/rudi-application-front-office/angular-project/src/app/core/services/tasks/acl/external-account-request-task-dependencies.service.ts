import {Injectable} from '@angular/core';
import {
    ExternalAccountRequestTaskMetierService
} from '@core/services/tasks/acl/external-account-request-task-metier.service';
import {
    ExternalAccountRequestTaskSearchCriteria
} from '@core/services/tasks/acl/external-account-request-task-search-criteria.interface';
import {TaskDependencies} from '@core/services/tasks/task-dependencies.interface';
import {TaskDependencyFetchers, TaskWithDependenciesService} from '@core/services/tasks/task-with-dependencies-service';
import {DependencyFetcher} from '@shared/utils/dependencies-utils';
import {TaskWithDependencies} from '@shared/utils/task-utils';
import {ExternalAccountRequest} from 'micro_service_modules/acl/acl-model';
import {AclService} from 'micro_service_modules/acl/acl-api';
import {Task} from 'micro_service_modules/api-bpmn';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';
import {Organization} from 'micro_service_modules/strukture/strukture-model';
import {Observable, of} from 'rxjs';
import {catchError} from 'rxjs/operators';

export interface ExternalAccountRequestDependencies extends TaskDependencies {
    organization?: Organization;
}

export class ExternalAccountRequestTask extends TaskWithDependencies<ExternalAccountRequest, ExternalAccountRequestDependencies> {
    constructor(task: Task) {
        super(task, {});
    }
}

@Injectable({
    providedIn: 'root'
})
export class ExternalAccountRequestTaskDependenciesService extends TaskWithDependenciesService<ExternalAccountRequestTask, ExternalAccountRequestTaskSearchCriteria, ExternalAccountRequest> {

    constructor(readonly externalAccountRequestTaskMetierService: ExternalAccountRequestTaskMetierService) {
        super(externalAccountRequestTaskMetierService);
    }

    defaultSearchCriteria(): ExternalAccountRequestTaskSearchCriteria {
        return {};
    }

    newTaskWithDependencies(task: Task): ExternalAccountRequestTask {
        return new ExternalAccountRequestTask(task);
    }

}

@Injectable({
    providedIn: 'root'
})
export class ExternalAccountRequestTaskDependencyFetchers extends TaskDependencyFetchers<ExternalAccountRequestTask, ExternalAccountRequest, ExternalAccountRequestDependencies> {

    constructor(organizationService: OrganizationService, readonly aclService: AclService) {
        super(organizationService, aclService);
    }

    get organization(): DependencyFetcher<ExternalAccountRequestTask, Organization> {
        return {
            hasPrerequisites: () => true,
            getKey: taskWithDependencies => taskWithDependencies.asset.organizationUuid,
            getValue: (uuid: string): Observable<Organization> => {
                if (!uuid) {
                    return of(null);
                }
                return this.organizationService.getOrganization(uuid).pipe(
                    catchError(() => of(null))
                );
            }
        };
    }
}
