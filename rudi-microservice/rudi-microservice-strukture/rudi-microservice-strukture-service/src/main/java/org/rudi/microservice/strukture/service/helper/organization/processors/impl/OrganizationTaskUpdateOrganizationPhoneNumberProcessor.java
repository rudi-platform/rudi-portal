package org.rudi.microservice.strukture.service.helper.organization.processors.impl;

import org.rudi.microservice.strukture.service.helper.organization.processors.AbstractOrganizationTaskUpdateOrganizationAbstractAddressProcessor;
import org.rudi.microservice.strukture.storage.dao.address.AddressRoleDao;
import org.rudi.microservice.strukture.storage.entity.address.AddressType;
import org.rudi.microservice.strukture.storage.entity.address.TelephoneAddressEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizationTaskUpdateOrganizationPhoneNumberProcessor extends AbstractOrganizationTaskUpdateOrganizationAbstractAddressProcessor<TelephoneAddressEntity> {

	private static final String ACCEPTED_FIELD = "phoneNumber";

	public OrganizationTaskUpdateOrganizationPhoneNumberProcessor(AddressRoleDao addressRoleDao) {
		super(ACCEPTED_FIELD, AddressType.PHONE, TelephoneAddressEntity.class, addressRoleDao);
	}

	@Override
	protected void setValue(String value, TelephoneAddressEntity addressEntity) {
		addressEntity.setPhoneNumber(value);
	}
}
