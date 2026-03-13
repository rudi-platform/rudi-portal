import { AsyncPipe } from '@angular/common';
import {Component, OnInit, signal} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle} from '@angular/material/expansion';
import {ActivatedRoute, Router} from '@angular/router';
import {LogService} from '@core/services/log.service';
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
import {WorkflowExpansionComponent} from '@shared/core/workflow/workflow-expansion/workflow-expansion.component';
import {PROCESS_DEFINITION_KEY_TYPES} from '@shared/models/title-icon-type';
import {injectDependencies} from '@shared/utils/dependencies-utils';
import {ProjectStatus, Task} from 'micro_service_modules/projekt/projekt-api';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';
import {Organization, OrganizationStatus, OwnerInfo} from 'micro_service_modules/strukture/strukture-model';
import {Observable} from 'rxjs';
import {map, tap} from 'rxjs/operators';
import {OrganizationInformationComponent} from '../../components/organization-information/organization-information.component';
import {OwnerInformationComponent} from '../../components/owner-information/owner-information.component';

@Component({
    selector: 'app-organization-task-detail',
    templateUrl: './organization-task-detail.component.html',
    styleUrls: ['./organization-task-detail.component.scss'],
    imports: [PageComponent, TaskDetailHeaderComponent, TabsComponent, TabComponent, WorkflowExpansionComponent, MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle, OrganizationInformationComponent, OwnerInformationComponent, BannerButtonComponent, AsyncPipe, TranslatePipe]
})
export class OrganizationTaskDetailComponent
    extends TaskDetailComponent<Organization, OrganizationDependencies, OrganizationTask, OrganizationTaskSearchCriteria>
    implements OnInit {

    isLoading: boolean;
    dependencies: OrganizationDependencies;
    idTask: string;
    currentTask: Task;
    hasSections: boolean = false;
    ownerInfo: Observable<OwnerInfo>;
    headerLibelle: string;
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
        private readonly router: Router
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
                },
                error: (err) => console.error('Error fetching task:', err)
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
                    console.error(error);
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
}
