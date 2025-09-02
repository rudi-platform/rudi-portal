import {Injectable} from '@angular/core';
import {AbstractControl, AbstractControlOptions, FormBuilder, FormGroup, ValidationErrors, Validators} from '@angular/forms';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {DefaultMatDialogConfig} from '@core/services/default-mat-dialog-config';
import {AccessStatusFiltersType} from '@core/services/filters/access-status-filters-type';
import {KonsultMetierService} from '@core/services/konsult-metier.service';
import {OrganizationMetierService} from '@core/services/organization/organization-metier.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {ObjectType} from '@core/services/tasks/object-type.enum';
import {LinkedDatasetTaskMetierService} from '@core/services/tasks/projekt/linked-dataset-task-metier.service';
import {NewDatasetRequestTaskMetierService} from '@core/services/tasks/projekt/new-dataset-request-task-metier.service';
import {ProjectTaskMetierService} from '@core/services/tasks/projekt/project-task-metier.service';
import {UserService} from '@core/services/user.service';
import {consistentPeriodValidator} from '@core/validators/consistent-period-validator';
import {
    SelectProjectDialogComponent,
    SelectProjectDialogData
} from '@features/data-set/components/select-project-dialog/select-project-dialog.component';
import {DialogClosedData} from '@features/data-set/models/dialog-closed-data';
import {LinkedDatasetFromProject} from '@features/data-set/models/linked-dataset-from-project';
import {AddDataSetDialogData} from '@features/project/components/add-data-set-dialog/add-data-set-dialog-data';
import {AddDataSetDialogComponent} from '@features/project/components/add-data-set-dialog/add-data-set-dialog.component';
import {
    EditNewDataSetDialogComponent,
    NewDataSetDialogData
} from '@features/project/components/edit-new-data-set-dialog/edit-new-data-set-dialog.component';
import {
    RequestDetailsDialogComponent,
    RequestDetailsDialogData
} from '@features/project/components/request-details-dialog/request-details-dialog.component';
import {
    SuccessProjectCreationDialogComponent
} from '@features/project/components/success-project-creation-dialog/success-project-creation-dialog.component';
import {DataRequestItem} from '@features/project/model/data-request-item';
import {ProjectDatasetItem} from '@features/project/model/project-dataset-item';
import {ProjectDatasetPictoType} from '@features/project/model/project-dataset-picto-type';
import {UpdateAction} from '@features/project/model/upate-action';
import {TranslateService} from '@ngx-translate/core';
import {RequestDetails} from '@shared/models/request-details';
import {TitleIconType} from '@shared/models/title-icon-type';
import {Level} from '@shared/notification-template/notification-template.component';
import {AccessConditionConfidentiality} from '@shared/utils/access-condition-confidentiality';
import {ActionFallbackUtils} from '@shared/utils/action-fallback-utils';
import {MetadataUtils} from '@shared/utils/metadata-utils';
import {RudiValidators} from '@shared/validators/rudi-validators';
import {User} from 'micro_service_modules/acl/acl-api';
import {Metadata} from 'micro_service_modules/api-kaccess';
import {DatasetConfidentiality, ReutilisationStatus} from 'micro_service_modules/projekt/projekt-api';
import {Task} from 'micro_service_modules/projekt/projekt-api/model/task';
import {
    Confidentiality,
    LinkedDataset,
    NewDatasetRequest,
    OwnerType,
    Project,
    ProjectStatus,
    ProjectType,
    Support,
    TargetAudience,
    TerritorialScale
} from 'micro_service_modules/projekt/projekt-model';
import {Organization} from 'micro_service_modules/strukture/strukture-model';
import {Moment} from 'moment';
import {forkJoin, Observable, of} from 'rxjs';
import {map, switchMap} from 'rxjs/operators';
import {ProjektMetierService} from './projekt-metier.service';

/**
 * Taille maximale de la description d'un projet ou d'une réutilisation
 */
const MAX_DESCRIPTION_LENGTH = 3000;

/**
 * Liste des éléments requis pour charger le formulaire de déclaration projet
 */
export interface FormProjectDependencies {
    confidentialities: Confidentiality[];
    projectPublicCible: TargetAudience[];
    territorialScales: TerritorialScale[];
    supports: Support[];
    projectTypes: ProjectType[];
    reuseStatus: ProjectType[];
    user: User;
    organizations: Organization[];
}

const RESTRICTED_DATASET_ICON: TitleIconType = 'key_icon_88_secondary-color';
const SELFDATA_DATASET_ICON: TitleIconType = 'self-data-icon';

@Injectable({
    providedIn: 'root'
})
export class ProjectSubmissionService {

    private static buildPartialLinkToCreate(metadata: Metadata): LinkedDatasetFromProject {
        return {
            project: undefined, requestDetail: undefined,
            datasetUuid: metadata.global_id
        };
    }

