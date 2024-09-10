package org.rudi.microservice.projekt.service.workflow.fieldcomputer.impl;

import org.rudi.microservice.projekt.core.bean.criteria.TerritorialScaleSearchCriteria;
import org.rudi.microservice.projekt.service.workflow.fieldcomputer.AbstractListFormFieldProcessor;
import org.rudi.microservice.projekt.storage.dao.territory.TerritorialScaleCustomDao;
import org.rudi.microservice.projekt.storage.entity.TerritorialScaleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.TERRITORIAL_SCALE_FIELD_NAME;

@Component
public class TerritorialScaleListFormFieldProcessor extends AbstractListFormFieldProcessor<TerritorialScaleEntity, TerritorialScaleSearchCriteria> {


	private final TerritorialScaleCustomDao territorialScaleCustomDao;

	public TerritorialScaleListFormFieldProcessor(TerritorialScaleCustomDao territorialScaleCustomDao) {
		super(TERRITORIAL_SCALE_FIELD_NAME);
		this.territorialScaleCustomDao = territorialScaleCustomDao;
	}

	/**
	 * @param criteria
	 * @param pageable
	 * @return
	 */
	@Override
	public Page<TerritorialScaleEntity> searchElements(TerritorialScaleSearchCriteria criteria, Pageable pageable) {
		return territorialScaleCustomDao.searchTerritorialScales(criteria, pageable);
	}

	/**
	 * @return
	 */
	@Override
	protected TerritorialScaleSearchCriteria getCriteria() {
		return TerritorialScaleSearchCriteria.builder().active(true).build();
	}
}
