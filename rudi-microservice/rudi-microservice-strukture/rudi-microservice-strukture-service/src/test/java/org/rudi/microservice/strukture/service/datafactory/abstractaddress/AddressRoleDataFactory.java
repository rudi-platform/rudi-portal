package org.rudi.microservice.strukture.service.datafactory.abstractaddress;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.rudi.common.service.datafactory.AbstractStampedDataFactory;
import org.rudi.microservice.strukture.core.bean.AddressRole;
import org.rudi.microservice.strukture.service.mapper.AddressRoleMapper;
import org.rudi.microservice.strukture.storage.dao.address.AddressRoleDao;
import org.rudi.microservice.strukture.storage.entity.address.AddressRoleEntity;
import org.rudi.microservice.strukture.storage.entity.address.AddressType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class AddressRoleDataFactory extends AbstractStampedDataFactory<AddressRoleEntity, AddressRoleDao> {

	// Constantes pour les codes d'address roles
	public static final String CODE_CONTACT = "CONTACT";

	private final AddressRoleMapper addressRoleMapper;

	public AddressRoleDataFactory(AddressRoleDao repository, AddressRoleMapper addressRoleMapper) {
		super(repository, AddressRoleEntity.class);
		this.addressRoleMapper = addressRoleMapper;
	}

	@Override
	protected void assignData(AddressRoleEntity item) {
		super.assignData(item);
	}

	/**
	 * Récupère ou crée une address role par code et type (idempotente)
	 * Cherche d'abord si une address role avec ce code ET ce type existe
	 *
	 * @param code le code de l'address role (ex: "CONTACT")
	 * @param type le type d'adresse (EMAIL, PHONE, WEBSITE)
	 * @return l'AddressRoleEntity existante ou nouvellement créée
	 */
	public AddressRoleEntity getOrCreateAddressRole(String code, AddressType type) {
		// Chercher une address role existante avec ce code ET ce type
		List<AddressRoleEntity> existing = repository.findByCode(code, null);
		AddressRoleEntity addressRoleEntity = existing.stream()
			.filter(role -> role.getType() == type)
			.findFirst()
			.orElse(null);

		if (addressRoleEntity != null) {
			return addressRoleEntity;
		}

		// Créer si n'existe pas
		AddressRoleEntity addressRole = new AddressRoleEntity();
		addressRole.setUuid(UUID.randomUUID());
		addressRole.setCode(code);
		addressRole.setLabel("Adresse de contact " + type.name());
		addressRole.setOrder(0);
		addressRole.setOpeningDate(LocalDateTime.now().minusDays(1));
		addressRole.setType(type);

		return repository.save(addressRole);
	}

	/**
	 * Récupère ou crée une address role pour le type EMAIL (idempotente)
	 *
	 * @return l'AddressRoleEntity pour EMAIL
	 */
	public AddressRoleEntity getOrCreateEmailAddressRole() {
		return getOrCreateAddressRole(CODE_CONTACT, AddressType.EMAIL);
	}

	/**
	 * Récupère ou crée une address role pour le type EMAIL en DTO (idempotente)
	 *
	 * @return l'AddressRole DTO pour EMAIL
	 */
	public AddressRole getOrCreateEmailAddressRoleDto() {
		return addressRoleMapper.entityToDto(getOrCreateEmailAddressRole());
	}

	/**
	 * Récupère ou crée une address role pour le type PHONE (idempotente)
	 *
	 * @return l'AddressRoleEntity pour PHONE
	 */
	public AddressRoleEntity getOrCreatePhoneAddressRole() {
		return getOrCreateAddressRole(CODE_CONTACT, AddressType.PHONE);
	}

	/**
	 * Récupère ou crée une address role pour le type PHONE en DTO (idempotente)
	 *
	 * @return l'AddressRole DTO pour PHONE
	 */
	public AddressRole getOrCreatePhoneAddressRoleDto() {
		return addressRoleMapper.entityToDto(getOrCreatePhoneAddressRole());
	}

	/**
	 * Récupère ou crée une address role pour le type WEBSITE (idempotente)
	 *
	 * @return l'AddressRoleEntity pour WEBSITE
	 */
	public AddressRoleEntity getOrCreateWebsiteAddressRole() {
		return getOrCreateAddressRole(CODE_CONTACT, AddressType.WEBSITE);
	}

	/**
	 * Récupère ou crée une address role pour le type WEBSITE en DTO (idempotente)
	 *
	 * @return l'AddressRole DTO pour WEBSITE
	 */
	public AddressRole getOrCreateWebsiteAddressRoleDto() {
		return addressRoleMapper.entityToDto(getOrCreateWebsiteAddressRole());
	}

}
