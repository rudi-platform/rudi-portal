import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {KonsultApiAccessService} from '@core/services/api-access/konsult/konsult-api-access.service';
import {ProjectConsultationService} from '@core/services/asset/project/project-consultation.service';
import {
    LinkedDatasetMetadatas,
    ProjectDependenciesFetchers,
    ProjectWithDependencies
} from '@core/services/asset/project/project-dependencies.service';
import {ProjectSubmissionService} from '@core/services/asset/project/project-submission.service';
import {ProjektMetierService} from '@core/services/asset/project/projekt-metier.service';
import {DataSetActionsAuthorizationService} from '@core/services/data-set/data-set-actions-authorization.service';
import {IconRegistryService} from '@core/services/icon-registry.service';
import {PropertiesMetierService} from '@core/services/properties-metier.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {LinkedDatasetFromProject} from '@features/data-set/models/linked-dataset-from-project';
import {TranslateService} from '@ngx-translate/core';
import {ALL_TYPES} from '@shared/models/title-icon-type';
import {Level} from '@shared/notification-template/notification-template.component';
import {RowTableData} from '@shared/project-datasets-tables/dataset.interface';
import {injectDependencies} from '@shared/utils/dependencies-utils';
import {WorkflowFormDialogComponent} from '@shared/workflow-form-dialog/workflow-form-dialog.component';
import {Field, NewDatasetRequest, ProjektService, Section} from 'micro_service_modules/projekt/projekt-api';
import {Form} from 'micro_service_modules/projekt/projekt-api/model/form';
import {Project} from 'micro_service_modules/projekt/projekt-model';
import {of} from 'rxjs';
import {switchMap, tap} from 'rxjs/operators';

@Component({
    selector: 'app-project-datasets-tab',
    templateUrl: './project-datasets-tab.component.html',
    styleUrls: ['./project-datasets-tab.component.scss'],
    standalone: false
})
export class ProjectDatasetsTabComponent implements OnInit {

    /**
     * Toutes les demandes d'un projet avec le JDD
     */
    linkedDatasetMetadatas: LinkedDatasetMetadatas[] = [];

    /**
     * Demandes enrichies auxquelles on peut souscrire
     */
    subscribableLinkedDatasetMetadatas: LinkedDatasetMetadatas[] = [];

    /**
     * Loader de l'onglet
     */
    loading = false;
    loadingCommentData: boolean;
    /**
     * Loader dédié
     */
    openedLinkedDatasetLoading = false;
    /**
     * Loader dédié
     */
    restrictedLinkedDatasetLoading = false;
    /**
     * Loader dédié
     */
    newLinkedDatasetLoading = false;
    /**
     * Indique aux composants enfants (les 2 autres) qu'il y a un ajout en cours
     */
    addingInProgress = true;
    /**
     * S'il y'a eu une erreur pendant la récupération des deps de l'onglet
     */
    initializationError = false;

    addActionAuthorized = false;
    deleteActionAuthorized = false;
    linkError: string;


    /**
     * Projet de l'onglet
     */
    _project: Project;

    linkedDatasetsRestricted: LinkedDatasetMetadatas[];
    newDatasetRequests: NewDatasetRequest[];
    linkedDatasetsOpened: LinkedDatasetMetadatas[];
    projectWithDependencies: ProjectWithDependencies;
    addFromConsultation = true;

    private startAllLoader(): void {
        this.loading = true;
        this.openedLinkedDatasetLoading = true;
        this.restrictedLinkedDatasetLoading = true;
        this.newLinkedDatasetLoading = true;
    }

    // Quand ajout en cours, tous les ajouts passent en disable
    // Mettre le loader vraiment sur le tableau où l'ajout est en cours

    private stopLoaders(): void {
        this.loading = false;
        this.openedLinkedDatasetLoading = false;
        this.restrictedLinkedDatasetLoading = false;
        this.newLinkedDatasetLoading = false;
    }

    /**
     * event emitter qui emit lorsqu'on rajoute un JDD à une réutilisation
     */
    @Output()
    onProjectIsUpdate: EventEmitter<void>;

