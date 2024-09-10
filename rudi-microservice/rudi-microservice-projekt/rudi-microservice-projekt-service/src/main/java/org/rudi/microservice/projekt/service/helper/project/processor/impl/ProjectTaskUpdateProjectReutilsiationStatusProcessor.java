package org.rudi.microservice.projekt.service.helper.project.processor.impl;

import java.util.Set;

import org.rudi.common.facade.util.UtilPageable;
import org.rudi.microservice.projekt.core.bean.criteria.ReutilisationStatusSearchCriteria;
import org.rudi.microservice.projekt.service.helper.project.processor.AbstractProjectTaskUpdateProjectStampedEntityProcessor;
import org.rudi.microservice.projekt.storage.dao.reutilisationstatus.ReutilisationStatusCustomDao;
import org.rudi.microservice.projekt.storage.entity.ReutilisationStatusEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.REUTILISATION_STATUS_FIELD_NAME;

@Component
public class ProjectTaskUpdateProjectReutilsiationStatusProcessor extends AbstractProjectTaskUpdateProjectStampedEntityProcessor<ReutilisationStatusSearchCriteria, ReutilisationStatusEntity> {

	private final ReutilisationStatusCustomDao reutilisationStatusCustomDao;

	protected ProjectTaskUpdateProjectReutilsiationStatusProcessor( UtilPageable utilPageable, ReutilisationStatusCustomDao reutilisationStatusCustomDao) {
		super(REUTILISATION_STATUS_FIELD_NAME, utilPageable);
		this.reutilisationStatusCustomDao = reutilisationStatusCustomDao;
	}



	@Override
	protected Page<ReutilisationStatusEntity> getSearch(ReutilisationStatusSearchCriteria criteria, Pageable pageable) {
		return reutilisationStatusCustomDao.searchReutilisationStatus(criteria, pageable);
	}

	@Override
	protected void assignEntities(Set<ReutilisationStatusEntity> set, ProjectEntity projectEntity) {
		// Si aucune entité n'est passée, on ne supprime pas l'existante : champ obigatoire.
		set.stream().findFirst().ifPresent(projectEntity::setReutilisationStatus);
	}

	/**
	 * @param value
	 * @return
	 */
	@Override
	protected ReutilisationStatusSearchCriteria getCriteria(String value) {
		return ReutilisationStatusSearchCriteria.builder().active(true).code(value).build();
	}
}
