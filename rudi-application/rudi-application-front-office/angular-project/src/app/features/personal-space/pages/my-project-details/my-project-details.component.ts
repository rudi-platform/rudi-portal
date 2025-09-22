import {Component, OnInit} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute} from '@angular/router';
import {ProjectDependenciesFetchers, ProjectDependenciesService} from '@core/services/asset/project/project-dependencies.service';
import {LogService} from '@core/services/log.service';
import {OrganizationMetierService} from '@core/services/organization/organization-metier.service';
import {PageTitleService} from '@core/services/page-title.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {ProjectTaskMetierService} from '@core/services/tasks/projekt/project-task-metier.service';
import {CloseEvent, DialogClosedData} from '@features/data-set/models/dialog-closed-data';
import {TranslateService} from '@ngx-translate/core';
import {Level} from '@shared/notification-template/notification-template.component';
import {GetBackendPropertyPipe} from '@shared/pipes/get-backend-property.pipe';
import {TabComponent} from '@shared/tab/tab.component';
import {TabsComponent} from '@shared/tabs/tabs.component';
import {injectDependencies} from '@shared/utils/dependencies-utils';
import {WorkflowFormDialogOutputData} from '@shared/workflow-form-dialog/types';
import {WorkflowFormDialogComponent} from '@shared/workflow-form-dialog/workflow-form-dialog.component';
import {Form, Status} from 'micro_service_modules/api-bpmn';
import {OwnerType} from 'micro_service_modules/konsent/konsent-api';
import {
    Confidentiality,
    DatasetConfidentiality,
    Field,
    LinkedDatasetStatus,
    OwnerInfo,
    Project,
    ProjectFormType,
    ProjectStatus,
    Task,
    TaskService
} from 'micro_service_modules/projekt/projekt-api';
import {ProjectType, Support, TargetAudience, TerritorialScale} from 'micro_service_modules/projekt/projekt-model';
import moment from 'moment';
import {forkJoin, Observable, of} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';

@Component({
    selector: 'app-my-project-details',
    templateUrl: './my-project-details.component.html',
    styleUrls: ['./my-project-details.component.scss'],
    standalone: false
})
export class MyProjectDetailsComponent implements OnInit {
    public childrenIsLoading: boolean;
    public isUpdateInProgress: boolean;
    public project: Project;
    public projectLogo: string;
    public projectOwnerInfo: OwnerInfo;
    public loading;
    public isModified;
    public draftTask: Task;
    public form: Form;
    public isLoading: boolean;
    public _displayApiTab: boolean;
    private urlToRedirectIfError: string;
    protected readonly ProjectStatus = ProjectStatus;
    protected readonly Status = Status;

    constructor(private readonly route: ActivatedRoute,
                private projectDependenciesService: ProjectDependenciesService,
                private projectDependenciesFetchers: ProjectDependenciesFetchers,
                private readonly pageTitleService: PageTitleService,
                private readonly organizationService: OrganizationMetierService,
                private readonly logService: LogService,
                private readonly translateService: TranslateService,
                private readonly taskService: TaskService,
                private readonly snackBarService: SnackBarService,
                private readonly dialog: MatDialog,
                private readonly projectTaskMetierService: ProjectTaskMetierService,
                private readonly getBackendProperty: GetBackendPropertyPipe) {
        this.loading = false;
        this.isLoading = false;
        this._displayApiTab = false;
        this.isModified = false;
        this.childrenIsLoading = false;
        this.isUpdateInProgress = false;
    }

