package org.rudi.facet.bpmn.validator.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Field;
import org.rudi.bpmn.core.bean.ValidatorType;
import org.rudi.facet.bpmn.validator.AbstractFieldValidator;
import org.springframework.stereotype.Component;

@Component
public class RequiredFieldValidator extends AbstractFieldValidator {
	@Override
	public boolean check(Field field) {
		boolean result = false;
		if (CollectionUtils.isNotEmpty(field.getValues())) {
			for (String value : field.getValues()) {
				if (StringUtils.isNotEmpty(value)) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	@Override
	public boolean accept(Field field) {
		return lookUpValidator(field, ValidatorType.REQUIRED) != null;
	}
}
