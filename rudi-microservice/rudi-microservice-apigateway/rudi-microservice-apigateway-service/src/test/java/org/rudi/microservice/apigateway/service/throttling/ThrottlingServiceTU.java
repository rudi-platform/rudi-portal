package org.rudi.microservice.apigateway.service.throttling;


import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.common.core.json.JsonResourceReader;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.apigateway.core.bean.Throttling;
import org.rudi.microservice.apigateway.core.bean.ThrottlingSearchCriteria;
import org.rudi.microservice.apigateway.service.ApigatewaySpringBootTest;
import org.rudi.microservice.apigateway.storage.dao.throttling.ThrottlingDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Classe de test de la couche service
 */
@ApigatewaySpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ThrottlingServiceTU {

	private final ThrottlingService throttlingService;
	private final ThrottlingDao throttlingDao;
	private final JsonResourceReader jsonResourceReader;

	private static final String JSON_CREATE = "throttling/create-null-uuid.json";
	private static final String JSON_CLOSE_DATE_VALID = "throttling/search-valid-close-date.json";
	private static final String JSON_CLOSE_DATE_PASSED = "throttling/search-old-close-date.json";
	private static final String JSON_CLOSE_DATE_NULL = "throttling/search-null-close-date.json";

	private Throttling createThrottling(String json) throws IOException, AppServiceException {
		final Throttling throttling = jsonResourceReader.read(json, Throttling.class);
		return throttlingService.createThrottling(throttling);
	}

	@AfterEach
	void tearDown() {
		throttlingDao.deleteAll();
	}

	@Test
	@DisplayName("Teste de la création d'un Throttling sans UUID (UUID null)")
	void createThrottling() throws IOException, AppServiceException {
		final Throttling throttlingToCreate = jsonResourceReader.read(JSON_CREATE, Throttling.class);
		final Throttling throttlingCreated = throttlingService.createThrottling(throttlingToCreate);
		assertThat(throttlingCreated.getUuid())
				.as("L'UUID ne doit pas être null").isNotNull();
	}

	@Test
	@DisplayName("Teste de la création d'un Throttling avec un UUID")
	void createThrottlingWithUUID() throws IOException, AppServiceException {
		final Throttling throttlingToCreate = jsonResourceReader.read(JSON_CREATE, Throttling.class);
		final UUID uuid = UUID.randomUUID();
		throttlingToCreate.setUuid(uuid);
		final Throttling throttlingCreated = throttlingService.createThrottling(throttlingToCreate);
		assertThat(throttlingCreated.getUuid())
				.as("L'UUID ne doit pas être égal à celui donné lors de la création").isNotEqualTo(uuid);
	}

	@Test
	@DisplayName("Teste de la création d'un Throttling avec un UUID déjà présent")
	void createThrottlingWithExistingUUID() throws IOException, AppServiceException {
		final Pageable pageable = Pageable.unpaged();
		final ThrottlingSearchCriteria searchCriteria = new ThrottlingSearchCriteria();
		final Page<Throttling> throttlings = throttlingService.searchThrottlings(searchCriteria, pageable);

		assertThat(throttlings).as("On s'assure que la base est vide")
				.isEmpty();

		final Throttling throttling1 = throttlingService.createThrottling(jsonResourceReader.read(JSON_CLOSE_DATE_VALID, Throttling.class));
		final Throttling throttlingWithSameUUID = jsonResourceReader.read(JSON_CLOSE_DATE_VALID, Throttling.class);
		throttlingWithSameUUID.setUuid(throttling1.getUuid());
		final Throttling throttling2 = throttlingService.createThrottling(throttlingWithSameUUID);

		assertThat(throttling1.getUuid())
				.as("Les UUIDs des 2 throttlings doivent être différents")
				.isNotEqualTo(throttling2.getUuid());
	}

	@Test
	@DisplayName("Teste la recherche d'un Throttling")
	void searchThrottling() throws IOException, AppServiceException {
		final Throttling closeDateValidToCreate = jsonResourceReader.read(JSON_CLOSE_DATE_VALID, Throttling.class);
		String code = "code";
		closeDateValidToCreate.setCode(code);
		final Throttling closeDateValid = throttlingService.createThrottling(closeDateValidToCreate);

		final Throttling closeDatePassed = createThrottling(JSON_CLOSE_DATE_PASSED);
		final Throttling closeDateNull = createThrottling(JSON_CLOSE_DATE_NULL);

		final Pageable pageable = Pageable.unpaged();
		final ThrottlingSearchCriteria searchCriteria = new ThrottlingSearchCriteria();
		final Page<Throttling> throttlings = throttlingService.searchThrottlings(searchCriteria, pageable);
		searchCriteria.setActive(true);
		final Page<Throttling> throttlingsWithActive = throttlingService.searchThrottlings(searchCriteria, pageable);

		final ThrottlingSearchCriteria searchCriteriaWithCode = new ThrottlingSearchCriteria();
		searchCriteriaWithCode.setCode(code);
		final Page<Throttling> throttlingsWithCode = throttlingService.searchThrottlings(searchCriteriaWithCode, pageable);


		assertThat(throttlings).as("On retrouve uniquement les Throttlings attendus")
				.containsOnly(closeDateValid, closeDatePassed, closeDateNull);

		assertThat(throttlingsWithActive).as("On retrouve uniquement les Throttlings attendus")
				.containsOnly(closeDateValid);

		assertThat(throttlingsWithCode).as("On retrouve uniquement les Throttlings attendus")
				.containsOnly(closeDateValid);
	}

	@Test
	@DisplayName("Teste la récupération d'un Throttling par UUID")
	void getThrottling() throws IOException, AppServiceException {
		final Throttling throttlingToGet = createThrottling(JSON_CREATE);

		final Throttling throttlingGet = throttlingService.getThrottling(throttlingToGet.getUuid());
		assertThat(throttlingGet)
				.as("Le Throttling récupéré doit être égale à celui passé lors de la création")
				.isEqualToComparingFieldByField(throttlingToGet);
	}

	@Test
	@DisplayName("Teste de la récupération d'un Throttling inexistant")
	void getThrottlingError() {
		assertThatThrownBy(() -> throttlingService.getThrottling(UUID.randomUUID()))
				.as("Une est exception est lancée").isInstanceOf(EmptyResultDataAccessException.class);
	}

	@Test
	@DisplayName("Teste la mise à jour d'un Throttling")
	void updateThrottling() throws IOException, AppServiceException, javax.persistence.NoResultException {
		final Throttling throttlingToModify = createThrottling(JSON_CREATE);
		throttlingToModify.setCode("TESTE");
		throttlingToModify.setLabel("TESTE");
		throttlingToModify.setOrder(5);
		throttlingToModify.setRate(5);
		throttlingToModify.setBurstCapacity(5);

		final Throttling throttlingUpdated = throttlingService.updateThrottling(throttlingToModify);
		assertThat(throttlingUpdated)
				.as("Le Throttling récupéré après la mise à jour doit être égale à celui passé en paramètre")
				.isEqualToComparingFieldByField(throttlingToModify);
	}

	@Test
	@DisplayName("Teste de la mise à jour d'un Throttling inexistant")
	void updateThrottlingError() {
		assertThatThrownBy(() -> throttlingService.updateThrottling(jsonResourceReader.read(JSON_CLOSE_DATE_VALID, Throttling.class)))
				.as("Une est exception est lancée").isInstanceOf(EmptyResultDataAccessException.class);
	}

	@Test
	@DisplayName("Teste la suppression d'un Throttling")
	void deleteThrottling() throws IOException, AppServiceException {
		final Throttling throttlingToDelete = createThrottling(JSON_CREATE);
		throttlingService.deleteThrottling(throttlingToDelete.getUuid());
		assertThatThrownBy(() -> throttlingDao.findByUUID(throttlingToDelete.getUuid()))
				.as("Le public throttling est bien supprimé").isInstanceOf(EmptyResultDataAccessException.class);
	}

	@Test
	@DisplayName("Teste la suppression d'un Throttling inexistant")
	void deleteThrottlingError() {
		assertThatThrownBy(() -> throttlingService.deleteThrottling(UUID.randomUUID()))
				.as("Une est exception est lancée").isInstanceOf(EmptyResultDataAccessException.class);
	}
}