    /**
     * Récupère les nouvelles demandes de projets ajoutées à un projet à enregistrer côté back
     * @param dataRequests l'ensemble des demandes de projets d'un projet
     * @private
     */
    private static getNewDatasetRequestsAdded(dataRequests: DataRequestItem[]): DataRequestItem[] {
        return dataRequests.filter((dataRequest: DataRequestItem) => dataRequest.uuid == null);
    }

    /**
     * Récupère les nouvelles demandes modifiées, mets à jour les objets à envoyer au back dans la liste de retour
     * @param dataRequests la liste des items front qui ont les modifs
     * @param fromBack la liste des éléments back attachés au projet
     * @private
     */
    private static getNewDatasetRequestsEdited(dataRequests: DataRequestItem[], fromBack: NewDatasetRequest[]): NewDatasetRequest[] {
        const modified: NewDatasetRequest[] = [];
        const withUuids: DataRequestItem[] = dataRequests.filter((element: DataRequestItem) => element.uuid != null);
        withUuids.forEach((withUuid: DataRequestItem) => {
            const backItem: NewDatasetRequest = fromBack.find((backElement: NewDatasetRequest) => backElement.uuid === withUuid.uuid);
            if (backItem.title !== withUuid.title || backItem.description !== withUuid.description) {
                backItem.title = withUuid.title;
                backItem.description = withUuid.description;
                modified.push(backItem);
            }
        });

        return modified;
    }

    /**
     * Récupère les nouvelles demandes de projets supprimées d'un projet à supprimer côté back
     * @param dataRequests l'ensemble des demandes de projets d'un projet (côte front)
     * @param newDatasetRequests l'ensemble des demandes de données d'un projet (côté back)
     * @private
     */
    private static getNewDatasetRequestsDeleted(dataRequests: DataRequestItem[], newDatasetRequests: NewDatasetRequest[])
        : NewDatasetRequest[] {
        // Ceux qui existent côté back mais pas côté front doivent être supprimés du back
        const uuidsFront: string[] = dataRequests.map((dataRequest: DataRequestItem) => dataRequest.uuid);
        return newDatasetRequests.filter((newDatasetRequest: NewDatasetRequest) => {
            return !uuidsFront.includes(newDatasetRequest.uuid);
        });
    }

    /**
     * Récupère les JDDs ajoutés à un projet à enregistrer côté back sous forme de demandes
     * @param metadatasLinked l'ensemble des JDDs liés d'un projet saisi dans l'interface
     * @param linkedDatasets l'ensemble des demandes d'accès a des JDDs venant du back-end
     * @private
     */
    private static getMetadatasAdded(metadatasLinked: Metadata[], linkedDatasets: LinkedDataset[]): Metadata[] {
        const datasetUuidsFromback: string[] = linkedDatasets.map((item: LinkedDataset) => item.dataset_uuid);
        return metadatasLinked.filter((metadataLinked: Metadata) => !datasetUuidsFromback.includes(metadataLinked.global_id));
    }

    /**
     * Récupère les demandes d'accès aux JDDs liés du back qui doivent être supprimés
     * @param metadatasLinked l'ensemble des JDDs liés d'un projet saisi dans l'interface
     * @param linkedDatasets l'ensemble des demandes d'accès a des JDDs venant du back-end
     * @private
     */
    private static getLinkedDatasetDeleted(metadatasLinked: Metadata[], linkedDatasets: LinkedDataset[]): LinkedDataset[] {
        const datasetsUuidsFront: string[] = metadatasLinked.map((metadata: Metadata) => metadata.global_id);
        return linkedDatasets.filter((linkedDataset: LinkedDataset) => {
            return !datasetsUuidsFront.includes(linkedDataset.dataset_uuid);
        });
    }

    /**
     * Récupère les demandes d'accès aux JDDs qui ont été modifies, le tableau retourné représente l'état modifié à persister côté back
     * @param mapUuidDatasetRequest la map : uuid de JDD -> détail de la demande (front)
     * @param fromBack les link venant du back
     * @private
     */
    private static getLinkedDatasetEdited(mapUuidDatasetRequest: Map<string, RequestDetails>, fromBack: LinkedDataset[]): LinkedDataset[] {
        const modified: LinkedDataset[] = [];
        const mapUuidDatasetLink: Map<string, LinkedDataset> = new Map();
        fromBack.forEach((link: LinkedDataset) => mapUuidDatasetLink.set(link.dataset_uuid, link));

        mapUuidDatasetRequest.forEach((requestDetail: RequestDetails, datasetUuid: string) => {
            const link: LinkedDataset = mapUuidDatasetLink.get(datasetUuid);
            if (link && requestDetail && link.comment !== requestDetail.comment) {
                link.comment = requestDetail.comment;
                modified.push(link);
            }
        });

        return modified;
    }

    constructor(private readonly projektMetierService: ProjektMetierService,
                private readonly konsultMetierService: KonsultMetierService,
                private readonly translateService: TranslateService,
                private readonly snackBarService: SnackBarService,
                private readonly userService: UserService,
                private readonly projectTaskMetierService: ProjectTaskMetierService,
                private readonly linkedDatasetTaskMetierService: LinkedDatasetTaskMetierService,
                private readonly formBuilder: FormBuilder,
                private readonly dialog: MatDialog,
                private readonly organizationMetierService: OrganizationMetierService,
                private readonly newDatasetRequestTaskMetierService: NewDatasetRequestTaskMetierService,
    ) {
    }

