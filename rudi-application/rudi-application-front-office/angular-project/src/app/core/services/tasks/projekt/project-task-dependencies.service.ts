import {Injectable} from '@angular/core';
import {ProjectAttachmentAdapter} from '@core/services/tasks/projekt/project-attachment.adapter';
import {ProjectAttachmentService} from '@core/services/project-attachment.service';
import {AttachmentService} from '@core/services/attachment.service';
import {UploaderAdapter} from '@shared/core/form/uploader/uploader.adapter';
import {DataSize} from '@shared/models/data-size';
import {DependencyFetcher, OwnerKey} from '@shared/utils/dependencies-utils';
import {TaskWithDependencies} from '@shared/utils/task-utils';
import {AclService} from 'micro_service_modules/acl/acl-api';
import {Task} from 'micro_service_modules/api-bpmn';
import {Field, NewDatasetRequest, ProjektService} from 'micro_service_modules/projekt/projekt-api';
import {OwnerInfo, Project} from 'micro_service_modules/projekt/projekt-model';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';
import {ProjectConsultationService} from '../../asset/project/project-consultation.service';
import {LinkedDatasetMetadatas} from '../../asset/project/project-dependencies.service';
import {ProjektMetierService} from '../../asset/project/projekt-metier.service';
import {KonsultMetierService} from '../../konsult-metier.service';
import {TaskDependencies} from '../task-dependencies.interface';
import {TaskDependencyFetchers, TaskWithDependenciesService} from '../task-with-dependencies-service';
import {ProjectTaskMetierService} from './project-task-metier.service';
import {Observable, of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {ProjektTaskSearchCriteria} from './projekt-task-search-criteria.interface';

export interface ProjectDependencies extends TaskDependencies {
    project?: Project;
    logo?: string;
    ownerInfo?: OwnerInfo;
    openLinkedDatasets?: LinkedDatasetMetadatas[];
    restrictedLinkedDatasets?: LinkedDatasetMetadatas[];
    newDatasetsRequest?: NewDatasetRequest[];
}

export class ProjectTask extends TaskWithDependencies<Project, ProjectDependencies> {
    constructor(task: Task) {
        super(task, {});
    }
}

@Injectable({
    providedIn: 'root'
})
export class ProjectTaskDependenciesService extends TaskWithDependenciesService<ProjectTask, ProjektTaskSearchCriteria, Project> {
    constructor(readonly projectTaskMetierService: ProjectTaskMetierService) {
        super(projectTaskMetierService);
    }

    newTaskWithDependencies(task: Task): ProjectTask {
        return new ProjectTask(task);
    }

    defaultSearchCriteria(): ProjektTaskSearchCriteria {
        return {};
    }
}

@Injectable({
    providedIn: 'root'
})
export class ProjectTaskDependencyFetcher extends TaskDependencyFetchers<ProjectTask, Project, ProjectDependencies> {

    constructor(organizationService: OrganizationService,
                aclService: AclService,
                private readonly konsultMetierService: KonsultMetierService,
                private readonly projektService: ProjektService,
                private readonly projektMetierService: ProjektMetierService,
                private readonly projectConsultService: ProjectConsultationService,
                private readonly projectAttachmentService: ProjectAttachmentService,
                private readonly projectAttachmentAdapter: ProjectAttachmentAdapter) {
        super(organizationService, aclService);
    }

    get attachmentService(): AttachmentService {
        return this.projectAttachmentService;
    }

    get attachmentAdapter(): UploaderAdapter<string> {
        return this.projectAttachmentAdapter;
    }

    getAllowedExtensions(field?: Field): Observable<string[]> {
        if (!field?.definition?.extendedType) {
            return of([]);
        }
        try {
            const parsed = JSON.parse(field.definition.extendedType);
            const extensions: string[] = (parsed.types ?? []).flatMap(
                (t: { associatedExtensions?: string[] }) => t.associatedExtensions ?? []
            );
            return of(extensions);
        } catch {
            return of([]);
        }
    }

    getFileMaxSize(field?: Field): Observable<DataSize> {
        if (!field?.definition?.extendedType) {
            return of(null);
        }
        try {
            const parsed = JSON.parse(field.definition.extendedType);
            const mb = Number.parseInt(parsed.maxSize, 10);
            return of(Number.isNaN(mb) ? null : DataSize.ofMegabytes(mb));
        } catch {
            return of(null);
        }
    }

    get project(): DependencyFetcher<ProjectTask, Project> {
        return {
            hasPrerequisites: (input: ProjectTask) => ProjectTaskDependencyFetcher.hasProjectUuid(input),
            getKey: taskWithDependencies => taskWithDependencies.asset.uuid,
            getValue: uuid => this.projektMetierService.getProject(uuid)
        };
    }

    get logo(): DependencyFetcher<ProjectTask, string> {
        return {
            hasPrerequisites: (input: ProjectTask) => ProjectTaskDependencyFetcher.hasProjectUuid(input),
            getKey: projectWithDependencies => projectWithDependencies.task.functionalId,
            getValue: projectUuid => this.projektMetierService.getProjectLogo(projectUuid).pipe(
                catchError(() => of('/assets/images/logo_projet_par_defaut.png'))
            )
        };
    }

    get ownerInfo(): DependencyFetcher<ProjectTask, OwnerInfo> {
        return {
            hasPrerequisites: (input: ProjectTask) => ProjectTaskDependencyFetcher.hasProject(input),
            getKey: projectWithDependencies => OwnerKey.serialize(projectWithDependencies.dependencies.project),
            getValue: ownerKey => {
                const {owner_type, owner_uuid} = OwnerKey.deserialize(ownerKey);
                return this.projektMetierService.getOwnerInfo(owner_type, owner_uuid);
            }
        };
    }

    get openLinkedDatasets(): DependencyFetcher<ProjectTask, LinkedDatasetMetadatas[]> {
        return {
            hasPrerequisites: (input: ProjectTask) => ProjectTaskDependencyFetcher.hasProject(input),
            getKey: projectTaskWithDependencies => projectTaskWithDependencies.dependencies.project.uuid,
            getValue: projectUuid => this.projectConsultService.getOpenedLinkedDatasetsMetadata(projectUuid).pipe(
                catchError(() => of([]))
            )
        };
    }

    get restrictedLinkedDatasets(): DependencyFetcher<ProjectTask, LinkedDatasetMetadatas[]> {
        return {
            hasPrerequisites: (input: ProjectTask) => ProjectTaskDependencyFetcher.hasProject(input),
            getKey: projectTaskWithDependencies => projectTaskWithDependencies.dependencies.project.uuid,
            getValue: projectUuid => this.projectConsultService.getRestrictedLinkedDatasetsMetadata(projectUuid).pipe(
                catchError(() => of([]))
            )
        };
    }

    get newDatasetsRequest(): DependencyFetcher<ProjectTask, NewDatasetRequest[]> {
        return {
            hasPrerequisites: (input: ProjectTask) => ProjectTaskDependencyFetcher.hasProject(input),
            getKey: projectTaskWithDependencies => projectTaskWithDependencies.dependencies.project.uuid,
            getValue: projectUuid => this.projectConsultService.getNewDatasetsRequest(projectUuid).pipe(
                catchError(() => of([]))
            )
        };
    }

    /**
     * Check les entrées des dépendances pour savoir s'il y a bien la dépendance projet qui a été loadée
     * @param input l'entrée à checker
     * @private
     */
    private static hasProjectUuid(input: ProjectTask): boolean {
        return input?.task?.functionalId != null;
    }

    /**
     *
     */
    private static hasProject(input: ProjectTask): boolean {
        return input?.dependencies?.project != null;
    }

}
