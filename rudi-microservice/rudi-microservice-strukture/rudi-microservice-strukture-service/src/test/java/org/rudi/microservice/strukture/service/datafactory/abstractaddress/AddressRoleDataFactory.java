package org.rudi.microservice.strukture.service.datafactory.abstractaddress;

import org.rudi.common.service.datafactory.AbstractStampedDataFactory;
import org.rudi.microservice.strukture.storage.dao.address.AddressRoleDao;
import org.rudi.microservice.strukture.storage.entity.address.AddressRoleEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class AddressRoleDataFactory extends AbstractStampedDataFactory<AddressRoleEntity, AddressRoleDao> {
	public AddressRoleDataFactory(AddressRoleDao repository) {
		super(repository, AddressRoleEntity.class);
	}


	@Override
	protected void assignData(AddressRoleEntity item) {
		super.assignData(item);
	}
}
