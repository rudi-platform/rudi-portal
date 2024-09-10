package org.rudi.microservice.projekt.service.workflow.fieldcomputer;

import org.rudi.bpmn.core.bean.Field;

public interface ListFormFieldProcessor {

	boolean accept(Field field);

	void process(Field field);
}
