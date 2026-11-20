package org.rudi.facet.bpmn.datafactory;

import org.rudi.bpmn.core.bean.FieldDefinition;
import org.springframework.stereotype.Component;

@Component
public class FieldDefinitionDataFactory {

	public FieldDefinition create(String name) {
		return new FieldDefinition().name(name);
	}

}
