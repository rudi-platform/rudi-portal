package org.rudi.microservice.kalim.storage.dao.mimetype.impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.rudi.common.storage.dao.AbstractStampedCustomDaoImpl;
import org.rudi.microservice.kalim.core.bean.MimeTypeSearchCriteria;
import org.rudi.microservice.kalim.storage.dao.mimetype.MimeTypeCustomDao;
import org.rudi.microservice.kalim.storage.entity.mimetype.MimeTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;


@Repository
public class MimeTypeCustomDaoImpl extends AbstractStampedCustomDaoImpl<MimeTypeEntity, MimeTypeSearchCriteria> implements MimeTypeCustomDao {


	public MimeTypeCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, MimeTypeEntity.class);
	}

	/**
	 * @param criteria les critères de recherche
	 * @return une liste paginée de MimeTypeEntity
	 */
	@Override
	public Page<MimeTypeEntity> searchMimeTypes(MimeTypeSearchCriteria criteria, Pageable pageable) {
		return search(criteria, pageable);
	}

	/**
	 * @param searchCriteria les critères de recherche
	 * @param builder        le builder
	 * @param criteriaQuery  la requête
	 * @param root           la racine de la recherche
	 * @param predicates     la liste des prédicats
	 */
	@Override
	protected void addPredicates(MimeTypeSearchCriteria searchCriteria, CriteriaBuilder builder, CriteriaQuery<?> criteriaQuery, Root<MimeTypeEntity> root, List<Predicate> predicates) {
		super.addPredicates(searchCriteria, builder, criteriaQuery, root, predicates);

		predicateUuidCriteria(searchCriteria.getUuid(), MimeTypeEntity.FIELD_UUID, predicates, builder, root);
		predicateCollectionCriteria(searchCriteria.getCodes(), MimeTypeEntity.FIELD_CODE, predicates, builder, root);
	}
}