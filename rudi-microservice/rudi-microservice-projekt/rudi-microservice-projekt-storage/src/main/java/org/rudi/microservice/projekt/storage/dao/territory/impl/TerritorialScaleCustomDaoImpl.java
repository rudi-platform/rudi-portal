package org.rudi.microservice.projekt.storage.dao.territory.impl;

import jakarta.persistence.EntityManager;
import org.rudi.common.storage.dao.AbstractStampedCustomDaoImpl;
import org.rudi.microservice.projekt.core.bean.criteria.TerritorialScaleSearchCriteria;
import org.rudi.microservice.projekt.storage.dao.territory.TerritorialScaleCustomDao;
import org.rudi.microservice.projekt.storage.entity.TerritorialScaleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class TerritorialScaleCustomDaoImpl extends AbstractStampedCustomDaoImpl<TerritorialScaleEntity, TerritorialScaleSearchCriteria> implements TerritorialScaleCustomDao {

	public TerritorialScaleCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, TerritorialScaleEntity.class);
	}

	@Override
	public Page<TerritorialScaleEntity> searchTerritorialScales(TerritorialScaleSearchCriteria searchCriteria, Pageable pageable) {
		return search(searchCriteria, pageable);
	}
}
