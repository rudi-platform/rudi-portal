package org.rudi.microservice.projekt.storage.dao.support.impl;

import javax.persistence.EntityManager;

import org.rudi.common.storage.dao.AbstractStampedCustomDaoImpl;
import org.rudi.microservice.projekt.core.bean.criteria.SupportSearchCriteria;
import org.rudi.microservice.projekt.storage.dao.support.SupportCustomDao;
import org.rudi.microservice.projekt.storage.entity.SupportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class SupportCustomDaoImpl extends AbstractStampedCustomDaoImpl<SupportEntity, SupportSearchCriteria> implements SupportCustomDao {

	public SupportCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, SupportEntity.class);
	}

	@Override
	public Page<SupportEntity> searchSupports(SupportSearchCriteria searchCriteria, Pageable pageable) {
		return search(searchCriteria, pageable);
	}
}
