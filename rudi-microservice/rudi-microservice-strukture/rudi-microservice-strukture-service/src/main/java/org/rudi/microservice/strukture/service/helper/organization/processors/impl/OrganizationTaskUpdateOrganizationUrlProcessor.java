package org.rudi.microservice.strukture.service.helper.organization.processors.impl;

import org.rudi.microservice.strukture.service.helper.organization.processors.AbstractOrganizationTaskUpdateOrganizationAbstractAddressProcessor;
import org.rudi.microservice.strukture.storage.dao.address.AddressRoleDao;
import org.rudi.microservice.strukture.storage.entity.address.AddressType;
import org.rudi.microservice.strukture.storage.entity.address.WebsiteAddressEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizationTaskUpdateOrganizationUrlProcessor
		extends AbstractOrganizationTaskUpdateOrganizationAbstractAddressProcessor<WebsiteAddressEntity> {

	private static final String ACCEPTED_FIELD = "url";

	public OrganizationTaskUpdateOrganizationUrlProcessor(AddressRoleDao addressRoleDao) {
		super(ACCEPTED_FIELD, AddressType.WEBSITE, WebsiteAddressEntity.class, addressRoleDao);
	}

	@Override
	protected void setValue(String value, WebsiteAddressEntity addressEntity) {
		addressEntity.setUrl(value);
	}
}


