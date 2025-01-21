package org.rudi.microservice.template.storage.dao.domaina.impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.rudi.common.storage.dao.AbstractCustomDaoImpl;
import org.rudi.microservice.template.core.bean.TemplateSearchCriteria;
import org.rudi.microservice.template.storage.dao.domaina.TemplateCustomDao;
import org.rudi.microservice.template.storage.entity.domaina.TemplateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TemplateCustomDaoImpl extends AbstractCustomDaoImpl<TemplateEntity, TemplateSearchCriteria> implements TemplateCustomDao {

	public TemplateCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, TemplateEntity.class);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Page<TemplateEntity> searchTemplates(TemplateSearchCriteria searchCriteria, Pageable pageable) {
		return search(searchCriteria, pageable);
	}

	@Override
	protected void addPredicates(TemplateSearchCriteria searchCriteria, CriteriaBuilder builder, CriteriaQuery<?> criteriaQuery, Root<TemplateEntity> root, List<Predicate> predicates) {
		// TODO ajouter les critères du TemplateSearchCriteria via les méthodes predicate...Criteria(...)
	}

}