    @Input()
    set project(value: Project) {
        this._project = value;
        if (this._project) {
            this.startAllLoader();
            of(new ProjectWithDependencies(this._project, {})).pipe(
                injectDependencies({
                    linkedDatasetMetadatas: this.projectDependenciesFetchers.linkedDatasetMetadatas(),
                    newDatasetRequests: this.projectDependenciesFetchers.newDatasetRequests,
                    linkedDatasetsOpened: this.projectDependenciesFetchers.linkedDatasetsOpened,
                    linkedDatasetsRestricted: this.projectDependenciesFetchers.linkedDatasetsRestricted
                }),
                tap((projectWithDependencies: ProjectWithDependencies) => {
                    this.linkedDatasetMetadatas = projectWithDependencies.dependencies.linkedDatasetMetadatas;
                    this.linkedDatasetsOpened = this.projektMetierService.getDatasetsByUpdatedDate(projectWithDependencies.dependencies.linkedDatasetsOpened);
                    this.linkedDatasetsRestricted = this.projektMetierService.getDatasetsByUpdatedDate(projectWithDependencies.dependencies.linkedDatasetsRestricted);
                    this.newDatasetRequests = this.projektMetierService.getRequestsByUpdatedDate(projectWithDependencies.dependencies.newDatasetRequests);
                }),
                switchMap((projectWithDependencies: ProjectWithDependencies) => this.apiAccessService.filterSubscribableMetadatas(
                        projectWithDependencies.dependencies.linkedDatasetMetadatas,
                        this._project
                    )
                ),
                tap((subscribables: LinkedDatasetMetadatas[]) => {
                    this.subscribableLinkedDatasetMetadatas = subscribables;
                })
            ).subscribe({
                complete: () => {
                    this.stopLoaders();
                    this.addingInProgress = false;
                    this.initializationError = false;
                },
                error: (error) => {
                    console.error(error);
                    this.initializationError = true;
                    this.addingInProgress = false;
                    this.stopLoaders();
                }
            });
            this.projektService.isAuthenticatedUserProjectOwner(this._project.uuid).subscribe(isOwner => {
                this.addActionAuthorized = isOwner && this.dataSetActionsAuthorizationService.canAddDatasetFromProjectFromDetail(this._project);
                this.deleteActionAuthorized = isOwner && this.dataSetActionsAuthorizationService.canDeleteDatasetFromProjectFromDetail(this._project);
            });
        }
    }

    constructor(
        private readonly dialog: MatDialog,
        private readonly apiAccessService: KonsultApiAccessService,
        private readonly projectDependenciesFetchers: ProjectDependenciesFetchers,
        private readonly projectSubmissionService: ProjectSubmissionService,
        private readonly projectConsultService: ProjectConsultationService,
        private readonly projektMetierService: ProjektMetierService,
        private readonly snackBarService: SnackBarService,
        private readonly translateService: TranslateService,
        private readonly projektService: ProjektService,
        private readonly dataSetActionsAuthorizationService: DataSetActionsAuthorizationService,
        private readonly propertiesMetierService: PropertiesMetierService,
        iconRegistryService: IconRegistryService,
    ) {
        iconRegistryService.addAllSvgIcons(ALL_TYPES);
        this.onProjectIsUpdate = new EventEmitter<void>;
        this.loadingCommentData = false;
    }

    ngOnInit(): void {
        this.propertiesMetierService.get('front.contact').subscribe(linkError => {
            this.linkError = linkError;
        });
    }


    updateAddButtonStatus(buttonStatus: boolean): void {
        this.addingInProgress = buttonStatus;
    }

    /**
     * Appel des services dédiés pour l'ajout d'un JDD (ouvert, restreint) et demarrage de workflow associé
     * @param linkToCreate JDD à ajouter
     * @param isRestricted indique si c'est un JDD restreint qu'on ajoute ou pas
     */
    addLinkedDatasetAndReloadDependencies(linkToCreate: LinkedDatasetFromProject, isRestricted: boolean): void {
        this.updateAddButtonStatus(true);
        linkToCreate.project = this._project;
        if (isRestricted) {
            this.restrictedLinkedDatasetLoading = true;
        } else {
            this.openedLinkedDatasetLoading = true;
        }
        this.projectSubmissionService.createLinkedDatasetFromProject(linkToCreate).pipe(
            // Reload dependencies
            switchMap(() => {
                if (isRestricted) {
                    return this.projectConsultService.getRestrictedLinkedDatasetsMetadata(this._project.uuid).pipe(
                        tap((links: LinkedDatasetMetadatas[]) => {
                            this.linkedDatasetsRestricted = links;
                            this.restrictedLinkedDatasetLoading = false;
                            this.addingInProgress = false;
                        })
                    );
                } else {
                    return this.projectConsultService.getOpenedLinkedDatasetsMetadata(this._project.uuid).pipe(
                        tap((links: LinkedDatasetMetadatas[]) => {
                            this.linkedDatasetsOpened = links;
                            this.openedLinkedDatasetLoading = false;
                            this.addingInProgress = false;
                        })
                    );
                }
            }),
        ).subscribe({
            next: res => {
                this.onProjectIsUpdate.emit();
            },
            error: err => {
                console.error(err);
                this.openedLinkedDatasetLoading = false;
                this.restrictedLinkedDatasetLoading = false;
                this.addingInProgress = false;
            }
        });
    }

    /**
     * Appel des services dédiés pour l'ajout d'un lien de type nouvelle demande de données
     * @param linkToCreate nouvelle demande de JDD à créer
     */
    addNewDatasetRequest(linkToCreate: NewDatasetRequest): void {
        this.updateAddButtonStatus(true);
        const projectUuid = this._project.uuid;
        this.newLinkedDatasetLoading = true;
        this.projectSubmissionService.addNewDatasetRequest(projectUuid, linkToCreate, this._project).pipe(
            // Reload dependencies
            switchMap(() => this.projectConsultService.getNewDatasetsRequest(projectUuid)),
            tap((values: NewDatasetRequest[]) => {
                this.newDatasetRequests = values;
                this.newLinkedDatasetLoading = false;
                this.updateAddButtonStatus(false);
            })
        ).subscribe({
            next: res => {
                this.onProjectIsUpdate.emit();
            },
            error: err => {
                console.error(err);
                this.newLinkedDatasetLoading = false;
                this.updateAddButtonStatus(false);
            }
        });
    }


