package org.rudi.microservice.projekt.service.helper.project.processor.impl;

import java.util.Set;

import org.rudi.common.facade.util.UtilPageable;
import org.rudi.microservice.projekt.core.bean.criteria.ProjectTypeSearchCriteria;
import org.rudi.microservice.projekt.service.helper.project.processor.AbstractProjectTaskUpdateProjectStampedEntityProcessor;
import org.rudi.microservice.projekt.storage.dao.type.ProjectTypeCustomDao;
import org.rudi.microservice.projekt.storage.entity.ProjectTypeEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.TYPE_FIELD_NAME;

@Component
public class ProjectTaskUpdateProjectTypeProcessor extends AbstractProjectTaskUpdateProjectStampedEntityProcessor<ProjectTypeSearchCriteria, ProjectTypeEntity> {

	private final ProjectTypeCustomDao projectTypeCustomDao;


	protected ProjectTaskUpdateProjectTypeProcessor(UtilPageable utilPageable, ProjectTypeCustomDao projectTypeCustomDao) {
		super(TYPE_FIELD_NAME, utilPageable);
		this.projectTypeCustomDao = projectTypeCustomDao;
	}


	@Override
	protected Page<ProjectTypeEntity> getSearch(ProjectTypeSearchCriteria criteria, Pageable pageable) {
		return projectTypeCustomDao.searchProjectTypes(criteria, pageable);
	}

	@Override
	protected void assignEntities(Set<ProjectTypeEntity> set, ProjectEntity projectEntity) {
		// Si aucune entité n'est passée, on ne supprime pas l'existante : champ obigatoire.
		set.stream().findFirst().ifPresent(projectEntity::setType);
	}

	/**
	 * @param value
	 * @return
	 */
	@Override
	protected ProjectTypeSearchCriteria getCriteria(String value) {
		return ProjectTypeSearchCriteria.builder().active(true).code(value).build();
	}
}
