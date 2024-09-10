package org.rudi.microservice.projekt.service.workflow.fieldcomputer.impl;

import org.rudi.microservice.projekt.core.bean.criteria.ProjectTypeSearchCriteria;
import org.rudi.microservice.projekt.service.workflow.fieldcomputer.AbstractListFormFieldProcessor;
import org.rudi.microservice.projekt.storage.dao.type.ProjectTypeCustomDao;
import org.rudi.microservice.projekt.storage.entity.ProjectTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.TYPE_FIELD_NAME;

@Component
public class ProjectTypeListFormFieldProcessor extends AbstractListFormFieldProcessor<ProjectTypeEntity, ProjectTypeSearchCriteria> {

	private final ProjectTypeCustomDao projectTypeCustomDao;

	public ProjectTypeListFormFieldProcessor(ProjectTypeCustomDao projectTypeCustomDao){
		super(TYPE_FIELD_NAME);
		this.projectTypeCustomDao = projectTypeCustomDao;
	}

	@Override
	public Page<ProjectTypeEntity> searchElements(ProjectTypeSearchCriteria criteria, Pageable pageable) {
		return projectTypeCustomDao.searchProjectTypes(criteria, pageable);
	}

	/**
	 * @return
	 */
	@Override
	protected ProjectTypeSearchCriteria getCriteria() {
		return ProjectTypeSearchCriteria.builder().active(true).build();
	}
}
