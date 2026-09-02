package org.rudi.microservice.strukture.service.datafactory.abstractaddress;

import java.util.UUID;

import org.apache.tika.utils.StringUtils;
import org.rudi.common.service.datafactory.AbstractDataFactory;
import org.rudi.microservice.strukture.core.bean.AddressType;
import org.rudi.microservice.strukture.core.bean.EmailAddress;
import org.rudi.microservice.strukture.core.bean.TelephoneAddress;
import org.rudi.microservice.strukture.core.bean.WebsiteAddress;
import org.rudi.microservice.strukture.service.mapper.AddressRoleMapper;
import org.rudi.microservice.strukture.storage.dao.address.AbstractAddressDao;
import org.rudi.microservice.strukture.storage.dao.address.AddressRoleDao;
import org.rudi.microservice.strukture.storage.dao.address.EmailAddressDao;
import org.rudi.microservice.strukture.storage.dao.address.TelephoneAddressDao;
import org.rudi.microservice.strukture.storage.dao.address.WebsiteAddressDao;
import org.rudi.microservice.strukture.storage.entity.address.EmailAddressEntity;
import org.rudi.microservice.strukture.storage.entity.address.TelephoneAddressEntity;
import org.rudi.microservice.strukture.storage.entity.address.WebsiteAddressEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional
@Component
@RequiredArgsConstructor
public class AbstractAddressDataFactory extends AbstractDataFactory {
	public static final String WEBSITE_EXPECTED_VALUE = "https://www.example.com";
	public static final String TELEPHONE_EXPECTED_VALUE = "01 23 45 67 89";
	public static final String EMAIL_EXPECTED_VALUE = "un-email@de-contact.com";

	private final AbstractAddressDao abstractAddressDao;
	private final EmailAddressDao emailAddressDao;
	private final WebsiteAddressDao websiteAddressDao;
	private final TelephoneAddressDao telephoneAddressDao;
	private final EmailAddressRoleDataFactory emailAddressRoleDataFactory;
	private final WebsiteAddressRoleDataFactory websiteAddressRoleDataFactory;
	private final TelephoneAddressRoleDataFactory telephoneAddressRoleDataFactory;
	private final AddressRoleDao addressRoleDao;
	private final AddressRoleMapper addressRoleMapper;

	/**
	 * Récupère ou crée une EmailAddress DTO minimale (idempotente)
	 * Utilise les constantes : EMAIL_EXPECTED_VALUE
	 * Délègue la création de AddressRole à EmailAddressRoleDataFactory
	 *
	 * @return EmailAddress DTO avec valeur par défaut
	 */
	public EmailAddress getOrCreateEmailAddressDto() {
		EmailAddress dto = new EmailAddress();
		dto.setUuid(UUID.randomUUID());
		dto.setType(AddressType.EMAIL);
		dto.setEmail(EMAIL_EXPECTED_VALUE);
		dto.setAddressRole(emailAddressRoleDataFactory.getOrCreateContactRoleDto());
		return dto;
	}

	/**
	 * Récupère ou crée une WebsiteAddress DTO minimale (idempotente)
	 * Utilise les constantes : WEBSITE_EXPECTED_VALUE
	 * Délègue la création de AddressRole à WebsiteAddressRoleDataFactory
	 *
	 * @return WebsiteAddress DTO avec valeur par défaut
	 */
	public WebsiteAddress getOrCreateWebsiteAddressDto() {
		WebsiteAddress dto = new WebsiteAddress();
		dto.setUuid(UUID.randomUUID());
		dto.setType(AddressType.WEBSITE);
		dto.setUrl(WEBSITE_EXPECTED_VALUE);
		dto.setAddressRole(websiteAddressRoleDataFactory.getOrCreateContactRoleDto());
		return dto;
	}

	/**
	 * Récupère ou crée une TelephoneAddress DTO minimale (idempotente)
	 * Utilise les constantes : TELEPHONE_EXPECTED_VALUE
	 * Délègue la création de AddressRole à TelephoneAddressRoleDataFactory
	 *
	 * @return TelephoneAddress DTO avec valeur par défaut
	 */
	public TelephoneAddress getOrCreateTelephoneAddressDto() {
		TelephoneAddress dto = new TelephoneAddress();
		dto.setUuid(UUID.randomUUID());
		dto.setType(AddressType.PHONE);
		dto.setPhoneNumber(TELEPHONE_EXPECTED_VALUE);
		dto.setAddressRole(telephoneAddressRoleDataFactory.getOrCreateContactRoleDto());
		return dto;
	}



	/**
	 * Crée une EmailAddressEntity avec adresse role (non idempotente, création directe)
	 * Utilise EmailAddressRoleDataFactory pour la création de l'adresse role
	 *
	 * @param addressRoleCode code non utilisé (pour compatibilité)
	 * @return EmailAddressEntity créée
	 */
	public EmailAddressEntity createEmailAddressEntity(String addressRoleCode) {
		try {
			EmailAddressEntity item = new EmailAddressEntity();
			item.setUuid(UUID.randomUUID());
			item.setType(org.rudi.microservice.strukture.storage.entity.address.AddressType.EMAIL);
			item.setEmail(EMAIL_EXPECTED_VALUE);
			item.setAddressRole(emailAddressRoleDataFactory.getOrCreateContactRole());
			return emailAddressDao.save(item);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to create item for " + EmailAddressEntity.class, e);
		}
	}

