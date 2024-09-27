import {Component} from '@angular/core';
import {MatDialogRef} from '@angular/material/dialog';
import {CloseEvent, DialogClosedData} from '@features/data-set/models/dialog-closed-data';


@Component({
    selector: 'app-remove-keys-dialog',
    templateUrl: './remove-keys-dialog.component.html',
    styleUrl: './remove-keys-dialog.component.scss'
})
export class RemoveKeysDialogComponent {

    constructor(
        public dialogRef: MatDialogRef<string, DialogClosedData<string>>,
    ) {
    }

    removeKey(){
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
