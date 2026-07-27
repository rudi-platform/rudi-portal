package org.rudi.microservice.projekt.service.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.microservice.projekt.core.bean.LinkedDatasetSearchCriteria;
import org.rudi.microservice.projekt.core.bean.LinkedDatasetStatus;
import org.rudi.microservice.projekt.service.ProjectSpringBootTest;
import org.rudi.microservice.projekt.service.datafactory.LinkedDatasetDataFactory;
import org.rudi.microservice.projekt.service.datafactory.ProjectDataFactory;
import org.rudi.microservice.projekt.storage.dao.linkeddataset.LinkedDatasetCustomDao;
import org.rudi.microservice.projekt.storage.dao.linkeddataset.LinkedDatasetDao;
import org.rudi.microservice.projekt.storage.dao.project.ProjectDao;
import org.rudi.microservice.projekt.storage.entity.DatasetConfidentiality;
import org.rudi.microservice.projekt.storage.entity.OwnerType;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.rudi.microservice.projekt.storage.entity.relatedorganization.RelationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;

@ProjectSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class LinkedDatasetCustomDaoUT {

	private final LinkedDatasetCustomDao linkedDatasetCustomDao;
	private final ProjectDao projectDao;
	private final LinkedDatasetDao linkedDatasetDao;
	private final ProjectDataFactory projectDataFactory;
	private final LinkedDatasetDataFactory linkedDatasetDataFactory;

	final UUID userUuid1 = UUID.randomUUID();
	final UUID userUuid2 = UUID.randomUUID();
	final UUID userUuid3 = UUID.randomUUID();
	final UUID userUuid4 = UUID.randomUUID();

	final UUID organization1Uuid = UUID.randomUUID();
	final UUID organization2Uuid = UUID.randomUUID();
	final UUID organization3Uuid = UUID.randomUUID();
	final UUID organization4Uuid = UUID.randomUUID();

	// datasets pour les relations VALIDATED (validées) entre owner et project pour vérifier que l'utilisateur a accès à ces datasets
	final UUID dataset1relationValidatedUuid = UUID.randomUUID();
	final UUID dataset2relationValidatedUuid = UUID.randomUUID();

	// datasets pour les relations PENDING (non validées) entre owner et project pour vérifier que l'utilisateur n'a pas accès à ces datasets
	final UUID dataset1relationPendingUuid = UUID.randomUUID();
	final UUID dataset2relationPendingUuid = UUID.randomUUID();

	// u1 est membre des organisations o1 et o2
	// u2 est membre des organisations o3 et o4
	// u3 est membre de l'organisation o4
	// u4 n'est membre d'aucune organisation
	Map<UUID, List<UUID>> userToOrganizationsMap = Map.of(
	// @formatter:off
		userUuid1, List.of(userUuid1, organization1Uuid, organization2Uuid), 
		userUuid2, List.of(userUuid2, organization3Uuid, organization4Uuid), 
		userUuid3, List.of(userUuid3, organization4Uuid), 
		userUuid4, List.of(userUuid4)
	// @formatter:on
	);

	@BeforeEach
	void setUp() {

		// projet owned by o1, linked to dataset ds1, related to o3 with relation status ACCEPTED
		createProjectWithOneLinkedDatasetAndRelation("p1", organization1Uuid, OwnerType.ORGANIZATION,
				dataset1relationValidatedUuid, organization3Uuid, RelationStatus.ACCEPTED);
		// projet owned by o3, not linked to any dataset, related to o4 with relation status ACCEPTED
		createProjectWithOneLinkedDatasetAndRelation("p2", organization3Uuid, OwnerType.ORGANIZATION, null,
				organization4Uuid, RelationStatus.ACCEPTED);

		// projet owned by u4, linked to dataset ds2, not related to any organization
		createProjectWithOneLinkedDatasetAndRelation("p3", userUuid4, OwnerType.USER, dataset2relationValidatedUuid,
				null, null);

		// projet owned by o1, linked to dataset ds1, related to o3 with relation status PENDING
		createProjectWithOneLinkedDatasetAndRelation("p4-pending", organization1Uuid, OwnerType.ORGANIZATION,
				dataset1relationPendingUuid, organization3Uuid, RelationStatus.PENDING);

		// projet owned by o3, not linked to any dataset, related to o4 with relation status PENDING
		createProjectWithOneLinkedDatasetAndRelation("p5-pending-relation", organization3Uuid, OwnerType.ORGANIZATION,
				null, organization4Uuid, RelationStatus.PENDING);

	}

	@Test
	@DisplayName("Vérifie que l'utilisateur membre d'une organisation propriétaire d'un projet avec un dataset validé peut le retrouver")
	void searchOwnerDatasetAccess_usersOrganizationOwningDatasetWithProject() {
		final var searchCriteria = new LinkedDatasetSearchCriteria().status(List.of(LinkedDatasetStatus.VALIDATED))
				.endDateIsNotOver(true).datasetUuid(dataset1relationValidatedUuid);
		searchCriteria.setProjectRelatedUuids(userToOrganizationsMap.get(userUuid1));
		searchCriteria.organizationProjectRelationStatuses(
				List.of(org.rudi.microservice.projekt.core.bean.RelationStatus.ACCEPTED));

		final var result = linkedDatasetCustomDao.searchRelatedDatasetAccess(searchCriteria, Pageable.unpaged());

		assertThat(result.getTotalElements()).isEqualTo(1L);

	}

	@Test
	@DisplayName("Vérifie que l'utilisateur membre d'une organisation propriétaire d'un dataset validé mais sans projet ne peut pas le retrouver")
	void searchOwnerDatasetAccess_usersOrganizationOwningDatasetWithoutProject() {
		final var searchCriteria = new LinkedDatasetSearchCriteria().status(List.of(LinkedDatasetStatus.VALIDATED))
				.endDateIsNotOver(true).datasetUuid(dataset2relationValidatedUuid);
		searchCriteria.setProjectRelatedUuids(userToOrganizationsMap.get(userUuid1));
		searchCriteria.organizationProjectRelationStatuses(
				List.of(org.rudi.microservice.projekt.core.bean.RelationStatus.ACCEPTED));

		final var result = linkedDatasetCustomDao.searchRelatedDatasetAccess(searchCriteria, Pageable.unpaged());

		assertThat(result.getTotalElements()).isZero();

	}

	@Test
	@DisplayName("Vérifie que l'utilisateur membre d'une organisation reliée à un projet validé avec un dataset validé peut le retrouver")
	void searchOwnerDatasetAccess_usersOrganizationRelatedToProjectWithDataset() {
		final var searchCriteria = new LinkedDatasetSearchCriteria().status(List.of(LinkedDatasetStatus.VALIDATED))
				.endDateIsNotOver(true).datasetUuid(dataset1relationValidatedUuid);
		searchCriteria.setProjectRelatedUuids(userToOrganizationsMap.get(userUuid2));
		searchCriteria.organizationProjectRelationStatuses(
				List.of(org.rudi.microservice.projekt.core.bean.RelationStatus.ACCEPTED));

		final var result = linkedDatasetCustomDao.searchRelatedDatasetAccess(searchCriteria, Pageable.unpaged());

		assertThat(result.getTotalElements()).isEqualTo(1L);

	}

	@Test
	@DisplayName("Vérifie que l'utilisateur membre d'une organisation reliée à un projet validé mais non lié à un dataset ne peut pas le retrouver")
	void searchOwnerDatasetAccess_usersOrganizationRelatedToProjectWithoutDataset() {
		final var searchCriteria = new LinkedDatasetSearchCriteria().status(List.of(LinkedDatasetStatus.VALIDATED))
				.endDateIsNotOver(true).datasetUuid(dataset2relationValidatedUuid);
		searchCriteria.setProjectRelatedUuids(userToOrganizationsMap.get(userUuid2));
		searchCriteria.organizationProjectRelationStatuses(
				List.of(org.rudi.microservice.projekt.core.bean.RelationStatus.ACCEPTED));

		final var result = linkedDatasetCustomDao.searchRelatedDatasetAccess(searchCriteria, Pageable.unpaged());

		assertThat(result.getTotalElements()).isZero();
	}

	@Test
	// U3 n'a pas accès à ds1
	@DisplayName("Vérifie que l'utilisateur membre d'une organisation reliée à un projet validé mais non lié au dataset ne peut pas le retrouver")
	void searchOwnerDatasetAccess_usersOrganizationRelatedToProjectWithoutDataset2() {
		final var searchCriteria = new LinkedDatasetSearchCriteria().status(List.of(LinkedDatasetStatus.VALIDATED))
				.endDateIsNotOver(true).datasetUuid(dataset1relationValidatedUuid);
		searchCriteria.setProjectRelatedUuids(userToOrganizationsMap.get(userUuid3));
		searchCriteria.organizationProjectRelationStatuses(
				List.of(org.rudi.microservice.projekt.core.bean.RelationStatus.ACCEPTED));

		final var result = linkedDatasetCustomDao.searchRelatedDatasetAccess(searchCriteria, Pageable.unpaged());

		assertThat(result.getTotalElements()).isZero();
	}

	// U3 n'a pas accès à ds2
	@Test
	@DisplayName("Vérifie que l'utilisateur membre d'une organisation reliée à un projet validé mais non lié au dataset ne peut pas le retrouver (2)")
	void searchOwnerDatasetAccess_usersOrganizationRelatedToProjectWithoutDataset3() {
		final var searchCriteria = new LinkedDatasetSearchCriteria().status(List.of(LinkedDatasetStatus.VALIDATED))
				.endDateIsNotOver(true).datasetUuid(dataset2relationValidatedUuid);
		searchCriteria.setProjectRelatedUuids(userToOrganizationsMap.get(userUuid3));
		searchCriteria.organizationProjectRelationStatuses(
				List.of(org.rudi.microservice.projekt.core.bean.RelationStatus.ACCEPTED));

		final var result = linkedDatasetCustomDao.searchRelatedDatasetAccess(searchCriteria, Pageable.unpaged());

		assertThat(result.getTotalElements()).isZero();
	}

	// U4 a accès à ds2
	@Test
	@DisplayName("Vérifie que l'utilisateur owner d'un projet validé et lié au dataset peut le retrouver")
	void searchOwnerDatasetAccess_userOwningProjectWithDataset() {
		final var searchCriteria = new LinkedDatasetSearchCriteria().status(List.of(LinkedDatasetStatus.VALIDATED))
				.endDateIsNotOver(true).datasetUuid(dataset2relationValidatedUuid);
		searchCriteria.setProjectRelatedUuids(userToOrganizationsMap.get(userUuid4));
		searchCriteria.organizationProjectRelationStatuses(
				List.of(org.rudi.microservice.projekt.core.bean.RelationStatus.ACCEPTED));

		final var result = linkedDatasetCustomDao.searchRelatedDatasetAccess(searchCriteria, Pageable.unpaged());

		assertThat(result.getTotalElements()).isEqualTo(1L);
	}

	@Test
	@DisplayName("Vérifie que l'utilisateur membre d'une organisation propriétaire d'un projet avec un dataset lié par une relation PENDING n'a pas accès au dataset")
	void searchOwnerDatasetAccess_usersOrganizationRelatedToProjectWithDatasetPendingRelation() {
		final var searchCriteria = new LinkedDatasetSearchCriteria().status(List.of(LinkedDatasetStatus.VALIDATED))
				.endDateIsNotOver(true).datasetUuid(dataset1relationPendingUuid);
		searchCriteria.setProjectRelatedUuids(userToOrganizationsMap.get(userUuid2));
		searchCriteria.organizationProjectRelationStatuses(
				List.of(org.rudi.microservice.projekt.core.bean.RelationStatus.ACCEPTED));

		final var result = linkedDatasetCustomDao.searchRelatedDatasetAccess(searchCriteria, Pageable.unpaged());

		assertThat(result.getTotalElements()).isZero();
	}

	private void createProjectWithOneLinkedDatasetAndRelation(String projectName, UUID ownerUuid, OwnerType ownerType,
			UUID datasetUuid, UUID relatedOrganizationUuid, RelationStatus relationStatus) {
		final ProjectEntity project = projectDataFactory.getOrCreateSimpleProject(projectName, ownerUuid, ownerType);
		project.getLinkedDatasets().clear();
		project.getRelatedOrganizations().clear();

		final var linkedDataset = linkedDatasetDataFactory.createNotPersisted(datasetUuid, UUID.randomUUID(),
				org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetStatus.VALIDATED,
				DatasetConfidentiality.RESTRICTED);
		linkedDataset.setEndDate(LocalDateTime.now().plusDays(30));
		project.getLinkedDatasets().add(linkedDataset);

		if (relatedOrganizationUuid != null) {
			project.getRelatedOrganizations().add(projectDataFactory
					.createNotPersistedRelatedOrganizationEntity(relatedOrganizationUuid, relationStatus));
		}

		projectDao.save(project);
	}

	@AfterEach
	void tearDown() {
		linkedDatasetDao.deleteAll();
		projectDao.deleteAll();
	}
}