    /**
     * Initialisation des champs du formulaire de l'étape 1 : projet
     */
    public initStep1ProjectFormGroup(): FormGroup {
        return this.formBuilder.group({
            title: ['', Validators.required],
            description: ['', [Validators.required, Validators.maxLength(MAX_DESCRIPTION_LENGTH)]],
            image: [''],
            begin_date: [null],
            end_date: [null],
            publicCible: [''],
            echelle: [null],
            territoire: [null],
            accompagnement: [null],
            type: ['', Validators.required],
            url: ['', Validators.pattern(/^(http|https|ftp):\/\/.*$/)],
            reuse_status: ['', Validators.required],
            confidentiality: ['', Validators.required],
        }, {
            // Contrôle cross champs sur la période
            validators: [consistentPeriodValidator({beginControlName: 'begin_date', endControlName: 'end_date'})],
            updateOn: 'blur',
            reuseStatus: ['', Validators.required]
        } as AbstractControlOptions);
    }

    /**
     * Conversion de l'état du formulaire saisi en un objet métier Project
     * @param step1FormGroup le form de l'étape 1
     * @param step2FormGroup le form de l'étape 2
     * @param user l'utilisateur connecté
     * @param projectType le type de projet créé
     * @param confidentiality le niveau de confidentialité choisi
     * @param reuseStatus statut de la réutilisation (réutilisation en cours ou terminé)
     */
    public projectFormGroupToProject(step1FormGroup: FormGroup,
                                     user: User,
                                     projectType: ProjectType,
                                     confidentiality: Confidentiality,
                                     reuseStatus: ReutilisationStatus,
                                     step2FormGroup?: FormGroup): Project {
        const ownerType = step2FormGroup.get('ownerType').value as OwnerType;
        return {
            title: step1FormGroup.get('title').value,
            expected_completion_start_date: step1FormGroup.get('begin_date').value,
            expected_completion_end_date: step1FormGroup.get('end_date').value,
            description: step1FormGroup.get('description').value,
            territorial_scale: step1FormGroup.get('echelle').value,
            detailed_territorial_scale: step1FormGroup.get('territoire').value,
            confidentiality,
            desired_supports: step1FormGroup.get('accompagnement').value,
            type: projectType,
            access_url: step1FormGroup.get('url').value === '' ? null : step1FormGroup.get('url').value,
            owner_uuid: ownerType === OwnerType.Organization ? step2FormGroup.get('organizationUuid').value : user.uuid,
            contact_email: step2FormGroup.get('contactEmail').value,
            owner_type: ownerType,
            object_type: ObjectType.PROJECT,
            target_audiences: step1FormGroup.get('publicCible').value === '' ? null : step1FormGroup.get('publicCible').value,
            reutilisation_status: reuseStatus
        };
    }

    /**
     * Mets à jour les champs du premier projet avec les valeurs du second (valeurs uniquement modifiables)
     * @param toUpdate le projet modifié
     * @param updated le projet qui contient les valeurs àjour
     */
    public updateProjectField(toUpdate: Project, updated: Project): void {
        toUpdate.title = updated.title;
        toUpdate.expected_completion_start_date = updated.expected_completion_start_date;
        toUpdate.expected_completion_end_date = updated.expected_completion_end_date;
        toUpdate.description = updated.description;
        toUpdate.territorial_scale = updated.territorial_scale;
        toUpdate.detailed_territorial_scale = updated.detailed_territorial_scale;
        toUpdate.confidentiality = updated.confidentiality;
        toUpdate.desired_supports = updated.desired_supports;
        toUpdate.target_audiences = updated.target_audiences;
        toUpdate.type = updated.type;
        toUpdate.contact_email = updated.contact_email;
    }

    /**
     * Initialisation des champs du formulaire de l'étape 2 pour l'instant identiques
     * pour retutilisation et projet
     */
    public initStep2FormGroup(): FormGroup {
        return this.formBuilder.group({
            ownerType: ['', Validators.required],
            lastname: [
                {
                    value: '',
                    disabled: true
                },
                Validators.required
            ],
            firstname: [
                {
                    value: '',
                    disabled: true
                },
                Validators.required
            ],
            contactEmail: ['', [RudiValidators.email]],
            organizationUuid: [null]
        }, {
            validators: [this.organizationUuidValidator]
        });
    }

    /**
     * Valide le champ organizationUuid en fonction du champ ownerType
     */
    protected organizationUuidValidator(step2FormGroup: FormGroup): ValidationErrors | null {
        const ownerType = step2FormGroup.get('ownerType').value as OwnerType;
        const organizationUuidFormControl: AbstractControl = step2FormGroup.get('organizationUuid');
        const organizationUuid = organizationUuidFormControl.value;
        if (ownerType === OwnerType.Organization && !organizationUuid) {
            const errors: ValidationErrors | null = {organizationUuidValidator: true};
            organizationUuidFormControl.setErrors(errors);
            return errors;
        }
        organizationUuidFormControl.setErrors(null);
        return null;
    }

