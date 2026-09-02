package org.rudi.microservice.strukture.service.helper.organization.processors;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Field;
import org.rudi.microservice.strukture.storage.dao.address.AddressRoleDao;
import org.rudi.microservice.strukture.storage.entity.address.AbstractAddressEntity;
import org.rudi.microservice.strukture.storage.entity.address.AddressRoleEntity;
import org.rudi.microservice.strukture.storage.entity.address.AddressType;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractOrganizationTaskUpdateOrganizationAbstractAddressProcessor<E extends AbstractAddressEntity> implements OrganizationTaskUpdateOrganizationProcessor {
	private static final String CONTACT_ADDRESS_ROLE_CODE = "CONTACT";

	@Getter
	private final String acceptedField;
	@Getter
	private final AddressType addressType;
	private final Class<E> clazz;

	private final AddressRoleDao addressRoleDao;

	protected abstract void setValue(String value, E addressEntity);

	public AbstractOrganizationTaskUpdateOrganizationAbstractAddressProcessor(String acceptedField, AddressType addressType, Class<E> clazz, AddressRoleDao addressRoleDao) {
		this.acceptedField = acceptedField;
		this.addressType = addressType;
		this.clazz = clazz;
		this.addressRoleDao = addressRoleDao;
	}

	public AddressRoleEntity getContactRole(){
		return addressRoleDao.findByCode(CONTACT_ADDRESS_ROLE_CODE, true).stream()
				.filter(addressRole -> Objects.equals(addressRole.getType(), getAddressType()))
				.findFirst()
				.orElse(null);
	}

	@Override
	public void process(Field field, OrganizationEntity organizationEntity) {
		String value = null;
		if (field != null && field.getValues() != null && !field.getValues().isEmpty()) {
			value = field.getValues().getFirst();
		}
		assign(value, organizationEntity);
	}

	protected void assign(String value, OrganizationEntity organizationEntity) {
		final E existingAddress = organizationEntity.getAddresses().stream()
				.filter(a -> a.getAddressRole() != null && CONTACT_ADDRESS_ROLE_CODE.equals(a.getAddressRole().getCode()))
				.filter(a -> a.getType().equals(getAddressType()))
				.map(clazz::cast)
				.findFirst().orElse(null);

		if (StringUtils.isEmpty(value)) {
			if (existingAddress != null) {
				organizationEntity.getAddresses().remove(existingAddress);
			}
			return;
		}

		if(existingAddress != null){
			setValue(value, existingAddress);
			return;
		}

		try {
			Constructor<E> constructor = clazz.getConstructor();
			E a = constructor.newInstance();
			a.setUuid(UUID.randomUUID());
			a.setType(getAddressType());
			a.setAddressRole(getContactRole());

			setValue(value, a);

			organizationEntity.getAddresses().add(a);
		}catch (Exception e) {
			log.error("Failed to create new address", e);
		}
	}

}
