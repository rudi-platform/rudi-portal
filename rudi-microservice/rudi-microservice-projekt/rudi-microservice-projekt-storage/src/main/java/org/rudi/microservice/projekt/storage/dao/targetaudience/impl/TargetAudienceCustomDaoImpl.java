package org.rudi.microservice.projekt.storage.dao.targetaudience.impl;

import javax.persistence.EntityManager;

import org.rudi.common.storage.dao.AbstractStampedCustomDaoImpl;
import org.rudi.microservice.projekt.core.bean.criteria.TargetAudienceSearchCriteria;
import org.rudi.microservice.projekt.storage.dao.targetaudience.TargetAudienceCustomDao;
import org.rudi.microservice.projekt.storage.entity.TargetAudienceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class TargetAudienceCustomDaoImpl extends AbstractStampedCustomDaoImpl<TargetAudienceEntity, TargetAudienceSearchCriteria> implements TargetAudienceCustomDao {
	public TargetAudienceCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, TargetAudienceEntity.class);
	}

	@Override
	public Page<TargetAudienceEntity> searchTargetAudiences(TargetAudienceSearchCriteria searchCriteria, Pageable pageable) {
		return search(searchCriteria, pageable);
	}
}
