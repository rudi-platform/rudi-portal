package org.rudi.microservice.projekt.service.helper.project.processor.impl;

import org.rudi.microservice.projekt.service.helper.project.processor.AbstractProjectTastUpdateProjectStringFieldProcessor;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.TITLE_FIELD_NAME;

@Component
public class ProjectTaskUpdateProjectTitleProcessor  extends AbstractProjectTastUpdateProjectStringFieldProcessor {

	protected ProjectTaskUpdateProjectTitleProcessor() {
		super(TITLE_FIELD_NAME);
	}

	@Override
	protected void assignEntities(Object value, ProjectEntity projectEntity) {
		// Si aucune value n'est passée, on ne supprime pas l'existante : champ obigatoire.
		if(value != null){
			projectEntity.setTitle(value.toString());
		}
	}

}
