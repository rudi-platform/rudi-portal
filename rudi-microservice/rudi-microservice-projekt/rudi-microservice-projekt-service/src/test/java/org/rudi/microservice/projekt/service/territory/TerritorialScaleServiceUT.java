package org.rudi.microservice.projekt.service.territory;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.common.core.json.JsonResourceReader;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.projekt.core.bean.TerritorialScale;
import org.rudi.microservice.projekt.core.bean.criteria.TerritorialScaleSearchCriteria;
import org.rudi.microservice.projekt.service.ProjectSpringBootTest;
import org.rudi.microservice.projekt.storage.dao.territory.TerritorialScaleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import lombok.RequiredArgsConstructor;
import lombok.val;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.setAllowComparingPrivateFields;

/**
 * Class de test de la couche service
 */
@ProjectSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class TerritorialScaleServiceUT {

	private static final String CODE_DE_L_ECHELLE_DU_TERRITOIRE_MEGALOPOLE = "megalopole";
	private static final String CODE_DE_L_ECHELLE_DU_TERRITOIRE_METROPOLE = "metropole";
	private static final String JSON_DE_L_ECHELLE_DU_TERRITOIRE_METROPOLE = "territorial-scales/metropole.json";

	private static final String TERRITORIAL_SCALE_CLOSE_DATE_NULL = "territorial-scales/closing-date-null.json";
	private static final String TERRITORIAL_SCALE_CLOSE_DATE_VALID = "territorial-scales/closing-date-valid.json";
	private static final String TERRITORIAL_SCALE_CLOSE_DATE_PASSED = "territorial-scales/closing-date-passed.json";

	private final TerritorialScaleService territorialScaleService;
	private final TerritorialScaleDao territorialScaleDao;

	private final JsonResourceReader jsonResourceReader;

	@AfterEach
	void tearDown() {
		territorialScaleDao.deleteAll();
	}

	private TerritorialScale createTerritorialScaleFromJson(String jsonPath) throws IOException, AppServiceException {
		final TerritorialScale territorialScale = jsonResourceReader.read(jsonPath, TerritorialScale.class);
		return territorialScaleService.createTerritorialScale(territorialScale);
	}

	@Test
	@DisplayName("Teste de la création d'un TerritorialScale sans UUID")
	void createTerritorialScaleWithoutUuid() throws IOException, AppServiceException {
		final TerritorialScale territorialScaleToCreate = jsonResourceReader.read(JSON_DE_L_ECHELLE_DU_TERRITOIRE_METROPOLE, TerritorialScale.class);
		territorialScaleToCreate.setUuid(null);

		final TerritorialScale createdTerritorialScale = territorialScaleService.createTerritorialScale(territorialScaleToCreate);

		assertThat(createdTerritorialScale.getUuid())
				.as("Même si on n'indique pas d'UUID à la création d'une échelle de territoire, il est automatiquement généré")
				.isNotNull();
	}

	@Test
	@DisplayName("Teste de la création d'un TerritorialScale avec un UUID")
	void createTerritorialScaleWithUuid() throws IOException, AppServiceException {
		final TerritorialScale territorialScaleToCreate = jsonResourceReader.read(JSON_DE_L_ECHELLE_DU_TERRITOIRE_METROPOLE, TerritorialScale.class);
		final UUID forcedUuid = UUID.randomUUID();
		territorialScaleToCreate.setUuid(forcedUuid);

		final TerritorialScale createdTerritorialScale = territorialScaleService.createTerritorialScale(territorialScaleToCreate);

		assertThat(createdTerritorialScale.getUuid())
				.as("Même si on indique un UUID à la création d'une échelle de territoire, il n'est pas pris en compte mais regénéré")
				.isNotEqualTo(forcedUuid);
	}

	@Test
	@DisplayName("Teste de la récupération d'un TerritorialScale par UUID")
	void getTerritorialScale() throws IOException, AppServiceException {
		final TerritorialScale territorialScaleToCreate = jsonResourceReader.read(JSON_DE_L_ECHELLE_DU_TERRITOIRE_METROPOLE, TerritorialScale.class);
		final TerritorialScale createdTerritorialScale = territorialScaleService.createTerritorialScale(territorialScaleToCreate);

		final TerritorialScale gotTerritorialScale = territorialScaleService.getTerritorialScale(createdTerritorialScale.getUuid());

		setAllowComparingPrivateFields(true);
		assertThat(gotTerritorialScale)
				.as("On retrouve l'échelle de territoire créée")
				.usingRecursiveComparison().isEqualTo(createdTerritorialScale);
	}

	@Test
	@DisplayName("Teste de la recherche des TerritorialScale")
	void searchTerritorialScales() throws IOException, AppServiceException {

		createTerritorialScaleFromJson(JSON_DE_L_ECHELLE_DU_TERRITOIRE_METROPOLE);
		createTerritorialScaleFromJson("territorial-scales/megalopole.json");

		val pageable = PageRequest.of(0, 2);
		final TerritorialScaleSearchCriteria searchCriteria = new TerritorialScaleSearchCriteria();
		final Page<TerritorialScale> territorialScales = territorialScaleService.searchTerritorialScales(searchCriteria, pageable);

		assertThat(territorialScales).as("On retrouve uniquement les échelles de territoire attendues")
				.extracting("code")
				.containsOnly(CODE_DE_L_ECHELLE_DU_TERRITOIRE_METROPOLE, CODE_DE_L_ECHELLE_DU_TERRITOIRE_MEGALOPOLE);
	}

	@Test
	@DisplayName("Teste de la recherche des TerritorialScale avec la variable Active")
	void searchTerritorialScalesWithActive() throws IOException, AppServiceException {

		final TerritorialScale territorialScaleWithCloseDateNull = createTerritorialScaleFromJson(TERRITORIAL_SCALE_CLOSE_DATE_NULL);
		final TerritorialScale territorialScaleWithCloseDateValid = createTerritorialScaleFromJson(TERRITORIAL_SCALE_CLOSE_DATE_VALID);
		final TerritorialScale territorialScaleWithCloseDatePassed = createTerritorialScaleFromJson(TERRITORIAL_SCALE_CLOSE_DATE_PASSED);

		val pageable = PageRequest.of(0, 10);
		final TerritorialScaleSearchCriteria searchCriteriaWithActive = TerritorialScaleSearchCriteria.builder().active(true).build();
		final TerritorialScaleSearchCriteria searchCriteriaWithoutActive = new TerritorialScaleSearchCriteria();
		final Page<TerritorialScale> territorialScalesWithActive = territorialScaleService.searchTerritorialScales(searchCriteriaWithActive, pageable);
		final Page<TerritorialScale> territorialScalesWithoutActive = territorialScaleService.searchTerritorialScales(searchCriteriaWithoutActive, pageable);

		assertThat(territorialScalesWithActive)
				.as("On retrouve les échelles de territoire avec une date de fin null").contains(territorialScaleWithCloseDateNull)
				.as("On retrouve les échelles de territoire avec une date de fin valide").contains(territorialScaleWithCloseDateValid)
				.as("On ne retrouve pas les échelles de territoire avec une date de fin passée").isNotIn(territorialScaleWithCloseDatePassed);

		assertThat(territorialScalesWithoutActive)
				.as("On retrouve les échelles de territoire avec une date de fin null").contains(territorialScaleWithCloseDateNull)
				.as("On retrouve les échelles de territoire avec une date de fin valide").contains(territorialScaleWithCloseDateValid)
				.as("On retrouve les échelles de territoire avec une date de fin passée").contains(territorialScaleWithCloseDatePassed);
	}

	@Test
	@DisplayName("Teste de la mise à jour des TerritorialScale")
	void updateTerritorialScale() throws IOException, AppServiceException {
		final TerritorialScale territorialScale = createTerritorialScaleFromJson(JSON_DE_L_ECHELLE_DU_TERRITOIRE_METROPOLE);
		territorialScale.setCode("nouveau_code");
		territorialScale.setLabel("Nouvelle étiquette");
		territorialScale.setOpeningDate(LocalDateTime.now());
		territorialScale.setClosingDate(LocalDateTime.now());
		territorialScale.setOrder(territorialScale.getOrder() + 1);

		final TerritorialScale updatedTerritorialScale = territorialScaleService.updateTerritorialScale(territorialScale);


		setAllowComparingPrivateFields(true);
		assertThat(updatedTerritorialScale)
				.as("Tous les champs sont bien modifiés")
				.usingRecursiveComparison().isEqualTo(territorialScale);
	}

	@Test
	@DisplayName("Teste de la suppression des TerritorialScale")
	void deleteTerritorialScale() throws IOException, AppServiceException {
		final long totalElementsBeforeCreate = countTerritorialScales();

		final TerritorialScale createdTerritorialScale = createTerritorialScaleFromJson(JSON_DE_L_ECHELLE_DU_TERRITOIRE_METROPOLE);
		final long totalElementsAfterCreate = countTerritorialScales();
		assertThat(totalElementsAfterCreate).as("L'échelle de territoire est bien créée").isEqualTo(totalElementsBeforeCreate + 1);

		territorialScaleService.deleteTerritorialScale(createdTerritorialScale.getUuid());
		final long totalElementsAfterDelete = countTerritorialScales();
		assertThat(totalElementsAfterDelete).as("L'échelle de territoire est bien supprimée").isEqualTo(totalElementsBeforeCreate);
	}

	private long countTerritorialScales() {
		val pageable = PageRequest.of(0, 100);
		return territorialScaleService.searchTerritorialScales(new TerritorialScaleSearchCriteria(), pageable).getTotalElements();
	}

}
