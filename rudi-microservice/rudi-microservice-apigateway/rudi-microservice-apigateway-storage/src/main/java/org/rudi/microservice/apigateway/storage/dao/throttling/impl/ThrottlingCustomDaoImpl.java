/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.storage.dao.throttling.impl;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.common.storage.dao.AbstractCustomDaoImpl;
import org.rudi.microservice.apigateway.core.bean.ThrottlingSearchCriteria;
import org.rudi.microservice.apigateway.storage.dao.throttling.ThrottlingCustomDao;
import org.rudi.microservice.apigateway.storage.entity.throttling.ThrottlingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * @author FNI18300
 *
 */
@Repository
public class ThrottlingCustomDaoImpl extends AbstractCustomDaoImpl<ThrottlingEntity, ThrottlingSearchCriteria>
		implements ThrottlingCustomDao {

	public static final String FIELD_CODE = "code";
	public static final String FIELD_OPENING_DATE = "openingDate";
	public static final String FIELD_CLOSING_DATE = "closingDate";

	public ThrottlingCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, ThrottlingEntity.class);
	}

	@Override
	public Page<ThrottlingEntity> searchThrottlings(ThrottlingSearchCriteria searchCriteria, Pageable pageable) {
		return search(searchCriteria, pageable);
	}

	@Override
	protected void addPredicates(ThrottlingSearchCriteria searchCriteria, CriteriaBuilder builder,
			CriteriaQuery<?> criteriaQuery, Root<ThrottlingEntity> root, List<Predicate> predicates) {

		if (searchCriteria != null) {

			if (BooleanUtils.isTrue(searchCriteria.getActive())) {
				final LocalDateTime actualDate = LocalDateTime.now();
				predicateDateCriteriaLessThan(actualDate, FIELD_OPENING_DATE, predicates, builder, root);
				predicateDateCriteriaGreaterThan(actualDate, FIELD_CLOSING_DATE, predicates, builder, root);
			}

			if (StringUtils.isNotEmpty(searchCriteria.getCode())) {
				predicateStringCriteria(searchCriteria.getCode(), FIELD_CODE, predicates, builder, root);
			}

		}
	}
}
