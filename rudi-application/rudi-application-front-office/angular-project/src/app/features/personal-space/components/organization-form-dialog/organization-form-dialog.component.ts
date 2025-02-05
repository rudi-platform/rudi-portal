import {Component, ViewChild} from '@angular/core';
import {MatDialogRef} from '@angular/material/dialog';
import {CloseEvent} from '@features/data-set/models/dialog-closed-data';
import {
    OrganizationFormComponent
} from '@features/personal-space/components/organization-form/organization-form.component';

@Component({
    selector: 'app-organization-form-dialog',
    templateUrl: './organization-form-dialog.component.html',
    styleUrls: ['./organization-form-dialog.component.scss']
})
export class OrganizationFormDialogComponent {
    @ViewChild('OrganizationForm', {static: true})
    organizationFormComponent: OrganizationFormComponent;

    constructor(
        public dialogRef: MatDialogRef<OrganizationFormDialogComponent>,
    ) {
    }

    onClickClose(): void {
        this.closeDialog(CloseEvent.CANCEL);
    }

    onClickConfirm(): void {
        this.dialogRef.close({
            closeEvent: CloseEvent.VALIDATION,
            data: this.organizationFormComponent.getOrganization()
        });
    }

    private closeDialog(closeEvent: CloseEvent): void {
        this.dialogRef.close({
            closeEvent
        });
    }

    isValidForm(): boolean {
        return this.organizationFormComponent.isValidForm();
    }

}