    set projectUuid(uuid: string) {
        this.loading = true;
        this.projectDependenciesService.getProject(uuid).pipe(
            injectDependencies({
                ownerInfo: this.projectDependenciesFetchers.ownerInfo,
                logo: this.projectDependenciesFetchers.logo,
            }),
            map(({project, dependencies}) => {
                return {
                    project,
                    logo: dependencies.logo,
                    ownerInfo: dependencies.ownerInfo
                };
            }),
            switchMap(data => {
                return forkJoin([
                    this.taskService.lookupProjectDraftForm(ProjectFormType.DraftArchive),
                    of(data)
                ]);
            }),
        ).subscribe({
            next: ([form, data]) => {
                if (data?.project?.status === Status.Completed) {
                    this.form = form;
                }
                loadProjectData(data);
            },
            error: (error) => {
                console.error(error);
                this.loading = false;
            }
        });

        const loadProjectData = (dependencies) => {
            if (dependencies.project.title) {
                this.pageTitleService.setPageTitle(dependencies.project.title, this.translateService.instant('pageTitle.defaultDetail'));
            } else {
                this.pageTitleService.setPageTitleFromUrl('/personal-space/my-activity');
            }
            this.project = dependencies.project;

            if (this.project.project_status != ProjectStatus.Validated) {
                this._displayApiTab = false;
            } else {
                if (this.project.owner_type === OwnerType.User) {
                    this._displayApiTab = true;
                } else {
                    this.isAdministrator(this.project.owner_uuid);
                }
            }
            this.projectLogo = dependencies.logo;
            this.projectOwnerInfo = dependencies.ownerInfo;
            this.loading = false;
        };
    }

    ngOnInit(): void {
        this.getBackendProperty.transform('front.contact').subscribe({
            next: url => {
                this.urlToRedirectIfError = url;
            },
            error: err => {
                this.urlToRedirectIfError = 'https://rudi.fr/contact';
            }
        });
        this.loadProject();
    }

    /**
     * Récupération d'une valeur de champ date au format DD/MM/YYYY
     * @param fieldName le nom du champ du projet
     */
    getDateToFormat(fieldName: string): string {
        if (!this.project || this.project[fieldName] == null) {
            return null;
        }

        return moment(this.project[fieldName]).format('DD/MM/YYYY');
    }

    isProjectCompleted(): boolean {
        return this.project?.status === Status.Completed;
    }

    projectContainsNewDatasetRequestInProgress(): boolean {
        return this.project?.dataset_requests?.filter((newDatasetRequest) => newDatasetRequest?.status !== Status.Completed).length > 0;
    }

    isProjectUpdatable(): boolean {
        const containsLinkedDatasetsRestricted = this.project?.linked_datasets?.filter((linkedDataset) => linkedDataset?.dataset_confidentiality === DatasetConfidentiality.Restricted).length > 0;
        return this.isProjectCompleted() && !this.projectContainsNewDatasetRequestInProgress() && !containsLinkedDatasetsRestricted;
    }

    isProjectArchived(): boolean {
        const containsLinkedDatasetsInProgress = this.project?.linked_datasets?.filter((linkedDataset) => linkedDataset?.linked_dataset_status === LinkedDatasetStatus.InProgress || linkedDataset?.linked_dataset_status === LinkedDatasetStatus.Draft).length > 0;
        return this.isProjectCompleted() && !this.projectContainsNewDatasetRequestInProgress() && !containsLinkedDatasetsInProgress;
    }

