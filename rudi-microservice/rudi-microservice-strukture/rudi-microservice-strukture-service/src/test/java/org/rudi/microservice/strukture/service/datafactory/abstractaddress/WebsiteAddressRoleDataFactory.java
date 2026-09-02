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
public class WebsiteAddressRoleDataFactory extends AddressRoleDataFactory {

	public WebsiteAddressRoleDataFactory(AddressRoleDao repository, AddressRoleMapper addressRoleMapper) {
		super(repository, addressRoleMapper);
	}

	@Override
	protected void assignData(AddressRoleEntity item) {
		item.setType(AddressType.WEBSITE);
	}

	/**
	 * Récupère ou crée une address role CONTACT pour WEBSITE (idempotente)
	 * Déléguée à la classe parent qui gère le type
	 *
	 * @return l'AddressRoleEntity pour WEBSITE
	 */
	public AddressRoleEntity getOrCreateContactRole() {
		return getOrCreateWebsiteAddressRole();
	}

	/**
	 * Récupère ou crée une address role CONTACT pour WEBSITE en DTO (idempotente)
	 * Déléguée à la classe parent qui gère le type
	 *
	 * @return l'AddressRole DTO pour WEBSITE
	 */
	public AddressRole getOrCreateContactRoleDto() {
		return getOrCreateWebsiteAddressRoleDto();
	}
}
