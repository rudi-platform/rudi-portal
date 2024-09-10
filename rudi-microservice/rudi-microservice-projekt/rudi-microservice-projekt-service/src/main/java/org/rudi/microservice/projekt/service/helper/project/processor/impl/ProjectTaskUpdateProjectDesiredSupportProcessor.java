package org.rudi.microservice.projekt.service.helper.project.processor.impl;

import java.util.Set;

import org.rudi.common.facade.util.UtilPageable;
import org.rudi.microservice.projekt.core.bean.criteria.SupportSearchCriteria;
import org.rudi.microservice.projekt.service.helper.project.processor.AbstractProjectTaskUpdateProjectStampedEntityProcessor;
import org.rudi.microservice.projekt.storage.dao.support.SupportCustomDao;
import org.rudi.microservice.projekt.storage.entity.SupportEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.DESIRED_SUPPORTS_FIELD_NAME;

@Component
public class ProjectTaskUpdateProjectDesiredSupportProcessor extends AbstractProjectTaskUpdateProjectStampedEntityProcessor<SupportSearchCriteria, SupportEntity> {

	private final SupportCustomDao supportCustomDao;

	public ProjectTaskUpdateProjectDesiredSupportProcessor( UtilPageable utilPageable, SupportCustomDao supportCustomDao) {
		super(DESIRED_SUPPORTS_FIELD_NAME, utilPageable);
		this.supportCustomDao = supportCustomDao;
	}

	/**
	 * @param criteria
	 * @param pageable
	 * @return
	 */
	@Override
	protected Page<SupportEntity> getSearch(SupportSearchCriteria criteria, Pageable pageable) {
		return supportCustomDao.searchSupports(criteria, pageable);
	}

	/**
	 * @param set
	 * @param projectEntity
	 */
	@Override
	protected void assignEntities(Set<SupportEntity> set, ProjectEntity projectEntity) {
		projectEntity.setDesiredSupports(set);
	}

	/**
	 * @param value
	 * @return
	 */
	@Override
	protected SupportSearchCriteria getCriteria(String value) {
		return SupportSearchCriteria.builder().active(true).code(value).build();
	}
}
