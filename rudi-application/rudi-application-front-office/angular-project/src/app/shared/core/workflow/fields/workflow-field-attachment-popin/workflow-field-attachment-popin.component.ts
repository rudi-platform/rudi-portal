import {Component, Inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatCheckbox} from '@angular/material/checkbox';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef} from '@angular/material/dialog';
import {MatIcon, MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {AttachmentService} from '@core/services/attachment.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {CloseEvent} from '@features/data-set/models/dialog-closed-data';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {Level} from '@shared/core/layout/notification-template/notification-template.component';
import {AttachmentPopinData} from '@shared/core/workflow/fields/workflow-field-attachment-popin/attachment-popin-data';
import {LoaderComponent} from '../../../common/loader/loader.component';

@Component({
    selector: 'app-workflow-field-attachment-popin',
    templateUrl: './workflow-field-attachment-popin.component.html',
    styleUrls: ['./workflow-field-attachment-popin.component.scss'],
    imports: [MatDialogContent, MatIconButton, MatIcon, MatCheckbox, FormsModule, LoaderComponent, MatDialogActions, MatButton, MatDialogClose, TranslatePipe]
})
export class WorkflowFieldAttachmentPopinComponent {

    consent: boolean;
    attachmentLoader = false;
    attachmentService: AttachmentService;

    constructor(public dialogRef: MatDialogRef<WorkflowFieldAttachmentPopinComponent>,
                matIconRegistry: MatIconRegistry,
                domSanitizer: DomSanitizer,
                private readonly snackBarService: SnackBarService,
                private readonly translateService: TranslateService,
                @Inject(MAT_DIALOG_DATA) public data: AttachmentPopinData,
    ) {
        matIconRegistry.addSvgIcon(
            'icon-close',
            domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/icon-close.svg')
        );
    }

    openDocument(): void {
        // On active le loader le temps de chargement du document
        this.attachmentLoader = true;
        this.attachmentService.downloadAttachement(this.data.attachmentUuid)
            .subscribe({
                    next: (content: Blob) => {
                        const url = window.URL.createObjectURL(content);
                        window.open(url, '_blank').focus();
                        this.attachmentLoader = false;
                    },
                    complete: () => {
                        this.attachmentLoader = false;
                        this.handleClose();
                    },
                    error: err => {
                        console.error(err);
                        this.attachmentLoader = false;
                        this.snackBarService.openSnackBar({
                            message: this.translateService.instant('metaData.selfdataInformationRequest.creation.error.errorDownload'),
                            level: Level.ERROR
                        });
                    }
                }
            );
    }

    /**
     * Fermeture de la popin
     */
    handleClose(): void {
        this.dialogRef.close({
            closeEvent: CloseEvent.VALIDATION,
            data: null
        });
    }
}