	/**
	 * Crée une WebsiteAddressEntity avec adresse role (non idempotente, création directe)
	 * Utilise WebsiteAddressRoleDataFactory pour la création de l'adresse role
	 *
	 * @param addressRoleCode code non utilisé (pour compatibilité)
	 * @return WebsiteAddressEntity créée
	 */
	public WebsiteAddressEntity createWebsiteAddressEntity(String addressRoleCode) {
		try {
			WebsiteAddressEntity item = new WebsiteAddressEntity();
			item.setUuid(UUID.randomUUID());
			item.setType(org.rudi.microservice.strukture.storage.entity.address.AddressType.WEBSITE);
			item.setUrl(WEBSITE_EXPECTED_VALUE);
			item.setAddressRole(websiteAddressRoleDataFactory.getOrCreateContactRole());
			return websiteAddressDao.save(item);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to create item for " + WebsiteAddressEntity.class, e);
		}
	}

	/**
	 * Crée une TelephoneAddressEntity avec adresse role (non idempotente, création directe)
	 * Utilise TelephoneAddressRoleDataFactory pour la création de l'adresse role
	 *
	 * @param addressRoleCode code non utilisé (pour compatibilité)
	 * @return TelephoneAddressEntity créée
	 */
	public TelephoneAddressEntity createPhoneAddressEntity(String addressRoleCode) {
		try {
			TelephoneAddressEntity item = new TelephoneAddressEntity();
			item.setUuid(UUID.randomUUID());
			item.setType(org.rudi.microservice.strukture.storage.entity.address.AddressType.PHONE);
			item.setPhoneNumber(TELEPHONE_EXPECTED_VALUE);
			item.setAddressRole(telephoneAddressRoleDataFactory.getOrCreateContactRole());
			return telephoneAddressDao.save(item);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to create item for " + TelephoneAddressEntity.class, e);
		}
	}


	/**
	 * Crée une EmailAddress DTO avec valeur personnalisée (non idempotente, création directe)
	 * Utilise EmailAddressRoleDataFactory pour la création de l'adresse role
	 *
	 * @param email la valeur d'email personnalisée (ou null pour utiliser EMAIL_EXPECTED_VALUE)
	 * @return EmailAddress DTO créée
	 */
	public EmailAddress createEmailAddressDto(String email) {
		EmailAddress dto = new EmailAddress();
		dto.setUuid(UUID.randomUUID());
		dto.setType(AddressType.EMAIL);
		dto.setEmail(StringUtils.isEmpty(email) ? EMAIL_EXPECTED_VALUE : email);
		dto.setAddressRole(emailAddressRoleDataFactory.getOrCreateContactRoleDto());
		return dto;
	}

	/**
	 * Crée une WebsiteAddress DTO avec valeur personnalisée (non idempotente, création directe)
	 * Utilise WebsiteAddressRoleDataFactory pour la création de l'adresse role
	 *
	 * @param url la valeur d'URL personnalisée (ou null pour utiliser WEBSITE_EXPECTED_VALUE)
	 * @return WebsiteAddress DTO créée
	 */
	public WebsiteAddress createWebsiteAddressDto(String url) {
		WebsiteAddress dto = new WebsiteAddress();
		dto.setUuid(UUID.randomUUID());
		dto.setType(AddressType.WEBSITE);
		dto.setUrl(StringUtils.isEmpty(url) ? WEBSITE_EXPECTED_VALUE : url);
		dto.setAddressRole(websiteAddressRoleDataFactory.getOrCreateContactRoleDto());
		return dto;
	}

	/**
	 * Crée une WebsiteAddress DTO avec une URL trop longue (pour test de validation)
	 * Utilise WebsiteAddressRoleDataFactory pour la création de l'adresse role
	 *
	 * @return WebsiteAddress DTO créée avec URL trop longue
	 */
	public WebsiteAddress createWebsiteAddressDtoTooLong() {
		WebsiteAddress dto = new WebsiteAddress();
		dto.setUuid(UUID.randomUUID());
		dto.setType(AddressType.WEBSITE);
		dto.setUrl(WEBSITE_EXPECTED_VALUE + "a".repeat(1010));
		dto.setAddressRole(websiteAddressRoleDataFactory.getOrCreateContactRoleDto());
		return dto;
	}

	/**
	 * Crée une TelephoneAddress DTO avec valeur personnalisée (non idempotente, création directe)
	 * Utilise TelephoneAddressRoleDataFactory pour la création de l'adresse role
	 *
	 * @param phoneNumber la valeur de téléphone personnalisée (ou null pour utiliser TELEPHONE_EXPECTED_VALUE)
	 * @return TelephoneAddress DTO créée
	 */
	public TelephoneAddress createTelephoneAddressDto(String phoneNumber) {
		TelephoneAddress dto = new TelephoneAddress();
		dto.setUuid(UUID.randomUUID());
		dto.setType(AddressType.PHONE);
		dto.setPhoneNumber(StringUtils.isEmpty(phoneNumber) ? TELEPHONE_EXPECTED_VALUE : phoneNumber);
		dto.setAddressRole(telephoneAddressRoleDataFactory.getOrCreateContactRoleDto());
		return dto;
	}


}
