import {Injectable} from '@angular/core';
import {AttachmentService} from '@core/services/attachment.service';
import {OrganizationAttachmentService} from '@core/services/organization-attachment.service';
import {OrganizationMetierService} from '@core/services/organization/organization-metier.service';
import {OrganizationAttachmentAdapter} from '@core/services/tasks/strukture/organization/organization-attachment.adapter';
import {OrganizationTaskSearchCriteria} from '@core/services/tasks/strukture/organization/organization-task-search-criteria.interface';
import {DataSize} from '@shared/models/data-size';
import {DependencyFetcher} from '@shared/utils/dependencies-utils';
import {TaskWithDependencies} from '@shared/utils/task-utils';
import {AclService, User} from 'micro_service_modules/acl/acl-api';
import {Field, Task} from 'micro_service_modules/api-bpmn';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';
import {AllowedAttachementType, Organization, OrganizationLogoInformations} from 'micro_service_modules/strukture/strukture-model';
import {Observable, of} from 'rxjs';
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
export class OrganizationTaskDependencyFetchers extends TaskDependencyFetchers<OrganizationTask, Organization, OrganizationDependencies> {

    constructor(organizationService: OrganizationService,
                readonly aclService: AclService,
                private readonly organizationMetierService: OrganizationMetierService,
                private readonly organizationAttachmentService: OrganizationAttachmentService,
                private readonly organizationAttachmentAdapter: OrganizationAttachmentAdapter
    ) {
        super(organizationService, aclService);
    }

    get organization(): DependencyFetcher<OrganizationTask, Organization> {
        return {
            hasPrerequisites: (input: OrganizationTask) => OrganizationTaskDependencyFetchers.hasOrganizationUuid(input),
            getKey: taskWithDependencies => taskWithDependencies.asset.uuid,
            getValue: uuid => this.organizationMetierService.getOrganizationByUuid(uuid)
        };
    }

    get logo(): DependencyFetcher<OrganizationTask, string> {
        return {
            hasPrerequisites: (input: OrganizationTask) => OrganizationTaskDependencyFetchers.hasOrganizationUuid(input),
            getKey: taskWithDependencies => taskWithDependencies.task.functionalId,
            getValue: uuid => this.organizationMetierService.getLogo(uuid)
        };
    }

    get userInfo(): DependencyFetcher<OrganizationTask, User> {
        return {
            hasPrerequisites: (input: OrganizationTask) => OrganizationTaskDependencyFetchers.hasInitiator(input),
            getKey: projectWithDependencies => projectWithDependencies.task.initiator,
            getValue: login => this.aclService.getUserInfoByLogin(login)
        };
    }


    get attachmentService(): AttachmentService {
        return this.organizationAttachmentService;
    }


    get attachmentAdapter(): OrganizationAttachmentAdapter {
        return this.organizationAttachmentAdapter;
    }


    getAllowedExtensions(field?: Field): Observable<string[]> {
        if (!field) {
            return of([]);
        }
        let fileExtensions: string[] = [];
        let extendedType: OrganizationLogoInformations = this.getExtendedType(field);
        if (extendedType?.types) {
            if (Array.isArray(extendedType.types)) {
                extendedType.types.flatMap((type: AllowedAttachementType) => type.associatedExtensions).forEach(value => fileExtensions.push(value));
            }
        }

        return of(fileExtensions);
    }


    getFileMaxSize(field: Field): Observable<DataSize> {
        let maxSize: DataSize = DataSize.ofMegabytes(10);
        let extendedType: OrganizationLogoInformations = this.getExtendedType(field);
        if (extendedType?.maxSize) {
            maxSize = DataSize.ofMegabytes(extendedType.maxSize);
        }
        return of(maxSize);
    }

    private getExtendedType(field: Field): OrganizationLogoInformations {
        return JSON.parse(field.definition.extendedType);
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
