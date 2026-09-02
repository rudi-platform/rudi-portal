package org.rudi.microservice.strukture.service.helper.organization.processors.impl;

import org.rudi.microservice.strukture.service.helper.organization.processors.AbstractOrganizationTaskUpdateOrganizationAbstractAddressProcessor;
import org.rudi.microservice.strukture.storage.dao.address.AddressRoleDao;
import org.rudi.microservice.strukture.storage.entity.address.AddressType;
import org.rudi.microservice.strukture.storage.entity.address.EmailAddressEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizationTaskUpdateOrganizationEmailProcessor extends AbstractOrganizationTaskUpdateOrganizationAbstractAddressProcessor<EmailAddressEntity> {

	private static final String ACCEPTED_FIELD = "email";

	public OrganizationTaskUpdateOrganizationEmailProcessor(AddressRoleDao addressRoleDao) {
		super(ACCEPTED_FIELD, AddressType.EMAIL, EmailAddressEntity.class, addressRoleDao);
	}

	@Override
	protected void setValue(String value, EmailAddressEntity addressEntity) {
		addressEntity.setEmail(value);
	}
}
