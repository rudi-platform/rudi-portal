package org.rudi.microservice.projekt.service.confidentiality.impl;

import org.rudi.microservice.projekt.core.bean.Confidentiality;
import org.rudi.microservice.projekt.core.bean.criteria.ConfidentialitySearchCriteria;
import org.rudi.microservice.projekt.service.mapper.ConfidentialityMapper;
import org.rudi.microservice.projekt.storage.dao.confidentiality.ConfidentialityCustomDao;
import org.rudi.microservice.projekt.storage.dao.confidentiality.ConfidentialityDao;
import org.rudi.microservice.projekt.storage.entity.ConfidentialityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConfidentialityHelper {

	public static final String CONFIDENTIAL_CONFIDENTIALITY_CODE = "CONFIDENTIAL";
	public static final String OPEN_CONFIDENTIALITY_CODE = "OPEN";
	public static final String DEFAULT_CONFIDENTIALITY_CODE = OPEN_CONFIDENTIALITY_CODE;

	private final ConfidentialityDao confidentialityDao;
	private final ConfidentialityCustomDao confidentialityCustomDao;
	private final ConfidentialityMapper confidentialityMapper;

	public ConfidentialityEntity getDefaultConfidentiality() {
		return confidentialityDao.findByCode(DEFAULT_CONFIDENTIALITY_CODE);
	}

	public Page<Confidentiality> searchConfidentialities(ConfidentialitySearchCriteria searchCriteria,
			Pageable pageable) {
		return confidentialityMapper
				.entitiesToDto(confidentialityCustomDao.searchConfidentialities(searchCriteria, pageable), pageable);
	}

}
