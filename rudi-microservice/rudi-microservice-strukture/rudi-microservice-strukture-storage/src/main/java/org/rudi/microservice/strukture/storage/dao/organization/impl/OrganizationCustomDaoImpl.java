package org.rudi.microservice.strukture.storage.dao.organization.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.storage.dao.AbstractCustomDaoImpl;
import org.rudi.common.storage.dao.PredicateListBuilder;
import org.rudi.common.storage.dao.RepositoryConstants;
import org.rudi.microservice.strukture.core.bean.criteria.NodeOrganizationSearchCriteria;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationSearchCriteria;
import org.rudi.microservice.strukture.storage.bean.NodeOrganizationProjectionBean;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationCustomDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationMemberEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nonnull;
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
public class OrganizationCustomDaoImpl extends AbstractCustomDaoImpl<OrganizationEntity, OrganizationSearchCriteria>
		implements OrganizationCustomDao {

	private final Class<NodeOrganizationProjectionBean> projectionBeanClass;

	public OrganizationCustomDaoImpl(EntityManager entityManager) {
		super(entityManager, OrganizationEntity.class);
		this.projectionBeanClass = NodeOrganizationProjectionBean.class;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public Page<OrganizationEntity> searchOrganizations(OrganizationSearchCriteria searchOrganizationCriteria,
			Pageable pageable) {
		return search(searchOrganizationCriteria, pageable);
	}

	@Override
	protected void addPredicates(OrganizationSearchCriteria searchCriteria, CriteriaBuilder builder,
			CriteriaQuery<?> criteriaQuery, Root<OrganizationEntity> root, List<Predicate> predicates) {
		predicateStringCriteria(searchCriteria.getUuid(), RepositoryConstants.FIELD_UUID, predicates, builder, root);
		predicateStringCriteria(searchCriteria.getName(), OrganizationEntity.FIELD_NAME, predicates, builder, root);
	}

	@Override
	protected void addPredicates(PredicateListBuilder<OrganizationEntity, OrganizationSearchCriteria> builder) {
		final var searchCriteria = builder.getSearchCriteria();
		builder.addIsNotNull(searchCriteria.getActive(),
				organization -> organization.get(RepositoryConstants.FIELD_CLOSING_DATE));
		builder.add(searchCriteria.getUserUuid(), (organization, userUuid) -> organization
				.join(OrganizationEntity.FIELD_MEMBERS).get(OrganizationMemberEntity.FIELD_USER_UUID).in(userUuid));
		builder.add(searchCriteria.getOrganizationStatus(), OrganizationStatus::valueOf,
				(organization, organizationStatus) -> organization.get(OrganizationEntity.FIELD_ORGANIZATION_STATUS)
						.in(organizationStatus));
		builder.add(searchCriteria.getStatus(), Status::valueOf,
				(organization, status) -> organization.get(OrganizationEntity.FIELD_STATUS).in(status));
	}

	@Override
	public Page<NodeOrganizationProjectionBean> searchNodeOrganizations(NodeOrganizationSearchCriteria searchCriteria,
			Pageable pageable) {

		if (searchCriteria == null || searchCriteria.getProviderUUID() == null) {
			return emptyPageNodeOrganizationProjectionBean(pageable);
		}

		final Long totalCount = getTotalCount(searchCriteria);
		if (totalCount == 0) {
			return emptyPageNodeOrganizationProjectionBean(pageable);
		}

		// Requête de recherche
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<NodeOrganizationProjectionBean> searchQuery = builder.createQuery(projectionBeanClass);
		Root<OrganizationEntity> searchRoot = searchQuery.from(entitiesClass);

		// construction de la sous-requête pour récupérer le statut du lien producteur - organisation
		Subquery<String> subQuery = searchQuery.subquery(String.class);

		Root<ProviderEntity> subFromProvider = subQuery.from(ProviderEntity.class);
		Join<ProviderEntity, LinkedProducerEntity> joinProviderLinkedProvider = subFromProvider
				.join(ProviderEntity.FIELD_LINKED_PRODUCERS, JoinType.LEFT);

		Join<LinkedProducerEntity, OrganizationEntity> joinLinkedProducerOrganization = joinProviderLinkedProvider
				.join(LinkedProducerEntity.FIELD_ORGANIZATION, JoinType.LEFT);

		// concaténation de l'uuid du provider et du statut du lien avec le producer sous forme de string
		// cette information sera découpée dans le bean de projection NodeOrganizationProjectionBean
		subQuery.select(
				builder.function("concat", String.class, joinProviderLinkedProvider.get(RepositoryConstants.FIELD_UUID),
						builder.literal(RepositoryConstants.QUERY_ITEM_SEPARATOR),
						joinProviderLinkedProvider.get(LinkedProducerEntity.FIELD_LINKED_PRODUCER_STATUS)));

		subQuery.where(builder.and(
				// jointure entre l'organisation de la requête principale et celle de la sous-requête
				builder.equal(joinLinkedProducerOrganization.get(RepositoryConstants.FIELD_UUID),
						(searchRoot.get(RepositoryConstants.FIELD_UUID))),
				// filtre sur l'UUID du provider correspondant au noeud producteur appelant
				subFromProvider.get(RepositoryConstants.FIELD_UUID).in(searchCriteria.getProviderUUID())));

		// Ajout des critères de recherche dans le where de la requête principale sur les organisations
		addWhereSearchNodeOrganization(searchCriteria, builder, searchQuery, searchRoot);

		// le select construit un bean de projection avec les infos de l'organisation et du lien avec le producteur
		searchQuery.select(builder.construct(NodeOrganizationProjectionBean.class,
				// infos de l'organisation
				searchRoot.get(RepositoryConstants.FIELD_UUID), searchRoot.get(OrganizationEntity.FIELD_NAME),
				searchRoot.get(OrganizationEntity.FIELD_ORGANIZATION_STATUS),
				searchRoot.get(OrganizationEntity.FIELD_STATUS), searchRoot.get(RepositoryConstants.FIELD_OPENING_DATE),
				searchRoot.get(RepositoryConstants.FIELD_CLOSING_DATE),
				searchRoot.get(OrganizationEntity.FIELD_DESCRIPTION), searchRoot.get(OrganizationEntity.FIELD_URL),
				searchRoot.get(OrganizationEntity.FIELD_ADDRESS),
				searchRoot.get(OrganizationEntity.FIELD_CREATION_DATE),
				searchRoot.get(OrganizationEntity.FIELD_UPDATED_DATE),
				// infos du lien avec le producteur
				subQuery.getSelection())).orderBy(QueryUtils.toOrders(pageable.getSort(), searchRoot, builder));

		TypedQuery<NodeOrganizationProjectionBean> typedQuery = entityManager.createQuery(searchQuery);
		if (pageable.isPaged()) {
			typedQuery.setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize());
		}
		List<NodeOrganizationProjectionBean> projectEntities = typedQuery.getResultList();
		return new PageImpl<>(projectEntities, pageable, totalCount.intValue());

	}

	@Nonnull
	private Page<NodeOrganizationProjectionBean> emptyPageNodeOrganizationProjectionBean(Pageable pageable) {
		return new PageImpl<>(new ArrayList<>(), pageable, 0);
	}

	private Long getTotalCount(NodeOrganizationSearchCriteria searchCriteria) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();

		CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
		Root<OrganizationEntity> countRoot = countQuery.from(entitiesClass);
		addWhereSearchNodeOrganization(searchCriteria, builder, countQuery, countRoot);
		countQuery.select(builder.countDistinct(countRoot));
		return entityManager.createQuery(countQuery).getSingleResult();
	}

	private void addWhereSearchNodeOrganization(NodeOrganizationSearchCriteria searchCriteria, CriteriaBuilder builder,
			CriteriaQuery<?> criteriaQuery, Root<OrganizationEntity> root) {

		if (searchCriteria != null) {

			List<Predicate> predicates = new ArrayList<>();

			addPredicates(searchCriteria, builder, criteriaQuery, root, predicates);

			final PredicateListBuilder<OrganizationEntity, OrganizationSearchCriteria> predicateListBuilder = new PredicateListBuilder<>(
					searchCriteria, predicates, builder, root);

			addPredicates(predicateListBuilder);

			// Définition de la clause Where
			if (CollectionUtils.isNotEmpty(predicates)) {
				criteriaQuery.where(builder.and(predicates.toArray(new Predicate[0])));
			}

		}
	}

}
