package org.rudi.microservice.projekt.service.support;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.common.core.json.JsonResourceReader;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.projekt.core.bean.Support;
import org.rudi.microservice.projekt.core.bean.criteria.SupportSearchCriteria;
import org.rudi.microservice.projekt.service.ProjectSpringBootTest;
import org.rudi.microservice.projekt.storage.dao.support.SupportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Class de test de la couche service
 */
@ProjectSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class SupportServiceUT {

	private static final KnownSupport TECHNIQUE = new KnownSupport("technique");
	private static final KnownSupport EXPERTISE = new KnownSupport("expertise");
	private static final KnownSupport SUPPORT_CLOSING_DATE_NULL = new KnownSupport("closing-date-null");
	private static final KnownSupport SUPPORT_CLOSING_DATE_VALID = new KnownSupport("closing-date-valid");
	private static final KnownSupport SUPPORT_CLOSING_DATE_PASSED = new KnownSupport("closing-date-passed");

	private final SupportService supportService;
	private final SupportDao supportDao;
	private final JsonResourceReader jsonResourceReader;

	@AfterEach
	void tearDown() {
		supportDao.deleteAll();
	}

	private Support createSupportFromJson(String jsonPath) throws IOException, AppServiceException {
		final Support support = jsonResourceReader.read(jsonPath, Support.class);
		return supportService.createSupport(support);
	}

	@Test
	@DisplayName("Teste de la création d'un Support sans UUID")
	void createSupportWithoutUuid() throws IOException, AppServiceException {
		final Support supportToCreate = jsonResourceReader.read(TECHNIQUE.getJsonPath(), Support.class);
		supportToCreate.setUuid(null);

		final Support createdSupport = supportService.createSupport(supportToCreate);

		assertThat(createdSupport.getUuid())
				.as("Même si on n'indique pas d'UUID à la création d'un type d'accompagnement, il est automatiquement généré")
				.isNotNull();
	}

	@Test
	@DisplayName("Teste de la création d'un Support avec un UUID")
	void createSupportWithUuid() throws IOException, AppServiceException {
		final Support supportToCreate = jsonResourceReader.read(TECHNIQUE.getJsonPath(), Support.class);
		final UUID forcedUuid = UUID.randomUUID();
		supportToCreate.setUuid(forcedUuid);

		final Support createdSupport = supportService.createSupport(supportToCreate);

		assertThat(createdSupport.getUuid())
				.as("Même si on indique un UUID à la création d'un type d'accompagnement, il n'est pas pris en compte mais regénéré")
				.isNotEqualTo(forcedUuid);
	}

	@Test
	@DisplayName("Teste de la récupération d'un Support par un UUID")
	void getSupport() throws IOException, AppServiceException {
		final Support supportToCreate = jsonResourceReader.read(TECHNIQUE.getJsonPath(), Support.class);
		final Support createdSupport = supportService.createSupport(supportToCreate);

		final Support gotSupport = supportService.getSupport(createdSupport.getUuid());

		assertThat(gotSupport)
				.as("On retrouve le type d'accompagnement créé")
				.isEqualToComparingFieldByField(createdSupport);
	}

	@Test
	@DisplayName("Teste de la recherche des Supports")
	void searchSupports() throws IOException, AppServiceException {

		createSupportFromJson(TECHNIQUE.getJsonPath());
		createSupportFromJson(EXPERTISE.getJsonPath());

		val pageable = PageRequest.of(0, 2);
		final SupportSearchCriteria searchCriteria = new SupportSearchCriteria();
		final Page<Support> supports = supportService.searchSupports(searchCriteria, pageable);

		assertThat(supports).as("On retrouve uniquement les types d'accompagnement attendus")
				.extracting("code")
				.containsOnly(TECHNIQUE.getCode(), EXPERTISE.getCode());
	}

	@Test
	@DisplayName("Teste de la variable active sur la recherche des Supports")
	void searchSupportsWithActiveDate() throws IOException, AppServiceException {

		final Support supportWithClosingDateNull = createSupportFromJson(SUPPORT_CLOSING_DATE_NULL.getJsonPath());
		final Support supportWithClosingDateValid = createSupportFromJson(SUPPORT_CLOSING_DATE_VALID.getJsonPath());
		final Support supportWithClosingDatePassed = createSupportFromJson(SUPPORT_CLOSING_DATE_PASSED.getJsonPath());

		val pageable = PageRequest.of(0, 10);

		final SupportSearchCriteria searchCriteriaWithActive = SupportSearchCriteria.builder().active(true).build();
		final SupportSearchCriteria searchCriteriaWithoutActive = SupportSearchCriteria.builder().active(false).build();

		final Page<Support> supportsWithActive = supportService.searchSupports(searchCriteriaWithActive, pageable);
		final Page<Support> supportsWithoutActive = supportService.searchSupports(searchCriteriaWithoutActive, pageable);

		assertThat(supportsWithActive)
				.as("On retrouve bien le support dont la closing date est null").contains(supportWithClosingDateNull)
				.as("On retrouve bien le support dont la closing date est valid").contains(supportWithClosingDateValid)
				.as("On ne retrouve pas le support dont la closing date est passé").isNotIn(supportWithClosingDatePassed);

		assertThat(supportsWithoutActive)
				.as("On retrouve bien le support dont la closing date est null").contains(supportWithClosingDateNull)
				.as("On retrouve bien le support dont la closing date est valid").contains(supportWithClosingDateValid)
				.as("On retrouve bien le support dont la closing date est dépassé").contains(supportWithClosingDatePassed);
	}

	@Test
	@DisplayName("Teste de la mise à jour d'un Support par UUID")
	void updateSupport() throws IOException, AppServiceException {
		final Support support = createSupportFromJson(TECHNIQUE.getJsonPath());
		support.setCode("nouveau_code");
		support.setLabel("Nouvelle étiquette");
		support.setOpeningDate(LocalDateTime.now());
		support.setClosingDate(LocalDateTime.now());
		support.setOrder(support.getOrder() + 1);

		final Support updatedSupport = supportService.updateSupport(support);

		assertThat(updatedSupport)
				.as("Tous les champs sont bien modifiés")
				.isEqualToComparingFieldByField(support);
	}

	@Test
	@DisplayName("Teste de la suppression d'un Support par UUID")
	void deleteSupport() throws IOException, AppServiceException {
		final long totalElementsBeforeCreate = countSupports();

		final Support createdSupport = createSupportFromJson(TECHNIQUE.getJsonPath());
		final long totalElementsAfterCreate = countSupports();
		assertThat(totalElementsAfterCreate).as("Le type d'accompagnement est bien créée").isEqualTo(totalElementsBeforeCreate + 1);

		supportService.deleteSupport(createdSupport.getUuid());
		final long totalElementsAfterDelete = countSupports();
		assertThat(totalElementsAfterDelete).as("Le type d'accompagnement est bien supprimée").isEqualTo(totalElementsBeforeCreate);
	}

	private long countSupports() {
		val pageable = PageRequest.of(0, 100);
		return supportService.searchSupports(new SupportSearchCriteria(), pageable).getTotalElements();
	}

	@Data
	private static class KnownSupport {
		private final String code;
		private UUID uuid;

		String getJsonPath() {
			return "supports/" + code + ".json";
		}
	}

}
