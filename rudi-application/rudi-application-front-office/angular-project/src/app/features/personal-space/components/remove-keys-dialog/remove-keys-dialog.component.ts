import {Component} from '@angular/core';
import {MatDialogRef, MatDialogContent, MatDialogActions} from '@angular/material/dialog';
import {CloseEvent, DialogClosedData} from '@features/data-set/models/dialog-closed-data';
import {CdkScrollable} from '@angular/cdk/scrolling';
import {MatIconButton, MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {TranslatePipe} from '@ngx-translate/core';


@Component({
    selector: 'app-remove-keys-dialog',
    templateUrl: './remove-keys-dialog.component.html',
    styleUrl: './remove-keys-dialog.component.scss',
    imports: [CdkScrollable, MatDialogContent, MatIconButton, MatIcon, MatDialogActions, MatButton, TranslatePipe]
})
export class RemoveKeysDialogComponent {

    constructor(
        public dialogRef: MatDialogRef<string, DialogClosedData<string>>,
    ) {
    }

    removeKey() {
        this.dialogRef.close({
            data: null,
            closeEvent: CloseEvent.VALIDATION
        });
    }

    cancel(): void {
        this.dialogRef.close({
            data: null,
            closeEvent: CloseEvent.CANCEL
        });
    }
}
