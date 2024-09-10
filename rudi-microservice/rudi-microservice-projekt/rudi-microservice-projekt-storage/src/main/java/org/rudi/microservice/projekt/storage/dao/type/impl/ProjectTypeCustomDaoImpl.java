package org.rudi.microservice.projekt.storage.dao.type.impl;

import javax.persistence.EntityManager;

import org.rudi.common.storage.dao.AbstractStampedCustomDaoImpl;
import org.rudi.microservice.projekt.core.bean.criteria.ProjectTypeSearchCriteria;
import org.rudi.microservice.projekt.storage.dao.type.ProjectTypeCustomDao;
import org.rudi.microservice.projekt.storage.entity.ProjectTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectTypeCustomDaoImpl extends AbstractStampedCustomDaoImpl<ProjectTypeEntity, ProjectTypeSearchCriteria> implements ProjectTypeCustomDao {

	public ProjectTypeCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, ProjectTypeEntity.class);
	}

	@Override
	public Page<ProjectTypeEntity> searchProjectTypes(ProjectTypeSearchCriteria searchCriteria, Pageable pageable) {
		return search(searchCriteria, pageable);
	}
}
