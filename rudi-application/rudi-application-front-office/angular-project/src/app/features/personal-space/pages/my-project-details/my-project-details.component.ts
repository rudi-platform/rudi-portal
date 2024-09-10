import {Component, OnInit} from '@angular/core';
import {FormGroup} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import {ProjectDependenciesFetchers, ProjectDependenciesService} from '@core/services/asset/project/project-dependencies.service';
import {LogService} from '@core/services/log.service';
import {OrganizationMetierService} from '@core/services/organization/organization-metier.service';
import {PageTitleService} from '@core/services/page-title.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {UserService} from '@core/services/user.service';
import {TranslateService} from '@ngx-translate/core';
import {injectDependencies} from '@shared/utils/dependencies-utils';
import {Status} from 'micro_service_modules/api-bpmn';
import {OwnerType} from 'micro_service_modules/konsent/konsent-api';
import {
    Confidentiality,
    DatasetConfidentiality,
    Field,
    OwnerInfo,
    Project,
    ProjectStatus,
    Task,
    TaskService
} from 'micro_service_modules/projekt/projekt-api';
import {ProjectType, Support, TargetAudience, TerritorialScale} from 'micro_service_modules/projekt/projekt-model';
import * as moment from 'moment';
import {map, switchMap} from 'rxjs/operators';

interface MyProjectDetailsDependencies {
    project?: Project;
    logo?: string;
    ownerInfo?: OwnerInfo;
}

@Component({
    selector: 'app-my-project-details',
    templateUrl: './my-project-details.component.html',
    styleUrls: ['./my-project-details.component.scss']
})
export class MyProjectDetailsComponent implements OnInit {
    public childrenIsLoading: boolean;
    public isUpdateInProgress: boolean;
    public project: Project;
    public projectLogo: string;
    public projectOwnerInfo: OwnerInfo;
    public loading;
    public isModified;
    public _displayApiTab: boolean = false;
    public draftTask: Task;

    constructor(private readonly route: ActivatedRoute,
                private projectDependenciesService: ProjectDependenciesService,
                private projectDependenciesFetchers: ProjectDependenciesFetchers,
                private readonly pageTitleService: PageTitleService,
                private readonly organizationService: OrganizationMetierService,
                private readonly userService: UserService,
                private readonly logService: LogService,
                private readonly translateService: TranslateService,
                private readonly taskService: TaskService,
                private readonly snackBarService: SnackBarService) {
        this.loading = false;
        this.isModified = false;
        this.childrenIsLoading = false;
        this.isUpdateInProgress = false;
    }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            this.projectUuid = params.projectUuid;
        });

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
            })
        ).subscribe({
            next: (dependencies: MyProjectDetailsDependencies) => {
                if (dependencies.project.title) {
                    this.pageTitleService.setPageTitle(dependencies.project.title, this.translateService.instant('pageTitle.defaultDetail'));
                } else {
                    this.pageTitleService.setPageTitleFromUrl('/personal-space/my-activity');
                }
                this.project = dependencies.project;
                if (dependencies.project.owner_type === OwnerType.User) {
                    this._displayApiTab = true;
                } else {
                    this.isAdministrator(dependencies.project.owner_uuid);
                }
                this.projectLogo = dependencies.logo;
                this.projectOwnerInfo = dependencies.ownerInfo;
                this.loading = false;
            },
            error: (error) => {
                console.error(error);
                this.loading = false;
            }
        });
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


    displayModifiedButton(): boolean {
        const containsLinkedDatasets = this.project?.linked_datasets.filter((d) => d?.dataset_confidentiality === DatasetConfidentiality.Restricted).length > 0;
        return this.project?.status === Status.Completed && !containsLinkedDatasets;
    }

    onHandleModified(): void {
        this.isModified = !this.isModified;
    }

    updateProjectTask(formModified: { confidentialities: Confidentiality[]; form: FormGroup, messageToModerator?: string }): void {
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

    protected readonly ProjectStatus = ProjectStatus;

    get displayApiTab(): boolean {
        return this._displayApiTab;
    }

    isAdministrator(organizationUuid: string): void {
        this.organizationService.isAdministrator(organizationUuid)
            .subscribe({
                next: (isAdministrator: boolean) => {
                    this._displayApiTab = isAdministrator;
                },
                error: (error) => {
                    this.logService.error(error);
                }
            });
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
