import {ChangeDetectorRef, Component, ElementRef, Inject, OnInit, ViewChild} from '@angular/core';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatIcon} from '@angular/material/icon';
import {CloseEvent} from '@features/data-set/models/dialog-closed-data';
import {OrganizationFormComponent} from '@features/personal-space/components/organization-form/organization-form.component';
import {TranslatePipe} from '@ngx-translate/core';
import {Form} from 'micro_service_modules/strukture/api-strukture';
import {Organization} from 'micro_service_modules/strukture/strukture-model';
import {take} from 'rxjs';
import {OrganizationFormComponent as OrganizationFormComponent_1} from '../organization-form/organization-form.component';

export interface OrganizationFormDialogData {
    draftForm: Form;
    title?: string;
    description?: string;
    organization?: Organization;
    messageControlName?: string;
}

@Component({
    selector: 'app-organization-form-dialog',
    templateUrl: './organization-form-dialog.component.html',
    styleUrls: ['./organization-form-dialog.component.scss'],
    imports: [MatDialogContent, MatDialogTitle,
        MatIconButton, MatIcon, OrganizationFormComponent_1, MatDialogActions, MatButton, TranslatePipe]
})
export class OrganizationFormDialogComponent implements OnInit {
    @ViewChild('OrganizationForm', {static: true})
    organizationFormComponent: OrganizationFormComponent;

    draftForm: Form;
    title: string;
    description: string;
    organization: Organization;
    messageControlName: string;

    constructor(
        public dialogRef: MatDialogRef<OrganizationFormDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: OrganizationFormDialogData,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly elementRef: ElementRef<HTMLElement>,
    ) {
    }

    ngOnInit(): void {
        this.draftForm = this.data.draftForm;
        this.title = this.data.title;
        this.description = this.data.description;
        this.organization = this.data.organization;
        this.messageControlName = this.data.messageControlName ?? 'messageToModerator';
    }

    onClickClose(): void {
        this.closeDialog(CloseEvent.CANCEL);
    }

    onClickConfirm(): void {
        if (!this.organizationFormComponent.submitWorkflowForm()) {
            this.focusFirstInvalidField();
            return;
        }

        this.organizationFormComponent.getOrganization$()
            .pipe(take(1))
            .subscribe({
                next: (organization) => {
                    this.dialogRef.close({
                        closeEvent: CloseEvent.VALIDATION,
                        data: organization,
                        messageToModerator: this.organizationFormComponent.messageToModerator
                    });
                },
                error: () => {
                    // Keep dialog open when organization payload cannot be built.
                }
            });
    }

    // Moves focus to the first invalid field so its error is announced
    private focusFirstInvalidField(): void {
        this.changeDetectorRef.detectChanges();
        this.elementRef.nativeElement
            .querySelector<HTMLElement>('input.ng-invalid, textarea.ng-invalid, select.ng-invalid')
            ?.focus();
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
