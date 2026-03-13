import {Component, Inject, ViewChild} from '@angular/core';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef} from '@angular/material/dialog';
import {MatIcon} from '@angular/material/icon';
import {CloseEvent, DialogClosedData} from '@features/data-set/models/dialog-closed-data';
import {TranslatePipe} from '@ngx-translate/core';
import {WorkflowFormDialogInputData, WorkflowFormDialogOutputData} from '@shared/core/workflow/forms/workflow-form-dialog/types';
import {WorkflowFormComponent} from '@shared/core/workflow/forms/workflow-form/workflow-form.component';
import {getSectionWithFields} from '@shared/utils/workflow-form-utils';
import {Field, Section} from 'micro_service_modules/projekt/projekt-api';
import {WorkflowFormComponent as WorkflowFormComponent_1} from '../workflow-form/workflow-form.component';

@Component({
    selector: 'app-workflow-form-dialog',
    templateUrl: './workflow-form-dialog.component.html',
    styleUrls: ['./workflow-form-dialog.component.scss'],
    imports: [MatDialogContent, MatIconButton, MatIcon, WorkflowFormComponent_1, MatDialogActions, MatButton, TranslatePipe]
})
export class WorkflowFormDialogComponent {
    @ViewChild('workflowForm', {static: true})
    workflowFormComponent: WorkflowFormComponent;

    constructor(
        @Inject(MAT_DIALOG_DATA) public dialogData: WorkflowFormDialogInputData,
        public dialogRef: MatDialogRef<WorkflowFormDialogComponent, DialogClosedData<WorkflowFormDialogOutputData>>,
    ) {
    }

    get hasRequiredFields(): boolean {
        if (this.dialogData.form?.sections == null || this.dialogData.form.sections.length === 0) {
            return false;
        }

        return getSectionWithFields(this.dialogData.form).some(
            (section: Section) => section.fields.some((field: Field) => field.definition.required)
        );
    }

    onClickClose(): void {
        this.closeDialog(CloseEvent.CANCEL);
    }

    onClickConfirm(): void {
        this.workflowFormComponent.submit();
    }

    handleFormSubmit(): void {
        this.closeDialog(CloseEvent.VALIDATION);
    }

    private closeDialog(closeEvent: CloseEvent): void {
        this.dialogRef.close({
            data: {
                form: this.dialogData.form
            },
            closeEvent,
        });
    }
}


