import { AsyncPipe } from '@angular/common';
import {Component, OnInit, signal} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle} from '@angular/material/expansion';
import {ActivatedRoute, Router} from '@angular/router';
import {LogService} from '@core/services/log.service';
import {AttachmentService} from '@core/services/attachment.service';
import {Base64EncodedLogo, ImageLogoService} from '@core/services/image-logo.service';
import {OrganizationAttachmentService} from '@core/services/organization-attachment.service';
import {PageTitleService} from '@core/services/page-title.service';
import {ProcessDefinitionsKeyIconRegistryService} from '@core/services/process-definitions-key-icon-registry.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {
    OrganizationDependencies,
    OrganizationTask,
    OrganizationTaskDependenciesService,
    OrganizationTaskDependencyFetchers
} from '@core/services/tasks/strukture/organization/organization-task-dependencies.service';
import {OrganizationTaskMetierService} from '@core/services/tasks/strukture/organization/organization-task-metier.service';
import {OrganizationTaskSearchCriteria} from '@core/services/tasks/strukture/organization/organization-task-search-criteria.interface';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {BannerButtonComponent} from '@shared/core/banner/banner-button/banner-button.component';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {TaskDetailHeaderComponent} from '@shared/core/workflow/common/task-detail-header/task-detail-header.component';
import {TaskDetailComponent} from '@shared/core/workflow/common/task-detail/task-detail.component';
import {PROCESS_DEFINITION_KEY_TYPES} from '@shared/models/title-icon-type';
import {injectDependencies} from '@shared/utils/dependencies-utils';
import {ProjectStatus, Task} from 'micro_service_modules/projekt/projekt-api';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';
import {Organization, OrganizationStatus, OwnerInfo} from 'micro_service_modules/strukture/strukture-model';
import {Observable} from 'rxjs';
import {map, switchMap, tap} from 'rxjs/operators';
import {OwnerInformationComponent} from '../../components/owner-information/owner-information.component';
import {OrganizationInformationComponent} from '../../components/organization-information/organization-information.component';

@Component({
    selector: 'app-organization-task-detail',
    templateUrl: './organization-task-detail.component.html',
    styleUrls: ['./organization-task-detail.component.scss'],
    imports: [PageComponent, TaskDetailHeaderComponent, TabsComponent,
        TabComponent, MatAccordion, MatExpansionPanel,
        MatExpansionPanelHeader, MatExpansionPanelTitle, 
        OrganizationInformationComponent, OwnerInformationComponent, BannerButtonComponent, AsyncPipe, TranslatePipe],
    providers: [{provide: AttachmentService, useExisting: OrganizationAttachmentService}]
})
export class OrganizationTaskDetailComponent
    extends TaskDetailComponent<Organization, OrganizationDependencies, OrganizationTask, OrganizationTaskSearchCriteria>
    implements OnInit {

    isLoading: boolean;
    dependencies: OrganizationDependencies;
    idTask: string;
    currentTask: Task;
    hasSections = false;
    ownerInfo: Observable<OwnerInfo>;
    headerLibelle: string;
    organizationImageBase64: Base64EncodedLogo;
    protected readonly ProjectStatus = ProjectStatus;
    readonly panelInitialTaskOpenState = signal(false);

    constructor(
        readonly dialog: MatDialog,
        readonly translateService: TranslateService,
        readonly snackBarService: SnackBarService,
        readonly taskWithDependenciesService: OrganizationTaskDependenciesService,
        readonly organizationTaskMetierService: OrganizationTaskMetierService,
        readonly organizationService: OrganizationService,
        protected logger: LogService,
        private readonly route: ActivatedRoute,
        private readonly pageTitleService: PageTitleService,
        readonly organizationTaskDependencyFetchers: OrganizationTaskDependencyFetchers,
        private readonly processDefinitionsKeyIconRegistryService: ProcessDefinitionsKeyIconRegistryService,
        private readonly router: Router,
        private readonly organizationAttachmentService: OrganizationAttachmentService,
        private readonly imageLogoService: ImageLogoService
    ) {
        super(dialog, translateService, snackBarService, taskWithDependenciesService, organizationTaskMetierService, logger);
        this.processDefinitionsKeyIconRegistryService.addAllSvgIcons(PROCESS_DEFINITION_KEY_TYPES);
        this.headerLibelle = this.translateService.instant('personalSpace.organizationDetails.declaration');
    }

    set taskId(idTask: string) {
        if (idTask) {
            this.isLoading = true;
            this.idTask = idTask;

            this.organizationTaskMetierService.getTask(this.idTask).subscribe({
                next: (task: Task) => {
                    this.currentTask = task;
                    this.hasSections = !!task?.asset?.form?.sections;
                    this.loadOrganizationImage(task);
                },
                error: (err) => this.logger.error('Error fetching task:', err)
            });
            this.taskWithDependenciesService.getTaskWithDependencies(idTask).pipe(
                tap(taskWithDependencies => {

                    const isArchive = taskWithDependencies.task?.asset?.form?.sections.some(section => section?.fields.some(field => field?.definition.name === 'organizationArchiveMode'));
                    const taskValidated = taskWithDependencies.asset.organizationStatus === OrganizationStatus.Validated;

                    if (taskValidated && isArchive) {
                        this.headerLibelle = this.translateService.instant('personalSpace.organizationDetails.archive.task.title');
                    }

                    this.taskWithDependencies = taskWithDependencies;
                }),
                injectDependencies({
                    organization: this.organizationTaskDependencyFetchers.organization,
                    userInfo: this.organizationTaskDependencyFetchers.userInfo
                }),
                map(({task, asset, dependencies}) => {
                    return {
                        organization: dependencies.organization,
                        userInfo: dependencies.userInfo
                    };
                })
            ).subscribe({
                next: (dependencies: OrganizationDependencies) => {
                    this.dependencies = dependencies;
                    // On définit ici le titre de l'onglet en se basant sur le name de l'organization
                    this.pageTitleService.setPageTitle(this.dependencies.organization.name);
                    // On récupère les informations du Owner
                    this.getOwnerInfo(this.dependencies.organization.uuid);

                    this.isLoading = false;
                },
                error: (error) => {
                    this.isLoading = false;
                    this.logger.error(error);
                }
            });
        }
    }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.taskId = params.taskId;
        });
    }

    public getOwnerInfo(uuid: string): void {
        this.ownerInfo = this.organizationService.getOrganizationOwnerInfo(uuid);
    }

    protected goBackToList(): Promise<boolean> {
        return this.router.navigate(['/personal-space/my-notifications']);
    }

    private loadOrganizationImage(task: Task): void {
        const imageUuid = task?.asset?.form?.sections
            ?.find(s => s.name === 'image-organization')
            ?.fields?.[0]?.values?.[0];
        if (imageUuid) {
            this.organizationAttachmentService.downloadAttachement(imageUuid).pipe(
                switchMap((blob: Blob) => this.imageLogoService.createImageFromBlob(blob))
            ).subscribe({
                next: (base64: Base64EncodedLogo) => {
                    this.organizationImageBase64 = base64;
                },
                error: (err) => this.logger.error('Failed to load organization image', err)
            });
        }
    }
}
