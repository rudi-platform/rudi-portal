package org.rudi.microservice.kalim.service.integration.impl.handlers;

import java.util.List;
import java.util.Set;

import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.acl.helper.RolesHelper;
import org.rudi.facet.apigateway.exceptions.ApiGatewayApiException;
import org.rudi.facet.apigateway.exceptions.CreateApiException;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.providers.helper.ProviderHelper;
import org.rudi.microservice.kalim.service.helper.ApiManagerHelper;
import org.rudi.microservice.kalim.service.helper.Error500Builder;
import org.rudi.microservice.kalim.service.integration.impl.transformer.metadata.AbstractMetadataTransformer;
import org.rudi.microservice.kalim.service.integration.impl.validator.authenticated.MetadataInfoProviderIsAuthenticatedValidator;
import org.rudi.microservice.kalim.service.integration.impl.validator.metadata.AbstractMetadataValidator;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestEntity;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Primary
@Slf4j
public class PostIntegrationRequestTreatmentHandler extends AbstractIntegrationRequestTreatmentHandlerWithValidation {

	protected PostIntegrationRequestTreatmentHandler(DatasetService datasetService,
			ApiManagerHelper apigatewayManagerHelper, ObjectMapper objectMapper,
			List<AbstractMetadataValidator<?>> metadataValidators,
			List<AbstractMetadataTransformer<?>> metadataTransformers, Error500Builder error500Builder,
			MetadataInfoProviderIsAuthenticatedValidator metadataInfoProviderIsAuthenticatedValidator,
			OrganizationHelper organizationHelper, ProviderHelper providerHelper, ACLHelper aclHelper,
			RolesHelper roleHelper) {
		super(datasetService, apigatewayManagerHelper, objectMapper, metadataValidators, metadataTransformers,
				error500Builder, metadataInfoProviderIsAuthenticatedValidator, organizationHelper, providerHelper,
				aclHelper, roleHelper);
	}

	@Override
	Set<IntegrationRequestErrorEntity> validateSpecificForOperation(IntegrationRequestEntity integrationRequest) {
		// Pas de vérification complémentaire nécessaire dans le cas du POST
		return Set.of();
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
