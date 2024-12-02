package org.rudi.microservice.kalim.service.integration.impl.validator.metadata;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.MetadataMetadataInfo;
import org.rudi.facet.kaccess.constant.ConstantMetadata;
import org.rudi.facet.kaccess.constant.RudiMetadataField;
import org.rudi.microservice.kalim.service.IntegrationError;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.springframework.stereotype.Component;

import static org.rudi.facet.kaccess.constant.RudiMetadataField.METADATA_INFO_PROVIDER;

@Component
public class MetadataInfoValidator extends AbstractMetadataValidator<MetadataMetadataInfo> {

	@Override
	public Set<IntegrationRequestErrorEntity> validate(MetadataMetadataInfo metadataMetadataInfo) {
		Set<IntegrationRequestErrorEntity> integrationRequestsErrors = new HashSet<>();
		if (metadataMetadataInfo == null) {
			String errorMessage = String.format(IntegrationError.ERR_202.getMessage(),
					RudiMetadataField.METADATA_INFO.getLocalName());
			IntegrationRequestErrorEntity integrationRequestError = new IntegrationRequestErrorEntity(UUID.randomUUID(),
					IntegrationError.ERR_202.getCode(), errorMessage, RudiMetadataField.METADATA_INFO.getLocalName(),
					LocalDateTime.now());

			integrationRequestsErrors.add(integrationRequestError);
		} else if (!ArrayUtils.contains(ConstantMetadata.SUPPORTED_METADATA_VERSIONS,
				metadataMetadataInfo.getApiVersion())) {
			String errorMessage = String.format(IntegrationError.ERR_106.getMessage(),
					metadataMetadataInfo.getApiVersion(), ConstantMetadata.CURRENT_METADATA_VERSION);
			IntegrationRequestErrorEntity integrationRequestError = new IntegrationRequestErrorEntity(UUID.randomUUID(),
					IntegrationError.ERR_106.getCode(), errorMessage,
					RudiMetadataField.METADATA_INFO_API_VERSION.getLocalName(), LocalDateTime.now());

			integrationRequestsErrors.add(integrationRequestError);
		} else if (metadataMetadataInfo.getApiVersion().equals(ConstantMetadata.CURRENT_METADATA_VERSION) && metadataMetadataInfo.getMetadataProvider() != null) {
			String errorMessage = String.format(IntegrationError.ERR_307.getMessage(),
					metadataMetadataInfo.getMetadataProvider().getOrganizationName(), METADATA_INFO_PROVIDER);
			IntegrationRequestErrorEntity integrationRequestError = new IntegrationRequestErrorEntity(UUID.randomUUID(),
					IntegrationError.ERR_307.getCode(), errorMessage,
					RudiMetadataField.METADATA_INFO_API_VERSION.getLocalName(), LocalDateTime.now());
			integrationRequestsErrors.add(integrationRequestError);
		}
		return integrationRequestsErrors;
	}

	@Override
	protected MetadataMetadataInfo getMetadataElementToValidate(Metadata metadata) {
		return metadata.getMetadataInfo();
	}
}
