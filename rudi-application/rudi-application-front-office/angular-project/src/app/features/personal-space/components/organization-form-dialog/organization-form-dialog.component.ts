import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef, MatDialogContent, MatDialogActions} from '@angular/material/dialog';
import {CloseEvent} from '@features/data-set/models/dialog-closed-data';
import {OrganizationFormComponent} from '@features/personal-space/components/organization-form/organization-form.component';
import {Form} from 'micro_service_modules/strukture/api-strukture';
import {CdkScrollable} from '@angular/cdk/scrolling';
import {MatIconButton, MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {OrganizationFormComponent as OrganizationFormComponent_1} from '../organization-form/organization-form.component';
import {TranslatePipe} from '@ngx-translate/core';

@Component({
    selector: 'app-organization-form-dialog',
    templateUrl: './organization-form-dialog.component.html',
    styleUrls: ['./organization-form-dialog.component.scss'],
    imports: [CdkScrollable, MatDialogContent, MatIconButton, MatIcon, OrganizationFormComponent_1, MatDialogActions, MatButton, TranslatePipe]
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
