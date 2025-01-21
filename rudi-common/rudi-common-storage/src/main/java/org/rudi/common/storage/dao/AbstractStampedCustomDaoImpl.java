package org.rudi.common.storage.dao;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.common.core.bean.criteria.AbstractStampedSearchCriteria;
import org.rudi.common.storage.entity.AbstractStampedEntity;


public class AbstractStampedCustomDaoImpl<E extends AbstractStampedEntity, C extends AbstractStampedSearchCriteria> extends AbstractCustomDaoImpl<E, C> {
	public static final String FIELD_CLOSING_DATE = "closingDate";
	public static final String FIELD_OPENING_DATE = "openingDate";
	public static final String FIELD_CODE = "code";
	public static final String FIELD_LABEL = "label";

	public AbstractStampedCustomDaoImpl(EntityManager entityManager, Class<E> entitiesClass) {
		super(entityManager, entitiesClass);
	}

	@Override
	protected void addPredicates(C searchCriteria, CriteriaBuilder builder, CriteriaQuery<?> criteriaQuery, Root<E> root, List<Predicate> predicates) {
		super.addPredicates(searchCriteria, builder, criteriaQuery, root, predicates);
		if (searchCriteria != null) {
			if (BooleanUtils.isTrue(searchCriteria.getActive())) {
				final LocalDateTime d = LocalDateTime.now(ZoneOffset.UTC);
				predicates.add(
					builder.and(
						builder.lessThanOrEqualTo(root.get(FIELD_OPENING_DATE), d),
						builder.or(
								builder.greaterThanOrEqualTo(root.get(FIELD_CLOSING_DATE), d),
								builder.isNull(root.get(FIELD_CLOSING_DATE))
						)
					)
				);
			}
			if (StringUtils.isNotEmpty(searchCriteria.getCode())) {
				predicates.add(builder.and(builder.equal(root.get(FIELD_CODE), searchCriteria.getCode())));
			}
			if (StringUtils.isNotEmpty(searchCriteria.getLabel())) {
				predicates.add(builder.and(builder.equal(root.get(FIELD_LABEL), searchCriteria.getLabel())));
			}
		}

	}
}
