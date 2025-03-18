import {Injectable} from '@angular/core';
import {SelfdataAttachmentService} from '@core/services/selfdata-attachment.service';
import {SelfdataAttachmentAdapter} from '@core/services/tasks/selfdata/selfdata-attachment.adapter';
import {DataSize} from '@shared/models/data-size';
import {DependencyFetcher} from '@shared/utils/dependencies-utils';
import {TaskWithDependencies} from '@shared/utils/task-utils';
import {AclService} from 'micro_service_modules/acl/acl-api';
import {Field, Task} from 'micro_service_modules/api-bpmn';
import {Metadata} from 'micro_service_modules/api-kaccess';
import {OwnerInfo} from 'micro_service_modules/projekt/projekt-model';
import {SelfdataInformationRequest, SelfdataRequestAllowedAttachementType} from 'micro_service_modules/selfdata/selfdata-model';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {KonsultMetierService} from '../../konsult-metier.service';
import {TaskDependencies} from '../task-dependencies.interface';
import {TaskDependencyFetchers, TaskWithDependenciesService} from '../task-with-dependencies-service';
import {SelfdataInformationRequestTaskMetierService} from './selfdata-information-request-task-metier.service';
import {SelfdataTaskSearchCriteria} from './selfdata-task-search-criteria.interface';

/**
 * les dépendances attendues pour une tâche
 */
export interface SelfdataInformationRequestDependencies extends TaskDependencies {
    dataset?: Metadata;
    ownerInfo?: OwnerInfo;
}

/**
 * Objet permettant de charger les dépendances d'une tâche
 */
export class SelfdataInformationRequestTask extends TaskWithDependencies
    <SelfdataInformationRequest, SelfdataInformationRequestDependencies> {
    constructor(task: Task) {
        super(task, {});
    }
}

@Injectable({
    providedIn: 'root'
})
export class SelfdataInformationRequestTaskDependenciesService extends TaskWithDependenciesService
    <SelfdataInformationRequestTask, SelfdataTaskSearchCriteria, SelfdataInformationRequest> {

    constructor(readonly selfdataInformationRequestTaskMetierService: SelfdataInformationRequestTaskMetierService) {
        super(selfdataInformationRequestTaskMetierService);
    }

    defaultSearchCriteria(): SelfdataTaskSearchCriteria {
        return {};
    }

    newTaskWithDependencies(task: Task): SelfdataInformationRequestTask {
        return new SelfdataInformationRequestTask(task);
    }
}

@Injectable({
    providedIn: 'root'
})
export class SelfdataInformationRequestTaskDependencyFetchers
    extends TaskDependencyFetchers<SelfdataInformationRequestTask, SelfdataInformationRequest, SelfdataInformationRequestDependencies> {

    constructor(
        private readonly konsultMetierService: KonsultMetierService,
        organizationService: OrganizationService,
        aclService: AclService,
        private readonly selfdataAttachmentService: SelfdataAttachmentService,
        private readonly selfdataAttachmentAdapter: SelfdataAttachmentAdapter,
    ) {
        super(organizationService, aclService);
    }

    get dataset(): DependencyFetcher<SelfdataInformationRequestTask, Metadata> {
        return {
            hasPrerequisites: (input: SelfdataInformationRequestTask) => input != null && input.asset != null && input.asset.dataset_uuid != null,
            getKey: taskWithDependencies => taskWithDependencies.asset.dataset_uuid,
            getValue: datasetUuid => this.konsultMetierService.getMetadataByUuid(datasetUuid)
        };
    }


    get attachmentService(): SelfdataAttachmentService {
        return this.selfdataAttachmentService;
    }

    get attachmentAdapter(): SelfdataAttachmentAdapter {
        return this.selfdataAttachmentAdapter;
    }


    getAllowedExtensions(field?: Field): Observable<string[]> {
        return this.selfdataAttachmentService.getAllowedAttachementTypes().pipe(
            map((result: SelfdataRequestAllowedAttachementType[]) => {
                let fileExtensions: string[] = [];
                result.forEach(attachementType => attachementType.associatedExtensions.forEach(value => fileExtensions.push(value)));
                return fileExtensions;
            })
        );
    }


    getFileMaxSize(field: Field): Observable<DataSize> {
        return this.selfdataAttachmentService.getDataSizeProperty('spring.servlet.multipart.max-file-size');
    }
}
