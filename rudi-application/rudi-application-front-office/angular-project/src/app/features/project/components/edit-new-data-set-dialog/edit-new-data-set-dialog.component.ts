import {Component, Inject, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef} from '@angular/material/dialog';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatIcon, MatIconRegistry} from '@angular/material/icon';
import {MatInput} from '@angular/material/input';
import {DomSanitizer} from '@angular/platform-browser';
import {BreakpointObserverService, MediaSize} from '@core/services/breakpoint-observer.service';
import {ProjectDatasetPictoType} from '@features/project/model/project-dataset-picto-type';

import {TranslatePipe} from '@ngx-translate/core';
import {DataRequestItem} from '../../model/data-request-item';

/**
 * Les données que peuvent accepter la Dialog
 */
export interface NewDataSetDialogData {
    data: {
        dataRequestItem: DataRequestItem;
        counter: number;
    };
}

@Component({
    selector: 'app-edit-new-data-set-dialog',
    templateUrl: './edit-new-data-set-dialog.component.html',
    styleUrls: ['./edit-new-data-set-dialog.component.scss'],
    imports: [MatDialogContent, MatIconButton, MatDialogClose, MatIcon, FormsModule, ReactiveFormsModule, MatLabel, MatHint, MatFormField, MatInput, MatError, MatDialogActions, MatButton, TranslatePipe]
})
export class EditNewDataSetDialogComponent implements OnInit {

    /**
     * Les propriétés média (dekstop/mobile)
     */
    @Input()
    mediaSize: MediaSize;

    /**
     * Le numéro de la demande parmi toutes les autres rattachées au même projet.
     * On commence à 1.
     */
    number = 1;

    /**
     * La demande de JDD a modifier si en mode édition
     */
    dataRequestItem: DataRequestItem;

    /**
     * Le formulaire de saisie de la demande
     */
    newDatasetRequestFormGroup: FormGroup;

    /**
     * Titre par défaut d'une demande sans titre
     */
    titleMessage = 'Nouvelle demande';

    constructor(
        public dialogRef: MatDialogRef<EditNewDataSetDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public dialogData: NewDataSetDialogData,
        private readonly matIconRegistry: MatIconRegistry,
        private readonly domSanitizer: DomSanitizer,
        private readonly formBuilder: FormBuilder,
        private readonly breakpointObserver: BreakpointObserverService) {
        if (this.dialogData) {
            this.dataRequestItem = this.dialogData.data.dataRequestItem;
            this.number = this.dialogData.data.counter;
        }
        this.matIconRegistry.addSvgIcon(
            'icon-close',
            this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/icon-close.svg')
        );
    }

    ngOnInit(): void {
        this.mediaSize = this.breakpointObserver.getMediaSize();

        this.newDatasetRequestFormGroup = this.formBuilder.group({
            title: ['', Validators.required],
            description: ['', Validators.required]
        });

        if (this.dataRequestItem) {
            this.newDatasetRequestFormGroup.get('title').setValue(this.dataRequestItem.title);
            this.newDatasetRequestFormGroup.get('description').setValue(this.dataRequestItem.description);
        }
    }

    /**
     * Récupération du titre par défaut d'une nouvelle demande à l'aide du compteur
     */
    get defaultTitle(): string {
        return (this.titleMessage + ' (' + this.number + ')');
    }

    /**
     * Création de la demande et fermeture de la popin
     */
    submit(): void {
        const request: DataRequestItem = {
            title: this.newDatasetRequestFormGroup.get('title').value
                ? this.newDatasetRequestFormGroup.get('title').value : this.defaultTitle,
            description: this.newDatasetRequestFormGroup.get('description').value,
            uuid: null,
            pictoType: ProjectDatasetPictoType.STATIC
        };

        this.dialogRef.close(request);
    }
}
