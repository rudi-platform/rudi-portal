package org.rudi.microservice.projekt.service.workflow.fieldcomputer.impl;

import org.rudi.microservice.projekt.core.bean.criteria.TargetAudienceSearchCriteria;
import org.rudi.microservice.projekt.service.workflow.fieldcomputer.AbstractListFormFieldProcessor;
import org.rudi.microservice.projekt.storage.dao.targetaudience.TargetAudienceCustomDao;
import org.rudi.microservice.projekt.storage.entity.TargetAudienceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.TARGET_AUDIENCES_FIELD_NAME;

@Component
public class TargetAudienceListFormFieldProcessor extends AbstractListFormFieldProcessor<TargetAudienceEntity, TargetAudienceSearchCriteria> {

	private final TargetAudienceCustomDao targetAudienceCustomDao;

	public TargetAudienceListFormFieldProcessor(TargetAudienceCustomDao targetAudienceCustomDao) {
		super(TARGET_AUDIENCES_FIELD_NAME);
		this.targetAudienceCustomDao = targetAudienceCustomDao;
	}

	@Override
	public Page<TargetAudienceEntity> searchElements(TargetAudienceSearchCriteria criteria, Pageable pageable) {
		return targetAudienceCustomDao.searchTargetAudiences(criteria, pageable);
	}

	/**
	 * @return
	 */
	@Override
	protected TargetAudienceSearchCriteria getCriteria() {
		return TargetAudienceSearchCriteria.builder().active(true).build();
	}
}
