package org.rudi.facet.bpmn.validator.impl;

import org.rudi.bpmn.core.bean.Field;
import org.rudi.bpmn.core.bean.FieldType;
import org.rudi.bpmn.core.bean.ValidatorType;
import org.springframework.stereotype.Component;

@Component
public class EmailFieldValidator extends RegexpFieldValidator{

	@Override
	protected String getRegexp(org.rudi.bpmn.core.bean.Validator validator) {
		return "/^[a-zA-Z0-9.!#$%&'*+\\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/";
	}

	@Override
	public boolean accept(Field field) {
		return hasValidator (field) && field.getDefinition().getType() == FieldType.STRING
				&& lookUpValidator(field, ValidatorType.EMAIL) != null;
	}
}
