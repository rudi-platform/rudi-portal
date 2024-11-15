package org.rudi.microservice.strukture.storage.dao.provider.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.common.storage.dao.AbstractCustomDaoImpl;
import org.rudi.microservice.strukture.core.bean.criteria.LinkedProducerSearchCriteria;
import org.rudi.microservice.strukture.storage.dao.provider.LinkedProducerCustomDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LinkedProducerCustomDaoImpl extends AbstractCustomDaoImpl<LinkedProducerEntity, LinkedProducerSearchCriteria> implements LinkedProducerCustomDao {

	LinkedProducerCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, LinkedProducerEntity.class);
	}

	@Override
	@Transactional
	public Page<LinkedProducerEntity> searchLinkedProducers(LinkedProducerSearchCriteria criteria, Pageable pageable) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();

		// Requête pour compter le nombre de resultats total
		CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
		Root<LinkedProducerEntity> countRoot = countQuery.from(LinkedProducerEntity.class);
		buildQuery(criteria, builder, countQuery, countRoot);
		countQuery.select(builder.countDistinct(countRoot));
		Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

		// si aucun resultat
		if (totalCount == 0) {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}

		// Requête de recherche
		CriteriaQuery<LinkedProducerEntity> searchQuery = builder.createQuery(LinkedProducerEntity.class);
		Root<LinkedProducerEntity> searchRoot = searchQuery.from(LinkedProducerEntity.class);
		buildQuery(criteria, builder, searchQuery, searchRoot);

		searchQuery.select(searchRoot);
		searchQuery.orderBy(QueryUtils.toOrders(pageable.getSort(), searchRoot, builder));

		TypedQuery<LinkedProducerEntity> typedQuery = entityManager.createQuery(searchQuery);
		List<LinkedProducerEntity> providerEntities = typedQuery.setFirstResult((int) pageable.getOffset())
				.setMaxResults(pageable.getPageSize()).getResultList();
		return new PageImpl<>(providerEntities, pageable, totalCount.intValue());
	}

	private void buildQuery(LinkedProducerSearchCriteria criteria, CriteriaBuilder builder,
			CriteriaQuery<?> criteriaQuery, Root<LinkedProducerEntity> root) {
		if(criteria != null){
			List<Predicate> predicates = new ArrayList<>();

			if(criteria.getUuid() != null){
				predicates.add(builder.equal(root.get(LinkedProducerEntity.FIELD_UUID), criteria.getUuid()));
			}

			if(criteria.getProviderUuid() != null){
				Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
				Root<ProviderEntity> subRoot = subquery.from(ProviderEntity.class);
				Join<ProviderEntity, LinkedProducerEntity> joinLinkedProducer = subRoot.join(ProviderEntity.FIELD_LINKED_PRODUCERS);
				subquery.select(joinLinkedProducer.get(LinkedProducerEntity.FIELD_ID));
				subquery.where(builder.equal(subRoot.get(ProviderEntity.FIELD_UUID), criteria.getProviderUuid()));
				predicates.add(root.get(LinkedProducerEntity.FIELD_ID).in(subquery));
			}

			if(criteria.getOrganizationUuid() != null){
				Join<LinkedProducerEntity, OrganizationEntity> organizationJoin = root.join(LinkedProducerEntity.FIELD_ORGANIZATION);
				predicates.add(builder.equal(organizationJoin.get(OrganizationEntity.FIELD_UUID), criteria.getOrganizationUuid()));
			}


			// Définition de la clause Where
			if (CollectionUtils.isNotEmpty(predicates)) {
				criteriaQuery.where(builder.and(predicates.toArray(Predicate[]::new)));
			}
		}
	}
}
