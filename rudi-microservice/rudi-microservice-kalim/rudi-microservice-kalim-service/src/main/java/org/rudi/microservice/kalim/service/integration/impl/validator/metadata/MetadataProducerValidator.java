package org.rudi.microservice.kalim.service.integration.impl.validator.metadata;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.Organization;
import org.rudi.facet.kaccess.constant.RudiMetadataField;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationException;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.kalim.service.IntegrationError.ERR_105;
import static org.rudi.microservice.kalim.service.IntegrationError.ERR_113;

@Component
public class MetadataProducerValidator extends AbstractMetadataValidator<Organization> {

	private final OrganizationHelper organizationHelper;

	public MetadataProducerValidator(OrganizationHelper organizationHelper) {
		this.organizationHelper = organizationHelper;
	}

	@Override
	protected Organization getMetadataElementToValidate(Metadata metadata) {
		return metadata.getProducer();
	}

	@Override
	public Set<IntegrationRequestErrorEntity> validate(Organization organization) {
		final Set<IntegrationRequestErrorEntity> errors = new HashSet<>();
		//vérifie l'existence du producer par UUID
		try {
			if (organizationHelper.getOrganization(organization.getOrganizationId()) == null) {
				//ajout de l'IntégrationRequestError -> L'organisation n'existe pas
				errors.add(new IntegrationRequestErrorEntity(ERR_113.getCode(), ERR_113.getMessage()));
			}
		} catch (GetOrganizationException e) {
			//ajout de l'IntégrationRequestError -> Erreur inconnue
			errors.add(new IntegrationRequestErrorEntity(UUID.randomUUID(), ERR_105.getCode(), ERR_105.getMessage(), RudiMetadataField.PRODUCER.getName(), LocalDateTime.now()));
		}
		return errors;
	}
}
