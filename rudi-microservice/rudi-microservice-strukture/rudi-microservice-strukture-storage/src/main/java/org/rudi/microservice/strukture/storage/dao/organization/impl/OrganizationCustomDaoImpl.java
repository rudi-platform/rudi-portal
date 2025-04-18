package org.rudi.microservice.strukture.storage.dao.organization.impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.rudi.common.storage.dao.AbstractCustomDaoImpl;
import org.rudi.common.storage.dao.PredicateListBuilder;
import org.rudi.microservice.strukture.core.bean.OrganizationSearchCriteria;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationCustomDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class OrganizationCustomDaoImpl extends AbstractCustomDaoImpl<OrganizationEntity, OrganizationSearchCriteria> implements OrganizationCustomDao {

	public OrganizationCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, OrganizationEntity.class);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Page<OrganizationEntity> searchOrganizations(OrganizationSearchCriteria searchOrganizationCriteria, Pageable pageable) {
		return search(searchOrganizationCriteria, pageable);
	}

	@Override
	protected void addPredicates(OrganizationSearchCriteria searchCriteria, CriteriaBuilder builder, CriteriaQuery<?> criteriaQuery, Root<OrganizationEntity> root, List<Predicate> predicates) {
		predicateStringCriteria(searchCriteria.getUuid(), "uuid", predicates, builder, root);
		predicateStringCriteria(searchCriteria.getName(), "name", predicates, builder, root);
	}

	@Override
	protected void addPredicates(PredicateListBuilder<OrganizationEntity, OrganizationSearchCriteria> builder) {
		final var searchCriteria = builder.getSearchCriteria();
		builder.addIsNotNull(searchCriteria.getActive(), organization -> organization.get("closing_date"));
		builder.add(searchCriteria.getUserUuid(), (organization, userUuid) -> organization.join("members").get("userUuid").in(userUuid));
		builder.add(searchCriteria.getOrganizationStatus(), OrganizationStatus::valueOf, (organization, status) -> organization.get(OrganizationEntity.FIELD_ORGANIZATION_STATUS).in(status));
	}


}
