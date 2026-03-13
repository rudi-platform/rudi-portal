import {Component} from '@angular/core';
import {MatIconButton} from '@angular/material/button';
import {MatDialogContent, MatDialogRef} from '@angular/material/dialog';
import {MatIcon, MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {Router} from '@angular/router';
import {TranslatePipe} from '@ngx-translate/core';
import {CloseEvent, DialogClosedData} from '../../models/dialog-closed-data';

@Component({
    selector: 'app-success-restricted-request-dialog',
    templateUrl: './success-restricted-request-dialog.component.html',
    styleUrls: ['./success-restricted-request-dialog.component.scss'],
    imports: [MatDialogContent, MatIconButton, MatIcon, TranslatePipe]
})
export class SuccessRestrictedRequestDialogComponent {

    constructor(private readonly matIconRegistry: MatIconRegistry,
                private readonly domSanitizer: DomSanitizer,
                private readonly router: Router,
                public dialogRef: MatDialogRef<DialogClosedData<void>>,
    ) {
        this.matIconRegistry.addSvgIcon(
            'icon-close',
            this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/icon-close.svg')
        );
    }

    goToMyReuses(): Promise<boolean> {
        this.handleClose();
        return this.router.navigate(['/personal-space/my-activity']);
    }

    /**
     * Fermeture de la popin
     */
    handleClose(): void {
        this.dialogRef.close({
            data: null,
            closeEvent: CloseEvent.VALIDATION
        });
    }

}
