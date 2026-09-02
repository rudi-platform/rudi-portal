import {Injectable} from '@angular/core';
import {Validators} from '@angular/forms';
import {LogService} from '@core/services/log.service';
import {Validator} from '@shared/core/workflow/forms/workflow-form/workflow-form.types';
import {ObjectUtils} from '@shared/utils/object-utils';
import {Field, Section, Validator as WorkflowValidator, ValidatorType} from 'micro_service_modules/api-bpmn';
import {Form} from 'micro_service_modules/projekt/projekt-api';

const REQUIRED_WORKFLOW_VALIDATOR: WorkflowValidator = {
    type: 'REQUIRED'
};

@Injectable({
    providedIn: 'root'
})
export class WorkflowFormUtils {
    constructor(
        private readonly logger: LogService
    ) {
    }

    computeFormControlName(section: Section, field: Field): string {
        return section.name + '_' + field.definition.name;
    }

    isFormReadonly(form: Form): boolean {
        // form is readonly only if all section are readonly
        if (!form?.sections) {
            return false;
        }
        for (const section of form.sections) {
            if (!section.readOnly) {
                return false;
            }
        }

        return true;
    }

    getValidatorsFor(field: Field): Validator[] {
        const validators = new Set<Validator>();

        if (field.definition.required) {
            validators.add(this.buildValidatorFromWorkflow(REQUIRED_WORKFLOW_VALIDATOR, field));
        }

        if (field.definition.validators) {
            field.definition.validators
                .map(workflowValidator => this.buildValidatorFromWorkflow(workflowValidator, field))
                .filter(ObjectUtils.nonNull)
                .forEach(validator => validators.add(validator));
        }

        return Array.from(validators.values());
    }

    /**
     * Convertit un pattern déclaré côté back (littéral JS entouré de {@code /.../}, avec flags
     * optionnels) en {@link RegExp} exploitable par {@link Validators.pattern}.
     */
    private parsePattern(pattern: string, field: Field): RegExp | null {
        try {
            const delimited = /^\/(.*)\/([a-z]*)$/.exec(pattern);
            return delimited ? new RegExp(delimited[1], delimited[2]) : new RegExp(pattern);
        } catch (error) {
            this.logger.warning(
                `Le pattern "${pattern}" du champ "${field.definition.name}" est invalide.`,
                error
            );
            return null;
        }
    }

    private buildValidatorFromWorkflow(workflowValidator: WorkflowValidator, field: Field): Validator {
        let validatorType = workflowValidator.type;
        let attribute = workflowValidator.attribute;
        switch (validatorType) {
            case ValidatorType.Required:
                return Validators.required;
            case ValidatorType.Maxlength:
                return Validators.maxLength(parseInt(attribute ?? '0'));
            case ValidatorType.Positive:
                return Validators.min(0);
            case ValidatorType.Negative:
                return Validators.max(0);
            case ValidatorType.Regexp: {
                const regexp = this.parsePattern(attribute ?? '', field);
                return regexp ? Validators.pattern(regexp) : () => null;
            }
            case ValidatorType.Email:
                return Validators.email;
            default:
                return () => {
                    return null;
                };
        }
    }
}

