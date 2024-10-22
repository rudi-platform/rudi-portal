import {Injectable} from '@angular/core';
import {OrganizationMetierService} from '@core/services/organization/organization-metier.service';
import {OrganizationTaskSearchCriteria} from '@core/services/tasks/strukture/organization/organization-task-search-criteria.interface';
import {DependencyFetcher} from '@shared/utils/dependencies-utils';
import {TaskWithDependencies} from '@shared/utils/task-utils';
import {AclService, User} from 'micro_service_modules/acl/acl-api';
import {Task} from 'micro_service_modules/api-bpmn';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';
import {Organization} from 'micro_service_modules/strukture/strukture-model';
import {OrganizationTaskMetierService} from 'src/app/core/services/tasks/strukture/organization/organization-task-metier.service';
import {TaskDependencies} from 'src/app/core/services/tasks/task-dependencies.interface';
import {TaskDependencyFetchers, TaskWithDependenciesService} from 'src/app/core/services/tasks/task-with-dependencies-service';

export interface OrganizationDependencies extends TaskDependencies {
    organization?: Organization;
    logo?: string;
    userInfo?: User;
}

export class OrganizationTask extends TaskWithDependencies<Organization, OrganizationDependencies> {
    constructor(task: Task) {
        super(task, {});
    }
}

@Injectable({
    providedIn: 'root'
})
export class OrganizationTaskDependenciesService extends TaskWithDependenciesService
    <OrganizationTask, OrganizationTaskSearchCriteria, Organization> {

    constructor(readonly organizationTaskMetierService: OrganizationTaskMetierService) {
        super(organizationTaskMetierService);
    }

    defaultSearchCriteria(): OrganizationTaskSearchCriteria {
        return {};
    }

    newTaskWithDependencies(task: Task): OrganizationTask {
        return new OrganizationTask(task);
    }

}

@Injectable({
    providedIn: 'root'
})
export class OrganizationTaskDependencyFetcher extends TaskDependencyFetchers<OrganizationTask, Organization, OrganizationDependencies> {

    constructor(organizationService: OrganizationService,
                readonly aclService: AclService,
                private readonly organizationMetierService: OrganizationMetierService
    ) {
        super(organizationService, aclService);
    }

    get organization(): DependencyFetcher<OrganizationTask, Organization> {
        return {
            hasPrerequisites: (input: OrganizationTask) => OrganizationTaskDependencyFetcher.hasOrganizationUuid(input),
            getKey: taskWithDependencies => taskWithDependencies.asset.uuid,
            getValue: uuid => this.organizationMetierService.getOrganizationByUuid(uuid)
        };
    }

    get logo(): DependencyFetcher<OrganizationTask, string> {
        return {
            hasPrerequisites: (input: OrganizationTask) => OrganizationTaskDependencyFetcher.hasOrganizationUuid(input),
            getKey: taskWithDependencies => taskWithDependencies.task.functionalId,
            getValue: uuid => this.organizationMetierService.getLogo(uuid)
        };
    }

    get userInfo(): DependencyFetcher<OrganizationTask, User> {
        return {
            hasPrerequisites: (input: OrganizationTask) => OrganizationTaskDependencyFetcher.hasInitiator(input),
            getKey: projectWithDependencies => projectWithDependencies.task.initiator,
            getValue: login => this.aclService.getUserInfoByLogin(login)
        };
    }

    /**
     * Check les entrées des dépendances pour savoir s'il y a bien la dépendance organization qui a été loadée
     * @param input l'entrée à checker
     * @private
     */
    private static hasOrganizationUuid(input: OrganizationTask): boolean {
        return input != null && input.task != null && input.task.functionalId != null;
    }

    private static hasInitiator(input: OrganizationTask): boolean {
        return input != null && input.task != null && input.task.initiator != null;
    }
}
