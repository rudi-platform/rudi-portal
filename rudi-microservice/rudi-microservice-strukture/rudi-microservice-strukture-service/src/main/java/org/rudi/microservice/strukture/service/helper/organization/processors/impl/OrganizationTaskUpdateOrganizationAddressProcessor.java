package org.rudi.microservice.strukture.service.helper.organization.processors.impl;

import org.rudi.microservice.strukture.service.helper.organization.processors.AbstractOrganizationTaskUpdateOrganizationStringFieldProcessor;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizationTaskUpdateOrganizationAddressProcessor
		extends AbstractOrganizationTaskUpdateOrganizationStringFieldProcessor {

	public OrganizationTaskUpdateOrganizationAddressProcessor() {
		super(OrganizationEntity.FIELD_ADDRESS);
	}

	@Override
	protected void assign(String value, OrganizationEntity organizationEntity) {
		organizationEntity.setAddress(value);
	}
}


