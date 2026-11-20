package org.rudi.microservice.strukture.service.organization;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.microservice.strukture.core.bean.criteria.NodeOrganizationSearchCriteria;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationSearchCriteria;
import org.rudi.microservice.strukture.service.StruktureSpringBootTest;
import org.rudi.microservice.strukture.service.datafactory.organization.OrganizationDataFactory;
import org.rudi.microservice.strukture.service.datafactory.organization.OrganizationSearchCriteriaDataFactory;
import org.rudi.microservice.strukture.service.datafactory.organizationmember.OrganizationMemberDataFactory;
import org.rudi.microservice.strukture.service.datafactory.provider.ProviderDataFactory;
import org.rudi.microservice.strukture.storage.bean.NodeOrganizationProjectionBean;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationCustomDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationRole;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerStatus;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;

@StruktureSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class OrganizationCustomDaoUT {

	private final OrganizationCustomDao organizationCustomDao;
	private final OrganizationDataFactory organizationDataFactory;
	private final OrganizationMemberDataFactory organizationMemberDataFactory;
	private final ProviderDataFactory providerDataFactory;
	private final OrganizationSearchCriteriaDataFactory organizationSearchCriteriaDataFactory;

	@Test
	@DisplayName("Recherche par uuids, nom, initiateur et organisations actives, en excluant une organisation listée")
	void test_searchOrganizations_byUuidsNameInitiatorExcludedAndActive() {
		final String suffix = UUID.randomUUID().toString();
		final String name = "Alpha-" + suffix;
		final String initiator = "alpha-" + suffix + "@test.fr";
		final LocalDateTime now = LocalDateTime.now();
		final LocalDateTime openingDate = now.minusDays(30);
		final LocalDateTime closingDate = now.plusDays(30);

		final OrganizationEntity matching = organizationDataFactory.create(name, initiator, "description alpha",
				OrganizationStatus.VALIDATED, Status.COMPLETED, openingDate, closingDate);
		final OrganizationEntity excluded = organizationDataFactory.create(name, initiator,
				"description alpha exclue", OrganizationStatus.VALIDATED, Status.COMPLETED, openingDate, closingDate);
		final OrganizationEntity wrongName = organizationDataFactory.create("Beta-" + suffix,
				"beta-" + suffix + "@test.fr", "description beta", OrganizationStatus.VALIDATED, Status.COMPLETED,
				openingDate, closingDate);

		final OrganizationSearchCriteria searchCriteria = organizationSearchCriteriaDataFactory
				.createForUuidsNameInitiatorExcludedAndActive(
						List.of(matching.getUuid(), excluded.getUuid(), wrongName.getUuid()), name, initiator,
						List.of(excluded.getUuid()), true);

		final Page<OrganizationEntity> result = organizationCustomDao.searchOrganizations(searchCriteria,
				Pageable.unpaged());

		assertThat(result.getContent()).extracting(OrganizationEntity::getUuid)
				.containsExactly(matching.getUuid());
	}

	@Test
	@DisplayName("Recherche par nom OU description : les deux organisations correspondant à l'un des deux champs sont retournées")
	void test_searchOrganizations_byNameOrDescription() {
		final String term = "ORGTERM-" + UUID.randomUUID();

		final OrganizationEntity matchingByName = organizationDataFactory.create(term, "initiator-" + term, "sans rapport",
				OrganizationStatus.VALIDATED, Status.COMPLETED, LocalDateTime.now().minusDays(1), null);
		final OrganizationEntity matchingByDescription = organizationDataFactory.create("nom sans rapport",
				"initiator-" + term, term, OrganizationStatus.VALIDATED, Status.COMPLETED,
				LocalDateTime.now().minusDays(1), null);
		organizationDataFactory.create("autre nom", "autre-" + term, "autre description", OrganizationStatus.VALIDATED,
				Status.COMPLETED, LocalDateTime.now().minusDays(1), null);

		final OrganizationSearchCriteria searchCriteria = organizationSearchCriteriaDataFactory
				.createForNameOrDescription(term);

		final Page<OrganizationEntity> result = organizationCustomDao.searchOrganizations(searchCriteria,
				Pageable.unpaged());

		assertThat(result.getContent()).extracting(OrganizationEntity::getUuid).containsExactlyInAnyOrder(
				matchingByName.getUuid(), matchingByDescription.getUuid());
	}

	@Test
	@DisplayName("Recherche par membre (userUuid), statut d'organisation et statut bpmn")
	void test_searchOrganizations_byUserUuidOrganizationStatusAndStatus() {
		final UUID userUuid = UUID.randomUUID();
		final UUID otherUserUuid = UUID.randomUUID();
		final String suffix = UUID.randomUUID().toString();

		final OrganizationEntity matching = organizationMemberDataFactory.createOrganizationMember(
				organizationDataFactory.create("org-match-" + suffix, "initiator-" + suffix, "description", OrganizationStatus.VALIDATED,
						Status.COMPLETED, LocalDateTime.now().minusDays(1), null),
				userUuid, OrganizationRole.EDITOR);
		organizationMemberDataFactory.createOrganizationMember(organizationDataFactory.create("org-other-member-" + suffix, "initiator-" + suffix, "description",
				OrganizationStatus.VALIDATED, Status.COMPLETED, LocalDateTime.now().minusDays(1), null),
				otherUserUuid, OrganizationRole.EDITOR);
		organizationMemberDataFactory.createOrganizationMember(organizationDataFactory.create("org-wrong-organization-status-" + suffix, "initiator-" + suffix, "description",
				OrganizationStatus.DRAFT, Status.COMPLETED, LocalDateTime.now().minusDays(1), null), userUuid,
				OrganizationRole.EDITOR);
		organizationMemberDataFactory.createOrganizationMember(organizationDataFactory.create("org-wrong-bpmn-status-" + suffix, "initiator-" + suffix, "description",
				OrganizationStatus.VALIDATED, Status.DRAFT, LocalDateTime.now().minusDays(1), null), userUuid,
				OrganizationRole.EDITOR);

		final OrganizationSearchCriteria searchCriteria = organizationSearchCriteriaDataFactory
				.createForUserUuidOrganizationStatusAndStatus(userUuid,
						List.of(org.rudi.microservice.strukture.core.bean.OrganizationStatus.VALIDATED),
						Status.COMPLETED);

		final Page<OrganizationEntity> result = organizationCustomDao.searchOrganizations(searchCriteria,
				Pageable.unpaged());

		assertThat(result.getContent()).extracting(OrganizationEntity::getUuid).containsExactly(matching.getUuid());
	}

	@Test
	@DisplayName("Recherche de mes organisations : statut autorisé pour un membre administrateur OU statut d'organisation standard")
	void test_searchMyOrganizations_byMemberOrganizationStatusAndAdminAllowedStatus() {
		final UUID userUuid = UUID.randomUUID();
		final UUID otherUserUuid = UUID.randomUUID();
		final String suffix = UUID.randomUUID().toString();

		final OrganizationEntity adminAllowed = organizationMemberDataFactory.createOrganizationMember(
				organizationDataFactory.create("org-admin-allowed-" + suffix, "initiator-" + suffix, "description",
						OrganizationStatus.IN_PROGRESS, Status.COMPLETED, LocalDateTime.now().minusDays(1), null),
				userUuid, OrganizationRole.ADMINISTRATOR);
		final OrganizationEntity standardStatus = organizationMemberDataFactory.createOrganizationMember(
				organizationDataFactory.create("org-standard-status-" + suffix, "initiator-" + suffix, "description",
						OrganizationStatus.VALIDATED, Status.COMPLETED, LocalDateTime.now().minusDays(1), null),
				userUuid, OrganizationRole.EDITOR);
		organizationMemberDataFactory.createOrganizationMember(organizationDataFactory.create("org-wrong-status-" + suffix, "initiator-" + suffix, "description",
				OrganizationStatus.DRAFT, Status.COMPLETED, LocalDateTime.now().minusDays(1), null), userUuid,
				OrganizationRole.EDITOR);
		organizationMemberDataFactory.createOrganizationMember(organizationDataFactory.create("org-not-member-" + suffix, "initiator-" + suffix, "description",
				OrganizationStatus.VALIDATED, Status.COMPLETED, LocalDateTime.now().minusDays(1), null),
				otherUserUuid, OrganizationRole.EDITOR);

		final OrganizationSearchCriteria searchCriteria = organizationSearchCriteriaDataFactory
				.createForUserUuidOrganizationStatusAndAdminAllowedStatus(userUuid,
						List.of(org.rudi.microservice.strukture.core.bean.OrganizationStatus.VALIDATED),
						List.of(org.rudi.microservice.strukture.core.bean.OrganizationStatus.IN_PROGRESS));

		final Page<OrganizationEntity> result = organizationCustomDao.searchMyOrganizations(searchCriteria,
				Pageable.unpaged());

		assertThat(result.getContent()).extracting(OrganizationEntity::getUuid)
				.containsExactlyInAnyOrder(adminAllowed.getUuid(), standardStatus.getUuid());
	}

	@Test
	@DisplayName("Recherche des organisations liées à un provider (nœud producteur), via la sous-requête de statut de lien")
	void test_searchNodeOrganizations_byProviderUuid() {
		final String suffix = UUID.randomUUID().toString().substring(0, 8);

		final ProviderEntity matchingProvider = providerDataFactory.createProvider("P-MATCH-" + suffix, null);

		final LinkedProducerEntity matchingLinkedProducer = matchingProvider.getLinkedProducers().iterator().next();
		final OrganizationEntity matchingOrganization = matchingLinkedProducer.getOrganization();

		final NodeOrganizationSearchCriteria searchCriteria = organizationSearchCriteriaDataFactory
				.createNodeOrganizationSearchCriteria(List.of(matchingOrganization.getUuid()),
						matchingProvider.getUuid());

		final Page<NodeOrganizationProjectionBean> result = organizationCustomDao
				.searchNodeOrganizations(searchCriteria, Pageable.unpaged());

		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).extracting(NodeOrganizationProjectionBean::getOrganizationId)
				.containsExactly(matchingOrganization.getUuid());
		assertThat(result.getContent().get(0).getProducerId()).isEqualTo(matchingLinkedProducer.getUuid());
		assertThat(result.getContent().get(0).getLinkedProducerStatus()).isEqualTo(LinkedProducerStatus.VALIDATED);
		assertThat(result.getContent().get(0).getLinkedStatus()).isEqualTo(Status.COMPLETED);
	}

}
