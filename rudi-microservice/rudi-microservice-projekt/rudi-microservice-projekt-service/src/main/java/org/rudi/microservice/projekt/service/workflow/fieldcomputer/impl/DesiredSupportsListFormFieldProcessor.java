package org.rudi.microservice.projekt.service.workflow.fieldcomputer.impl;

import org.rudi.microservice.projekt.core.bean.criteria.SupportSearchCriteria;
import org.rudi.microservice.projekt.service.workflow.fieldcomputer.AbstractListFormFieldProcessor;
import org.rudi.microservice.projekt.storage.dao.support.SupportCustomDao;
import org.rudi.microservice.projekt.storage.entity.SupportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.DESIRED_SUPPORTS_FIELD_NAME;

@Component
public class DesiredSupportsListFormFieldProcessor extends AbstractListFormFieldProcessor<SupportEntity, SupportSearchCriteria> {

	private final SupportCustomDao supportCustomDao;

	public DesiredSupportsListFormFieldProcessor(SupportCustomDao supportCustomDao){
		super(DESIRED_SUPPORTS_FIELD_NAME);
		this.supportCustomDao = supportCustomDao;
	}

	@Override
	public Page<SupportEntity> searchElements(SupportSearchCriteria criteria, Pageable pageable) {
		return supportCustomDao.searchSupports(criteria, pageable);
	}

	/**
	 * @return
	 */
	@Override
	protected SupportSearchCriteria getCriteria() {
		return SupportSearchCriteria.builder().active(true).build();
	}
}
