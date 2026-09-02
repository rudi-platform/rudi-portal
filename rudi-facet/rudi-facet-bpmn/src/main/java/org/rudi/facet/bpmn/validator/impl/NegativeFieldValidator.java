package org.rudi.facet.bpmn.validator.impl;


import org.rudi.bpmn.core.bean.Field;
import org.rudi.bpmn.core.bean.ValidatorType;
import org.rudi.facet.bpmn.validator.AbstractNumericFieldValidator;
import org.springframework.stereotype.Component;

@Component
public class NegativeFieldValidator extends AbstractNumericFieldValidator {

	@Override
	protected boolean innerCheck(double d) {
		return d >= 0;
	}

	@Override
	protected boolean innerCheck(long d) {
		return d >= 0;
	}

	@Override
	protected boolean innerAccept(Field field) {
		return lookUpValidator(field, ValidatorType.NEGATIVE) != null;
	}
}
