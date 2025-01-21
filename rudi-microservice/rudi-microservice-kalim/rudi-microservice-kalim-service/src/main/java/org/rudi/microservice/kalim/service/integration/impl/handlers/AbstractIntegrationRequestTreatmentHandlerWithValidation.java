package org.rudi.microservice.kalim.service.integration.impl.handlers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.acl.helper.RolesHelper;
import org.rudi.facet.apigateway.exceptions.ApiGatewayApiException;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.Organization;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.providers.bean.Provider;
import org.rudi.facet.providers.helper.ProviderHelper;
import org.rudi.microservice.kalim.core.bean.IntegrationStatus;
import org.rudi.microservice.kalim.core.exception.IntegrationException;
import org.rudi.microservice.kalim.service.helper.ApiManagerHelper;
import org.rudi.microservice.kalim.service.helper.Error500Builder;
import org.rudi.microservice.kalim.service.integration.impl.validator.authenticated.MetadataInfoProviderIsAuthenticatedValidator;
import org.rudi.microservice.kalim.service.integration.impl.validator.metadata.AbstractMetadataValidator;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestEntity;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.springframework.beans.factory.annotation.Value;

import lombok.Getter;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_108;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_110;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_112;

public abstract class AbstractIntegrationRequestTreatmentHandlerWithValidation
		extends AbstractIntegrationRequestTreatmentHandler {

	protected final List<AbstractMetadataValidator<?>> metadataValidators;
	protected final MetadataInfoProviderIsAuthenticatedValidator metadataInfoProviderIsAuthenticatedValidator;
	protected final OrganizationHelper organizationHelper;

	@Getter
	@Value("${default.organization.password:12345678Mm$}")
	private String defaultOrganizationPassword;

	protected AbstractIntegrationRequestTreatmentHandlerWithValidation(DatasetService datasetService,
			ApiManagerHelper apiGatewayManagerHelper, ObjectMapper objectMapper,
			List<AbstractMetadataValidator<?>> metadataValidators, Error500Builder error500Builder,
			MetadataInfoProviderIsAuthenticatedValidator metadataInfoProviderIsAuthenticatedValidator,
			OrganizationHelper organizationHelper, ProviderHelper providerHelper, ACLHelper aclHelper, RolesHelper roleHelper) {
		super(datasetService, apiGatewayManagerHelper, error500Builder, providerHelper, objectMapper, aclHelper, roleHelper);
		this.metadataValidators = metadataValidators;
		this.metadataInfoProviderIsAuthenticatedValidator = metadataInfoProviderIsAuthenticatedValidator;
		this.organizationHelper = organizationHelper;
	}

	protected void handleInternal(IntegrationRequestEntity integrationRequest)
			throws IntegrationException, DataverseAPIException, ApiGatewayApiException {
		final var metadata = hydrateMetadata(integrationRequest.getFile());
		if (validateAndSetErrors(metadata, integrationRequest)) {
			final Provider provider = providerHelper.getProviderByNodeProviderUUID(integrationRequest.getNodeProviderId());

			if (provider != null) {
				Organization orgProvider = new Organization();
				orgProvider.setOrganizationId(provider.getUuid());
				orgProvider.setOrganizationName(provider.getLabel());
				metadata.getMetadataInfo().setMetadataProvider(orgProvider);
			}

			treat(integrationRequest, metadata);
			integrationRequest.setIntegrationStatus(IntegrationStatus.OK);
		} else {
			integrationRequest.setIntegrationStatus(IntegrationStatus.KO);
		}
	}

	private boolean validateAndSetErrors(Metadata metadata, IntegrationRequestEntity integrationRequest) {
		final Set<IntegrationRequestErrorEntity> errors = validate(metadata);
		if (!errors.isEmpty()) {
			integrationRequest.setErrors(errors);
			return errors.isEmpty();
		}

		UUID nodeProviderId = integrationRequest.getNodeProviderId();

		//vérifie que l'utilisateur est du bon type
		User user = aclHelper.getUserByLogin(nodeProviderId.toString());
		if (user == null || !isUserNodeProvider(nodeProviderId)) {
			errors.add(new IntegrationRequestErrorEntity(ERR_108.getCode(), ERR_108.getMessage()));
			integrationRequest.setErrors(errors);
			return errors.isEmpty();
		}

		final Provider provider = providerHelper.getFullProviderByNodeProviderUUID(nodeProviderId);

		if (provider == null) {
			// Le node provider n'est lié a aucun provider
			errors.add(new IntegrationRequestErrorEntity(ERR_110.getCode(), ERR_110.getMessage()));
			integrationRequest.setErrors(errors);
			return errors.isEmpty();
		}

		//vérifie que le provider du nodeProvider est liée à l'organization
		if (!isLinkedToOrganization(metadata, provider)) {
			// le provider n'est pas associé à l'organisation producer du JDD
			errors.add(new IntegrationRequestErrorEntity(ERR_112.getCode(), ERR_112.getMessage()));
		}

		//ajout des erreurs dans le rapport d'integration
		errors.addAll(validateSpecificForOperation(integrationRequest));

		// Sauvegarde des erreurs
		integrationRequest.setErrors(errors);

		return errors.isEmpty();
	}

	abstract Set<IntegrationRequestErrorEntity> validateSpecificForOperation(IntegrationRequestEntity integrationRequest);

	protected Set<IntegrationRequestErrorEntity> validate(Metadata metadata) {
		Set<IntegrationRequestErrorEntity> integrationRequestsErrors = new HashSet<>();

		metadataValidators.forEach(metadataValidator -> {
			if (metadataValidator.canBeUsedBy(this)) {
				integrationRequestsErrors.addAll(metadataValidator.validateMetadata(metadata));
			}
		});
		return integrationRequestsErrors;
	}

	protected abstract void treat(IntegrationRequestEntity integrationRequest, Metadata metadata)
			throws DataverseAPIException, ApiGatewayApiException;
}
