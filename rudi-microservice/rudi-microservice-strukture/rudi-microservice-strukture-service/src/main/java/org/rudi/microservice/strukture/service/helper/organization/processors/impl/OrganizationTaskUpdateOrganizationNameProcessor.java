package org.rudi.microservice.strukture.service.helper.organization.processors.impl;

import org.rudi.microservice.strukture.service.helper.organization.processors.AbstractOrganizationTaskUpdateOrganizationStringFieldProcessor;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizationTaskUpdateOrganizationNameProcessor
		extends AbstractOrganizationTaskUpdateOrganizationStringFieldProcessor {

	public OrganizationTaskUpdateOrganizationNameProcessor() {
		super(OrganizationEntity.FIELD_NAME);
	}

	@Override
	protected void assign(String value, OrganizationEntity organizationEntity) {
		// Champ requis : on ne le vide pas si aucune valeur n'est fournie.
		if (value != null) {
			organizationEntity.setName(value);
		}
	}
}
