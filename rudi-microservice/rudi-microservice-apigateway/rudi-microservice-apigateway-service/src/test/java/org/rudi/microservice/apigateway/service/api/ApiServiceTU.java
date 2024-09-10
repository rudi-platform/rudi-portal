package org.rudi.microservice.apigateway.service.api;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.common.core.json.JsonResourceReader;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.apigateway.core.bean.Api;
import org.rudi.microservice.apigateway.core.bean.ApiMethod;
import org.rudi.microservice.apigateway.core.bean.ApiParameter;
import org.rudi.microservice.apigateway.core.bean.ApiSearchCriteria;
import org.rudi.microservice.apigateway.core.bean.Throttling;
import org.rudi.microservice.apigateway.service.ApigatewaySpringBootTest;
import org.rudi.microservice.apigateway.service.throttling.ThrottlingService;
import org.rudi.microservice.apigateway.storage.dao.api.ApiDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.RequiredArgsConstructor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Class de test de la couche service de domaina
 */
@ApigatewaySpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ApiServiceTU {

	private final ApiService apiService;
	private final ThrottlingService throttlingService;
	private final ApiDao apiDao;
	private final JsonResourceReader jsonResourceReader;

	private static final String JSON_UUID_NULL = "api/create-null-uuid.json";

	private Api createApi(String json) throws IOException, AppServiceException {
		final Api api = jsonResourceReader.read(json, Api.class);

		final Api createdApi = apiService.createApi(api);
		if (createdApi.getParameters() == null) {
			createdApi.setParameters(new ArrayList<>());
		}
		if (createdApi.getThrottlings() == null) {
			createdApi.setThrottlings(new ArrayList<>());
		}
		return createdApi;
	}

	@AfterEach
	void tearDown() {
		apiDao.deleteAll();
	}

	@Test
	@DisplayName("Teste de la création d'une API sans UUID (null UUID)")
	void createApiTest() throws IOException, AppServiceException {
		final Api apiToCreate = jsonResourceReader.read(JSON_UUID_NULL, Api.class);
		final Api apiCreated = apiService.createApi(apiToCreate);
		assertThat(apiCreated.getUuid())
				.as("L'UUID ne doit pas être null").isNotNull();
	}

	@Test
	@DisplayName("Teste de la création d'une API avec un UUID")
	void createApiWithUUID() throws IOException, AppServiceException {
		final Api apiToCreate = jsonResourceReader.read(JSON_UUID_NULL, Api.class);
		final UUID uuid = UUID.randomUUID();
		apiToCreate.setUuid(uuid);

		final Api apiCreated = apiService.createApi(apiToCreate);
		assertThat(apiCreated.getUuid())
				.as("L'UUID ne doit pas être égal à celui donné lors de la création").isNotEqualTo(uuid);
	}

	@Test
	@DisplayName("Teste de la création d'une API avec un UUID déjà présent")
	void createApiWithExistingUUID() throws IOException, AppServiceException {
		final Pageable pageable = Pageable.unpaged();

		final ApiSearchCriteria searchCriteria = new ApiSearchCriteria();
		Page<Api> apis = apiService.searchApis(searchCriteria, pageable);

		assertThat(apis).as("On s'assure que la base est vide")
				.isEmpty();

		final Api api1 = apiService.createApi(jsonResourceReader.read(JSON_UUID_NULL, Api.class));
		apis = apiService.searchApis(searchCriteria, pageable);
		assertThat(apis.toList().size()).as("On s'assure qu'une API a été ajouté").isEqualTo(1);


		final Api apiWithSameUUID = jsonResourceReader.read(JSON_UUID_NULL, Api.class);
		apiWithSameUUID.setUuid(api1.getUuid());
		final Api api2 = apiService.createApi(apiWithSameUUID);

		apis = apiService.searchApis(searchCriteria, pageable);
		assertThat(apis.toList().size()).as("On s'assure que deux APIs ont été ajouté").isEqualTo(2);

		assertThat(api1.getUuid())
				.as("Les UUIDs des 2 apis doivent être différents")
				.isNotEqualTo(api2.getUuid());
	}

	@Test
	@DisplayName("Teste la recherche d'une API")
	void searchApi() throws IOException, AppServiceException {
		final Api api1 = createApi(JSON_UUID_NULL);
		final Api api2 = createApi(JSON_UUID_NULL);
		final Api api3 = createApi(JSON_UUID_NULL);

		final Pageable pageable = Pageable.unpaged();
		final ApiSearchCriteria searchCriteria = new ApiSearchCriteria();
		final Page<Api> apis = apiService.searchApis(searchCriteria, pageable);

		assertThat(apis).as("On retrouve uniquement les Apis attendus")
				.containsOnly(api1, api2, api3);
	}

	@Test
	@DisplayName("Teste la recherche d'une API avec des critères avec le tri")
	void searchApiWithCriteria() throws IOException, AppServiceException {
		final Api api1 = createApi(JSON_UUID_NULL);
		final Api api2 = apiService.createApi(new Api()
				.apiId(UUID.randomUUID())
				.globalId(UUID.randomUUID())
				.providerId(UUID.randomUUID())
				.nodeProviderId(UUID.randomUUID())
				.producerId(UUID.randomUUID())
				.mediaId(UUID.randomUUID())
				.contract("a")
				.url("https://fr.wikipedia.org/wiki/Okapi")
				.methods(Collections.emptyList())).parameters(Collections.emptyList()).throttlings(Collections.emptyList());

		final Api api3 = apiService.createApi(new Api()
				.apiId(UUID.randomUUID())
				.globalId(UUID.randomUUID())
				.providerId(UUID.randomUUID())
				.nodeProviderId(UUID.randomUUID())
				.producerId(UUID.randomUUID())
				.mediaId(UUID.randomUUID())
				.contract("b")
				.url("https://fr.wikipedia.org/wiki/Matrix_(film)")
				.methods(Collections.emptyList())).parameters(Collections.emptyList()).throttlings(Collections.emptyList());

		final Pageable pageable = Pageable.unpaged();

		final ApiSearchCriteria searchCriteria = new ApiSearchCriteria()
				.apiId(api3.getApiId())
				.globalId(api3.getGlobalId())
				.providerId(api3.getProviderId())
				.nodeProviderId(api3.getNodeProviderId())
				.producerId(api3.getProducerId())
				.mediaId(api3.getMediaId())
				.contract(api3.getContract())
				.url(api3.getUrl());

		final Page<Api> apis = apiService.searchApis(searchCriteria, pageable);

		assertThat(apis).as("On retrouve uniquement les Apis attendus")
				.containsOnly(api3);

		final Pageable pageableWithOrder = PageRequest.of(0, 3, Sort.by("contract"));
		final ApiSearchCriteria searchCriteriaWithOrder = new ApiSearchCriteria();
		final Page<Api> apisWithOrder = apiService.searchApis(searchCriteriaWithOrder, pageableWithOrder);
		List<Api> expectedResult = List.of(api2, api3, api1);
		List<Api> result = new ArrayList<>(apisWithOrder.getContent());
		assertThat(expectedResult).as("On verifie qu'on récupère bien les apis dans l'ordre demandé").isEqualTo(result);

		final Pageable pageablePage1 = PageRequest.of(0, 1, Sort.by("contract"));
		final Pageable pageablePage2 = PageRequest.of(1, 1, Sort.by("contract"));
		final Pageable pageablePage3 = PageRequest.of(2, 1, Sort.by("contract"));

		final Page<Api> apisPage1 = apiService.searchApis(searchCriteriaWithOrder, pageablePage1);
		final Page<Api> apisPage2 = apiService.searchApis(searchCriteriaWithOrder, pageablePage2);
		final Page<Api> apisPage3 = apiService.searchApis(searchCriteriaWithOrder, pageablePage3);

		assertThat(apisPage1).containsOnly(api2);
		assertThat(apisPage2).containsOnly(api3);
		assertThat(apisPage3).containsOnly(api1);
	}


	@Test
	@DisplayName("Teste la récupération d'une API par UUID")
	void getApi() throws IOException, AppServiceException {
		final Api apiToGet = createApi(JSON_UUID_NULL);
		apiToGet.setMethods(new ArrayList<>());
		apiToGet.setThrottlings(new ArrayList<>());
		apiToGet.setParameters(new ArrayList<>());
		final Api apiGet = apiService.getApi(apiToGet.getUuid());
		assertThat(apiGet)
				.as("L'API récupérée doit être égale à celui passé lors de la création")
				.usingRecursiveComparison()
				.isEqualTo(apiToGet);
	}

	@Test
	@DisplayName("Teste la récupération d'une API inexistante")
	void getApiNull() {
		assertThat(apiService.getApi(UUID.randomUUID()))
				.as("On doit récupérer un objet null")
				.isNull();
	}

	@Test
	@DisplayName("Teste la mise à jour d'une API")
	void updateApi() throws IOException, AppServiceException, javax.persistence.NoResultException {
		final Api apiToModify = createApi(JSON_UUID_NULL);
		apiToModify.setUrl("https://digitalbyopen.sharepoint.com/sites/myopenspace");
		apiToModify.setMethods(List.of(ApiMethod.POST, ApiMethod.GET));
		apiToModify.setThrottlings(List.of(throttlingService.createThrottling(new Throttling().code("teste1").label("taste1").order(1).openingDate(LocalDateTime.now().truncatedTo(ChronoUnit.NANOS)))));
		apiToModify.setParameters(List.of(new ApiParameter().name("teste1").value("teste1"), new ApiParameter().name("teste2").value("teste2")));

		Comparator<LocalDateTime> localDateTimeComparator = Comparator.comparing(dt -> dt.truncatedTo(ChronoUnit.SECONDS));

		final Api apiUpdated = apiService.updateApi(apiToModify);
		assertThat(apiUpdated)
				.as("Le Throttling récupéré après la mise à jour doit être égale à celui passé en paramètre")
				.usingRecursiveComparison()
				.withComparatorForType(localDateTimeComparator, LocalDateTime.class)
				.isEqualTo(apiToModify);
	}

	@Test
	@DisplayName("Teste la mise à jour d'une API inexistante")
	void updateApiError() throws IOException {
		final Api api = jsonResourceReader.read(JSON_UUID_NULL, Api.class);
		api.setUuid(UUID.fromString("27eaefd2-2b71-4ead-a496-eaa053ac98ee"));
		assertThatThrownBy(() -> apiService.updateApi(api))
				.as("Une est exception est lancée").isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("Teste la récupération d'une API inexistante")
	void deleteApi() throws IOException, AppServiceException {
		final Api apiToDelete = createApi(JSON_UUID_NULL);
		apiService.deleteApi(apiToDelete.getUuid());
		assertThat(apiDao.findByUuid(apiToDelete.getUuid()))
				.as("L'api est bien supprimé").isNull();
	}

	@Test
	@DisplayName("Teste la récupération d'une API inexistante")
	void deleteApiError() {
		assertThatThrownBy(() -> apiService.deleteApi(UUID.randomUUID()))
				.as("Une est exception est lancée").isInstanceOf(AppServiceException.class);
	}
}
