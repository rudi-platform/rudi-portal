
import {Component, OnInit, inject, signal} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {MatCard, MatCardContent} from '@angular/material/card';
import {MatDialog} from '@angular/material/dialog';
import {MatAccordion, MatExpansionPanel, MatExpansionPanelHeader, MatExpansionPanelTitle} from '@angular/material/expansion';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {ActivatedRoute, Router} from '@angular/router';
import {ProjectConsultationService} from '@core/services/asset/project/project-consultation.service';
import {LinkedDatasetMetadatas} from '@core/services/asset/project/project-dependencies.service';
import {ProjectSubmissionService} from '@core/services/asset/project/project-submission.service';
import {ProjektMetierService} from '@core/services/asset/project/projekt-metier.service';
import {DataSetActionsAuthorizationService} from '@core/services/data-set/data-set-actions-authorization.service';
import {LogService} from '@core/services/log.service';
import {PageTitleService} from '@core/services/page-title.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {
    ProjectDependencies,
    ProjectTask,
    ProjectTaskDependenciesService,
    ProjectTaskDependencyFetcher
} from '@core/services/tasks/projekt/project-task-dependencies.service';
import {ProjectTaskMetierService} from '@core/services/tasks/projekt/project-task-metier.service';
import {ProjektTaskSearchCriteria} from '@core/services/tasks/projekt/projekt-task-search-criteria.interface';
import {LinkedDatasetFromProject} from '@features/data-set/models/linked-dataset-from-project';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {
    NewDatasetRequestTableComponent
} from '@shared/business/projects/projects-datasets-tables/new-dataset-request-table/new-dataset-request-table.component';
import {
    OpenDatasetTableComponent
} from '@shared/business/projects/projects-datasets-tables/open-dataset-table/open-dataset-table.component';
import {
    RestrictedDatasetTableComponent
} from '@shared/business/projects/projects-datasets-tables/restricted-dataset-table/restricted-dataset-table.component';
import {BannerButtonComponent} from '@shared/core/banner/banner-button/banner-button.component';
import {TabComponent} from '@shared/core/common/tab/tab.component';
import {TabsComponent} from '@shared/core/common/tabs/tabs.component';
import {Level} from '@shared/core/layout/notification-template/notification-template.component';
import {PageComponent} from '@shared/core/layout/page/page.component';
import {TaskDetailHeaderComponent} from '@shared/core/workflow/common/task-detail-header/task-detail-header.component';
import {TaskDetailComponent} from '@shared/core/workflow/common/task-detail/task-detail.component';
import {WorkflowExpansionComponent} from '@shared/core/workflow/workflow-expansion/workflow-expansion.component';
import {WorkflowExpansionImageComponent} from '@shared/core/workflow/workflow-expansion/workflow-expansion-image/workflow-expansion-image.component';
import {injectDependencies} from '@shared/utils/dependencies-utils';
import {Confidentiality, NewDatasetRequest, ProjectStatus, ProjektService} from 'micro_service_modules/projekt/projekt-api';
import {Task} from 'micro_service_modules/projekt/projekt-api/model/task';
import {Project} from 'micro_service_modules/projekt/projekt-model';
import {forkJoin, of} from 'rxjs';
import {map, switchMap, tap} from 'rxjs/operators';
import {ProjectMainInformationsComponent} from '../../../project/components/project-main-informations/project-main-informations.component';
import {OwnerInformationComponent} from '../../components/owner-information/owner-information.component';
import {ProjectTaskHistoricComponent} from '../../components/project-task-historic/project-task-historic.component';