    /**
     * Chargement des éléments requis pour afficher le formulaire de projet
     */
    public loadDependenciesProject(): Observable<FormProjectDependencies> {
        const connectedUser$: Observable<User | undefined> = this.userService.getConnectedUser();
        const dependencies = {
            confidentialities: this.projektMetierService.searchProjectConfidentialities({active: true}),
            territorialScales: this.projektMetierService.searchTerritorialScales(true),
            supports: this.projektMetierService.searchSupports(true),
            projectPublicCible: this.projektMetierService.searchProjectPublicCible(true),
            projectTypes: this.projektMetierService.searchProjectTypes(true),
            user: connectedUser$,
            organizations: connectedUser$.pipe(
                switchMap(connectedUser => this.organizationMetierService.getMyOrganizations(connectedUser.uuid))
            ),
            reuseStatus: this.projektMetierService.searchReuseStatus(true)
        };

        return forkJoin(dependencies);
    }

    /**
     * Obtention d'un objet vue : ProejctDatasetItem à partir d'un JDD
     * @param metadata le JDD pour créer une vue
     */
    public metadataToProjectDatasetItem(metadata: Metadata): ProjectDatasetItem {
        let iconTitle: TitleIconType;
        const accessConditionConfidentiality = MetadataUtils.getAccessConditionConfidentiality(metadata);
        if (accessConditionConfidentiality === AccessConditionConfidentiality.Restricted) {
            iconTitle = RESTRICTED_DATASET_ICON;
        }
        if (accessConditionConfidentiality === AccessConditionConfidentiality.Selfdata) {
            iconTitle = SELFDATA_DATASET_ICON;
        }
        return {
            title: metadata.resource_title,
            overTitle: metadata.producer.organization_name,
            pictoType: ProjectDatasetPictoType.LOGO,
            pictoValue: metadata.producer.organization_id,
            identifier: metadata.local_id,
            editable: metadata.access_condition?.confidentiality?.restricted_access,
            titleIcon: iconTitle
        };
    }

    /**
     * Obtention d'un objet vue : ProejctDatasetItem à partir d'une vue demande de données
     * @param dataRequest la demande de données (front pas back)
     */
    public dataRequestToProjectDatasetItem(dataRequest: DataRequestItem): ProjectDatasetItem {
        return {
            title: dataRequest.title,
            overTitle: null,
            pictoType: ProjectDatasetPictoType.STATIC,
            pictoValue: 'rudi_picto_nouvelle_demande',
            identifier: null,
            editable: true
        };
    }

    /**
     * Ouvre une popin de sélection d'un JDD
     */
    public openDialogMetadata(restrictedAccessFilterValue: AccessStatusFiltersType, restrictedAccessHiddenValues?: AccessStatusFiltersType[]): Observable<Metadata> {
        const dialogConfig = new DefaultMatDialogConfig<AddDataSetDialogData>();
        dialogConfig.width = '';
        dialogConfig.data = {
            accessStatusForcedValue: restrictedAccessFilterValue,
            accessStatusHiddenValues: restrictedAccessHiddenValues,
        };

        const dialogRef = this.dialog.open(AddDataSetDialogComponent, dialogConfig);
        return dialogRef.afterClosed();
    }

    /**
     * Ouvre la popin de selection de projet
     * @param metadata le JDD qu'on souhaite lier au projet qui sera choisi
     */
    public selectProjectsDialog(metadata: Metadata): Observable<DialogClosedData<Project>> {
        const dialogConfig = new DefaultMatDialogConfig<SelectProjectDialogData>();
        dialogConfig.data = {
            data: {
                metadata
            }
        };

        const dialogRef = this.dialog.open(SelectProjectDialogComponent, dialogConfig);
        return dialogRef.afterClosed();
    }

    /**
     * Ouverture de la popin de saisie des détails d'une demande d'accès à un JDD
     * @param endDate la date de fin de la demande par défaut
     */
    public openDialogRequestDetails(endDate: Moment): Observable<DialogClosedData<RequestDetails>> {
        const dialogConfig = new DefaultMatDialogConfig<RequestDetailsDialogData>();
        dialogConfig.data = {
            data: {
                endDate,
                requestDetails: null,
            }
        };

        const dialogRef = this.dialog.open(RequestDetailsDialogComponent, dialogConfig);
        return dialogRef.afterClosed();
    }

    /**
     * Ouverture de la popin de saisie des details d'une demande d'accès à un JDD en mode édition
     * @param original les détails originaux qu'on vient modifier
     */
    public openDialogEditRequest(original: RequestDetails): Observable<DialogClosedData<RequestDetails>> {
        const dialogConfig = new DefaultMatDialogConfig<RequestDetailsDialogData>();
        dialogConfig.data = {
            data: {
                endDate: null,
                requestDetails: original
            }
        };

        const dialogRef = this.dialog.open(RequestDetailsDialogComponent, dialogConfig);
        return dialogRef.afterClosed();
    }

