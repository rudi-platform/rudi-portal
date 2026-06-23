import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef} from '@angular/material/dialog';
import {MatIcon} from '@angular/material/icon';
import {ObjectType} from '@core/services/tasks/object-type.enum';
import {ORGANIZATION_PROCESS_KEY_DEFINITION} from '@core/services/tasks/TaskDependencyFetcherFactory';
import {CloseEvent} from '@features/data-set/models/dialog-closed-data';
import {TranslatePipe} from '@ngx-translate/core';
import {WorkflowFormComponent} from '@shared/core/workflow/forms/workflow-form/workflow-form.component';
import {WorkflowProperties} from '@shared/core/workflow/forms/workflow-form/workflow-properties';
import {Form} from 'micro_service_modules/strukture/api-strukture';
import {Organization} from 'micro_service_modules/strukture/strukture-model';

export interface OrganizationUpdateFormDialogData {
    draftForm: Form;
    title?: string;
    description?: string;
    organization?: Organization;
}

@Component({
    selector: 'app-organization-update-form-dialog',
    imports: [
        MatButton,
        MatDialogActions,
        MatDialogContent,
        MatIcon,
        MatIconButton,
        TranslatePipe,
        WorkflowFormComponent
    ],
    templateUrl: './organization-update-form-dialog.component.html',
    styleUrl: './organization-update-form-dialog.component.scss'
})
export class OrganizationUpdateFormDialogComponent implements OnInit {
    FORM_CONTROL_NAME_MESSAGE = 'updateMessageToModerator';
    FORM_CONTROL_NAME_NAME = 'name';
    FORM_CONTROL_NAME_DESCRIPTION = 'description';
    FORM_CONTROL_NAME_URL = 'url';
    FORM_CONTROL_NAME_ADDRESS = 'address';

    @ViewChild(WorkflowFormComponent)
    workflowFormComponent!: WorkflowFormComponent;

    draftForm: Form;
    title: string;
    description: string;
    organization: Organization;

    constructor(
        public dialogRef: MatDialogRef<OrganizationUpdateFormDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: OrganizationUpdateFormDialogData
    ) {
    }

    ngOnInit(): void {
        this.draftForm = this.data.draftForm;
        this.title = this.data.title;
        this.description = this.data.description;
        this.organization = this.data.organization;

        this.prefillDraftFormIfEmpty();
    }

    onClickClose(): void {
        this.closeDialog(CloseEvent.CANCEL);
    }

    onClickConfirm(): void {
        if (!this.workflowFormComponent?.submit()) {
            return;
        }
        this.dialogRef.close({
            closeEvent: CloseEvent.VALIDATION,
            data: this.getOrganization(),
            messageToModerator: this.getControlValue(this.FORM_CONTROL_NAME_MESSAGE) ?? ''
        });
    }

    private closeDialog(closeEvent: CloseEvent): void {
        this.dialogRef.close({
            closeEvent
        });
    }

    isValidForm(): boolean {
        return this.workflowFormComponent ? !this.workflowFormComponent.invalid : false;
    }

    get properties(): WorkflowProperties {
        return {
            fileMaxSize: undefined,
            processDefinitionKey: ORGANIZATION_PROCESS_KEY_DEFINITION,
        };
    }

    getOrganization(): Organization {
        return {
            name: this.getControlValue(this.FORM_CONTROL_NAME_NAME) ?? '',
            description: this.getControlValue(this.FORM_CONTROL_NAME_DESCRIPTION) ?? '',
            url: this.getControlValue(this.FORM_CONTROL_NAME_URL),
            address: this.getControlValue(this.FORM_CONTROL_NAME_ADDRESS),
            object_type: ObjectType.ORGANIZATION,
        };
    };

    private getControlValue(fieldName: string): string | null {
        const formGroup = this.workflowFormComponent?.formGroup;
        if (!formGroup) {
            return null;
        }

        // Les contrôles du workflow sont nommés "section_field".
        const controlKey = Object.keys(formGroup.controls)
            .find(name => name === fieldName || name.endsWith(`_${fieldName}`));

        if (!controlKey) {
            return null;
        }

        const value = formGroup.controls[controlKey]?.value;
        return value === undefined || value === null || value === '' ? null : String(value);
    }

    private prefillDraftFormIfEmpty(): void {
        if (!this.draftForm || !this.organization) {
            return;
        }

        const orgValues: Record<string, string | undefined> = {
            name: this.organization.name,
            description: this.organization.description,
            url: this.organization.url,
            address: this.organization.address,
        };

        this.draftForm.sections?.forEach(section => {
            section.fields?.forEach(field => {
                const key = field.definition?.name;
                const incomingValue = key ? orgValues[key] : undefined;
                const currentValue = field.values?.[0];

                const isEmpty = currentValue === undefined || currentValue === null || currentValue === '';
                if (isEmpty && incomingValue !== undefined && incomingValue !== null && incomingValue !== '') {
                    field.values = [incomingValue];
                }
            });
        });
    }
}
