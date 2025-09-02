import {Component, OnInit, signal} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
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
import {TranslateService} from '@ngx-translate/core';
import {PROCESS_DEFINITION_KEY_TYPES} from '@shared/models/title-icon-type';
import {TaskDetailComponent} from '@shared/task-detail/task-detail.component';
import {injectDependencies} from '@shared/utils/dependencies-utils';
import {ProjectStatus, Task} from 'micro_service_modules/projekt/projekt-api';
import {OrganizationService} from 'micro_service_modules/strukture/api-strukture';
import {Organization, OrganizationStatus, OwnerInfo} from 'micro_service_modules/strukture/strukture-model';
import {Observable} from 'rxjs';
import {map, tap} from 'rxjs/operators';

@Component({
    selector: 'app-organization-task-detail',
    templateUrl: './organization-task-detail.component.html',
    styleUrls: ['./organization-task-detail.component.scss']
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

                    const isArchive = taskWithDependencies.task?.asset?.form?.sections.some(section => section?.fields.some(field => field?.definition.name === 'archivageType'));
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