    /**
     * Méthode qui récupère l'uuid du open-dataset-request à supprimer , le supprime et recharge le tableau des new-dataset-requests
     */
    handleOpenDatasetRequestUuidChanged(openDatasetRequestUuid: string): void {
        this.openedLinkedDatasetLoading = true;
        this.projektMetierService.deleteLinkedDatasetRequest(this._project.uuid, openDatasetRequestUuid).pipe(
            // Reload dependencies
            switchMap(() => this.projectConsultService.getOpenedLinkedDatasetsMetadata(this._project.uuid)),
            tap((links: LinkedDatasetMetadatas[]) => {
                this.linkedDatasetsOpened = links;
                this.openedLinkedDatasetLoading = false;
            })
        ).subscribe({
            error: err => {
                console.error(err);
                this.openedLinkedDatasetLoading = false;
                this.snackBarService.openSnackBar({
                    message: `${this.translateService.instant('personalSpace.projectDatasets.delete.error')}<a href="${this.linkError}">${this.translateService.instant('common.ici')}</a>`,
                    level: Level.ERROR
                });
            }
        });
    }

    /**
     * Méthode qui récupère l'uuid du restricted-dataset-request à supprimer , le supprime et recharge le tableau des new-dataset-requests
     */
    handleRestrictedDatasetRequestUuidChanged(restrictedDatasetRequestUuid: string): void {
        this.restrictedLinkedDatasetLoading = true;
        this.projektMetierService.deleteLinkedDatasetRequest(this._project.uuid, restrictedDatasetRequestUuid).pipe(
            // Reload dependencies
            switchMap(() => this.projectConsultService.getRestrictedLinkedDatasetsMetadata(this._project.uuid)),
            tap((links: LinkedDatasetMetadatas[]) => {
                this.linkedDatasetsRestricted = links;
                this.restrictedLinkedDatasetLoading = false;
            })
        ).subscribe({
            error: err => {
                console.error(err);
                this.restrictedLinkedDatasetLoading = false;
                this.snackBarService.openSnackBar({
                    message: `${this.translateService.instant('personalSpace.projectDatasets.delete.error')}<a href="${this.linkError}">${this.translateService.instant('common.ici')}</a>`,
                    level: Level.ERROR
                });
            }
        });
    }

    /**
     * Méthode qui récupère l'uuid du new-dataset-request à supprimer , le supprime et recharge le tableau des new-dataset-requests
     */
    handleNewDatasetRequestUuidChanged(newDatasetRequestUuid: string): void {
        this.newLinkedDatasetLoading = true;
        this.projektMetierService.deleteNewDatasetRequest(this._project.uuid, newDatasetRequestUuid).pipe(
            // Reload dependencies
            switchMap(() => this.projectConsultService.getNewDatasetsRequest(this._project.uuid)),
            tap((values: NewDatasetRequest[]) => {
                this.newDatasetRequests = values;
                this.newLinkedDatasetLoading = false;
            })
        ).subscribe({
            error: err => {
                console.error(err);
                this.newLinkedDatasetLoading = false;
                this.snackBarService.openSnackBar({
                    message: `${this.translateService.instant('personalSpace.projectDatasets.delete.error')}<a href="${this.linkError}">${this.translateService.instant('common.ici')}</a>`,
                    level: Level.ERROR
                });
            }
        });
    }

    onClickCommentAction(element: RowTableData, isRestricted: boolean): void {
        this.loadingCommentData = true;
        if (isRestricted) {
            this.projektService
                .getDecisionInformationsForLinkedDataset(this._project.uuid, element.uuid)
                .pipe(
                    tap(() => {
                        this.loadingCommentData = false;
                    })
                )
                .subscribe((data: Form) => this.showCommentPopupOrSnackbar(data));
        } else {
            this.projektService
                .getDecisionInformationsForNewRequest(this._project.uuid, element.uuid)
                .pipe(
                    tap(() => {
                        this.loadingCommentData = false;
                    })
                )
                .subscribe((data: Form) => this.showCommentPopupOrSnackbar(data));
        }
    }

    private showCommentPopupOrSnackbar(form: Form): void {
        const commentExistPredicate = (field: Field) => field.definition.name === 'commentDate' && field.values.length;
        const formHasComment: boolean = !!(form?.sections
                .map((section: Section) => !!section.fields?.filter(commentExistPredicate).length)
                .some(Boolean)
        );

        if (formHasComment) {
            this.dialog.open(WorkflowFormDialogComponent, {
                data: {
                    title: this.translateService.instant('personalSpace.projectDatasets.show-comment.dialog-title'),
                    form
                }
            });
        } else {
            this.snackBarService.showInfo('personalSpace.projectDatasets.show-comment.error');
        }
    }
}