    /**
     * Ouvre une popin d'info que l'enregistrement PROJET à bien marché
     */
    public openDialogSuccess(): void {
        const dialogConfig = new MatDialogConfig();

        dialogConfig.autoFocus = false;
        dialogConfig.panelClass = 'my-custom-dialog-class';
        dialogConfig.data = {};
        dialogConfig.width = '768px';

        this.dialog.open(SuccessProjectCreationDialogComponent, dialogConfig);
    }

    /**
     * Ouvre une popin de saisie d'une nouvelle demande de projet
     * @param numberOfRequests le nombre de requêtes présentes dans le contexte actuel
     */
    public openDialogAskNewDatasetRequest(numberOfRequests: number): Observable<DataRequestItem> {
        const dialogConfig = new MatDialogConfig<NewDataSetDialogData>();

        dialogConfig.disableClose = true;
        dialogConfig.autoFocus = false;
        dialogConfig.width = '768px';
        dialogConfig.data = {
            data: {
                dataRequestItem: null,
                counter: numberOfRequests + 1 // On veut "ajouter" cette requête si elle a pas de titre elle est n° au dessus
            }
        };

        const dialogRef = this.dialog.open(EditNewDataSetDialogComponent, dialogConfig);
        return dialogRef.afterClosed();
    }

    /**
     * Ouvre une popin d'édition de nouvelle demande de projet
     * @param dataRequestItem la demande éditée
     * @param demandNumber le n° de la demande si on lui retire son titre pour lui donner son titre par défaut (jamais 0)
     */
    public openDialogEditNewDatasetRequest(dataRequestItem: DataRequestItem, demandNumber: number): Observable<DataRequestItem> {
        const dialogConfig = new DefaultMatDialogConfig();

        dialogConfig.data = {
            data: {
                dataRequestItem,
                counter: demandNumber // On veut "editer" cette requête si elle a pas de titre elle est n° courant
            }
        };

        const dialogRef = this.dialog.open(EditNewDataSetDialogComponent, dialogConfig);
        return dialogRef.afterClosed();
    }

    /**
     * Regarde si un JDD est présent dans une liste grâce à son UUID
     * @param metadataUuid l'UUID du JDD cherché
     * @param linkedDatasets les JDDs parcourus
     */
    public isMetadataPresent(metadataUuid: string, linkedDatasets: Metadata[]): boolean {
        const existingLinkedDataset: Metadata = linkedDatasets.find(linkedDataset => linkedDataset.global_id === metadataUuid);
        if (existingLinkedDataset) {
            this.snackBarService.openSnackBar({
                message: this.translateService.instant('project.stepper.submission.step3.linkedDatasets.alreadyAdded'),
                level: Level.ERROR
            });
            return true;
        }

        return false;
    }

    /**
     * Action de sauvegarde d'une réutilisation/projet côté back
     * @param project l'objet Projet à persister en entity
     * @param linkedDatasets les JDDs liés
     * @param dataRequests les éventuelles demandes de nouveaux JDD (uniquement pour un projet)
     * @param image l'image du projet
     * @param mapRequestDetailsByDatasetUuid associations JDD -> détails de la demande, si nécessaire
     */
    public createProject(project: Project, linkedDatasets: Metadata[], dataRequests: DataRequestItem[], image: Blob,
                         mapRequestDetailsByDatasetUuid: Map<string, RequestDetails>): Observable<Project> {

        // 1) Créer le projet côté back
        return this.projektMetierService.createProject(project).pipe(
            // 2) S'il y a des demandes de JDD les créer et les lier au projet
            switchMap((createdProject: Project) => {
                return this.manageRequestsToProjects(createdProject, dataRequests).pipe(
                    map(() => createdProject)
                );
            }),

            // 3) S'il y a des JDDs liés, on les gère (ajout et suppression)
            switchMap(createdProject => {
                return this.manageLinkedDatasetsToProjects(createdProject, linkedDatasets, mapRequestDetailsByDatasetUuid).pipe(
                    map(() => createdProject)
                );
            }),

            // 4) S'il y a une image, on l'ajoute au projet
            switchMap(createdProject => {
                if (image != null) {
                    return this.uploadImage(createdProject, image).pipe(
                        map(() => createdProject)
                    );
                } else {
                    return of(createdProject);
                }
            })
        );
    }

