package org.rudi.microservice.strukture.service.organization.impl.fields;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.microservice.strukture.core.bean.AbstractAddress;
import org.rudi.microservice.strukture.core.bean.EmailAddress;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.core.bean.TelephoneAddress;
import org.rudi.microservice.strukture.core.bean.WebsiteAddress;
import org.rudi.microservice.strukture.storage.dao.address.AbstractAddressDao;
import org.rudi.microservice.strukture.storage.dao.address.AddressRoleDao;
import org.rudi.microservice.strukture.storage.entity.address.AbstractAddressEntity;
import org.rudi.microservice.strukture.storage.entity.address.AddressRoleEntity;
import org.rudi.microservice.strukture.storage.entity.address.AddressType;
import org.rudi.microservice.strukture.storage.entity.address.EmailAddressEntity;
import org.rudi.microservice.strukture.storage.entity.address.TelephoneAddressEntity;
import org.rudi.microservice.strukture.storage.entity.address.WebsiteAddressEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContactAddressesProcessor implements CreateOrganizationFieldProcessor, UpdateOrganizationFieldProcessor {

	private static final String CONTACT_ADDRESS_ROLE_CODE = "CONTACT";
	private final AddressRoleDao addressRoleDao;
	private final AbstractAddressDao abstractAddressDao;

	@Override
	public void processBeforeCreate(OrganizationEntity organization, Organization organizationDto) throws AppServiceBadRequestException {
		mergeContactAddresses(organization, organizationDto);
	}

	@Override
	public void processBeforeUpdate(Organization organization, OrganizationEntity existingOrganization) throws AppServiceBadRequestException {
		mergeContactAddresses(existingOrganization, organization);
	}

	private void mergeContactAddresses(OrganizationEntity entity, Organization dto) {
		mergeAddress(entity, dto, CONTACT_ADDRESS_ROLE_CODE,
				org.rudi.microservice.strukture.core.bean.AddressType.EMAIL, AddressType.EMAIL,
				EmailAddress.class, EmailAddress::getEmail,
				EmailAddressEntity::new, EmailAddressEntity::setEmail, EmailAddressEntity.class);

		mergeAddress(entity, dto, CONTACT_ADDRESS_ROLE_CODE,
				org.rudi.microservice.strukture.core.bean.AddressType.PHONE, AddressType.PHONE,
				TelephoneAddress.class, TelephoneAddress::getPhoneNumber,
				TelephoneAddressEntity::new, TelephoneAddressEntity::setPhoneNumber, TelephoneAddressEntity.class);

		mergeAddress(entity, dto, CONTACT_ADDRESS_ROLE_CODE,
				org.rudi.microservice.strukture.core.bean.AddressType.WEBSITE, AddressType.WEBSITE,
				WebsiteAddress.class, WebsiteAddress::getUrl,
				WebsiteAddressEntity::new, WebsiteAddressEntity::setUrl, WebsiteAddressEntity.class);
	}

	/**
	 * Generic method to extract, from an organization DTO, the address of the given type and role code.
	 * Each organization has at most one address per (type, role) couple.
	 *
	 * @param dto         the organization DTO to search into
	 * @param addressType the type of address to look for (PHONE, EMAIL, WEBSITE…)
	 * @param roleCode    the address role code to match (e.g. CONTACT_ADDRESS_ROLE_CODE)
	 * @param dtoClass    class used to cast the matching address DTO
	 * @return an Optional containing the matching address DTO, cast to the expected type, or empty if none found
	 */
	private <D extends AbstractAddress> Optional<D> extractDtoAddress(Organization dto,
			org.rudi.microservice.strukture.core.bean.AddressType addressType, String roleCode, Class<D> dtoClass) {
		if (CollectionUtils.isEmpty(dto.getAddresses())) {
			return Optional.empty();
		}
		return dto.getAddresses().stream()
				.filter(a -> a.getType().equals(addressType)
						&& a.getAddressRole() != null
						&& roleCode.equals(a.getAddressRole().getCode()))
				.findFirst()
				.map(dtoClass::cast);
	}

	/**
	 * Generic method to merge an address of any type into the organization's address collection, based on the
	 * value found in the organization DTO (retrieved via {@link #extractDtoAddress}).
	 * If an address of the given type/role already exists, its value is updated; otherwise a new one is added
	 * (the matching {@link AddressRoleEntity} is resolved via its code).
	 * If the DTO value is blank, any existing address of that type/role is removed from the collection and
	 * deleted from DB.
	 *
	 * @param entity            the organization entity to update
	 * @param dto               the organization DTO holding the source addresses
	 * @param dtoAddressType    the DTO address type to look for (core.bean.AddressType)
	 * @param entityAddressType the entity address type to look for (storage.entity.address.AddressType)
	 * @param dtoClass          class used to cast the matching DTO address
	 * @param valueGetter       function extracting the string value to persist from the DTO address
	 * @param newEntityFactory  supplier to create a new address entity
	 * @param valueSetter       bi-consumer to apply the value on the entity
	 * @param entityClass       class used to cast the matching existing entity
	 */
	private <E extends AbstractAddressEntity, D extends AbstractAddress> void mergeAddress(
			OrganizationEntity entity,
			Organization dto,
			String roleCode,
			org.rudi.microservice.strukture.core.bean.AddressType dtoAddressType,
			AddressType entityAddressType,
			Class<D> dtoClass,
			Function<D, String> valueGetter,
			Supplier<E> newEntityFactory,
			BiConsumer<E, String> valueSetter,
			Class<E> entityClass
	) {
		String value = extractDtoAddress(dto, dtoAddressType, roleCode, dtoClass)
				.map(valueGetter)
				.orElse(null);

		// Filter by type and role code: each organization has at most one address per (type, role) couple
		Optional<E> existing = entity.getAddresses().stream()
				.filter(a -> a.getType() == entityAddressType)
				.filter(a -> a.getAddressRole() != null && roleCode.equals(a.getAddressRole().getCode()))
				.map(entityClass::cast)
				.findFirst();

		if (StringUtils.isEmpty(value)) {
			// Remove and physically delete the existing address if present
			existing.ifPresent(address -> {
				entity.getAddresses().remove(address);
				abstractAddressDao.delete(address);
			});
			return;
		}

		if (existing.isPresent()) {
			valueSetter.accept(existing.get(), value);
		} else {
			AddressRoleEntity addressRole = addressRoleDao.findByCode(roleCode, true).stream()
					.filter(role -> role.getType() == entityAddressType)
					.findFirst()
					.orElse(null);

			E address = newEntityFactory.get();
			address.setUuid(UUID.randomUUID());
			address.setAddressRole(addressRole);
			address.setType(entityAddressType);
			valueSetter.accept(address, value);
			entity.getAddresses().add(address);
		}
	}
}
