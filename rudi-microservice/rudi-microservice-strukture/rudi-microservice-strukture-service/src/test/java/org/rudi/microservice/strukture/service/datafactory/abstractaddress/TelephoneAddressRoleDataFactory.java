package org.rudi.microservice.strukture.service.datafactory.abstractaddress;

import org.rudi.microservice.strukture.core.bean.AddressRole;
import org.rudi.microservice.strukture.service.mapper.AddressRoleMapper;
import org.rudi.microservice.strukture.storage.dao.address.AddressRoleDao;
import org.rudi.microservice.strukture.storage.entity.address.AddressRoleEntity;
import org.rudi.microservice.strukture.storage.entity.address.AddressType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class TelephoneAddressRoleDataFactory extends AddressRoleDataFactory {

	public TelephoneAddressRoleDataFactory(AddressRoleDao repository, AddressRoleMapper addressRoleMapper) {
		super(repository, addressRoleMapper);
	}

	@Override
	protected void assignData(AddressRoleEntity item) {
		item.setType(AddressType.PHONE);
	}

	/**
	 * Récupère ou crée une address role CONTACT pour PHONE (idempotente)
	 * Déléguée à la classe parent qui gère le type
	 *
	 * @return l'AddressRoleEntity pour PHONE
	 */
	public AddressRoleEntity getOrCreateContactRole() {
		return getOrCreatePhoneAddressRole();
	}

	/**
	 * Récupère ou crée une address role CONTACT pour PHONE en DTO (idempotente)
	 * Déléguée à la classe parent qui gère le type
	 *
	 * @return l'AddressRole DTO pour PHONE
	 */
	public AddressRole getOrCreateContactRoleDto() {
		return getOrCreatePhoneAddressRoleDto();
	}
}