    /**
     * Action de mise à jour d'une réutilisation/projet côté back
     * @param project l'objet Projet à persister en entity
     * @param linkedDatasets les JDDs liés
     * @param dataRequests les éventuelles demandes de nouveaux JDD (uniquement pour un projet)
     * @param mapRequestDetailsByDatasetUuid la map d'association : JDD UUID -> détails d'une demande
     * @param image l'image du projet
     * @param imageAction quelle action de MAJ on applique à l'image projet
     */
    public updateProject(project: Project, linkedDatasets: Metadata[], dataRequests: DataRequestItem[],
                         mapRequestDetailsByDatasetUuid: Map<string, RequestDetails>,
                         image: Blob, imageAction: UpdateAction): Observable<Project> {
        return this.projektMetierService.updateProject(project).pipe(
            switchMap(() => {
                return this.manageRequestsToProjects(project, dataRequests).pipe(
                    map(() => project)
                );
            }),
            switchMap(updatedProject => {
                return this.manageLinkedDatasetsToProjects(updatedProject, linkedDatasets, mapRequestDetailsByDatasetUuid).pipe(
                    map(() => updatedProject)
                );
            }),
            switchMap(updatedProject => {
                if (imageAction === UpdateAction.AJOUT) {
                    return this.uploadImage(updatedProject, image).pipe(
                        map(() => updatedProject)
                    );
                } else if (imageAction === UpdateAction.SUPPRESSION) {
                    return this.projektMetierService.removeLogo(updatedProject.uuid).pipe(
                        map(() => updatedProject)
                    );
                } else if (imageAction === UpdateAction.MISE_A_JOUR) {
                    return this.projektMetierService.removeLogo(updatedProject.uuid).pipe(
                        switchMap(() => this.uploadImage(updatedProject, image)),
                        map(() => updatedProject)
                    );
                } else {
                    return of(updatedProject);
                }
            })
        );
    }

    /**
     * Créé et démarre le workflow pour les demandes d'accès aux JDDs
     * @param links l'ensemble des demandes d'accès à traiter
     */
    private createAndStartTaskForLinkedDatasets(links: LinkedDataset[], createdProject: Project): Observable<Task[]> {
        const link$: Observable<Task>[] = links.map((link) => {
            if (link.dataset_confidentiality === DatasetConfidentiality.Restricted) {
                return this.linkedDatasetTaskMetierService.createDraft(link).pipe(
                    switchMap((task: Task) => {
                        if (createdProject.project_status === ProjectStatus.Validated) {
                            // Only start the task if the project is "Validated"
                            return this.linkedDatasetTaskMetierService.startTask(task);
                        } else {
                            // Return the task without starting if the project isn't "Validated"
                            return of(task);
                        }
                    })
                );
            }
            return of(null);
        });

        return forkJoin(link$);
    }

    /**
     * Soumets un objet "Project" réutilisation ou projet pour démarrer son workflow
     * @param project l'asset qui doit démarrer le workflow
     */
    public submitProject(project: Project): Observable<Task> {
        // 1) On crée le draft à partir du projet
        return this.projectTaskMetierService.createDraft(project).pipe(
            // 2 et maintenant on doit démarrer le workflow de cet élément
            switchMap((task: Task) => {
                return this.projectTaskMetierService.startTask(task);
            })
        );
    }

    /**
     * Recherche d'un type de projet par son code
     * @param codeProject le code
     * @param projectTypes les types
     */
    public searchProjectType(codeProject: string, projectTypes: ProjectType[]): ProjectType {
        return projectTypes.find(projectType => projectType.code === codeProject);
    }

    /**
     * Recherche d'un niveau de confidentialité par code
     * @param codeConfidentiality le code
     * @param confidentialities les niveaux
     */
    public searchConfidentiality(codeConfidentiality: string, confidentialities: Confidentiality[]): Confidentiality {
        return confidentialities.find(confidentiality => confidentiality.code === codeConfidentiality);
    }

    /**
     * Recherche d'un statut de réutilisation par code
     * @param codeStatus le code
     * @param statuses les statuts
     */
    public findCorrespondingReutilisationStatus(codeStatus: string, statuses: ReutilisationStatus[]): ReutilisationStatus {
        return statuses.find(status => status.code === codeStatus);
    }

    /**
     * Appel de l'API d'upload d'image
     * @param createdProject le projet possédant l'image
     * @param image l'imag en binaire
     * @private
     */
    private uploadImage(createdProject: Project, image: Blob): Observable<void> {
        return this.projektMetierService.uploadLogo(createdProject.uuid, image);
    }

    /**
     * Création d'une demande d'accès (JDD lié) à partir d'un projet
     * @param linkToCreate l'objet à créer côté back
     */
    public createLinkedDatasetFromProject(linkToCreate: LinkedDatasetFromProject): Observable<void> {
        const createdProject: Project = linkToCreate.project;
        const mapDataset: Map<string, RequestDetails> = new Map();
        mapDataset.set(linkToCreate.datasetUuid, linkToCreate.requestDetail);

        // Lier les demandes (la seule en l'occurence) au projet
        return this.projektMetierService.linkProjectToDatasets(createdProject.uuid, [linkToCreate.datasetUuid], mapDataset).pipe(
            switchMap((linksCreated: LinkedDataset[]) => {
                if (createdProject.status == 'COMPLETED') {
                    // le workflow n'est démarré que si le workflow de reuse est COMPLETED (terminé et validé)
                    // dans les autres cas, c'est la suite du workflow de reuse qui doit démarrer les sous-workflow si nécessaire (projectWorkflowContext.startSubProcess(execution); )
                    return this.createAndStartLinkedDatasetsAndFallback(linksCreated, createdProject);
                } else {
                    return of([]);
                }
            }),

            // Observable mappé sur void
            map(() => null)
        );
    }

