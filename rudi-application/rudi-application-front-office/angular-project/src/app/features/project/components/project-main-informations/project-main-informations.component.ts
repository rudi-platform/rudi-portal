import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormControl, FormGroup} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {FormProjectDependencies, ProjectSubmissionService} from '@core/services/asset/project/project-submission.service';
import {ProjektMetierService} from '@core/services/asset/project/projekt-metier.service';
import {DialogSubscribeDatasetsService} from '@core/services/dialog-subscribe-datasets.service';
import {FiltersService} from '@core/services/filters.service';
import {CloseEvent} from '@features/data-set/models/dialog-closed-data';
import {UpdateAction} from '@features/project/model/upate-action';
import {RadioListItem} from '@shared/radio-list/radio-list-item';
import {User} from 'micro_service_modules/acl/acl-api';
import {Project, ProjektService, ReutilisationStatus} from 'micro_service_modules/projekt/projekt-api';
import {
    Confidentiality,
    ProjectStatus,
    ProjectType,
    Support,
    TargetAudience,
    TerritorialScale
} from 'micro_service_modules/projekt/projekt-model';
import * as moment from 'moment';
import {switchMap} from 'rxjs/operators';

@Component({
    selector: 'app-project-main-informations',
    templateUrl: './project-main-informations.component.html',
    styleUrls: ['./project-main-informations.component.scss']
})
export class ProjectMainInformationsComponent implements OnInit {
    @Input() project: Project;
    @Input() isProjectUpdatable = false;
    @Input() isUpdating = false;
    @Input() dialogDescription: string;
    @Input() isLoading: boolean;
    @Input() showTitle: boolean = true;

    @Output() updateForm = new EventEmitter<{ confidentialities: Confidentiality[], form: FormGroup, messageToModerator?: string }>();
    @Output() updateInProgress = new EventEmitter<boolean>();
    public step1FormGroup: FormGroup;
    public messageToModeratorFormGroup: FormGroup;
    public projectType: ProjectType[];
    public suggestions: RadioListItem[];
    public publicCible: TargetAudience[];
    public reuseStatus: ReutilisationStatus[];
    public territorialScales: TerritorialScale[];
    private confidentialities: Confidentiality[];
    public supports: Support[];
    public isRefusedProject: boolean;
    public user: User;
    public taskId = '';

    /**
     * L'action de mise à jour d'image à apppliquer pour un projet
     * @private
     */
    private updateImageAction: UpdateAction;

    /**
     * L'image du projet qui a été sauvegardée, pour savoir si une mise à jour a eu lieu
     * @private
     */
    private projectImageSaved: Blob;

    constructor(
        readonly projektMetierService: ProjektMetierService,
        readonly filtersService: FiltersService,
        readonly projectSubmissionService: ProjectSubmissionService,
        public dialog: MatDialog,
        readonly projektService: ProjektService,
        private readonly personalSpaceProjectService: DialogSubscribeDatasetsService,
        public formBuilder: FormBuilder
    ) {
    }

    ngOnInit(): void {
        this.isRefusedProject = this.project?.project_status === ProjectStatus.Rejected;
        this.messageToModeratorFormGroup = this.formBuilder.group({
            messageToModerator: new FormControl('')
        });
    }

    // Chargement des infos de la réutilisation
    loadProjectInformations(): void {
        this.suggestions = [];
        this.step1FormGroup = this.projectSubmissionService.initStep1ProjectFormGroup();
        this.isLoading = true;
        // start
        this.projectSubmissionService.loadDependenciesProject().pipe(
            switchMap((dependencies: FormProjectDependencies) => {
                this.confidentialities = dependencies.confidentialities;
                this.publicCible = dependencies.projectPublicCible;
                this.territorialScales = dependencies.territorialScales;
                this.projectType = dependencies.projectTypes;
                this.supports = dependencies.supports;
                this.user = dependencies.user;
                this.reuseStatus = dependencies.reuseStatus;

                this.isLoading = false;
                this.suggestions = [];


                return this.projektMetierService.searchProjectConfidentialities({active: true});
            })
        ).subscribe(values => {
            this.suggestions = values.map(value => ({
                code: value.code,
                label: value.label,
                description: value.description
            } as RadioListItem));
        });

        this.step1FormGroup.patchValue({
            title: this.project.title,
            description: this.project.description,
            reuse_status: this.project.reutilisation_status,
            begin_date: this.project.expected_completion_start_date ? moment(this.project.expected_completion_start_date) : null,
            end_date: this.project.expected_completion_end_date ? moment(this.project.expected_completion_end_date) : null,
            publicCible: this.project.target_audiences,
            echelle: this.project.territorial_scale,
            territoire: this.project.detailed_territorial_scale,
            accompagnement: this.project.desired_supports,
            type: this.project.type,
            url: this.project.access_url,
            confidentiality: this.project.confidentiality,
        });
    }

    // Activation du mode modification
    updateProjectTaskInfo(isUpdate: boolean): void {
        this.updateInProgress.emit(isUpdate);
        this.loadProjectInformations();
        this.isUpdating = !this.isUpdating;
    }

    /**
     * On gère le fait que l'image saisie a l'étape 1 change
     * @param image l'image qui a été saisie
     */
    handleImageChanged(image: Blob): void {
        // On détermine l'action de mise à jour à faire si on update le projet
        if (this.projectImageSaved == null && image) {
            this.updateImageAction = UpdateAction.AJOUT;
        } // Si l'utillisateur saisit une image et qu'il y'en avait déjà une avant ET si leurs tailles sont différentes
        // Comparaison de blob nécessite fileReader qui bloque le Thread
        else if (this.projectImageSaved && image && this.projectImageSaved.size !== image.size) {
            this.updateImageAction = UpdateAction.MISE_A_JOUR;
        } else if (this.projectImageSaved && image == null) {
            this.updateImageAction = UpdateAction.SUPPRESSION;
        }
    }

    /**
     * Ouverture d'une dialog permettant de confirmer la modification de la réutilisation
     */
    public updateConfirmation(): void {
        this.personalSpaceProjectService.openDialogUpdateConfirmation(this.dialogDescription)
            .subscribe(item => {
                if (item && item.closeEvent === CloseEvent.VALIDATION) {
                    this.updateForm.emit({
                        confidentialities: this.confidentialities,
                        form: this.step1FormGroup,
                        messageToModerator: this.messageToModeratorFormGroup.controls['messageToModerator'].value
                    });
                }
            });
    }
}
