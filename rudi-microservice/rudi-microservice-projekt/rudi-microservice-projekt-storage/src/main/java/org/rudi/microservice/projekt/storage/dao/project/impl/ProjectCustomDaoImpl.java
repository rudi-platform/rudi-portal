package org.rudi.microservice.projekt.storage.dao.project.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.collections4.CollectionUtils;
import org.rudi.common.storage.dao.AbstractCustomDaoImpl;
import org.rudi.common.storage.dao.PredicateListBuilder;
import org.rudi.microservice.projekt.core.bean.ComputeIndicatorsSearchCriteria;
import org.rudi.microservice.projekt.core.bean.Indicators;
import org.rudi.microservice.projekt.core.bean.ProjectByOwner;
import org.rudi.microservice.projekt.core.bean.criteria.EnhancedProjectSearchCriteria;
import org.rudi.microservice.projekt.core.bean.criteria.ProjectSearchCriteria;
import org.rudi.microservice.projekt.storage.dao.project.ProjectCustomDao;
import org.rudi.microservice.projekt.storage.entity.DatasetConfidentiality;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.val;

@Repository
public class ProjectCustomDaoImpl extends AbstractCustomDaoImpl<ProjectEntity, ProjectSearchCriteria>
		implements ProjectCustomDao {

	private static final String FIELD_PROJECT_STATUS = "projectStatus";
	private static final String FIELD_STATUS = "status";
	private static final String FIELD_OWNER_UUID = ProjectEntity.FIELD_OWNER_UUID;
	private static final String FIELD_KEYWORDS = "keywords";
	private static final String FIELD_TARGET_AUDIENCES = "targetAudiences";
	private static final String FIELD_LINKED_DATASETS = "linkedDatasets";
	private static final String FIELD_DATASET_REQUESTS = "datasetRequests";
	private static final String FIELD_DATASET_UUID = "datasetUuid";
	private static final String FIELD_UUID = "uuid";
	private static final String FIELD_PRODUCER_UUID = "datasetOrganisationUuid";
	private static final String FIELD_DATASET_CONFIDENTIALITY = "datasetConfidentiality";
	private static final String FIELD_PROJECT_CONFIDENTIALITY = "confidentiality";
	private static final String FIELD_PROJECT_CONFIDENTIALITY_IS_PRIVATE = "privateAccess";

	public ProjectCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, ProjectEntity.class);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Page<ProjectEntity> searchProjects(ProjectSearchCriteria searchCriteria, Pageable pageable) {
		return search(searchCriteria, pageable);
	}

	@Override
	protected void addPredicates(PredicateListBuilder<ProjectEntity, ProjectSearchCriteria> builder) {
		val searchCriteria = builder.getSearchCriteria();
		builder.add(searchCriteria.getKeywords(), (project, keywords) -> project.join(FIELD_KEYWORDS).in(keywords))
				.add(searchCriteria.getTargetAudiences(),
						(project, targetAudiences) -> project.join(FIELD_TARGET_AUDIENCES).in(targetAudiences))
				.add(searchCriteria.getThemes(), (project, themes) -> project.join("themes").in(themes))
				.add(searchCriteria.getDatasetUuids(),
						(project, linkedDatasetsUuids) -> project.join(FIELD_LINKED_DATASETS).get(FIELD_DATASET_UUID)
								.in(linkedDatasetsUuids))
				.add(searchCriteria.getLinkedDatasetUuids(),
						(project, linkedDatasetEntityUuids) -> project.join(FIELD_LINKED_DATASETS).get(FIELD_UUID)
								.in(linkedDatasetEntityUuids))
				.add(searchCriteria.getOwnerUuids(),
						(project, ownerUuids) -> project.get(FIELD_OWNER_UUID).in(ownerUuids))
				.add(searchCriteria.getProjectUuids(),
						(project, projectUuids) -> project.get(FIELD_UUID).in(projectUuids))
				.add(searchCriteria.getProjectStatus(), ProjectStatus::valueOf,
						(project, status) -> project.get(FIELD_PROJECT_STATUS).in(status))
				.add(searchCriteria.getIsPrivate(),
						(project, confidentialities) -> project.join(FIELD_PROJECT_CONFIDENTIALITY)
								.get(FIELD_PROJECT_CONFIDENTIALITY_IS_PRIVATE).in(confidentialities))
				.add(searchCriteria.getStatus(),(project, status) -> project.get(FIELD_STATUS).in(status));
	}

	@Override
	public ProjectEntity findProjectByNewDatasetRequestUuid(UUID newDatasetRequestUuid) {
		val builder = entityManager.getCriteriaBuilder();
		val searchQuery = builder.createQuery(entitiesClass);
		val searchRoot = searchQuery.from(entitiesClass);

		Join<ProjectEntity, NewDatasetRequestEntity> joinNewDatasetRequest = searchRoot.join(FIELD_DATASET_REQUESTS);

		searchQuery.where(builder.equal(joinNewDatasetRequest.get(FIELD_UUID), newDatasetRequestUuid));

		TypedQuery<ProjectEntity> typedQuery = entityManager.createQuery(searchQuery);
		return typedQuery.getSingleResult();
	}

	@Override
	public ProjectEntity findProjectByLinkedDatasetUuid(UUID linkedDatasetUuid) {
		val builder = entityManager.getCriteriaBuilder();
		val searchQuery = builder.createQuery(entitiesClass);
		val searchRoot = searchQuery.from(entitiesClass);

		Join<ProjectEntity, LinkedDatasetEntity> joinNewDatasetRequest = searchRoot.join(FIELD_LINKED_DATASETS);

		searchQuery.where(builder.equal(joinNewDatasetRequest.get(FIELD_UUID), linkedDatasetUuid));

		TypedQuery<ProjectEntity> typedQuery = entityManager.createQuery(searchQuery);
		return typedQuery.getSingleResult();
	}

	@Override
	public Indicators computeProjectInfos(ComputeIndicatorsSearchCriteria searchCriteria) {
		val builder = entityManager.getCriteriaBuilder();
		val countQuery = builder.createQuery(Long.class);
		val countRoot = countQuery.from(entitiesClass);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(countRoot.get(FIELD_UUID), searchCriteria.getProjectUuid()));
		final Join<ProjectEntity, LinkedDatasetEntity> join = countRoot.join(FIELD_LINKED_DATASETS);
		// Seulement les jdds restreints demandés sont à récuperer, les jdds ouverts n'étant pas des demandes au sen fonctionnel
		predicates.add(builder.equal(join.get(FIELD_DATASET_CONFIDENTIALITY), DatasetConfidentiality.RESTRICTED));
		if (searchCriteria.getExcludedProducerUuid() != null) {
			predicates.add(builder.notEqual(join.get(FIELD_PRODUCER_UUID), searchCriteria.getExcludedProducerUuid()));
		}
		countQuery.where(builder.and(predicates.toArray(Predicate[]::new)));
		countQuery.select(builder.countDistinct(join));
		Long numberOfRequest = entityManager.createQuery(countQuery).getSingleResult();

		countQuery.select(builder.countDistinct(join.get(FIELD_PRODUCER_UUID)));
		Long numberOfProducer = entityManager.createQuery(countQuery).getSingleResult();

		Indicators results = new Indicators();
		results.setNumberOfRequest(numberOfRequest.intValue());
		results.setNumberOfProducer(numberOfProducer.intValue());
		return results;
	}

	@Override
	public Integer getNumberOfLinkedDatasets(UUID projectUuid) { // etendre en 2 méthodes
		return queryLauncher(projectUuid, FIELD_LINKED_DATASETS).intValue();
	}

	@Override
	public Integer getNumberOfNewRequests(UUID projectUuid) { // etendre en 2 méthodes
		return queryLauncher(projectUuid, FIELD_DATASET_REQUESTS).intValue();
	}

	private <T> Long queryLauncher(UUID projectUuid, String fieldToJoin) {
		val builder = entityManager.getCriteriaBuilder();
		val countQuery = builder.createQuery(Long.class);
		val countRoot = countQuery.from(entitiesClass);
		// Parametrer avec la classe
		final Join<ProjectEntity, T> join = countRoot.join(fieldToJoin);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(builder.equal(countRoot.get(FIELD_UUID), projectUuid));
		countQuery.where(builder.and(predicates.toArray(Predicate[]::new)));

		countQuery.select(builder.countDistinct(join));
		return entityManager.createQuery(countQuery).getSingleResult();
	}

	@Override
	public List<ProjectByOwner> getNumberOfProjectsPerOwners(EnhancedProjectSearchCriteria enhancedProjectSearchCriteria) {
		val builder = entityManager.getCriteriaBuilder();
		val countQuery = builder.createQuery(ProjectByOwner.class);
		val countRoot = countQuery.from(entitiesClass);

		addWhereSearchRelatedProjects(builder, countQuery, countRoot, enhancedProjectSearchCriteria);

		countQuery.groupBy(countRoot.get(FIELD_OWNER_UUID));
		countQuery.select(builder.construct(ProjectByOwner.class, countRoot.get(FIELD_OWNER_UUID),
				builder.countDistinct(countRoot)));

		return entityManager.createQuery(countQuery).getResultList();
	}

	public Page<ProjectEntity> searchRelatedProjects(EnhancedProjectSearchCriteria searchCriteria, Pageable pageable) {
		if (searchCriteria == null) {
			return emptyPage(pageable);
		}

		final Long totalCount = getTotalProjectRelatedToAuthenticatedUser(searchCriteria);
		if (totalCount == 0) {
			return emptyPage(pageable);
		}

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<ProjectEntity> searchQuery = builder.createQuery(entitiesClass);
		Root<ProjectEntity> searchRoot = searchQuery.from(entitiesClass);
		addWhereSearchRelatedProjects(builder, searchQuery, searchRoot, searchCriteria);
		searchQuery.select(searchRoot).distinct(true)
				.orderBy(QueryUtils.toOrders(pageable.getSort(), searchRoot, builder));

		TypedQuery<ProjectEntity> typedQuery = entityManager.createQuery(searchQuery);
		if (pageable.isPaged()) {
			typedQuery.setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize());
		}

		List<ProjectEntity> projectEntities = typedQuery.getResultList();
		return new PageImpl<>(projectEntities, pageable, totalCount.intValue());
	}

	private void addWhereSearchRelatedProjects(CriteriaBuilder builder, CriteriaQuery<?> criteriaQuery,
			Root<ProjectEntity> root, EnhancedProjectSearchCriteria searchCriteria) {
		List<Predicate> predicates = new ArrayList<>();

		Predicate isNotPrivatePredicate = builder
				.equal(root.join(FIELD_PROJECT_CONFIDENTIALITY).get(FIELD_PROJECT_CONFIDENTIALITY_IS_PRIVATE), false);
		Predicate isPrivatePredicate = builder
				.equal(root.join(FIELD_PROJECT_CONFIDENTIALITY).get(FIELD_PROJECT_CONFIDENTIALITY_IS_PRIVATE), true);
		
		List<Predicate> or1 = new ArrayList<>();
		if (!Boolean.TRUE.equals(searchCriteria.getIsPrivate())) {
			or1.add(isNotPrivatePredicate);
		}
		List<Predicate> or2 = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(searchCriteria.getMyOrganizationsUuids())) {
			Predicate owners = root.get(FIELD_OWNER_UUID).in(searchCriteria.getMyOrganizationsUuids());
			or2.add(owners);
		}
		if (CollectionUtils.isNotEmpty(searchCriteria.getMyOrganizationsDatasetsUuids())) {
			Predicate dataSetOwner = root.join(FIELD_LINKED_DATASETS, JoinType.LEFT).get(FIELD_DATASET_UUID)
					.in(searchCriteria.getMyOrganizationsDatasetsUuids());
			or2.add(dataSetOwner);
		}
		if (CollectionUtils.isNotEmpty(or2)) {
			or1.add(builder.and(isPrivatePredicate, builder.or(or2.toArray(Predicate[]::new))));
		}
		if (CollectionUtils.isNotEmpty(or1)) {
			predicates.add(builder.or(or1.toArray(Predicate[]::new)));
		}

		ProjectSearchCriteria projectSearchCriteria = searchCriteria.getProjectSearchCriteria();

		// On rajoute les predicates issus du projectSearchCriteria
		if (projectSearchCriteria != null) {
			addPredicates(projectSearchCriteria, builder, criteriaQuery, root, predicates);
			final PredicateListBuilder<ProjectEntity, ProjectSearchCriteria> predicateListBuilder = new PredicateListBuilder<>(
					projectSearchCriteria, predicates, builder, root);

			addPredicates(predicateListBuilder);
		}

		// Je ne suis pas sûr de ce que fais ce code par rapport à mes OR // AND dans les predicates plus haut.
		if (CollectionUtils.isNotEmpty(predicates)) {
			criteriaQuery.where(builder.and(predicates.toArray(new Predicate[0])));
		}
	}

	private Long getTotalProjectRelatedToAuthenticatedUser(EnhancedProjectSearchCriteria searchCriteria) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();

		CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
		Root<ProjectEntity> countRoot = countQuery.from(entitiesClass);

		addWhereSearchRelatedProjects(builder, countQuery, countRoot, searchCriteria);

		countQuery.select(builder.countDistinct(countRoot));

		return entityManager.createQuery(countQuery).getSingleResult();

	}

}
