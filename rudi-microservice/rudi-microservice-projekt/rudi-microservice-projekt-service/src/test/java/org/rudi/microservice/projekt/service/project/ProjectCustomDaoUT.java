package org.rudi.microservice.projekt.service.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.microservice.projekt.core.bean.ProjectByOrganization;
import org.rudi.microservice.projekt.core.bean.RelationStatus;
import org.rudi.microservice.projekt.core.bean.criteria.EnhancedProjectSearchCriteria;
import org.rudi.microservice.projekt.core.bean.criteria.ProjectSearchCriteria;
import org.rudi.microservice.projekt.service.ProjectSpringBootTest;
import org.rudi.microservice.projekt.service.datafactory.ProjectDataFactory;
import org.rudi.microservice.projekt.storage.dao.project.ProjectCustomDao;
import org.rudi.microservice.projekt.storage.dao.project.ProjectDao;
import org.rudi.microservice.projekt.storage.entity.OwnerType;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectStatus;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.RequiredArgsConstructor;

@ProjectSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ProjectCustomDaoUT {

	private final ProjectCustomDao projectCustomDao;
	private final ProjectDao projectDao;
	private final ProjectDataFactory projectDataFactory;

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

		ProjectSearchCriteria projectSearchCriteria = new ProjectSearchCriteria();
		projectSearchCriteria
				.setProjectStatus(List.of(org.rudi.microservice.projekt.core.bean.ProjectStatus.VALIDATED));
		projectSearchCriteria.setRelationStatus(List.of(RelationStatus.ACCEPTED));
		projectSearchCriteria.setRelatedOrganizationUuids(List.of(organization1Uuid, organization2Uuid));

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

		ProjectSearchCriteria projectSearchCriteria = new ProjectSearchCriteria();
		projectSearchCriteria
				.setProjectStatus(List.of(org.rudi.microservice.projekt.core.bean.ProjectStatus.VALIDATED));
		projectSearchCriteria.setOwnerUuids(List.of(organization1Uuid, organization2Uuid));

		List<ProjectByOrganization> projectByOrganizations = projectCustomDao
				.getNumberOfProjectsPerOwners(new EnhancedProjectSearchCriteria(projectSearchCriteria));

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

	@AfterEach
	void tearDown() {
		projectDataFactory.deleteAll();
	}

}
