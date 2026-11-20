package org.rudi.facet.bpmn.validator.impl;

import org.rudi.bpmn.core.bean.Field;
import org.rudi.bpmn.core.bean.FieldType;
import org.rudi.bpmn.core.bean.ValidatorType;
import org.rudi.common.facade.config.filter.CommonConstants;
import org.springframework.stereotype.Component;

@Component
public class EmailFieldValidator extends RegexpFieldValidator{

	@Override
	protected String getRegexp(org.rudi.bpmn.core.bean.Validator validator) {
		return CommonConstants.EMAIL_REGEX;
	}

	@Override
	public boolean accept(Field field) {
		return hasValidator (field) && field.getDefinition().getType() == FieldType.STRING
				&& lookUpValidator(field, ValidatorType.EMAIL) != null;
	}
}
