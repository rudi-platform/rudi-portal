package org.rudi.facet.bpmn.validator;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.bpmn.core.bean.Field;
import org.rudi.bpmn.core.bean.Validator;
import org.rudi.bpmn.core.bean.ValidatorType;

/**
 * Base implementation for field validators.
 *
 * Provides shared helper methods to inspect validator definitions configured on a field.
 */
public abstract class AbstractFieldValidator implements FieldValidator {

	/**
	 * Finds the first validator matching the requested type in the field definition.
	 *
	 * @param field the field containing validator definitions
	 * @param validatorType the validator type to search
	 * @return the matching validator, or {@code null} if none is configured
	 */
	protected Validator lookUpValidator(Field field, ValidatorType validatorType) {
		Validator result = null;
		if (CollectionUtils.isNotEmpty(field.getDefinition().getValidators())) {
			result = field.getDefinition().getValidators().stream().filter(v -> v.getType() == validatorType)
					.findFirst().orElse(null);
		}
		return result;
	}

	/**
	 * Indicates whether the field defines at least one validator.
	 *
	 * @param field the field to inspect
	 * @return {@code true} if the field has validator definitions, {@code false} otherwise
	 */
	protected boolean hasValidator(Field field){
		return CollectionUtils.isNotEmpty(field.getDefinition().getValidators());
	}
}
