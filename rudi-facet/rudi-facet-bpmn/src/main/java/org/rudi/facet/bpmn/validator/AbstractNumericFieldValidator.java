package org.rudi.facet.bpmn.validator;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Field;
import org.rudi.bpmn.core.bean.FieldType;

import lombok.extern.slf4j.Slf4j;

/**
 * Base validator for numeric fields.
 * <p>
 * Supports {@link FieldType#DOUBLE} and {@link FieldType#LONG} values and delegates
 * rule evaluation to subtype-specific checks.
 */
@Slf4j
public abstract class AbstractNumericFieldValidator extends AbstractFieldValidator{

	/**
	 * Validates all non-empty values of the field.
	 *
	 * @param field the field to validate
	 * @return {@code true} if all values satisfy the numeric rule, {@code false} otherwise
	 */
	@Override
	public boolean check(Field field) {
		boolean result = true;
		if (CollectionUtils.isNotEmpty(field.getValues())) {
			for (String value : field.getValues()) {
				if (StringUtils.isNotEmpty(value)) {
					try {
						result &= check(field, value);
					} catch (Exception e) {
						log.error("Invalid configuration for " + field.getDefinition());
					}
				}
			}
		}
		return result;
	}

	/**
	 * Validates a single numeric value according to the field type.
	 *
	 * @param field the field metadata containing the expected type
	 * @param value the raw value to validate
	 * @return {@code true} when the value is valid for the configured rule, {@code false} otherwise
	 */
	protected boolean check(Field field, String value) {
		boolean result = true;
		if (field.getDefinition().getType() == FieldType.DOUBLE) {
			double d = Double.parseDouble(value);
			if (innerCheck(d)) {
				result = false;
			}
		} else if (field.getDefinition().getType() == FieldType.LONG) {
			long d = Long.parseLong(value);
			if (innerCheck(d)) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * Applies the subtype-specific rule to a double value.
	 *
	 * @param d the parsed double value
	 * @return {@code true} when the value violates the rule, {@code false} otherwise
	 */
	protected abstract boolean innerCheck(double d);

	/**
	 * Applies the subtype-specific rule to a long value.
	 *
	 * @param d the parsed long value
	 * @return {@code true} when the value violates the rule, {@code false} otherwise
	 */
	protected abstract boolean innerCheck(long d);

	/**
	 * Indicates whether this validator supports the given field.
	 * <p>
	 * A field is supported when it has validators, is numeric (double or long), and
	 * the subtype-specific validator is present.
	 *
	 * @param field the field to evaluate
	 * @return {@code true} if the validator applies to this field, {@code false} otherwise
	 */
	@Override
	public boolean accept(Field field) {
		return hasValidator (field) && (field.getDefinition().getType() == FieldType.DOUBLE
				|| field.getDefinition().getType() == FieldType.LONG) && innerAccept(field);
	}

	/**
	 * Checks whether the subtype-specific validator configuration is present.
	 *
	 * @param field the field to inspect
	 * @return {@code true} if the concrete validator can be applied, {@code false} otherwise
	 */
	protected abstract boolean innerAccept(Field field);
}
