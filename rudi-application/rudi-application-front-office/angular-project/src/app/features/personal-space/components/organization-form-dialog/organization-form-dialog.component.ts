import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {CloseEvent} from '@features/data-set/models/dialog-closed-data';
import {OrganizationFormComponent} from '@features/personal-space/components/organization-form/organization-form.component';
import {Form} from 'micro_service_modules/strukture/api-strukture';

@Component({
    selector: 'app-organization-form-dialog',
    templateUrl: './organization-form-dialog.component.html',
    styleUrls: ['./organization-form-dialog.component.scss']
})
export class OrganizationFormDialogComponent implements OnInit {
    @ViewChild('OrganizationForm', {static: true})
    organizationFormComponent: OrganizationFormComponent;

    draftForm: Form;

    constructor(
        public dialogRef: MatDialogRef<OrganizationFormDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: { draftForm: Form }
    ) {
    }

    ngOnInit(): void {
        this.draftForm = this.data.draftForm;
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
