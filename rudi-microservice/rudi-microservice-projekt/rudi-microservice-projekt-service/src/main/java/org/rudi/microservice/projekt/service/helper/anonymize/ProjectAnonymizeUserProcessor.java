package org.rudi.microservice.projekt.service.helper.anonymize;

import java.util.UUID;

import org.rudi.facet.bpmn.helper.AbstractAnonymizeUserProcessor;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.microservice.projekt.core.bean.criteria.ProjectSearchCriteria;
import org.rudi.microservice.projekt.service.workflow.ProjectTaskServiceImpl;
import org.rudi.microservice.projekt.storage.dao.project.ProjectDao;
import org.rudi.microservice.projekt.storage.dao.project.impl.ProjectCustomDaoImpl;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ProjectAnonymizeUserProcessor extends AbstractAnonymizeUserProcessor<ProjectEntity, ProjectSearchCriteria, ProjectCustomDaoImpl, ProjectDao> {

	protected ProjectAnonymizeUserProcessor(ProjectCustomDaoImpl assetDescriptionCustomDao, ProjectDao assetDescriptionDao, BpmnHelper bpmnHelper) {
		super(assetDescriptionCustomDao, assetDescriptionDao, bpmnHelper);
	}

	@Override
	protected ProjectSearchCriteria createAssetDescriptionCriteria(String login) {
		return ProjectSearchCriteria.builder().initiator(login).build();
	}

	@Override
	protected Page<ProjectEntity> search(ProjectSearchCriteria criteria, Pageable pageable) {
		return getAssetDescriptionCustomDao().searchProjects(criteria, pageable);
	}

	@Override
	protected String getProcessDefinitionId() {
		return ProjectTaskServiceImpl.PROCESS_DEFINITION_ID;
	}

	@Override
	protected void anonymizeSpecificFields(ProjectEntity entity, String login, UUID userUuid) {
		if (login.equals(entity.getContactEmail())) {
			entity.setContactEmail(userUuid.toString());
		}
	}
}
