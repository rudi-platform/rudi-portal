package org.rudi.microservice.kalim.service.integration.impl.handlers;

import java.util.List;

import org.rudi.facet.apigateway.exceptions.ApiGatewayApiException;
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
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PutIntegrationRequestTreatmentHandler extends AbstractIntegrationRequestTreatmentHandlerWithValidation {

	protected PutIntegrationRequestTreatmentHandler(DatasetService datasetService,
			ApiManagerHelper apigatewayManagerHelper, ObjectMapper objectMapper,
			List<AbstractMetadataValidator<?>> metadataValidators, Error500Builder error500Builder,
			MetadataInfoProviderIsAuthenticatedValidator metadataInfoProviderIsAuthenticatedValidator,
			DatasetCreatorIsAuthenticatedValidator datasetCreatorIsAuthenticatedValidator,
			OrganizationHelper organizationHelper) {
		super(datasetService, apigatewayManagerHelper, objectMapper, metadataValidators, error500Builder,
				metadataInfoProviderIsAuthenticatedValidator, datasetCreatorIsAuthenticatedValidator,
				organizationHelper);
	}

	@Override
	protected void treat(IntegrationRequestEntity integrationRequest, Metadata metadata)
			throws DataverseAPIException, ApiGatewayApiException {
		// récupération des métadonnées à partir du globalid, pour récupérer le dataverse doi
		final Metadata actualMetadata = datasetService.getDataset(metadata.getGlobalId());
		metadata.setDataverseDoi(actualMetadata.getDataverseDoi());

		updateApi(integrationRequest, metadata, actualMetadata);
	}

	private void updateApi(IntegrationRequestEntity integrationRequest, Metadata metadata, Metadata actualMetadata)
			throws DataverseAPIException, ApiGatewayApiException {
		try {
			// Mise à jour du jeu de données dans le dataverse
			final Metadata metadataUpdated = datasetService.updateDataset(metadata);
			// mise à jour de l'API dans dans l'API Gateway
			apiGatewayManagerHelper.updateApis(integrationRequest, metadataUpdated, actualMetadata);
		} catch (final DataverseAPIException | ApiGatewayApiException | RuntimeException e) {
			// restauration du jdd dans le dataverse en cas d'erreur lors de la mise à jour dans l'API Gateway
			datasetService.updateDataset(actualMetadata);
			apiGatewayManagerHelper.synchronizeMedias(actualMetadata);
			throw e;
		}
	}
}
