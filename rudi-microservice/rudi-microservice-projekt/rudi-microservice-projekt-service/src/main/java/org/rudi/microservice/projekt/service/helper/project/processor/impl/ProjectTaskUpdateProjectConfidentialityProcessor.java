package org.rudi.microservice.projekt.service.helper.project.processor.impl;

import java.util.Set;

import org.rudi.common.facade.util.UtilPageable;
import org.rudi.microservice.projekt.core.bean.criteria.ConfidentialitySearchCriteria;
import org.rudi.microservice.projekt.service.helper.project.processor.AbstractProjectTaskUpdateProjectStampedEntityProcessor;
import org.rudi.microservice.projekt.storage.dao.confidentiality.ConfidentialityCustomDao;
import org.rudi.microservice.projekt.storage.entity.ConfidentialityEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.CONFIDENTIALITY_FIELD_NAME;

@Component
public class ProjectTaskUpdateProjectConfidentialityProcessor extends AbstractProjectTaskUpdateProjectStampedEntityProcessor<ConfidentialitySearchCriteria, ConfidentialityEntity> {

	private final ConfidentialityCustomDao confidentialityCustomDao;

	protected ProjectTaskUpdateProjectConfidentialityProcessor(UtilPageable utilPageable, ConfidentialityCustomDao confidentialityCustomDao) {
		super(CONFIDENTIALITY_FIELD_NAME, utilPageable);
		this.confidentialityCustomDao = confidentialityCustomDao;
	}

	/**
	 * @return
	 */
	@Override
	public String getAcceptedField() {
		return CONFIDENTIALITY_FIELD_NAME;
	}

	/**
	 * @param criteria
	 * @param pageable
	 * @return
	 */
	@Override
	protected Page<ConfidentialityEntity> getSearch(ConfidentialitySearchCriteria criteria, Pageable pageable) {
		return confidentialityCustomDao.searchConfidentialities(criteria, pageable);
	}

	/**
	 * @param set
	 * @param projectEntity
	 */
	@Override
	protected void assignEntities(Set<ConfidentialityEntity> set, ProjectEntity projectEntity) {
		// Si aucune entité n'est passée, on ne supprime pas l'existante : champ obigatoire.
		set.stream().findFirst().ifPresent(projectEntity::setConfidentiality);
	}

	/**
	 * @param value
	 * @return
	 */
	@Override
	protected ConfidentialitySearchCriteria getCriteria(String value) {
		return ConfidentialitySearchCriteria.builder().active(true).code(value).build();
	}
}
