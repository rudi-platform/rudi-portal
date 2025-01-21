/**
 * RUDI
 */
package org.rudi.microservice.acl.storage.dao.token.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.rudi.common.storage.dao.AbstractCustomDaoImpl;
import org.rudi.microservice.acl.core.bean.TokenSearchCritera;
import org.rudi.microservice.acl.storage.dao.token.TokenCustomDao;
import org.rudi.microservice.acl.storage.entity.token.TokenEntity;
import org.rudi.microservice.acl.storage.entity.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class TokenCustomDaoImpl extends AbstractCustomDaoImpl<TokenEntity, TokenSearchCritera>
		implements TokenCustomDao {

	public TokenCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, TokenEntity.class);
	}

	@Override
	public Page<TokenEntity> searchTokens(TokenSearchCritera searchCriteria, Pageable pageable) {
		return search(searchCriteria, pageable);
	}

	@Override
	protected void addPredicates(TokenSearchCritera searchCriteria, CriteriaBuilder builder,
			CriteriaQuery<?> criteriaQuery, Root<TokenEntity> root, List<Predicate> predicates) {
		predicateEnumCollectionCriteria(searchCriteria.getTypes(), "type", predicates, builder, root);
		if (searchCriteria.getUserId() != null) {
			Join<TokenEntity, UserEntity> joinUser = root.join("user", JoinType.INNER);
			predicates.add(builder.equal(joinUser.get("id"), searchCriteria.getUserId()));
		}
		if (StringUtils.isNotEmpty(searchCriteria.getToken())) {
			predicates.add(builder.equal(root.get("value"), searchCriteria.getToken()));
		}
	}
}
