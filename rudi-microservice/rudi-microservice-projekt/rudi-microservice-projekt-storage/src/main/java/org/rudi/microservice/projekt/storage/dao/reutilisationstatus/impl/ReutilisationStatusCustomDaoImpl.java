package org.rudi.microservice.projekt.storage.dao.reutilisationstatus.impl;

import jakarta.persistence.EntityManager;
import org.rudi.common.storage.dao.AbstractStampedCustomDaoImpl;
import org.rudi.microservice.projekt.core.bean.criteria.ReutilisationStatusSearchCriteria;
import org.rudi.microservice.projekt.storage.dao.reutilisationstatus.ReutilisationStatusCustomDao;
import org.rudi.microservice.projekt.storage.entity.ReutilisationStatusEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class ReutilisationStatusCustomDaoImpl extends AbstractStampedCustomDaoImpl<ReutilisationStatusEntity, ReutilisationStatusSearchCriteria> implements ReutilisationStatusCustomDao {

	public ReutilisationStatusCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, ReutilisationStatusEntity.class);
	}

	@Override
	public Page<ReutilisationStatusEntity> searchReutilisationStatus(ReutilisationStatusSearchCriteria criteria, Pageable pageable) {
		return search(criteria, pageable);
	}
}
