import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {AttachmentService} from '@core/services/attachment.service';
import {SnackBarService} from '@core/services/snack-bar.service';
import {CloseEvent} from '@features/data-set/models/dialog-closed-data';
import {TranslateService} from '@ngx-translate/core';
import {Level} from '../notification-template/notification-template.component';
import {AttachmentPopinData} from './attachment-popin-data';

@Component({
    selector: 'app-workflow-field-attachment-popin',
    templateUrl: './workflow-field-attachment-popin.component.html',
    styleUrls: ['./workflow-field-attachment-popin.component.scss'],
    standalone: false
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