    /**
     * Gestion des nouvelles demandes de JDD pour un projet (ajout/suppression)
     * @param createdProject le projet porteur
     * @param dataRequests l'ensemble des demandes côté front
     * @private
     */
    public manageRequestsToProjects(createdProject: Project, dataRequests: DataRequestItem[]): Observable<boolean> {

        // On récupère d'abord les demandes du projet avec le back pour comparer
        return this.projektMetierService.getNewDatasetRequests(createdProject.uuid).pipe(
            switchMap((newDatasetRequests: NewDatasetRequest[]) => {

                // Récupération des items créés et supprimés du projets par comparaison
                const added: DataRequestItem[] = ProjectSubmissionService.getNewDatasetRequestsAdded(dataRequests);
                const deleted: NewDatasetRequest[] = ProjectSubmissionService
                    .getNewDatasetRequestsDeleted(dataRequests, newDatasetRequests);
                const edited: NewDatasetRequest[] = ProjectSubmissionService.getNewDatasetRequestsEdited(dataRequests, newDatasetRequests);

                // Si on a rien à ajouter ou suppr on fait rien
                if (added.length === 0 && deleted.length === 0 && edited.length === 0) {
                    return of(true);
                }

                // Ajout des observables et lancement du traitement parallèle d'ajout/suppression
                return forkJoin({
                    add: this.projektMetierService.addNewDatasetRequests(createdProject, added),
                    delete: this.projektMetierService.deleteDatasetRequests(createdProject, deleted),
                    edit: this.projektMetierService.upddateDatasetRequests(createdProject, edited)
                }).pipe(map(() => true));
            })
        );
    }

    /**
     * Gestion des demandes d'accès aux JDDs pour un projet (ajout/suppression)
     * @param createdProject le projet porteur
     * @param metadatasLinked l'ensemble des JDDs présents côté front
     * @param mapRequestDetailsByDatasetUuid associations JDD → détails de la demande, si nécessaire
     * @private
     */
    public manageLinkedDatasetsToProjects(createdProject: Project, metadatasLinked: Metadata[],
                                          mapRequestDetailsByDatasetUuid: Map<string, RequestDetails>): Observable<boolean> {

        // On récupère les DEMANDES côté back
        return this.projektMetierService.getLinkedDatasets(createdProject.uuid).pipe(
            switchMap((linkedDatasets: LinkedDataset[]) => {

                // Pour l'ajout on doit se débrouiller avec les METADATA côté front pour déterminer les DEMANDES à créer
                const added: string[] = ProjectSubmissionService.getMetadatasAdded(metadatasLinked, linkedDatasets)
                    .map((element: Metadata) => element.global_id);

                // Pour la Suppression on peut directement filtrer les DEMANDES à l'aides des information de METADATA front
                const deleted: string[] = ProjectSubmissionService.getLinkedDatasetDeleted(metadatasLinked, linkedDatasets)
                    .map((element: LinkedDataset) => element.uuid);

                // Pour l'édition on doit manipuler la map d'association : uuid jdd -> détail demande, pour comparer aux DEMANDES côté back
                const modified: LinkedDataset[] = ProjectSubmissionService
                    .getLinkedDatasetEdited(mapRequestDetailsByDatasetUuid, linkedDatasets);

                if (added.length === 0 && deleted.length === 0 && modified.length === 0) {
                    return of(true);
                }

                return forkJoin({
                    add: this.projektMetierService.linkProjectToDatasets(createdProject.uuid, added, mapRequestDetailsByDatasetUuid),
                    delete: this.projektMetierService.unlinkDatasetsToProject(createdProject.uuid, deleted),
                    edit: this.projektMetierService.updateLinkedDatasets(createdProject, modified)
                }).pipe(map(() => true));
            })
        );
    }

    /**
     * Crée/démarre la tâche liée à un JDD lié avec gestion de suppression si erreur de workflow
     * @param linksCreated les JDDs liés créés
     * @param createdProject le proejt les contenant
     */
    createAndStartLinkedDatasetsAndFallback(linksCreated: LinkedDataset[], createdProject: Project): Observable<Task[]> {
        const linksAction = new ActionFallbackUtils<Task[]>({
            action: this.createAndStartTaskForLinkedDatasets(linksCreated, createdProject),
            fallback: this.projektMetierService.unlinkDatasetsToProject(createdProject.uuid, linksCreated.map(link => link.uuid)),
            fallbackSuccessMessage: 'Une erreur a eu lieu lors du démarrage du workflow de la demande d\'accès',
            fallbackErrorMessage: 'Impossible de supprimer la demande d\'accès alors que son workflow a échoué, ' +
                'une incohérence a été créée'
        });
        return linksAction.doActionFallbackOnfailure();
    }

