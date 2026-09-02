package org.rudi.facet.bpmn.validator.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Field;
import org.rudi.bpmn.core.bean.FieldType;
import org.rudi.bpmn.core.bean.Validator;
import org.rudi.bpmn.core.bean.ValidatorType;
import org.rudi.facet.bpmn.validator.AbstractFieldValidator;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RegexpFieldValidator extends AbstractFieldValidator {
	@Override
	public boolean check(Field field) {
		boolean result = true;
		Validator validator = lookUpValidator(field, ValidatorType.REGEXP);
		try {
			String regexp = getRegexp(validator);
			if (CollectionUtils.isNotEmpty(field.getValues())) {
				for (String value : field.getValues()) {
					if (StringUtils.isNotEmpty(value) && !value.matches(regexp)) {
						result = false;
						break;
					}
				}
			}
		} catch (Exception e) {
			log.error("Invalid configuration for " + field.getDefinition());
		}
		return result;
	}

	protected String getRegexp(Validator validator) {
		return validator.getAttribute();
	}

	@Override
	public boolean accept(Field field) {
		return hasValidator (field) && (field.getDefinition().getType() == FieldType.STRING
				|| field.getDefinition().getType() == FieldType.TEXT)
				&& lookUpValidator(field, ValidatorType.REGEXP) != null;
	}
}
