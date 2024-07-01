import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {ActivatedRoute, Router} from '@angular/router';
import {PageTitleService} from '@core/services/page-title.service';
import {LinkedDatasetFromProject} from '@features/data-set/models/linked-dataset-from-project';
import {ProjectConsultationService} from '@core/services/asset/project/project-consultation.service';
import {LinkedDatasetMetadatas} from '@core/services/asset/project/project-dependencies.service';
import {ProjectSubmissionService} from '@core/services/asset/project/project-submission.service';
import {ProjektMetierService} from '@core/services/asset/project/projekt-metier.service';
import {DataSetActionsAuthorizationService} from '@core/services/data-set/data-set-actions-authorization.service';
import {LogService} from '@core/services/log.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {
    ProjectDependencies,
    ProjectTask,
    ProjectTaskDependenciesService,
    ProjectTaskDependencyFetcher
} from '@core/services/tasks/projekt/project-task-dependencies.service';
import {ProjectTaskMetierService} from '@core/services/tasks/projekt/project-task-metier.service';
import {ProjektTaskSearchCriteria} from '@core/services/tasks/projekt/projekt-task-search-criteria.interface';
import {TranslateService} from '@ngx-translate/core';
import {Level} from '@shared/notification-template/notification-template.component';
import {TaskDetailComponent} from '@shared/task-detail/task-detail.component';
import {injectDependencies} from '@shared/utils/dependencies-utils';
import {NewDatasetRequest, ProjektService} from 'micro_service_modules/projekt/projekt-api';
import {Project} from 'micro_service_modules/projekt/projekt-model';
import {map, switchMap, tap} from 'rxjs/operators';
import {Task} from 'micro_service_modules/projekt/projekt-api/model/task';

@Component({
    selector: 'app-project-task-detail',
    templateUrl: './project-task-detail.component.html',
    styleUrls: ['./project-task-detail.component.scss']
})
export class ProjectTaskDetailComponent
    extends TaskDetailComponent<Project, ProjectDependencies, ProjectTask, ProjektTaskSearchCriteria>
    implements OnInit {

    isLoading: boolean;
    isLoadingOpenDataset: boolean;
    isLoadingRestrictedDataset: boolean;
    isLoadingNewDatasetRequest: boolean;

    isUpdateInProgress = false;

    currentTask: Task;

    public dependencies: ProjectDependencies;
    addingInProgress = false;

    addActionAuthorized = false;
    deleteActionAuthorized = false;


    constructor(
        private readonly route: ActivatedRoute,
        private readonly router: Router,
        private readonly projectTaskDependencyFetcher: ProjectTaskDependencyFetcher,
        private readonly iconRegistry: MatIconRegistry,
        private readonly sanitizer: DomSanitizer,
        private readonly projektService: ProjektService,
        private readonly dataSetActionsAuthorizationService: DataSetActionsAuthorizationService,
        protected logger: LogService,
        readonly dialog: MatDialog,
        readonly translateService: TranslateService,
        readonly snackBarService: SnackBarService,
        readonly taskWithDependenciesService: ProjectTaskDependenciesService,
        readonly projectTaskMetierService: ProjectTaskMetierService,
        readonly projektMetierService: ProjektMetierService,
        readonly projectSubmissionService: ProjectSubmissionService,
        readonly projectConsultService: ProjectConsultationService,
        private readonly pageTitleService: PageTitleService,
    ) {
        super(dialog, translateService, snackBarService, taskWithDependenciesService, projectTaskMetierService, logger);
        iconRegistry.addSvgIcon('project-svg-icon',
            sanitizer.bypassSecurityTrustResourceUrl('assets/icons/process-definitions-key/project_definition_key.svg'));
    }


    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.taskId = params.taskId;
        });
    }

    set taskId(idTask: string) {
        if (idTask) {
            this.isLoading = true;
            this.taskWithDependenciesService.getTaskWithDependencies(idTask).pipe(
                tap(taskWithDependencies => {
                     // On définit ici le titre de l'onglet en se basant sur le titre de la réutilisation
                     // et si undefined, on définit le titre de l'onglet sur "Mes notifications"
                    if (taskWithDependencies.task.asset.title){
                    this.pageTitleService.setPageTitle(taskWithDependencies.task.asset.title, this.translateService.instant('pageTitle.defaultDetail'));
                    } else {
                        this.pageTitleService.setPageTitleFromUrl('/personal-space/my-notifications');
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
                        this.addActionAuthorized = isOwner && this.dataSetActionsAuthorizationService.canAddDatasetFromProject(dependencies.project);
                        this.deleteActionAuthorized = isOwner && this.dataSetActionsAuthorizationService.canDeleteDatasetFromProject(dependencies.project);
                    });
                },
                error: (error) => {
                    this.isLoading = false;
                    console.error(error);
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
                console.error(err);
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
                console.error(err);
                this.isLoadingOpenDataset = false;
                this.snackBarService.openSnackBar({
                    message: this.translateService.instant('personalSpace.projectDatasets.delete.error'),
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
                console.error(err);
                this.isLoadingRestrictedDataset = false;
                this.snackBarService.openSnackBar({
                    message: this.translateService.instant('personalSpace.projectDatasets.delete.error'),
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
                console.error(err);
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
                console.error(err);
                this.isLoadingNewDatasetRequest = false;
                this.snackBarService.openSnackBar({
                    message: this.translateService.instant('personalSpace.projectDatasets.delete.error'),
                    level: Level.ERROR
                });
            }
        });
    }

    // Mettre à jour le mode modification de la réutilisation
    updateInProgress(data: boolean): void {
        this.isUpdateInProgress = data;
    }

    // Mettre à jour la task en cours
    updateCurrentTask(data: Task): void {
        this.currentTask = data;
    }
}
