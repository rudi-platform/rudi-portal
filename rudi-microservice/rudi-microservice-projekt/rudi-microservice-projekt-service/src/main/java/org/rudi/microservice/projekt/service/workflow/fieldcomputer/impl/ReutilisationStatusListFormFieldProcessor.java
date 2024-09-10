package org.rudi.microservice.projekt.service.workflow.fieldcomputer.impl;

import org.rudi.microservice.projekt.core.bean.criteria.ReutilisationStatusSearchCriteria;
import org.rudi.microservice.projekt.service.workflow.fieldcomputer.AbstractListFormFieldProcessor;
import org.rudi.microservice.projekt.storage.dao.reutilisationstatus.ReutilisationStatusCustomDao;
import org.rudi.microservice.projekt.storage.entity.ReutilisationStatusEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.REUTILISATION_STATUS_FIELD_NAME;

@Component
public class ReutilisationStatusListFormFieldProcessor extends AbstractListFormFieldProcessor<ReutilisationStatusEntity, ReutilisationStatusSearchCriteria> {

	private final ReutilisationStatusCustomDao reutilisationStatusCustomDao;


	public ReutilisationStatusListFormFieldProcessor(ReutilisationStatusCustomDao reutilisationStatusCustomDao){
		super(REUTILISATION_STATUS_FIELD_NAME);
		this.reutilisationStatusCustomDao = reutilisationStatusCustomDao;
	}

	@Override
	public Page<ReutilisationStatusEntity> searchElements(ReutilisationStatusSearchCriteria criteria, Pageable pageable) {
		return reutilisationStatusCustomDao.searchReutilisationStatus(criteria, pageable);
	}

	/**
	 * @return
	 */
	@Override
	protected ReutilisationStatusSearchCriteria getCriteria() {
		return ReutilisationStatusSearchCriteria.builder().active(true).build();
	}
}