@Component({
    selector: 'app-project-task-detail',
    templateUrl: './project-task-detail.component.html',
    styleUrls: ['./project-task-detail.component.scss'],
    imports: [
        PageComponent,
        TaskDetailHeaderComponent,
        TabsComponent,
        TabComponent,
        WorkflowExpansionComponent,
        WorkflowExpansionImageComponent,
        MatAccordion,
        MatExpansionPanel,
        MatExpansionPanelHeader,
        MatExpansionPanelTitle,
        ProjectMainInformationsComponent,
        MatCard,
        MatCardContent,
        OpenDatasetTableComponent,
        RestrictedDatasetTableComponent,
        NewDatasetRequestTableComponent,
        OwnerInformationComponent,
        ProjectTaskHistoricComponent,
        BannerButtonComponent,
        TranslatePipe
    ]
})
export class ProjectTaskDetailComponent
    extends TaskDetailComponent<Project, ProjectDependencies, ProjectTask, ProjektTaskSearchCriteria>
    implements OnInit {
    isLoading: boolean;
    childrenIsLoading: boolean;
    isLoadingOpenDataset: boolean;
    isLoadingRestrictedDataset: boolean;
    isLoadingNewDatasetRequest: boolean;
    headerLibelle: string;
    isUpdateInProgress = false;
    idTask: string;
    projectPictureUuid: string;

    currentTask: Task;

    public dependencies: ProjectDependencies;
    addingInProgress = false;

    addActionAuthorized = false;
    deleteActionAuthorized = false;
    project: Project;
    readonly panelInitialTaskOpenState = signal(false);
    hasSections = false;
    linkError: string;
    protected readonly ProjectStatus = ProjectStatus;

    private readonly route = inject(ActivatedRoute);
    private readonly router = inject(Router);
    private readonly projectTaskDependencyFetcher = inject(ProjectTaskDependencyFetcher);
    private readonly iconRegistry = inject(MatIconRegistry);
    private readonly sanitizer = inject(DomSanitizer);
    private readonly projektService = inject(ProjektService);
    private readonly dataSetActionsAuthorizationService = inject(DataSetActionsAuthorizationService);
    private readonly projektMetierService = inject(ProjektMetierService);
    private readonly projectSubmissionService = inject(ProjectSubmissionService);
    private readonly projectConsultService = inject(ProjectConsultationService);
    private readonly pageTitleService = inject(PageTitleService);
    private readonly propertiesMetierService = inject(PropertiesMetierService);
    protected override readonly logger = inject(LogService);
    protected override readonly dialog = inject(MatDialog);
    protected override readonly translateService = inject(TranslateService);
    protected override readonly snackBarService = inject(SnackBarService);
    protected override readonly taskWithDependenciesService = inject(ProjectTaskDependenciesService);
    protected override readonly taskMetierService = inject(ProjectTaskMetierService);
    private readonly projectTaskMetierService = this.taskMetierService;

    constructor() {
        super(
            inject(MatDialog),
            inject(TranslateService),
            inject(SnackBarService),
            inject(ProjectTaskDependenciesService),
            inject(ProjectTaskMetierService),
            inject(LogService),
        );
        this.iconRegistry.addSvgIcon('project-svg-icon',
            this.sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/project_definition_key.svg'));
        this.headerLibelle = this.translateService.instant('personalSpace.projectDetails.headerTitlePublication');
    }


    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.taskId = params.taskId;
        });
        this.propertiesMetierService.get('front.contact').subscribe(linkError => {
            this.linkError = linkError;
        });
    }

    set taskId(idTask: string) {
        if (idTask) {
            this.isLoading = true;
            this.idTask = idTask;

            this.projectTaskMetierService.getTask(this.idTask).subscribe({
                next: (task: Task) => {
                    this.currentTask = task;
                    this.hasSections = !!task?.asset?.form?.sections;
                    this.projectPictureUuid = task?.asset?.form?.sections
                        ?.find(s => s.name?.includes('modified-project-picture'))
                        ?.fields?.[0]?.values?.[0];
                },
                error: (err) => this.logger.error('Error fetching task:', err)
            });
            this.taskWithDependenciesService.getTaskWithDependencies(idTask).pipe(
                tap(taskWithDependencies => {
                    // On définit ici le titre de l'onglet en se basant sur le titre de la réutilisation
                    // et si undefined, on définit le titre de l'onglet sur "Mes notifications"
                    if (taskWithDependencies.task.asset.title) {
                        this.pageTitleService.setPageTitle(taskWithDependencies.task.asset.title, this.translateService.instant('pageTitle.defaultDetail'));
                    } else {
                        this.pageTitleService.setPageTitleFromUrl('/personal-space/my-notifications');
                    }

                    if (taskWithDependencies.asset.project_status == ProjectStatus.Validated) {
                        this.headerLibelle = this.translateService.instant('personalSpace.projectDetails.headerTitleModification');
                    }

                    this.taskWithDependencies = taskWithDependencies;
                }),
                injectDependencies({
                    project: this.projectTaskDependencyFetcher.project,
                    logo: this.projectTaskDependencyFetcher.logo,
                }),
                injectDependencies({
                    ownerInfo: this.projectTaskDependencyFetcher.ownerInfo,
                    openLinkedDatasets: this.projectTaskDependencyFetcher.openLinkedDatasets,
                    restrictedLinkedDatasets: this.projectTaskDependencyFetcher.restrictedLinkedDatasets,
                    newDatasetsRequest: this.projectTaskDependencyFetcher.newDatasetsRequest,
                }),
                map(({task, asset, dependencies}) => {
                    return {
                        logo: dependencies.logo,
                        project: dependencies.project,
                        ownerInfo: dependencies.ownerInfo,
                        openLinkedDatasets: dependencies.openLinkedDatasets,
                        restrictedLinkedDatasets: dependencies.restrictedLinkedDatasets,
                        newDatasetsRequest: dependencies.newDatasetsRequest
                    };
                })
            ).subscribe({
                next: (dependencies: ProjectDependencies) => {
                    this.dependencies = dependencies;
                    this.isLoading = false;
                    this.projektService.isAuthenticatedUserProjectOwner(dependencies.project.uuid).subscribe(isOwner => {
                        this.addActionAuthorized = isOwner &&
                            this.dataSetActionsAuthorizationService.canAddDatasetFromProjectFromTask(dependencies.project);
                        this.deleteActionAuthorized = isOwner &&
                            this.dataSetActionsAuthorizationService.canDeleteDatasetFromProjectFromTask(dependencies.project);
                    });
                },
                error: (error) => {
                    this.isLoading = false;
                    this.logger.error('Error loading project dependencies:', error);
                }
            });
        }
    }

    protected goBackToList(): Promise<boolean> {
        return this.router.navigate(['/personal-space/my-notifications']);
    }

    updateAddButtonStatus(buttonStatus: boolean): void {
        this.addingInProgress = buttonStatus;
    }

    addLinkedDatasetAndReloadDependencies(linkToCreate: LinkedDatasetFromProject, isRestricted: boolean): void {
        this.updateAddButtonStatus(true);
        linkToCreate.project = this.dependencies.project;
        this.isLoadingRestrictedDataset = isRestricted;
        this.isLoadingOpenDataset = !isRestricted;

        this.projectSubmissionService.createLinkedDatasetFromProject(linkToCreate).pipe(
            // Reload dependencies
            switchMap(() => {
                if (isRestricted) {
                    return this.projectConsultService.getRestrictedLinkedDatasetsMetadata(this.dependencies.project.uuid).pipe(
                        tap((links: LinkedDatasetMetadatas[]) => {
                            this.dependencies.restrictedLinkedDatasets = links;
                            this.isLoadingRestrictedDataset = false;
                            this.addingInProgress = false;
                        })
                    );
                }

                return this.projectConsultService.getOpenedLinkedDatasetsMetadata(this.dependencies.project.uuid).pipe(
                    tap((links: LinkedDatasetMetadatas[]) => {
                        this.dependencies.openLinkedDatasets = links;
                        this.isLoadingOpenDataset = false;
                        this.addingInProgress = false;
                    })
                );
            }),
        ).subscribe({
            error: err => {
                this.logger.error('Error adding linked dataset:', err);
                this.isLoadingRestrictedDataset = false;
                this.isLoadingOpenDataset = false;
                this.addingInProgress = false;
            }
        });
    }

    handleOpenDatasetRequestUuidChanged(openDatasetRequestUuid: string): void {
        this.isLoadingOpenDataset = true;
        this.projektMetierService.deleteLinkedDatasetRequest(this.dependencies.project.uuid, openDatasetRequestUuid).pipe(
            // Reload dependencies
            switchMap(() => this.projectConsultService.getOpenedLinkedDatasetsMetadata(this.dependencies.project.uuid)),
            tap((links: LinkedDatasetMetadatas[]) => {
                this.dependencies.openLinkedDatasets = links;
                this.isLoadingOpenDataset = false;
            })
        ).subscribe({
            error: err => {
                this.logger.error('Error deleting open dataset request:', err);
                this.isLoadingOpenDataset = false;
                this.snackBarService.openSnackBar({
                    message: `${this.translateService.instant('personalSpace.projectDatasets.delete.error')}<a href="${this.linkError}">${this.translateService.instant('common.ici')}</a>`,
                    level: Level.ERROR
                });
            }
        });
    }

    handleRestrictedDatasetRequestUuidChanged(restrictedDatasetRequestUuid: string): void {
        this.isLoadingRestrictedDataset = true;
        this.projektMetierService.deleteLinkedDatasetRequest(this.dependencies.project.uuid, restrictedDatasetRequestUuid).pipe(
            // Reload dependencies
            switchMap(() => this.projectConsultService.getRestrictedLinkedDatasetsMetadata(this.dependencies.project.uuid)),
            tap((links: LinkedDatasetMetadatas[]) => {
                this.dependencies.restrictedLinkedDatasets = links;
                this.isLoadingRestrictedDataset = false;
            })
        ).subscribe({
            error: err => {
                this.logger.error('Error deleting restricted dataset request:', err);
                this.isLoadingRestrictedDataset = false;
                this.snackBarService.openSnackBar({
                    message: `${this.translateService.instant('personalSpace.projectDatasets.delete.error')}<a href="${this.linkError}">${this.translateService.instant('common.ici')}</a>`,
                    level: Level.ERROR
                });
            }
        });
    }

    addNewDatasetRequest(linkToCreate: NewDatasetRequest): void {
        this.updateAddButtonStatus(true);
        const projectUuid = this.dependencies.project.uuid;
        this.isLoadingNewDatasetRequest = true;
        this.projectSubmissionService.addNewDatasetRequest(projectUuid, linkToCreate, this.dependencies.project).pipe(
            // Reload dependencies
            switchMap(() => this.projectConsultService.getNewDatasetsRequest(projectUuid)),
            tap((values: NewDatasetRequest[]) => {
                this.dependencies.newDatasetsRequest = values;
                this.isLoadingNewDatasetRequest = false;
                this.updateAddButtonStatus(false);
            })
        ).subscribe({
            error: err => {
                this.logger.error('Error adding new dataset request:', err);
                this.isLoadingNewDatasetRequest = false;
                this.updateAddButtonStatus(false);
            }
        });
    }

    handleNewDatasetRequestUuidChanged(newDatasetRequestUuid: string): void {
        this.isLoadingNewDatasetRequest = true;
        this.projektMetierService.deleteNewDatasetRequest(this.dependencies.project.uuid, newDatasetRequestUuid).pipe(
            // Reload dependencies
            switchMap(() => this.projectConsultService.getNewDatasetsRequest(this.dependencies.project.uuid)),
            tap((values: NewDatasetRequest[]) => {
                this.dependencies.newDatasetsRequest = values;
                this.isLoadingNewDatasetRequest = false;
            })
        ).subscribe({
            error: err => {
                this.logger.error('Error deleting new dataset request:', err);
                this.isLoadingNewDatasetRequest = false;
                this.snackBarService.openSnackBar({
                    message: `${this.translateService.instant('personalSpace.projectDatasets.delete.error')}<a href="${this.linkError}">${this.translateService.instant('common.ici')}</a>`,
                    level: Level.ERROR
                });
            }
        });
    }

    updateInProgress($event: boolean): void {
        this.isUpdateInProgress = $event;
    }

    updateProjectTask(obj: { confidentialities: Confidentiality[], form: FormGroup }): void {
        this.isUpdateInProgress = true;
        this.childrenIsLoading = true;
        this.projectSubmissionService.updateProjectTaskField(this.currentTask, obj.form, obj.confidentialities);
        this.projectTaskMetierService.claimTask(this.idTask).pipe(
            switchMap(item => {
                return forkJoin([
                    this.projectTaskMetierService.updateTask(this.currentTask),
                    of(item) // Utilisation de of pour conserver la valeur du premier switchMap
                ]);
            }),
            tap(([updatedTask, item]) => {
                this.project = updatedTask.asset as Project;
                this.projektMetierService.uploadLogo(this.project.uuid, obj.form.get('image').value.file).subscribe();
            }),
            switchMap(() => {
                return this.projectTaskMetierService.unclaimTask(this.idTask);
            })
        ).subscribe({
            next: () => {
                this.isUpdateInProgress = false;
                this.childrenIsLoading = false;
                this.taskId = this.idTask;
                this.snackBarService.showSuccess(this.translateService.instant('personalSpace.project.tabs.update.successUpdate'));
            },
            error: (e) => {
                this.logger.error(e);
                this.snackBarService.add(this.translateService.instant('personalSpace.project.tabs.update.error'));
                this.childrenIsLoading = false;
            }
        });
    }
}