    /**
     * @param selectedMetadata$ Observable renvoyé par la dialog de selection
     * @param existingLinks liens déjà existants
     * @return Observable de futureLinkDataset Objet de lien correspondant au JDD selectionné
     */
    checkLinkExistsOrCreateLinkObject(selectedMetadata$: Observable<Metadata>, existingLinks: Metadata[]): Observable<LinkedDatasetFromProject> {
        return selectedMetadata$.pipe(
            map((value: Metadata) => {
                if (this.isMetadataPresent(value.global_id, existingLinks)) {
                    throw new Error('Dataset already added');
                }
                return value;
            }),
            switchMap((value: Metadata) => {
                const futureLink = ProjectSubmissionService.buildPartialLinkToCreate(value);
                const confidentiality = MetadataUtils.getAccessConditionConfidentiality(value);
                if (confidentiality === AccessConditionConfidentiality.Selfdata) {
                    throw new Error('Cannot add selfdata to project here');
                }
                if (confidentiality === AccessConditionConfidentiality.Opened) {
                    return of(futureLink);
                }
                if (confidentiality === AccessConditionConfidentiality.Restricted) {
                    return this.openDialogRequestDetails(null).pipe(
                        map((requestDetail: DialogClosedData<RequestDetails>) => {
                            futureLink.requestDetail = requestDetail.data;
                            return futureLink;
                        })
                    );
                }
                throw new Error('Confidentiality not handled');
            })
        );
    }

    /**
     * Ajoute une nouvelle demande à un projet et demarre le workflow de cette demande
     * @param projectUuid uuid du projet
     * @param requestToAdd demande à ajouter
     */
    addNewDatasetRequest(projectUuid: string, requestToAdd: NewDatasetRequest, createdProject: Project): Observable<Task> {
        return this.projektMetierService.addNewDatasetRequest(projectUuid, requestToAdd).pipe(
            switchMap((requestAdded: NewDatasetRequest) => {
                if (createdProject.status === 'COMPLETED') {
                    // le workflow n'est démarré que si le workflow de reuse est COMPLETED (terminé et validé)
                    // dans les autres cas, c'est la suite du workflow de reuse qui doit démarrer les sous-workflow si nécessaire (projectWorkflowContext.startSubProcess(execution); )
                    const manageNewDatasetRequestWorkflowAction = new ActionFallbackUtils<Task>({
                        action: this.manageNewDatasetRequestWorkflow(requestAdded, createdProject),
                        fallback: this.projektMetierService.deleteNewDatasetRequest(projectUuid, requestAdded.uuid),
                        fallbackSuccessMessage: 'Une erreur a eu lieu lors du démarrage du workflow de la demande de nouveaux JDDs',
                        fallbackErrorMessage: 'Erreur lors de la suppression de la demande de nouveaux JDDs après avoir eu une erreur dans le workflow, ' +
                            'une incohérence a été créée'
                    });
                    return manageNewDatasetRequestWorkflowAction.doActionFallbackOnfailure();
                } else {
                    // Handle the case where the project is not completed
                    return of(null);
                }
            })
        );
    }

    /**
     * Lance le workflow sur cette demande
     * @param requestAdded asset de la demande créée auparavant côté back
     * @private
     */
    private manageNewDatasetRequestWorkflow(requestAdded: NewDatasetRequest, createdProject: Project): Observable<Task> {
        return this.newDatasetRequestTaskMetierService.createDraft(requestAdded).pipe(
            switchMap((associatedTask: Task) => {
                if (createdProject.project_status === ProjectStatus.Validated) {
                    // Only start the task if the project is "Validated"
                    return this.newDatasetRequestTaskMetierService.startTask(associatedTask);
                } else {
                    // Return the task without starting if the project isn't "Validated"
                    return of(associatedTask);
                }
            })
        );
    }

    /**
     * Mets à jour les champs du task avec les valeurs du premier formulaire
     * @param toUpdate le task à modifier
     * @param form le formulaire qui contient les valeurs à jour
     */
    // tslint:disable-next-line:no-any
    public updateProjectTaskField(toUpdate: any, form: FormGroup, confidentialities: Confidentiality[]): void {
        toUpdate.asset.title = form.value.title;
        toUpdate.asset.description = form.value.description;
        toUpdate.asset.expected_completion_start_date = form.value.begin_date;
        toUpdate.asset.expected_completion_end_date = form.value.end_date;
        toUpdate.asset.target_audiences = form.value.publicCible;
        toUpdate.asset.territorial_scale = form.value.echelle;
        toUpdate.asset.detailed_territorial_scale = form.value.territoire;
        toUpdate.asset.desired_supports = form.value.accompagnement;
        toUpdate.asset.access_url = form.value.url;
        toUpdate.asset.reutilisation_status = form.value.reuse_status;
        toUpdate.asset.confidentiality = this.searchConfidentiality(
            form.value.confidentiality.code, confidentialities
        );
        toUpdate.asset.type = form.value.type;
    }
}

