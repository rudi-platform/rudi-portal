package org.rudi.microservice.projekt.storage.dao.confidentiality.impl;

import jakarta.persistence.EntityManager;
import org.rudi.common.storage.dao.AbstractStampedCustomDaoImpl;
import org.rudi.common.storage.dao.PredicateListBuilder;
import org.rudi.microservice.projekt.core.bean.criteria.ConfidentialitySearchCriteria;
import org.rudi.microservice.projekt.storage.dao.confidentiality.ConfidentialityCustomDao;
import org.rudi.microservice.projekt.storage.entity.ConfidentialityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import lombok.val;

@Repository
public class ConfidentialityCustomDaoImpl extends AbstractStampedCustomDaoImpl<ConfidentialityEntity, ConfidentialitySearchCriteria> implements ConfidentialityCustomDao {

	public ConfidentialityCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, ConfidentialityEntity.class);
	}

	@Override
	public Page<ConfidentialityEntity> searchConfidentialities(ConfidentialitySearchCriteria searchCriteria, Pageable pageable) {
		return search(searchCriteria, pageable);
	}

	@Override
	protected void addPredicates(PredicateListBuilder<ConfidentialityEntity, ConfidentialitySearchCriteria> builder) {
		val searchCriteria = builder.getSearchCriteria();
		builder.add(searchCriteria.getIsPrivate(),
				(confidentiality, keywords) -> confidentiality.get(ConfidentialityEntity.FIELD_CONFIDENTIALITY_ISPRIVATE).in(keywords));
	}


}
