package org.rudi.microservice.projekt.service.helper.project.processor.impl;


import java.time.LocalDateTime;

import org.rudi.microservice.projekt.service.helper.project.processor.AbstractProjectTastUpdateProjectStringFieldProcessor;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.EXPECTED_COMPLETION_END_DATE_FIELD_NAME;

@Component
public class ProjectTaskUpdateProjectExpectedCompletionEndDateProcessor  extends AbstractProjectTastUpdateProjectStringFieldProcessor {

	protected ProjectTaskUpdateProjectExpectedCompletionEndDateProcessor() {
		super(EXPECTED_COMPLETION_END_DATE_FIELD_NAME);
	}

	@Override
	protected void assignEntities(Object value, ProjectEntity projectEntity) {
		LocalDateTime date = value == null ? null : (LocalDateTime) value;
		projectEntity.setExpectedCompletionEndDate(date);
	}

}
