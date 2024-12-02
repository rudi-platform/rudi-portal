package org.rudi.microservice.kalim.service.integration.impl.handlers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.acl.helper.RolesHelper;
import org.rudi.facet.apigateway.exceptions.ApiGatewayApiException;
import org.rudi.facet.dataverse.api.exceptions.DatasetNotFoundException;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.providers.bean.Provider;
import org.rudi.facet.providers.helper.ProviderHelper;
import org.rudi.microservice.kalim.service.helper.ApiManagerHelper;
import org.rudi.microservice.kalim.service.helper.Error500Builder;
import org.rudi.microservice.kalim.service.integration.impl.validator.authenticated.MetadataInfoProviderIsAuthenticatedValidator;
import org.rudi.microservice.kalim.service.integration.impl.validator.metadata.AbstractMetadataValidator;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestEntity;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_109;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_110;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_111;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_112;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_500;

@Slf4j
@Component
public class PutIntegrationRequestTreatmentHandler extends AbstractIntegrationRequestTreatmentHandlerWithValidation {

	protected PutIntegrationRequestTreatmentHandler(DatasetService datasetService,
			ApiManagerHelper apigatewayManagerHelper, ObjectMapper objectMapper,
			List<AbstractMetadataValidator<?>> metadataValidators, Error500Builder error500Builder,
			MetadataInfoProviderIsAuthenticatedValidator metadataInfoProviderIsAuthenticatedValidator,
			OrganizationHelper organizationHelper, ProviderHelper providerHelper, ACLHelper aclHelper, RolesHelper roleHelper) {
		super(datasetService, apigatewayManagerHelper, objectMapper, metadataValidators, error500Builder,
				metadataInfoProviderIsAuthenticatedValidator,
				organizationHelper, providerHelper, aclHelper, roleHelper);
	}

	@Override
	protected void treat(IntegrationRequestEntity integrationRequest, Metadata metadata)
			throws DataverseAPIException, ApiGatewayApiException {
		// récupération des métadonnées à partir du globalid, pour récupérer le dataverse doi
		final Metadata actualMetadata = datasetService.getDataset(metadata.getGlobalId());
		metadata.setDataverseDoi(actualMetadata.getDataverseDoi());

		updateApi(integrationRequest, metadata, actualMetadata);
	}

	@Override
	Set<IntegrationRequestErrorEntity> validateSpecificForOperation(IntegrationRequestEntity integrationRequest) {
		//Vérification complémentaire dans le cas du PUT
		//Cohérence des données soumises par rapport à celles dans dataverse
		UUID nodeProviderId = integrationRequest.getNodeProviderId();
		UUID datasetGlobalId = integrationRequest.getGlobalId();
		final Set<IntegrationRequestErrorEntity> errors = new HashSet<>();

		Metadata datasetMetadata = null;
		// Test si le JDD existe
		try {
			datasetMetadata = datasetService.getDataset(datasetGlobalId);
			if (datasetMetadata == null) {
				errors.add(new IntegrationRequestErrorEntity(ERR_109.getCode(), ERR_109.getMessage()));
			}
		} catch (DatasetNotFoundException e) {
			errors.add(new IntegrationRequestErrorEntity(ERR_109.getCode(), ERR_109.getMessage()));
		} catch (DataverseAPIException e) {
			log.error("Erreur interne de la récupération du JDD d'UUID: {}", datasetGlobalId, e);
			errors.add(new IntegrationRequestErrorEntity(ERR_500.getCode(), ERR_500.getMessage()));
		}
		if (!errors.isEmpty()) {
			return errors;
		}

		final Provider provider = providerHelper.getFullProviderByNodeProviderUUID(nodeProviderId);

		if (provider == null) {
			// Le node provider n'est lié a aucun provider
			errors.add(new IntegrationRequestErrorEntity(ERR_110.getCode(), ERR_110.getMessage()));
			return errors;
		}

		//vérifie que le provider du nodeProvider est autorisé à réaliser cette demande
		if (!isSameProvider(datasetMetadata, provider)) {
			// le JDD est associé à un autre provider
			errors.add(new IntegrationRequestErrorEntity(ERR_111.getCode(), ERR_111.getMessage()));
		}

		//vérifie que le provider du nodeProvider est liée à l'organization
		if (!isLinkedToOrganization(datasetMetadata, provider)) {
			// le provider n'est pas associé à l'organisation producer du JDD
			errors.add(new IntegrationRequestErrorEntity(ERR_112.getCode(), ERR_112.getMessage()));
		}

		return errors;
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
