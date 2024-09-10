package org.rudi.microservice.projekt.service.project;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.common.core.json.JsonResourceReader;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.projekt.core.bean.ProjectType;
import org.rudi.microservice.projekt.core.bean.criteria.ProjectTypeSearchCriteria;
import org.rudi.microservice.projekt.service.ProjectSpringBootTest;
import org.rudi.microservice.projekt.service.type.ProjectTypeService;
import org.rudi.microservice.projekt.storage.dao.type.ProjectTypeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import lombok.RequiredArgsConstructor;
import lombok.val;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Class de test de la couche service
 */
@ProjectSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ProjectTypeServiceUT {

	private static final String CODE_DE_LA_CATEGORIE_INUTILE = "inutile";
	private static final String CODE_DE_LA_CATEGORIE_NULLE = "nul";
	private static final String JSON_DE_LA_CATEGORIE_INUTILE = "types/inutile.json";
	private static final String JSON_CATEGORIE_CLOSING_DATE_PASSED = "types/closing-date-null.json";
	private static final String JSON_CATEGORIE_CLOSING_DATE_VALID = "types/closing-date-valid.json";
	private static final String JSON_CATEGORIE_CLOSING_DATE_NULL = "types/closing-date-null.json";

	private final ProjectTypeService projectTypeService;
	private final ProjectTypeDao projectTypeDao;
	private final UtilPageable utilPageable;

	private final JsonResourceReader jsonResourceReader;

	@AfterEach
	void tearDown() {
		projectTypeDao.deleteAll();
	}

	private ProjectType createProjectTypeFromJson(String jsonPath) throws IOException, AppServiceException {
		final ProjectType projectType = jsonResourceReader.read(jsonPath, ProjectType.class);
		return getOrCreate(projectType);
	}

	@Test
	@DisplayName("Teste de la création d'un project type sans UUID")
	void createProjectTypeWithoutUuid() throws IOException, AppServiceException {
		final ProjectType projectTypeToCreate = jsonResourceReader.read(JSON_DE_LA_CATEGORIE_INUTILE, ProjectType.class);
		projectTypeToCreate.setUuid(null);

		final ProjectType createdProjectType = projectTypeService.createProjectType(projectTypeToCreate);

		assertThat(createdProjectType.getUuid())
				.as("Même si on n'indique pas d'UUID à la création d'un type de projet, il est automatiquement généré")
				.isNotNull();
	}

	@Test
	@DisplayName("Teste de la création d'un project type avec un UUID")
	void createProjectTypeWithUuid() throws IOException, AppServiceException {
		final ProjectType projectTypeToCreate = jsonResourceReader.read(JSON_DE_LA_CATEGORIE_INUTILE, ProjectType.class);
		final UUID forcedUuid = UUID.randomUUID();
		projectTypeToCreate.setUuid(forcedUuid);

		final ProjectType createdProjectType = projectTypeService.createProjectType(projectTypeToCreate);

		assertThat(createdProjectType.getUuid())
				.as("Même si on indique un UUID à la création d'un type de projet, il n'est pas pris en compte mais regénéré")
				.isNotEqualTo(forcedUuid);
	}

	@Test
	@DisplayName("Teste de la récupération d'un project type avec un UUID")
	void getProjectType() throws IOException, AppServiceException {
		final ProjectType projectTypeToCreate = jsonResourceReader.read(JSON_DE_LA_CATEGORIE_INUTILE, ProjectType.class);
		final ProjectType createdProjectType = projectTypeService.createProjectType(projectTypeToCreate);

		final ProjectType gotProjectType = projectTypeService.getProjectType(createdProjectType.getUuid());

		assertThat(gotProjectType)
				.as("On retrouve le type de projet créée")
				.isEqualToComparingFieldByField(createdProjectType);
	}

	@Test
	@DisplayName("Teste de la recherche des projects type")
	void searchProjectTypes() throws IOException, AppServiceException {

		createProjectTypeFromJson(JSON_DE_LA_CATEGORIE_INUTILE);
		createProjectTypeFromJson("types/nulle.json");

		val pageable = PageRequest.of(0, 2);
		final ProjectTypeSearchCriteria searchCriteria = new ProjectTypeSearchCriteria();
		final Page<ProjectType> types = projectTypeService.searchProjectTypes(searchCriteria, pageable);

		assertThat(types).as("On retrouve uniquement le type de projet attendue")
				.extracting("code")
				.containsOnly(CODE_DE_LA_CATEGORIE_INUTILE, CODE_DE_LA_CATEGORIE_NULLE);
	}

	@Test
	@DisplayName("Teste la variable active sur le search")
	void searchProjectTypesWithActiveDate() throws IOException, AppServiceException {
		final ProjectType projectClosingDateNull = createProjectTypeFromJson(JSON_CATEGORIE_CLOSING_DATE_NULL);
		final ProjectType projectClosingDateValid = createProjectTypeFromJson(JSON_CATEGORIE_CLOSING_DATE_VALID);
		final ProjectType projectClosingDatePassed = createProjectTypeFromJson(JSON_CATEGORIE_CLOSING_DATE_PASSED);

		final ProjectTypeSearchCriteria searchCriteriaWithActive = ProjectTypeSearchCriteria.builder().active(true).build();
		final ProjectTypeSearchCriteria searchCriteriaWithoutActive = ProjectTypeSearchCriteria.builder().active(false).build();
		final PageRequest pageable = PageRequest.of(0, 10);
		final Page<ProjectType> pageWithActive = projectTypeService.searchProjectTypes(searchCriteriaWithActive, pageable);
		final Page<ProjectType> pageWithoutActive = projectTypeService.searchProjectTypes(searchCriteriaWithoutActive, pageable);

		assertThat(pageWithActive).as("On retrouve bien le type dont la closing date est null")
				.contains(projectClosingDateNull)
				.as("On retrouve bien le type dont la closing date est valid")
				.contains(projectClosingDateValid)
				.as("On ne retrouve pas le type dont la closing date est dépassé")
				.isNotIn(projectClosingDatePassed);

		assertThat(pageWithoutActive).as("On retrouve bien le type dont la closing date est null")
				.contains(projectClosingDateNull)
				.as("On retrouve bien le type dont la closing date est valid")
				.contains(projectClosingDateValid)
				.as("On retrouve bien le type dont la closing date est dépassé")
				.contains(projectClosingDatePassed);
	}

	@Test
	@DisplayName("Teste la mise à jour d'un Porject Type")
	void updateProjectType() throws IOException, AppServiceException {
		final ProjectType projectType = createProjectTypeFromJson(JSON_DE_LA_CATEGORIE_INUTILE);
		projectType.setCode("nouveau_code");
		projectType.setLabel("Nouvelle étiquette");
		projectType.setOpeningDate(LocalDateTime.now());
		projectType.setClosingDate(LocalDateTime.now());
		projectType.setOrder(projectType.getOrder() + 1);

		final ProjectType updatedProjectType = projectTypeService.updateProjectType(projectType);

		assertThat(updatedProjectType)
				.as("Tous les champs sont bien modifiés")
				.isEqualToComparingFieldByField(projectType);
	}

	@Test
	@DisplayName("Teste la suppression d'un Porject Type")
	void deleteProjectType() throws IOException, AppServiceException {
		final long totalElementsBeforeCreate = countProjectTypes();

		final ProjectType createdProjectType = createProjectTypeFromJson(JSON_DE_LA_CATEGORIE_INUTILE);
		final long totalElementsAfterCreate = countProjectTypes();
		assertThat(totalElementsAfterCreate).as("Le type de projet est bien créée").isEqualTo(totalElementsBeforeCreate + 1);

		projectTypeService.deleteProjectType(createdProjectType.getUuid());
		final long totalElementsAfterDelete = countProjectTypes();
		assertThat(totalElementsAfterDelete).as("Le type de projet est bien supprimée").isEqualTo(totalElementsBeforeCreate);
	}

	private long countProjectTypes() {
		val pageable = PageRequest.of(0, 100);
		return projectTypeService.searchProjectTypes(new ProjectTypeSearchCriteria(), pageable).getTotalElements();
	}

	private ProjectType getOrCreate(ProjectType projectType) throws AppServiceException {
		ProjectType finalProjectType = null;
		//On tente de récuperer le projecType de base
		if (projectType != null) {
			finalProjectType = projectTypeService.getProjectTypeByCode(projectType.getCode());
		}
		//Si c'est la première tentative de création du projectType ayant ce code, on la crée
		if (finalProjectType == null && projectType != null) {
			return projectTypeService.createProjectType(projectType);
		}

		return finalProjectType;
	}

}
