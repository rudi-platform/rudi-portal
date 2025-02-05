import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {Organization} from '../../../../../../micro_service_modules/strukture/strukture-model';
import {TranslateService} from '@ngx-translate/core';
import {ObjectType} from '@core/services/tasks/object-type.enum';

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
        this.form = this.formBuilder.group({
            name: ['', [Validators.required, Validators.maxLength(MAX_NAME_LENGTH)]],
            description: ['', [Validators.required, Validators.maxLength(MAX_DESCRIPTION_LENGTH)]],
            url: ['', [Validators.pattern(/^(http|https|ftp):\/\/.*$/), Validators.maxLength(MAX_URL_LENGTH)]],
            address: ['', Validators.maxLength(MAX_ADDRESS_LENGTH)]
        });
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
    }

    isErrorOnField(formControlName: string): boolean {
        return this.form.controls[formControlName].hasError('required')
            || this.form.controls[formControlName].hasError('pattern')
            || this.form.controls[formControlName].hasError('maxlength');
    }

    getErrorMessage(formControlName: string): string {
        return this.form.controls[formControlName].hasError('required') ? this.translateService.instant('personalSpace.organization.form.errorRequired') :
            this.form.controls[formControlName].hasError('pattern') ? this.translateService.instant('personalSpace.organization.form.errorPattern') :
                this.form.controls[formControlName].hasError('maxlength') ? this.getMaxLengthError(formControlName) :
                    '';
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
}
