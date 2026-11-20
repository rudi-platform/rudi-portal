package org.rudi.microservice.projekt.service.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.microservice.projekt.core.bean.ComputeIndicatorsSearchCriteria;
import org.rudi.microservice.projekt.core.bean.Indicators;
import org.rudi.microservice.projekt.core.bean.ProjectByOrganization;
import org.rudi.microservice.projekt.core.bean.RelationStatus;
import org.rudi.microservice.projekt.core.bean.criteria.EnhancedProjectSearchCriteria;
import org.rudi.microservice.projekt.core.bean.criteria.ProjectSearchCriteria;
import org.rudi.microservice.projekt.service.ProjectSpringBootTest;
import org.rudi.microservice.projekt.service.datafactory.ComputeIndicatorsSearchCriteriaDataFactory;
import org.rudi.microservice.projekt.service.datafactory.EnhancedProjectSearchCriteriaDataFactory;
import org.rudi.microservice.projekt.service.datafactory.LinkedDatasetDataFactory;
import org.rudi.microservice.projekt.service.datafactory.NewDatasetRequestDataFactory;
import org.rudi.microservice.projekt.service.datafactory.ProjectDataFactory;
import org.rudi.microservice.projekt.service.datafactory.ProjectSearchCriteriaDataFactory;
import org.rudi.microservice.projekt.storage.dao.project.ProjectCustomDao;
import org.rudi.microservice.projekt.storage.dao.project.ProjectDao;
import org.rudi.microservice.projekt.storage.entity.DatasetConfidentiality;
import org.rudi.microservice.projekt.storage.entity.OwnerType;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetStatus;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;

@ProjectSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ProjectCustomDaoUT {

	private final ProjectCustomDao projectCustomDao;
	private final ProjectDao projectDao;
	private final ProjectDataFactory projectDataFactory;
	private final LinkedDatasetDataFactory linkedDatasetDataFactory;
	private final NewDatasetRequestDataFactory newDatasetRequestDataFactory;
	private final ProjectSearchCriteriaDataFactory projectSearchCriteriaDataFactory;
	private final EnhancedProjectSearchCriteriaDataFactory enhancedProjectSearchCriteriaDataFactory;
	private final ComputeIndicatorsSearchCriteriaDataFactory computeIndicatorsSearchCriteriaDataFactory;

	final UUID organization1Uuid = UUID.randomUUID();
	final UUID organization2Uuid = UUID.randomUUID();

	private ProjectEntity project1;
	private ProjectEntity project2;
	private ProjectEntity project3;
	private ProjectEntity project4;
	private ProjectEntity project5NotValidated;

	@BeforeEach
	void setUp() {
		project1 = projectDataFactory.getOrCreateProjectWithRelatedOrganizationsOnly("p1-o1-validated",
				ProjectStatus.VALIDATED, organization1Uuid, OwnerType.ORGANIZATION, null, null);

		project2 = projectDataFactory.getOrCreateProjectWithRelatedOrganizationsOnly("p2-o1-validated_r-o2-accepted",
				ProjectStatus.VALIDATED, organization1Uuid, OwnerType.ORGANIZATION, Set.of(organization2Uuid),
				org.rudi.microservice.projekt.storage.entity.relatedorganization.RelationStatus.ACCEPTED);

		project3 = projectDataFactory.getOrCreateProjectWithRelatedOrganizationsOnly("p3-o1-validated_r-o2-pending",
				ProjectStatus.VALIDATED, organization1Uuid, OwnerType.ORGANIZATION, Set.of(organization2Uuid),
				org.rudi.microservice.projekt.storage.entity.relatedorganization.RelationStatus.PENDING);

		project4 = projectDataFactory.getOrCreateProjectWithRelatedOrganizationsOnly("p4-o2-validated",
				ProjectStatus.VALIDATED, organization2Uuid, OwnerType.ORGANIZATION, null, null);

		project5NotValidated = projectDataFactory.getOrCreateProjectWithRelatedOrganizationsOnly(
				"p5-o1-not-validated_r-o2-pending", ProjectStatus.DRAFT, organization1Uuid, OwnerType.ORGANIZATION,
				Set.of(organization2Uuid),
				org.rudi.microservice.projekt.storage.entity.relatedorganization.RelationStatus.PENDING);

	}

	@Test
	@DisplayName("Test getNumberOfProjectsPerRelatedOrganizations")
	void testGetNumberOfProjectsPerRelatedOrganizations() {

		ProjectSearchCriteria projectSearchCriteria = projectSearchCriteriaDataFactory
				.createWithProjectStatusRelationStatusAndRelatedOrganizationUuids(
						List.of(org.rudi.microservice.projekt.core.bean.ProjectStatus.VALIDATED),
						List.of(RelationStatus.ACCEPTED), List.of(organization1Uuid, organization2Uuid));

		List<ProjectByOrganization> projectByOrganizations = projectCustomDao
				.getNumberOfProjectsPerRelatedOrganizations(projectSearchCriteria);

		ProjectByOrganization organization1 = projectByOrganizations.stream()
				.filter(p -> p.getOrganizationUuid().equals(organization1Uuid)).findFirst().orElse(null);
		// Aucun projet n'a pour organisation partenaire l'organisation 1
		assertThat(organization1).isNull();

		ProjectByOrganization organization2 = projectByOrganizations.stream()
				.filter(p -> p.getOrganizationUuid().equals(organization2Uuid)).findFirst().orElse(null);
		assertThat(organization2).isNotNull();
		// OK : projet 2 est validé et a pour organisation partenaire l'organisation 2 avec un statut ACCEPTED
		// non compté : projet 3 est validé et a pour organisation partenaire l'organisation 2 avec un statut PENDING
		// non compté : projet 5 n'est pas valide
		assertThat(organization2.getProjectCount()).isEqualTo(1);

	}

	@Test
	@DisplayName("Test testGetNumberOfProjectsPerOwners")
	void testGetNumberOfProjectsPerOwners() {

		ProjectSearchCriteria projectSearchCriteria = projectSearchCriteriaDataFactory
				.createWithProjectStatusAndOwnerUuids(
						List.of(org.rudi.microservice.projekt.core.bean.ProjectStatus.VALIDATED),
						List.of(organization1Uuid, organization2Uuid));

		List<ProjectByOrganization> projectByOrganizations = projectCustomDao
				.getNumberOfProjectsPerOwners(enhancedProjectSearchCriteriaDataFactory
						.createFromProjectSearchCriteria(projectSearchCriteria));

		assertThat(projectByOrganizations).hasSize(2);

		ProjectByOrganization organization1 = projectByOrganizations.stream()
				.filter(p -> p.getOrganizationUuid().equals(organization1Uuid)).findFirst().orElse(null);
		assertThat(organization1).isNotNull();
		// projet 1, 2 et 3 sont validés et ont pour owner l'organisation 1
		assertThat(organization1.getProjectCount()).isEqualTo(3);

		ProjectByOrganization organization2 = projectByOrganizations.stream()
				.filter(p -> p.getOrganizationUuid().equals(organization2Uuid)).findFirst().orElse(null);
		assertThat(organization2).isNotNull();
		// projet 4 est validé et a pour owner l'organisation 2
		assertThat(organization2.getProjectCount()).isEqualTo(1);

	}

	@Test
	@DisplayName("Test searchProjects")
	void test_searchProjects_nominal() {
		ProjectSearchCriteria projectSearchCriteria = projectSearchCriteriaDataFactory
				.createWithOwnerUuids(List.of(organization1Uuid));

		Page<ProjectEntity> page = projectCustomDao.searchProjects(projectSearchCriteria, Pageable.unpaged());

		assertThat(page.getContent()).extracting(ProjectEntity::getUuid).contains(project1.getUuid());
	}

	@Test
	@DisplayName("Test findProjectByNewDatasetRequestUuid")
	void test_findProjectByNewDatasetRequestUuid_nominal() {
		UUID newDatasetRequestUuid = UUID.randomUUID();
		NewDatasetRequestEntity request = newDatasetRequestDataFactory.createNotPersisted("Demande de jeu de données",
				org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestStatus.DRAFT);
		request.setUuid(newDatasetRequestUuid);
		ProjectEntity project = projectDataFactory.createProjectWithRequests(organization1Uuid, OwnerType.ORGANIZATION,
				request);

		ProjectEntity found = projectCustomDao.findProjectByNewDatasetRequestUuid(newDatasetRequestUuid);

		assertThat(found.getUuid()).isEqualTo(project.getUuid());
	}

	@Test
	@DisplayName("Test findProjectByLinkedDatasetUuid")
	void test_findProjectByLinkedDatasetUuid_nominal() {
		ProjectEntity project = projectDataFactory.getOrCreateProjectWithLinkedDatasetsOnly(
				"p-findByLinkedDataset-" + organization1Uuid, organization1Uuid, OwnerType.ORGANIZATION, 1);
		UUID linkedDatasetUuid = project.getLinkedDatasets().iterator().next().getUuid();

		ProjectEntity found = projectCustomDao.findProjectByLinkedDatasetUuid(linkedDatasetUuid);

		assertThat(found.getUuid()).isEqualTo(project.getUuid());
	}

	@Test
	@DisplayName("Test computeProjectInfos")
	void test_computeProjectInfos_nominal() {
		UUID producerUuid = UUID.randomUUID();
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("p-computeInfos-" + organization1Uuid,
				organization1Uuid, OwnerType.ORGANIZATION);
		LinkedDatasetEntity restrictedLinkedDataset = linkedDatasetDataFactory.createNotPersisted(UUID.randomUUID(),
				producerUuid, LinkedDatasetStatus.DRAFT, DatasetConfidentiality.RESTRICTED);
		project.getLinkedDatasets().add(restrictedLinkedDataset);
		projectDao.save(project);

		ComputeIndicatorsSearchCriteria searchCriteria = computeIndicatorsSearchCriteriaDataFactory
				.createWithProjectUuid(project.getUuid());

		Indicators indicators = projectCustomDao.computeProjectInfos(searchCriteria);

		assertThat(indicators.getNumberOfRequest()).isEqualTo(1);
		assertThat(indicators.getNumberOfProducer()).isEqualTo(1);
	}

	@Test
	@DisplayName("Test getNumberOfLinkedDatasets")
	void test_getNumberOfLinkedDatasets_nominal() {
		ProjectEntity project = projectDataFactory.getOrCreateProjectWithLinkedDatasetsOnly(
				"p-numberOfLinkedDatasets-" + organization1Uuid, organization1Uuid, OwnerType.ORGANIZATION, 2);

		Integer numberOfLinkedDatasets = projectCustomDao.getNumberOfLinkedDatasets(project.getUuid());

		assertThat(numberOfLinkedDatasets).isEqualTo(2);
	}

	@Test
	@DisplayName("Test getNumberOfNewRequests")
	void test_getNumberOfNewRequests_nominal() {
		ProjectEntity project = projectDataFactory.getOrCreateProjectWithNewDatasetRequestsOnly(
				"p-numberOfNewRequests-" + organization1Uuid, organization1Uuid, OwnerType.ORGANIZATION, 3);

		Integer numberOfNewRequests = projectCustomDao.getNumberOfNewRequests(project.getUuid());

		assertThat(numberOfNewRequests).isEqualTo(3);
	}

	@Test
	@DisplayName("Test searchProjectsPerOwner")
	void test_searchProjectsPerOwner_nominal() {
		EnhancedProjectSearchCriteria searchCriteria = enhancedProjectSearchCriteriaDataFactory
				.createWithProjectUuids(List.of(project1.getUuid()));

		Page<ProjectEntity> page = projectCustomDao.searchProjectsPerOwner(searchCriteria, Pageable.unpaged());

		assertThat(page.getContent()).extracting(ProjectEntity::getUuid).containsExactly(project1.getUuid());
	}

	@AfterEach
	void tearDown() {
		projectDataFactory.deleteAll();
	}

}
