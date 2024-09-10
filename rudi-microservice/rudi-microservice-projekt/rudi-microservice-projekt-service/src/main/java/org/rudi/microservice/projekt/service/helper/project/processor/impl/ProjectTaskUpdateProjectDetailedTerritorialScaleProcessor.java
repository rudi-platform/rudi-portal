package org.rudi.microservice.projekt.service.helper.project.processor.impl;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.DETAILED_TERRITORIAL_SCALE_FIELD_NAME;

import org.apache.commons.lang3.StringUtils;
import org.rudi.microservice.projekt.service.helper.project.processor.AbstractProjectTastUpdateProjectStringFieldProcessor;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.stereotype.Component;

@Component

public class ProjectTaskUpdateProjectDetailedTerritorialScaleProcessor
		extends AbstractProjectTastUpdateProjectStringFieldProcessor {

	protected ProjectTaskUpdateProjectDetailedTerritorialScaleProcessor() {
		super(DETAILED_TERRITORIAL_SCALE_FIELD_NAME);
	}

	@Override
	protected void assignEntities(Object value, ProjectEntity projectEntity) {
		if (value != null) {
			projectEntity.setDetailedTerritorialScale(value.toString());
		} else {
			projectEntity.setDetailedTerritorialScale(StringUtils.EMPTY);
		}

	}

}
