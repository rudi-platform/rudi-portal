package org.rudi.facet.bpmn.datafactory;

import org.rudi.bpmn.core.bean.Field;
import org.rudi.bpmn.core.bean.FieldDefinition;
import org.springframework.stereotype.Component;

@Component
public class FieldDataFactory {

	public Field create(FieldDefinition definition) {
		return new Field().definition(definition);
	}

}
