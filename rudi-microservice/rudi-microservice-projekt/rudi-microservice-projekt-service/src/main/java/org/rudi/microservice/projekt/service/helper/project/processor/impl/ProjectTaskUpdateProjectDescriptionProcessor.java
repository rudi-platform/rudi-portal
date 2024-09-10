package org.rudi.microservice.projekt.service.helper.project.processor.impl;

import org.rudi.microservice.projekt.service.helper.project.processor.AbstractProjectTastUpdateProjectStringFieldProcessor;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.DESCRIPTION_FIELD_NAME;

@Component
public class ProjectTaskUpdateProjectDescriptionProcessor extends AbstractProjectTastUpdateProjectStringFieldProcessor {

	protected ProjectTaskUpdateProjectDescriptionProcessor() {
		super(DESCRIPTION_FIELD_NAME);
	}

	/**
	 * @param value
	 * @param projectEntity
	 */
	@Override
	protected void assignEntities(Object value, ProjectEntity projectEntity) {
		// Si aucune value n'est passée, on ne supprime pas l'existante : champ obigatoire.
		if(value != null){
			projectEntity.setDescription(value.toString());
		}
	}
}
