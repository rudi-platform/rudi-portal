package org.rudi.microservice.projekt.service.workflow.fieldcomputer.impl;

import org.rudi.microservice.projekt.core.bean.criteria.ConfidentialitySearchCriteria;
import org.rudi.microservice.projekt.service.workflow.fieldcomputer.AbstractListFormFieldProcessor;
import org.rudi.microservice.projekt.storage.dao.confidentiality.ConfidentialityCustomDao;
import org.rudi.microservice.projekt.storage.entity.ConfidentialityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.CONFIDENTIALITY_FIELD_NAME;

@Component
public class ConfidentialityListFormFieldProcessor extends AbstractListFormFieldProcessor<ConfidentialityEntity, ConfidentialitySearchCriteria> {


	private final ConfidentialityCustomDao confidentialityCustomDao;

	public ConfidentialityListFormFieldProcessor(ConfidentialityCustomDao confidentialityCustomDao) {
		super(CONFIDENTIALITY_FIELD_NAME);
		this.confidentialityCustomDao = confidentialityCustomDao;
	}


	/**
	 * @param criteria
	 * @param pageable
	 * @return
	 */
	@Override
	protected Page<ConfidentialityEntity> searchElements(ConfidentialitySearchCriteria criteria, Pageable pageable) {
		return confidentialityCustomDao.searchConfidentialities(criteria, pageable);
	}

	/**
	 * @return
	 */
	@Override
	protected ConfidentialitySearchCriteria getCriteria() {
		return ConfidentialitySearchCriteria.builder().active(true).build();
	}
}
