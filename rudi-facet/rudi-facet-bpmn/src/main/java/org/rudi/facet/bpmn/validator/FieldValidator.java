package org.rudi.facet.bpmn.validator;

import org.rudi.bpmn.core.bean.Field;

/**
 * Contract for field validators used in BPMN form validation.
 *
 * A validator first declares if it can handle a field via {@link #accept(Field)},
 * then performs validation with {@link #check(Field)}.
 */
public interface FieldValidator {

	/**
	 * Indicates whether this validator supports the given field and its configuration.
	 *
	 * @param field the field to evaluate
	 * @return {@code true} if this validator can validate the field, {@code false} otherwise
	 */
	boolean accept(Field field);

	/**
	 * Validates the field values according to this validator's rule.
	 *
	 * @param field the field to validate
	 * @return {@code true} when validation succeeds, {@code false} otherwise
	 */
	boolean check(Field field);
}
