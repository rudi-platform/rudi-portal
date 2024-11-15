package org.rudi.microservice.strukture.service.datafactory.abstractaddress;

import java.util.UUID;

import org.apache.tika.utils.StringUtils;
import org.rudi.common.service.datafactory.AbstractDataFactory;
import org.rudi.microservice.strukture.storage.dao.address.AbstractAddressDao;
import org.rudi.microservice.strukture.storage.dao.address.EmailAddressDao;
import org.rudi.microservice.strukture.storage.entity.address.AddressType;
import org.rudi.microservice.strukture.storage.entity.address.EmailAddressEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional
@Component
@RequiredArgsConstructor
public class AbstractAddressDataFactory extends AbstractDataFactory {

	private final AbstractAddressDao abstractAddressDao;
	private final EmailAddressDao emailAddressDao;
	private final EmailAddressRoleDataFactory emailAddressRoleDataFactory;


	public EmailAddressEntity createEmailAddress(String addressRoleCode){
		addressRoleCode = StringUtils.isEmpty(addressRoleCode) ? "CONTACT" : addressRoleCode;

		try {
			EmailAddressEntity item = new EmailAddressEntity();
			item.setUuid(UUID.randomUUID());
			item.setType(AddressType.EMAIL);
			item.setAddressRole(emailAddressRoleDataFactory.getOrCreate(addressRoleCode, addressRoleCode.toLowerCase()));
			item.setEmail("un-email@de-contact.com");

			return emailAddressDao.save(item);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to create item for " + EmailAddressEntity.class, e);
		}
	}
}
