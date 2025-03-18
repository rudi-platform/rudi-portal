import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ObjectType} from '@core/services/tasks/object-type.enum';
import {ORGANIZATION_PROCESS_KEY_DEFINITION} from '@core/services/tasks/TaskDependencyFetcherFactory';
import {TranslateService} from '@ngx-translate/core';
import {WorkflowProperties} from '@shared/workflow-form/workflow-properties';
import {Form} from 'micro_service_modules/strukture/api-strukture';
import {Organization} from 'micro_service_modules/strukture/strukture-model';

// Taille maximum requise pour les différents champs du formulaire
const MAX_NAME_LENGTH = 100;
const MAX_DESCRIPTION_LENGTH = 1024;
const MAX_URL_LENGTH = 80;
const MAX_ADDRESS_LENGTH = 255;

@Component({
    selector: 'app-organization-form',
    templateUrl: './organization-form.component.html',
    styleUrls: ['./organization-form.component.scss']
})
export class OrganizationFormComponent implements OnInit {
    @Input() draftForm: Form;
    form: FormGroup;
    FORM_CONTROL_NAME_NAME = 'name';
    FORM_CONTROL_NAME_DESCRIPTION = 'description';
    FORM_CONTROL_NAME_URL = 'url';
    FORM_CONTROL_NAME_ADDRESS = 'address';

    constructor(
        private readonly formBuilder: FormBuilder,
        private readonly translateService: TranslateService
    ) {
    }

    ngOnInit(): void {
        const formFields = [
            {name: this.FORM_CONTROL_NAME_NAME, validators: [Validators.required, Validators.maxLength(MAX_NAME_LENGTH)]},
            {name: this.FORM_CONTROL_NAME_DESCRIPTION, validators: [Validators.required, Validators.maxLength(MAX_DESCRIPTION_LENGTH)]},
            {
                name: this.FORM_CONTROL_NAME_URL,
                validators: [Validators.pattern(/^(http|https|ftp):\/\/.*$/), Validators.maxLength(MAX_URL_LENGTH)]
            },
            {name: this.FORM_CONTROL_NAME_ADDRESS, validators: [Validators.maxLength(MAX_ADDRESS_LENGTH)]},
        ];

        this.form = this.formBuilder.group(
            formFields.reduce((acc, field) => {
                acc[field.name] = ['', field.validators];
                return acc;
            }, {})
        );
    }

    isValidForm(): boolean {
        return this.form.valid;
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
