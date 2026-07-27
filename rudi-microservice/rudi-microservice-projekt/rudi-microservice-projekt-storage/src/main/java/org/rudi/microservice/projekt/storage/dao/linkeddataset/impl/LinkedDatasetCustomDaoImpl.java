package org.rudi.microservice.projekt.storage.dao.linkeddataset.impl;

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity.DATASET_CONFIDENTIALITY_FIELD;
import static org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity.DATASET_ORGANIZATION_UUID_FIELD;
import static org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity.DATASET_UUID_FIELD;
import static org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity.END_DATE_FIELD;
import static org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity.STATUS_FIELD;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.rudi.common.storage.dao.AbstractCustomDaoImpl;
import org.rudi.common.storage.dao.PredicateListBuilder;
import org.rudi.microservice.projekt.core.bean.LinkedDatasetSearchCriteria;
import org.rudi.microservice.projekt.storage.dao.linkeddataset.LinkedDatasetCustomDao;
import org.rudi.microservice.projekt.storage.entity.DatasetConfidentiality;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetStatus;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.rudi.microservice.projekt.storage.entity.relatedorganization.RelatedOrganizationEntity;
import org.rudi.microservice.projekt.storage.entity.relatedorganization.RelationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Repository
public class LinkedDatasetCustomDaoImpl extends AbstractCustomDaoImpl<LinkedDatasetEntity, LinkedDatasetSearchCriteria>
		implements LinkedDatasetCustomDao {

	public LinkedDatasetCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, LinkedDatasetEntity.class);
	}

	@Override
	public Page<LinkedDatasetEntity> searchLinkedDatasets(LinkedDatasetSearchCriteria searchCriteria,
			Pageable pageable) {
		return search(searchCriteria, pageable);
	}

	@Override
	public Page<LinkedDatasetEntity> searchRelatedDatasetAccess(LinkedDatasetSearchCriteria searchCriteria,
			Pageable pageable) {
		if (searchCriteria == null) {
			return emptyPage(pageable);
		}

		final Long totalCount = getTotalLinkedDatasetRelatedToAuthenticatedUser(searchCriteria);
		if (totalCount == 0) {
			return emptyPage(pageable);
		}

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<LinkedDatasetEntity> searchQuery = builder.createQuery(entitiesClass);
		Root<LinkedDatasetEntity> searchRoot = searchQuery.from(entitiesClass);
		addWhereSearchOwnerOrRelatedLinkedDatasets(builder, searchQuery, searchRoot, searchCriteria);
		searchQuery.select(searchRoot).distinct(true)
				.orderBy(QueryUtils.toOrders(pageable.getSort(), searchRoot, builder));

		TypedQuery<LinkedDatasetEntity> typedQuery = entityManager.createQuery(searchQuery);
		if (pageable.isPaged()) {
			typedQuery.setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize());
		}

		List<LinkedDatasetEntity> linkedDatasetEntities = typedQuery.getResultList();
		return new PageImpl<>(linkedDatasetEntities, pageable, totalCount.intValue());
	}

	private void addWhereSearchOwnerOrRelatedLinkedDatasets(CriteriaBuilder builder, CriteriaQuery<?> searchQuery,
			Root<LinkedDatasetEntity> searchRoot, LinkedDatasetSearchCriteria searchCriteria) {
		List<Predicate> predicates = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(searchCriteria.getProjectRelatedUuids())) {

			// Cas 1 : linked datasets rattachés à des projets dont l'utilisateur (ou l'une de ses organisations) est owner.
			Subquery<Long> projectOwnerSubquery = searchQuery.subquery(Long.class);
			Root<ProjectEntity> projectOwnerRoot = projectOwnerSubquery.from(ProjectEntity.class);
			Join<ProjectEntity, LinkedDatasetEntity> linkedDatasetsOwnerJoin = projectOwnerRoot
					.join(ProjectEntity.FIELD_LINKED_DATASET, JoinType.INNER);
			projectOwnerSubquery.select(linkedDatasetsOwnerJoin.get(LinkedDatasetEntity.FIELD_ID));
			projectOwnerSubquery.where(
					projectOwnerRoot.get(ProjectEntity.FIELD_OWNER_UUID).in(searchCriteria.getProjectRelatedUuids()));
			Predicate projectOwnerPredicate = searchRoot.get(LinkedDatasetEntity.FIELD_ID).in(projectOwnerSubquery);

			// Cas 2 : linked datasets rattachés à des projets où l'utilisateur (ou une de ses organisations)
			// est présent dans les organisations liées au projet.
			Predicate projectRelatedPredicate = computeProjectRelatedSubquery(builder, searchQuery, searchRoot,
					searchCriteria);

			// Règle d'accès globale : owner OU [organisation liée ET status recherché].
			predicates.add(builder.or(projectOwnerPredicate, projectRelatedPredicate));

		}

		// Ajout des autres filtres de search (statut, endDate, datasetUuid, confidentialité, etc.).
		addPredicates(searchCriteria, builder, searchQuery, searchRoot, predicates);
		final PredicateListBuilder<LinkedDatasetEntity, LinkedDatasetSearchCriteria> predicateListBuilder = new PredicateListBuilder<>(
				searchCriteria, predicates, builder, searchRoot);

		addPredicates(predicateListBuilder);

		// Le WHERE final applique un ET entre la règle d'accès et les filtres fonctionnels éventuels.
		if (CollectionUtils.isNotEmpty(predicates)) {
			searchQuery.where(builder.and(predicates.toArray(new Predicate[0])));
		}

	}

	private Predicate computeProjectRelatedSubquery(CriteriaBuilder builder, CriteriaQuery<?> searchQuery,
			Root<LinkedDatasetEntity> searchRoot, LinkedDatasetSearchCriteria searchCriteria) {

		List<Predicate> predicates = new ArrayList<>();
		Subquery<Long> projectRelatedSubquery = searchQuery.subquery(Long.class);
		Root<ProjectEntity> projectRelatedRoot = projectRelatedSubquery.from(ProjectEntity.class);
		Join<ProjectEntity, LinkedDatasetEntity> linkedDatasetsRelatedJoin = projectRelatedRoot
				.join(ProjectEntity.FIELD_LINKED_DATASET, JoinType.INNER);
		Join<ProjectEntity, ?> relatedOrganizationsJoin = projectRelatedRoot
				.join(ProjectEntity.FIELD_RELATED_ORGANIZATION, JoinType.INNER);

		if (CollectionUtils.isNotEmpty(searchCriteria.getProjectRelatedUuids())) {
			// predicat de selection sur les uuid d'organisation (ou user)
			predicates.add(relatedOrganizationsJoin.get(RelatedOrganizationEntity.FIELD_ORGANIZATION_UUID)
					.in(searchCriteria.getProjectRelatedUuids()));
		}

		if (CollectionUtils.isNotEmpty(searchCriteria.getOrganizationProjectRelationStatuses())) {
			List<RelationStatus> statusList = searchCriteria.getOrganizationProjectRelationStatuses().stream()
					.map(status -> RelationStatus.valueOf(status.name())).collect(Collectors.toList());
			// predicat de selection sur les status de relation
			predicates
					.add(relatedOrganizationsJoin.get(RelatedOrganizationEntity.FIELD_RELATION_STATUS).in(statusList));
		}

		projectRelatedSubquery.select(linkedDatasetsRelatedJoin.get(LinkedDatasetEntity.FIELD_ID));
		projectRelatedSubquery.where(builder.and(predicates.toArray(Predicate[]::new)));
		return searchRoot.get(LinkedDatasetEntity.FIELD_ID).in(projectRelatedSubquery);
	}

	@Override
	protected void addPredicates(LinkedDatasetSearchCriteria searchCriteria, CriteriaBuilder builder,
			CriteriaQuery<?> criteriaQuery, Root<LinkedDatasetEntity> root, List<Predicate> predicates) {
		predicateUuidCriteria(searchCriteria.getDatasetUuid(), DATASET_UUID_FIELD, predicates, builder, root);
		// Les demandes non expirées
		if (isTrue(searchCriteria.getEndDateIsNotOver())) {
			final var endDateIsNotOver = builder.greaterThan(root.get(END_DATE_FIELD), LocalDateTime.now());
			predicates.add(endDateIsNotOver);
		}
		// Les demandes expirées
		if (isTrue(searchCriteria.getEndDateIsOver())) {
			final var endDateIsOver = builder.lessThan(root.get(END_DATE_FIELD), LocalDateTime.now());
			predicates.add(endDateIsOver);
		}
		// Les demandes n'ayant pas de endDate
		if (isTrue(searchCriteria.getEndDateIsNull())) {
			final var endDateIsNull = root.get(END_DATE_FIELD).isNull();
			predicates.add(endDateIsNull);
		}

		if (CollectionUtils.isNotEmpty(searchCriteria.getProjectOwnerUuids())) {
			Subquery<Long> subqueryProjectOwner = criteriaQuery.subquery(Long.class);
			Root<ProjectEntity> subRoot = subqueryProjectOwner.from(ProjectEntity.class);
			Join<ProjectEntity, LinkedDatasetEntity> joinDatasetRequest = subRoot
					.join(ProjectEntity.FIELD_LINKED_DATASET, JoinType.INNER);
			subqueryProjectOwner.select(joinDatasetRequest.get(LinkedDatasetEntity.FIELD_ID));
			subqueryProjectOwner
					.where(subRoot.get(ProjectEntity.FIELD_OWNER_UUID).in(searchCriteria.getProjectOwnerUuids()));
			predicates.add(root.get(ProjectEntity.FIELD_ID).in(subqueryProjectOwner));
		}

		if (CollectionUtils.isNotEmpty(searchCriteria.getStatus())) {
			List<LinkedDatasetStatus> statusList = searchCriteria.getStatus().stream()
					.map(status -> LinkedDatasetStatus.valueOf(status.name())).collect(Collectors.toList());
			predicates.add(root.get(STATUS_FIELD).in(statusList));
		}

		if (searchCriteria.getDatasetUuid() != null) {
			predicates.add(root.get(LinkedDatasetEntity.DATASET_UUID_FIELD).in(searchCriteria.getDatasetUuid()));
		}

		if (searchCriteria.getDatasetConfidentiality() != null) {
			DatasetConfidentiality confidentiality = DatasetConfidentiality
					.valueOf(searchCriteria.getDatasetConfidentiality());
			predicates.add(root.get(DATASET_CONFIDENTIALITY_FIELD).in(confidentiality));
		}

		if (CollectionUtils.isNotEmpty(searchCriteria.getDatasetOwnerUuids())) {
			predicates.add(root.get(DATASET_ORGANIZATION_UUID_FIELD).in(searchCriteria.getDatasetOwnerUuids()));
		}
	}

	private Long getTotalLinkedDatasetRelatedToAuthenticatedUser(LinkedDatasetSearchCriteria searchCriteria) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();

		CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
		Root<LinkedDatasetEntity> countRoot = countQuery.from(entitiesClass);

		addWhereSearchOwnerOrRelatedLinkedDatasets(builder, countQuery, countRoot, searchCriteria);

		countQuery.select(builder.countDistinct(countRoot));

		return entityManager.createQuery(countQuery).getSingleResult();

	}
}