    updateProjectTask(formModified: {
        confidentialities: Confidentiality[];
        form: FormGroup,
        messageToModerator?: string
    }): void {
        if (this.project) {
            this.childrenIsLoading = true;
            this.isUpdateInProgress = true;
            this.taskService.createProjectDraft(this.project).pipe(
                switchMap(task => {
                    this.draftTask = task;
                    const resultMap = {
                        title: this.getFieldValue(formModified.form.value.title),
                        description: this.getFieldValue(formModified.form.value?.description),
                        expectedCompletionStartDate: this.getFieldValue(formModified.form.value.begin_date, this.formatDate),
                        expectedCompletionEndDate: this.getFieldValue(formModified.form.value.end_date, this.formatDate),
                        targetAudiences: this.getFieldList(formModified.form.value.publicCible, (audience: TargetAudience) => audience?.code),
                        territorialScale: this.getFieldList([formModified.form.value?.echelle], (scale: TerritorialScale) => scale?.code),
                        detailedTerritorialScale: this.getFieldValue(formModified.form.value?.territoire),
                        desiredSupports: this.getFieldList(formModified.form.value.accompagnement, (support: Support) => support?.code),
                        type: this.getFieldList([formModified.form.value?.type], (type: ProjectType) => type?.code),
                        reutilisationStatus: this.getFieldValue(formModified.form.value.reuse_status?.code),
                        confidentiality: this.getFieldValue(formModified.form.value?.confidentiality?.code),
                        accessUrl: this.getFieldValue(formModified.form.value?.url),
                        messageToModerator: this.getFieldValue(formModified.messageToModerator),
                    };

                    const firstSection = this.draftTask.asset.form.sections[0].fields?.map((field: Field) => {
                        return {...field, values: resultMap.messageToModerator};
                    });
                    const secondSection = this.draftTask.asset.form.sections[1]?.fields.map(field => {
                        return {...field, values: resultMap[field.definition.name]};
                    });

                    const draftTaskModified = {
                        ...this.draftTask, asset: {
                            ...this.draftTask.asset, form: {
                                ...this.draftTask.asset.form, sections: [
                                    {...this.draftTask.asset.form.sections[0], fields: firstSection},
                                    {...this.draftTask.asset.form.sections[1], fields: secondSection}
                                ]
                            }
                        }
                    };
                    return this.taskService.startProjectTask(draftTaskModified);
                })
            ).subscribe({
                next: (task) => {
                    this.loadProject();
                    this.snackBarService.showSuccess(this.translateService.instant('personalSpace.project.tabs.update.success'));
                    this.childrenIsLoading = false;
                    this.isUpdateInProgress = false;
                },
                error: (error) => {
                    console.log(error);
                    this.snackBarService.add(this.translateService.instant('personalSpace.project.tabs.update.error'));
                    this.childrenIsLoading = false;
                    this.isUpdateInProgress = false;
                }
            });
        }


    }

    get displayApiTab(): boolean {
        return this._displayApiTab;
    }

    isAdministrator(organizationUuid: string): void {
        this.organizationService.isAdministrator(organizationUuid).subscribe({
            next: (isAdministrator: boolean) => {
                this._displayApiTab = isAdministrator;
            },
            error: (error) => {
                this.logService.error(error);
            }
        });

    }

    loadProject(): void {
        this.route.params.subscribe(params => {
            this.projectUuid = params.projectUuid;
        });
    }

    openPopinArchive(tabs: TabsComponent, tabInformation: TabComponent): void {
        this.dialog.open(WorkflowFormDialogComponent, {
            data: {
                title: this.translateService.instant('personalSpace.project.archive.archived'),
                form: this.form
            }
        }).afterClosed().subscribe((result: DialogClosedData<WorkflowFormDialogOutputData>) => {
            if (result?.closeEvent === CloseEvent.VALIDATION) {
                this.archiveProject().subscribe({
                    next: (value: Task) => {
                        this.snackBarService.openSnackBar({
                            level: Level.SUCCESS,
                            message: this.translateService.instant('personalSpace.project.archive.success'),
                        }, 3000);
                        tabs.selectTab(tabInformation);
                        this.loadProject();
                    },
                    error: err => {
                        if (err.status === 400) {
                            this.snackBarService.openSnackBar({
                                level: Level.ERROR,
                                message: this.translateService.instant('personalSpace.project.archive.errorTaskInProgress'),
                            }, 3000);
                        } else {
                            this.snackBarService.openSnackBar({
                                level: Level.ERROR,
                                message: `${this.translateService.instant('personalSpace.project.archive.error')} <a href="${this.urlToRedirectIfError}" target="_blank">${this.translateService.instant('common.ici')}</a>`,
                            }, 3000);
                        }
                    }
                });
            }
        });
    }

    archiveProject(): Observable<Task> {
        this.project.form = this.form;
        // 1) On crée le draft à partir du projet
        return this.projectTaskMetierService.createDraft(this.project).pipe(
            // 2 et maintenant on doit démarrer le workflow de cet élément
            switchMap((task: Task) => {
                return this.projectTaskMetierService.startTask(task);
            }),
        );
    }

    private isNotExisted(value: any): boolean {
        return (value == null || value === '');
    }

    private getFieldValue<T>(field: T, formatter: (value: T) => any = (v) => v): any[] | null {
        return this.isNotExisted(field) ? null : [formatter(field)];
    }

    private getFieldList<T>(field: T[], mapper: (item: T) => any): any[] | null {
        return field?.length ? field.map(mapper) : null;
    }

    private formatDate(date: any): string {
        return date?.format();
    }
}
