package org.rudi.microservice.strukture.service.datafactory.abstractaddress;

import org.rudi.microservice.strukture.storage.dao.address.AddressRoleDao;
import org.rudi.microservice.strukture.storage.entity.address.AddressRoleEntity;
import org.rudi.microservice.strukture.storage.entity.address.AddressType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class EmailAddressRoleDataFactory extends AddressRoleDataFactory{

	public EmailAddressRoleDataFactory(AddressRoleDao repository) {
		super(repository);
	}

	@Override
	protected void assignData(AddressRoleEntity item) {
		item.setType(AddressType.EMAIL);
	}
}
