
import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MatError, MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {ObjectType} from '@core/services/tasks/object-type.enum';
import {ORGANIZATION_PROCESS_KEY_DEFINITION} from '@core/services/tasks/TaskDependencyFetcherFactory';
import {TranslatePipe, TranslateService} from '@ngx-translate/core';
import {WorkflowFormComponent} from '@shared/core/workflow/forms/workflow-form/workflow-form.component';
import {WorkflowProperties} from '@shared/core/workflow/forms/workflow-form/workflow-properties';
import {Form} from 'micro_service_modules/strukture/api-strukture';
import {Organization} from 'micro_service_modules/strukture/strukture-model';
import {MAX_ADDRESS_LENGTH, MAX_DESCRIPTION_LENGTH, MAX_MESSAGE_LENGTH, MAX_NAME_LENGTH, MAX_URL_LENGTH, URL_PATTERN} from './organization-form.constants';

@Component({
    selector: 'app-organization-form',
    templateUrl: './organization-form.component.html',
    styleUrls: ['./organization-form.component.scss'],
    imports: [FormsModule, ReactiveFormsModule, MatLabel, MatHint, MatFormField, MatInput, MatError, WorkflowFormComponent, TranslatePipe]
})
export class OrganizationFormComponent implements OnInit {
    @ViewChild(WorkflowFormComponent) workflowFormComponent: WorkflowFormComponent;
    @Input() draftForm: Form;
    @Input() organization: Organization;
    form: FormGroup;
    FORM_CONTROL_NAME_MESSAGE = 'messageToModerator';
    FORM_CONTROL_NAME_NAME = 'name';
    FORM_CONTROL_NAME_DESCRIPTION = 'description';
    FORM_CONTROL_NAME_URL = 'url';
    FORM_CONTROL_NAME_ADDRESS = 'address';

    constructor(
        private readonly formBuilder: FormBuilder,
        private readonly translateService: TranslateService
    ) {
    }

    get isModification(): boolean {
        return !!this.organization;
    }

    ngOnInit(): void {
        const formFields = [
            ...(this.isModification
                ? [{name: this.FORM_CONTROL_NAME_MESSAGE, validators: [Validators.maxLength(MAX_MESSAGE_LENGTH)]}]
                : []),
            {name: this.FORM_CONTROL_NAME_NAME, validators: [Validators.required, Validators.maxLength(MAX_NAME_LENGTH)]},
            {name: this.FORM_CONTROL_NAME_DESCRIPTION, validators: [Validators.required, Validators.maxLength(MAX_DESCRIPTION_LENGTH)]},
            {
                name: this.FORM_CONTROL_NAME_URL,
                validators: [Validators.pattern(URL_PATTERN), Validators.maxLength(MAX_URL_LENGTH)]
            },
            {name: this.FORM_CONTROL_NAME_ADDRESS, validators: [Validators.maxLength(MAX_ADDRESS_LENGTH)]},
        ];

        this.form = this.formBuilder.group(
            formFields.reduce((acc, field) => {
                acc[field.name] = ['', field.validators];
                return acc;
            }, {})
        );

        if (this.organization) {
            this.form.patchValue({
                [this.FORM_CONTROL_NAME_NAME]: this.organization.name || '',
                [this.FORM_CONTROL_NAME_DESCRIPTION]: this.organization.description || '',
                [this.FORM_CONTROL_NAME_URL]: this.organization.url || '',
                [this.FORM_CONTROL_NAME_ADDRESS]: this.organization.address || '',
            });
        }
    }

    isValidForm(): boolean {
        return this.form.valid;
    }

    get messageToModerator(): string {
        return this.form.get(this.FORM_CONTROL_NAME_MESSAGE)?.value || null;
    }

    submitWorkflowForm(): boolean {
        return this.workflowFormComponent?.submit() ?? true;
    }

    getOrganization(): Organization {
        return {
            name: this.form.get(this.FORM_CONTROL_NAME_NAME).value,
            description: this.form.get(this.FORM_CONTROL_NAME_DESCRIPTION).value,
            url: this.form.get(this.FORM_CONTROL_NAME_URL).value ? this.form.get(this.FORM_CONTROL_NAME_URL).value : null,
            address: this.form.get(this.FORM_CONTROL_NAME_ADDRESS).value ? this.form.get(this.FORM_CONTROL_NAME_ADDRESS).value : null,
            object_type: ObjectType.ORGANIZATION,
        };
    };

    isErrorOnField(formControlName: string): boolean {
        return this.form.controls[formControlName].hasError('required')
            || this.form.controls[formControlName].hasError('pattern')
            || this.form.controls[formControlName].hasError('maxlength');
    }

    getErrorMessage(formControlName: string): string {
        const control = this.form.controls[formControlName];

        if (control.hasError('required')) {
            return this.translateService.instant('personalSpace.organization.form.errorRequired');
        }

        if (control.hasError('pattern')) {
            return this.translateService.instant('personalSpace.organization.form.errorPattern');
        }

        if (control.hasError('maxlength')) {
            return this.getMaxLengthError(formControlName);
        }

        return '';
    }

    private getMaxLengthForField(formControlName: string): number {
        switch (formControlName) {
            case this.FORM_CONTROL_NAME_NAME:
                return MAX_NAME_LENGTH;
            case this.FORM_CONTROL_NAME_DESCRIPTION:
                return MAX_DESCRIPTION_LENGTH;
            case this.FORM_CONTROL_NAME_URL:
                return MAX_URL_LENGTH;
            case this.FORM_CONTROL_NAME_ADDRESS:
                return MAX_ADDRESS_LENGTH;
            case this.FORM_CONTROL_NAME_MESSAGE:
                return MAX_MESSAGE_LENGTH;
            default:
                return 0;
        }
    }

    private getMaxLengthError(formControlName: string): string {
        return this.translateService.instant('personalSpace.organization.form.errorMaxlength', {maxLength: this.getMaxLengthForField(formControlName)});
    }

    get properties(): WorkflowProperties {
        return {
            fileMaxSize: undefined,
            processDefinitionKey: ORGANIZATION_PROCESS_KEY_DEFINITION,
        };
    }
}
