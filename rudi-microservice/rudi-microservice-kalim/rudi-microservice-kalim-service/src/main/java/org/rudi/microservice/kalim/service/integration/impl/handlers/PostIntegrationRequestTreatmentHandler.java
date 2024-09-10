package org.rudi.microservice.kalim.service.integration.impl.handlers;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rudi.facet.apigateway.exceptions.ApiGatewayApiException;
import org.rudi.facet.apigateway.exceptions.CreateApiException;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.microservice.kalim.service.helper.ApiManagerHelper;
import org.rudi.microservice.kalim.service.helper.Error500Builder;
import org.rudi.microservice.kalim.service.integration.impl.validator.authenticated.DatasetCreatorIsAuthenticatedValidator;
import org.rudi.microservice.kalim.service.integration.impl.validator.authenticated.MetadataInfoProviderIsAuthenticatedValidator;
import org.rudi.microservice.kalim.service.integration.impl.validator.metadata.AbstractMetadataValidator;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Primary
@Slf4j
public class PostIntegrationRequestTreatmentHandler extends AbstractIntegrationRequestTreatmentHandlerWithValidation {

	protected PostIntegrationRequestTreatmentHandler(DatasetService datasetService,
			ApiManagerHelper apigatewayManagerHelper, ObjectMapper objectMapper,
			List<AbstractMetadataValidator<?>> metadataValidators, Error500Builder error500Builder,
			MetadataInfoProviderIsAuthenticatedValidator metadataInfoProviderIsAuthenticatedValidator,
			DatasetCreatorIsAuthenticatedValidator datasetCreatorIsAuthenticatedValidator,
			OrganizationHelper organizationHelper) {
		super(datasetService, apigatewayManagerHelper, objectMapper, metadataValidators,
				error500Builder, metadataInfoProviderIsAuthenticatedValidator, datasetCreatorIsAuthenticatedValidator,
				organizationHelper);
	}

	@Override
	protected void treat(IntegrationRequestEntity integrationRequest, Metadata metadata)
			throws DataverseAPIException, ApiGatewayApiException {
		final String doi = datasetService.createDataset(metadata);
		try {
			final Metadata metadataCreated = datasetService.getDataset(doi);
			createApi(integrationRequest, metadataCreated);
		} catch (final ApiGatewayApiException | RuntimeException e) {
			log.error("On va supprimer le JDD qui vient d'être créé car une erreur est survenue", e);
			datasetService.deleteDataset(doi);
			throw e;
		}
	}

	private void createApi(IntegrationRequestEntity integrationRequest, Metadata metadataCreated)
			throws DataverseAPIException, ApiGatewayApiException {
		try {
			apiGatewayManagerHelper.createApis(integrationRequest, metadataCreated);
		} catch (final CreateApiException | RuntimeException e) {
			datasetService.deleteDataset(metadataCreated.getGlobalId());
			throw e;
		}
	}

}
