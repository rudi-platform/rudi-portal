package org.rudi.microservice.acl.storage.dao.role.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.collections4.CollectionUtils;
import org.rudi.common.storage.dao.AbstractCustomDaoImpl;
import org.rudi.microservice.acl.core.bean.RoleSearchCriteria;
import org.rudi.microservice.acl.storage.dao.role.RoleCustomDao;
import org.rudi.microservice.acl.storage.entity.role.RoleEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository custom pour les roles d'utilisateurs
 * 
 *
 */
@Repository
public class RoleCustomDaoImpl extends AbstractCustomDaoImpl<RoleEntity, RoleSearchCriteria> implements RoleCustomDao {

	// Champs utilisés pour le filtrage
	public static final String FIELD_CODE = "code";
	public static final String FIELD_LABEL = "label";
	public static final String FIELD_OPENING_DATE = "openingDate";
	public static final String FIELD_CLOSING_DATE = "closingDate";

	public RoleCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, RoleEntity.class);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public List<RoleEntity> searchRoles(RoleSearchCriteria searchCriteria) {

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<RoleEntity> searchQuery = builder.createQuery(RoleEntity.class);
		Root<RoleEntity> searchRoot = searchQuery.from(RoleEntity.class);
		buildQuery(searchCriteria, builder, searchQuery, searchRoot);

		searchQuery.select(searchRoot);
		searchQuery.orderBy(QueryUtils.toOrders(Sort.by("order", FIELD_LABEL), searchRoot, builder));

		TypedQuery<RoleEntity> typedQuery = entityManager.createQuery(searchQuery);
		return typedQuery.getResultList();
	}

	private void buildQuery(RoleSearchCriteria searchCriteria, CriteriaBuilder builder, CriteriaQuery<?> criteriaQuery,
			Root<RoleEntity> root) {

		if (searchCriteria != null) {
			List<Predicate> predicates = new ArrayList<>();

			// code
			predicateStringCriteria(searchCriteria.getCode(), FIELD_CODE, predicates, builder, root);

			// label
			predicateStringCriteria(searchCriteria.getLabel(), FIELD_LABEL, predicates, builder, root);

			// inactif
			if (Boolean.TRUE.equals(searchCriteria.getActive())) {
				final LocalDateTime d = LocalDateTime.now();
				predicates.add(builder.and(builder.lessThanOrEqualTo(root.get(FIELD_OPENING_DATE), d),
						builder.or(builder.greaterThanOrEqualTo(root.get(FIELD_CLOSING_DATE), d),
								builder.isNull(root.get(FIELD_CLOSING_DATE)))));
			}

			// Définition de la clause Where
			if (CollectionUtils.isNotEmpty(predicates)) {
				criteriaQuery.where(builder.and(predicates.toArray(new Predicate[predicates.size()])));
			}

		}
	}

}
