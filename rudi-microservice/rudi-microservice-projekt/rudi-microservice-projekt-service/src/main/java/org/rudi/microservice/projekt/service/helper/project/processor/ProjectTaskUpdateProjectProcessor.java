package org.rudi.microservice.projekt.service.helper.project.processor;

import org.rudi.bpmn.core.bean.Field;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;

public interface ProjectTaskUpdateProjectProcessor {

	String getAcceptedField();

	default boolean accept(Field field){
		return field.getDefinition().getName().equals(getAcceptedField());
	}

	void process(Field field, ProjectEntity projectEntity);
}
