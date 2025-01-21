package org.rudi.microservice.selfdata.service.helper.selfdatadataset;

import java.time.OffsetDateTime;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.apigateway.helper.ApiGatewayHelper;
import org.rudi.facet.kaccess.bean.Media;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.helper.selfdata.SelfdataMediaHelper;
import org.rudi.microservice.apigateway.core.bean.Api;
import org.rudi.microservice.selfdata.core.bean.BarChartData;
import org.rudi.microservice.selfdata.core.bean.GenericDataObject;
import org.rudi.microservice.selfdata.service.exception.InvalidSelfdataApisException;
import org.rudi.microservice.selfdata.service.exception.MissingApiForMediaException;
import org.rudi.microservice.selfdata.service.helper.apigateway.SelfdataApiGatewayHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SelfdataDatasetApisHelper {

	@Value("${tpbc.param.minDate:min-date}")
	private String minDateParam;

	@Value("${tpbc.param.maxDate:max-date}")
	private String maxDateParam;

	private final SelfdataMediaHelper selfdataMediaHelper;

	private final ApiGatewayHelper apiGatewayHelper;

	private final SelfdataApiGatewayHelper selfdataApiGatewayHelper;

	@Autowired
	@Qualifier("rudi_selfdata")
	private WebClient webClient;

	/**
	 * Récupère le média GDATA pour consommer les données personnelles de l'utilisateur connecté
	 *
	 * @param metadata le JDD selfdata
	 * @return le média GDATA du JDD
	 * @throws AppServiceException si erreur de malformation du JDD
	 */
	protected Media getGdataMedia(Metadata metadata) throws AppServiceException {

		if (metadata == null) {
			throw new AppServiceException("Impossible de chercher un média GDATA si aucun JDD fourni");
		}

		// Erreur 404 si JDD pas dans le bon état => ce n'est pas sensé arriver
		if (!selfdataMediaHelper.hasMandatoryMediasForAutomaticTreatment(metadata)) {
			throw new InvalidSelfdataApisException(metadata.getGlobalId());
		}

		// Recherche du média GDATA
		return selfdataMediaHelper.getGdataMedia(metadata.getAvailableFormats());
	}

	/**
	 * Récupère le média TPBC pour consommer les données personnelles de l'utilisateur connecté
	 *
	 * @param metadata le JDD selfdata
	 * @return le média TPBC du JDD
	 * @throws AppServiceException si erreur de malformation du JDD
	 */
	protected Media getTpbcMedia(Metadata metadata) throws AppServiceException {

		if (metadata == null) {
			throw new AppServiceException("Impossible de chercher un média TPBC si aucun JDD fourni");
		}

		// Erreur 404 si JDD pas dans le bon état => ce n'est pas sensé arriver
		if (!selfdataMediaHelper.hasMandatoryMediasForAutomaticTreatment(metadata)) {
			throw new InvalidSelfdataApisException(metadata.getGlobalId());
		}

		// Recherche du média TPBC
		return selfdataMediaHelper.getTpbcMedia(metadata.getAvailableFormats());
	}

	/**
	 * Appelle API Gateway pour récupérer les données personnelles
	 *
	 * @param parameters l'ensemble des pré-requis pour réaliser l'appel vers API Gateway
	 * @return les données personnelles au format GDATA
	 * @throws AppServiceException si erreur technique
	 */
	public GenericDataObject getGdataData(SelfdataApiParameters parameters) throws AppServiceException {

		if (parameters == null || parameters.getMetadata() == null || parameters.getUser() == null
				|| StringUtils.isBlank(parameters.getUser().getLogin())) {
			throw new AppServiceException("Paramètres obligatoires manquants");
		}

		Metadata metadata = parameters.getMetadata();

		// Recherche du média GDATA
		Media gdataMedia = getGdataMedia(metadata);
		if (gdataMedia == null) {
			throw new AppServiceException(
					"Malgré les contrôles, pas de média GDATA sur le JDD d'uuid : " + metadata.getGlobalId());
		}

		// orchestration des appels vers API Gateway
		ClientResponse response = callApiGatewayApi(gdataMedia, metadata, new LinkedMultiValueMap<>());

		return response.bodyToMono(GenericDataObject.class).block();
	}

	/**
	 * Appelle API Gateway pour récupérer les données personnelles
	 *
	 * @param parameters l'ensemble des pré-requis pour réaliser l'appel vers l'API Gateway
	 * @return les données personnelles au format TPBC
	 * @throws AppServiceException si erreur technique
	 */
	public BarChartData getTpbcData(SelfdataApiParameters parameters) throws AppServiceException {

		if (parameters == null || parameters.getMetadata() == null || parameters.getUser() == null
				|| StringUtils.isBlank(parameters.getUser().getLogin())) {
			throw new AppServiceException("Paramètres obligatoires manquants");
		}

		OffsetDateTime minDate = parameters.getMinDate();
		OffsetDateTime maxDate = parameters.getMaxDate();

		if (minDate != null && maxDate != null && minDate.isAfter(maxDate)) {
			throw new AppServiceBadRequestException("Période saisie non valide (dateMax < dateMin)");
		}

		Metadata metadata = parameters.getMetadata();

		// Recherche du média TPBC
		Media tpbcMedia = getTpbcMedia(metadata);
		if (tpbcMedia == null) {
			throw new AppServiceException(
					"Malgré les contrôles, pas de média TPBC sur le JDD d'uuid : " + metadata.getGlobalId());
		}

		// Gestion des paramètres de date en filtrage
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		if (minDate != null) {
			queryParams.add(minDateParam, minDate.toString());
		}
		if (maxDate != null) {
			queryParams.add(maxDateParam, maxDate.toString());
		}

		// orchestration des appels vers API Gateway
		ClientResponse response = callApiGatewayApi(tpbcMedia, metadata, queryParams);

		return response.bodyToMono(BarChartData.class).block();
	}

	/**
	 * Appel technique vers apigateway pour récupérer des données
	 *
	 * @param media       le media concerné par les données personnelles
	 * @param metadata    le JDD contenant le média
	 * @param queryParams paramètres dans l'URI optionnels à transmettre à l'API
	 * @return une réponse wrappant les données personnelles
	 * @throws AppServiceException problème d'appel vers l'API gateway
	 */
	private ClientResponse callApiGatewayApi(Media media, Metadata metadata, MultiValueMap<String, String> queryParams)
			throws AppServiceException {

		// Récupération du token initial de la request
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		String token = curRequest.getHeader(HttpHeaders.AUTHORIZATION);

		// récupération de l'objet API de la apigateway
		Api api = apiGatewayHelper.getApiById(metadata.getGlobalId(), media.getMediaId());

		// Check : a-t-on une API valide ?
		if (api == null || CollectionUtils.isEmpty(api.getMethods())) {
			throw new MissingApiForMediaException(metadata.getGlobalId(), media.getMediaId());
		}

		// Appel de l'API dans l'API Gateway
		return selfdataApiGatewayHelper.datasets(api, token, queryParams);

	}
}
