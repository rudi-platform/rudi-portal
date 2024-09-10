package org.rudi.microservice.projekt.service.helper.project.processor.impl;

import java.util.Set;

import org.rudi.common.facade.util.UtilPageable;
import org.rudi.microservice.projekt.core.bean.criteria.TargetAudienceSearchCriteria;
import org.rudi.microservice.projekt.service.helper.project.processor.AbstractProjectTaskUpdateProjectStampedEntityProcessor;
import org.rudi.microservice.projekt.storage.dao.targetaudience.TargetAudienceCustomDao;
import org.rudi.microservice.projekt.storage.entity.TargetAudienceEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.TARGET_AUDIENCES_FIELD_NAME;

@Component
public class ProjectTaskUpdateProjectTargetAudienceProcessor extends AbstractProjectTaskUpdateProjectStampedEntityProcessor<TargetAudienceSearchCriteria, TargetAudienceEntity> {

	private final TargetAudienceCustomDao targetAudienceCustomDao;

	protected ProjectTaskUpdateProjectTargetAudienceProcessor(UtilPageable utilPageable, TargetAudienceCustomDao targetAudienceCustomDao) {
		super(TARGET_AUDIENCES_FIELD_NAME, utilPageable);
		this.targetAudienceCustomDao = targetAudienceCustomDao;
	}

	@Override
	protected Page<TargetAudienceEntity> getSearch(TargetAudienceSearchCriteria criteria, Pageable pageable) {
		return targetAudienceCustomDao.searchTargetAudiences(criteria, pageable);
	}

	@Override
	protected void assignEntities(Set<TargetAudienceEntity> set, ProjectEntity projectEntity) {
		projectEntity.setTargetAudiences(set);
	}

	/**
	 * @param value
	 * @return
	 */
	@Override
	protected TargetAudienceSearchCriteria getCriteria(String value) {
		return TargetAudienceSearchCriteria.builder().active(true).code(value).build();
	}
}
