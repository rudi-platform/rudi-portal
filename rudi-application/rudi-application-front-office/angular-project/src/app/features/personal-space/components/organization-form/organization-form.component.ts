import {Component, inject, Input, OnInit, ViewChild} from '@angular/core';
import {OrganizationAddresses} from '@core/services/organization/organization-addresses';
import {OrganizationAddressesMapper} from '@core/services/organization/organization-addresses-mapper';
import {ObjectType} from '@core/services/tasks/object-type.enum';
import {ORGANIZATION_PROCESS_KEY_DEFINITION} from '@core/services/tasks/TaskDependencyFetcherFactory';
import {WorkflowFormComponent} from '@shared/core/workflow/forms/workflow-form/workflow-form.component';
import {WorkflowProperties} from '@shared/core/workflow/forms/workflow-form/workflow-properties';
import {Form} from 'micro_service_modules/strukture/api-strukture';
import {Organization} from 'micro_service_modules/strukture/strukture-model';
import {map, Observable} from 'rxjs';

@Component({
    selector: 'app-organization-form',
    templateUrl: './organization-form.component.html',
    styleUrls: ['./organization-form.component.scss'],
    imports: [WorkflowFormComponent]
})
export class OrganizationFormComponent implements OnInit {
    @ViewChild(WorkflowFormComponent) workflowFormComponent: WorkflowFormComponent;
    @Input() draftForm: Form;
    @Input() organization: Organization;
    @Input() messageControlName = 'messageToModerator';

    FORM_CONTROL_NAME_NAME = 'name';
    FORM_CONTROL_NAME_DESCRIPTION = 'description';
    FORM_CONTROL_NAME_ADDRESS = 'address';
    FORM_CONTROL_NAME_URL = 'url';
    FORM_CONTROL_NAME_EMAIL = 'email';
    FORM_CONTROL_NAME_PHONE_NUMBER = 'phoneNumber';
    organizationAddresses: OrganizationAddresses = {};

    private readonly organizationAddressesMapper = inject(OrganizationAddressesMapper);

    get properties(): WorkflowProperties {
        return {
            fileMaxSize: undefined,
            processDefinitionKey: ORGANIZATION_PROCESS_KEY_DEFINITION,
        };
    }

    get messageToModerator(): string | null {
        return this.getControlValue(this.messageControlName);
    }

    isValidForm(): boolean {
        return this.workflowFormComponent ? !this.workflowFormComponent.invalid : false;
    }

    submitWorkflowForm(): boolean {
        return this.workflowFormComponent?.submit() ?? true;
    }

    getOrganization$(): Observable<Organization> {
        const organizationAddresses: OrganizationAddresses = {
            email: this.getControlValue(this.FORM_CONTROL_NAME_EMAIL) ?? '',
            phoneNumber: this.getControlValue(this.FORM_CONTROL_NAME_PHONE_NUMBER) ?? '',
            url: this.getControlValue(this.FORM_CONTROL_NAME_URL) ?? ''
        };

        return this.organizationAddressesMapper
            .mapOrganizationAddressesToAbstractAddresses(organizationAddresses)
            .pipe(
                map((addresses) => ({
                    name: this.getControlValue(this.FORM_CONTROL_NAME_NAME) ?? '',
                    description: this.getControlValue(this.FORM_CONTROL_NAME_DESCRIPTION) ?? '',
                    address: this.getControlValue(this.FORM_CONTROL_NAME_ADDRESS) ?? '',
                    object_type: ObjectType.ORGANIZATION,
                    addresses
                }))
            );
    }

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

    ngOnInit(): void {
        this.organizationAddresses = this.organizationAddressesMapper
            .mapAbstractAddressesToOrganizationAddresses(this.organization?.addresses ?? []);
        this.prefillDraftFormIfEmpty();
    }

    private prefillDraftFormIfEmpty(): void {
        if (!this.draftForm || !this.organization) {
            return;
        }

        const orgValues: Record<string, string | undefined> = {
            name: this.organization.name,
            description: this.organization.description,
            url: this.organizationAddresses.url,
            address: this.organization.address,
            email: this.organizationAddresses.email,
            phoneNumber: this.organizationAddresses.phoneNumber,
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
