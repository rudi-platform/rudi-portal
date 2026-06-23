package org.rudi.microservice.strukture.service.helper.organization.processors.impl;

import org.rudi.microservice.strukture.service.helper.organization.processors.AbstractOrganizationTaskUpdateOrganizationStringFieldProcessor;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizationTaskUpdateOrganizationUrlProcessor
		extends AbstractOrganizationTaskUpdateOrganizationStringFieldProcessor {

	public OrganizationTaskUpdateOrganizationUrlProcessor() {
		super(OrganizationEntity.FIELD_URL);
	}

	@Override
	protected void assign(String value, OrganizationEntity organizationEntity) {
		organizationEntity.setUrl(value);
	}
}


