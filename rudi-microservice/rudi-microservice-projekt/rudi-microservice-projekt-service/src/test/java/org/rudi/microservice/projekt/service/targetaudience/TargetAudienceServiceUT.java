package org.rudi.microservice.projekt.service.targetaudience;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.common.core.json.JsonResourceReader;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.projekt.core.bean.TargetAudience;
import org.rudi.microservice.projekt.core.bean.criteria.TargetAudienceSearchCriteria;
import org.rudi.microservice.projekt.service.ProjectSpringBootTest;
import org.rudi.microservice.projekt.storage.dao.targetaudience.TargetAudienceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Classe de test de la couche service
 */
@ProjectSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class TargetAudienceServiceUT {
	private static final String JSON_EVERYBODY = "targetaudience/everybody.json";

	private static final KnownTargetAudience TARGET_AUDIENCE_CLOSING_DATE_NULL = new KnownTargetAudience("closing-date-null");
	private static final KnownTargetAudience TARGET_AUDIENCE_CLOSING_DATE_VALID = new KnownTargetAudience("closing-date-valid");
	private static final KnownTargetAudience TARGET_AUDIENCE_CLOSING_DATE_PASSED = new KnownTargetAudience("closing-date-passed");

	private final TargetAudienceService targetAudienceService;
	private final TargetAudienceDao targetAudienceDao;
	private final JsonResourceReader jsonResourceReader;

	@AfterEach
	void tearDown() {
		targetAudienceDao.deleteAll();
	}

	private TargetAudience createTargetAudienceFromJson(String jsonPath) throws IOException, AppServiceException {
		final TargetAudience targetAudience = jsonResourceReader.read(jsonPath, TargetAudience.class);
		return targetAudienceService.createTargetAudience(targetAudience);
	}

	@Test
	@DisplayName("Teste la création d'une TargetAudience sans UUID")
	void createTargetAudienceWithoutUuid() throws IOException, AppServiceException {
		final TargetAudience targetAudienceToCreate = jsonResourceReader.read(JSON_EVERYBODY, TargetAudience.class);
		targetAudienceToCreate.setUuid(null);

		final TargetAudience createdTargetAudience = targetAudienceService.createTargetAudience(targetAudienceToCreate);

		assertThat(createdTargetAudience.getUuid())
				.as("Même si on n'indique pas d'UUID à la création d'un public cible, il est automatiquement généré")
				.isNotNull();
	}

	@Test
	@DisplayName("Teste la création d'une TargetAudience avec une UUID")
	void createTargetAudienceWithUuid() throws IOException, AppServiceException {
		final TargetAudience targetAudienceToCreate = jsonResourceReader.read(JSON_EVERYBODY, TargetAudience.class);
		final UUID forcedUuid = UUID.randomUUID();
		targetAudienceToCreate.setUuid(forcedUuid);

		final TargetAudience createdTargetAudience = targetAudienceService.createTargetAudience(targetAudienceToCreate);

		assertThat(createdTargetAudience.getUuid()).as(
						"Même si on indique un UUID à la création d'un public cible, il n'est pas pris en compte mais regénéré")
				.isNotEqualTo(forcedUuid);
	}

	@Test
	@DisplayName("Teste la récupération d'une TargetAudience")
	void searchTargetAudienceWithActive() throws IOException, AppServiceException {
		final TargetAudience targetAudienceWithClosingDateNull = createTargetAudienceFromJson(TARGET_AUDIENCE_CLOSING_DATE_NULL.getJsonPath());
		final TargetAudience targetAudienceWithClosingDateValid = createTargetAudienceFromJson(TARGET_AUDIENCE_CLOSING_DATE_VALID.getJsonPath());
		final TargetAudience targetAudienceWithClosingDatePassed = createTargetAudienceFromJson(TARGET_AUDIENCE_CLOSING_DATE_PASSED.getJsonPath());

		val pageable = PageRequest.of(0, 10);

		final TargetAudienceSearchCriteria searchCriteriaWithActive = TargetAudienceSearchCriteria.builder().active(true).build();
		final TargetAudienceSearchCriteria searchCriteriaWithoutActive = TargetAudienceSearchCriteria.builder().active(false).build();

		final Page<TargetAudience> targetsAudienceWithActive = targetAudienceService.searchTargetAudiences(searchCriteriaWithActive, pageable);
		final Page<TargetAudience> targetsAudienceWithoutActive = targetAudienceService.searchTargetAudiences(searchCriteriaWithoutActive, pageable);

		assertThat(targetsAudienceWithActive)
				.as("On retrouve bien les targets audience dont la date est null").contains(targetAudienceWithClosingDateNull)
				.as("On retrouve bien les targets audience dont la date est null").contains(targetAudienceWithClosingDateValid)
				.as("On ne retrouve pas les targets audience dont la date est null").isNotIn(targetAudienceWithClosingDatePassed);

		assertThat(targetsAudienceWithoutActive)
				.as("On retrouve bien les targets audience dont la date est null").contains(targetAudienceWithClosingDateNull)
				.as("On retrouve bien les targets audience dont la date est null").contains(targetAudienceWithClosingDateValid)
				.as("On retrouve bien les targets audience dont la date est null").contains(targetAudienceWithClosingDatePassed);
	}

	@Test
	@DisplayName("Teste de la recherche des Supports")
	void searchSupports() throws IOException, AppServiceException {

		createTargetAudienceFromJson(TARGET_AUDIENCE_CLOSING_DATE_NULL.getJsonPath());
		createTargetAudienceFromJson(TARGET_AUDIENCE_CLOSING_DATE_VALID.getJsonPath());
		createTargetAudienceFromJson(TARGET_AUDIENCE_CLOSING_DATE_PASSED.getJsonPath());

		val pageable = PageRequest.of(0, 3);
		final TargetAudienceSearchCriteria searchCriteria = new TargetAudienceSearchCriteria();
		final Page<TargetAudience> targetsAudience = targetAudienceService.searchTargetAudiences(searchCriteria, pageable);

		assertThat(targetsAudience).as("On retrouve uniquement les targets audience attendus")
				.extracting("code")
				.contains(TARGET_AUDIENCE_CLOSING_DATE_NULL.getCode(), TARGET_AUDIENCE_CLOSING_DATE_VALID.getCode(), TARGET_AUDIENCE_CLOSING_DATE_PASSED.getCode());
	}

	@Test
	@DisplayName("Teste la récupération d'une TargetAudience")
	void getTargetAudience() throws IOException, AppServiceException {
		final TargetAudience targetAudienceToCreate = jsonResourceReader.read(JSON_EVERYBODY, TargetAudience.class);
		final TargetAudience createdTargetAudience = targetAudienceService.createTargetAudience(targetAudienceToCreate);

		final TargetAudience gotTargetAudience = targetAudienceService
				.getTargetAudience(createdTargetAudience.getUuid());

		assertThat(gotTargetAudience).as("On retrouve le public cible créé")
				.isEqualToComparingFieldByField(createdTargetAudience);
	}

	@Test
	@DisplayName("Teste de la mise à jour d'une TargetAudience")
	void updateTargetAudience() throws IOException, AppServiceException {
		final TargetAudience targetAudience = createTargetAudienceFromJson(JSON_EVERYBODY);
		targetAudience.setCode("nouveau_code");
		targetAudience.setLabel("Nouvelle étiquette");
		targetAudience.setOpeningDate(LocalDateTime.now());
		targetAudience.setClosingDate(LocalDateTime.now());
		targetAudience.setOrder(targetAudience.getOrder() + 1);

		final TargetAudience updatedTargetAudience = targetAudienceService.updateTargetAudience(targetAudience);

		assertThat(updatedTargetAudience).as("Tous les champs sont bien modifiés")
				.isEqualToComparingFieldByField(targetAudience);
	}

	@Test
	@DisplayName("Teste de la suppression d'une TargetAudience")
	void deleteTargetAudience() throws IOException, AppServiceException {
		final TargetAudience createdTargetAudience = createTargetAudienceFromJson(JSON_EVERYBODY);
		targetAudienceService.deleteTargetAudience(createdTargetAudience.getUuid());
		assertThatThrownBy(() -> targetAudienceDao.findByUUID(createdTargetAudience.getUuid()))
				.as("Le public cible est bien supprimé").isInstanceOf(EmptyResultDataAccessException.class);
	}

	@Data
	private static class KnownTargetAudience {
		private final String code;
		private UUID uuid;

		String getJsonPath() {
			return "targetaudience/" + code + ".json";
		}
	}
}
