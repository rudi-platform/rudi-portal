package org.rudi.microservice.strukture.service.datafactory.organization;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.facet.acl.datafactory.UserDataFactory;
import org.rudi.facet.bpmn.datafactory.AbstractAssetDescriptionDataFactory;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.service.datafactory.abstractaddress.AbstractAddressDataFactory;
import org.rudi.microservice.strukture.service.datafactory.organizationmember.OrganizationMemberDataFactory;
import org.rudi.microservice.strukture.service.mapper.OrganizationFullMapper;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationDao;
import org.rudi.microservice.strukture.storage.entity.address.EmailAddressEntity;
import org.rudi.microservice.strukture.storage.entity.address.TelephoneAddressEntity;
import org.rudi.microservice.strukture.storage.entity.address.WebsiteAddressEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class OrganizationDataFactory extends AbstractAssetDescriptionDataFactory<OrganizationEntity, OrganizationDao> {

	public static final String PROCESS_DEFINITION_KEY = "organization-process";
	public static final String FUNCTIONAL_STATUS_KEY = "Créée";

	// Constantes pour getOrCreateMinimalOrganization()
	public static final String MINIMAL_ORGANIZATION_NAME = "Minimal Organization";
	public static final String MINIMAL_ORGANIZATION_DESCRIPTION = "Description de Minimal Organization";
	public static final String MINIMAL_ORGANIZATION_INITIATOR = "initiator@mail.fr";

	// Constantes pour organizations pré-configurées
	public static final String IRISA_ORGANIZATION_NAME = "IRISA";
	public static final String IRISA_ORGANIZATION_DESCRIPTION = "Institut de Recherche en Informatique et Systèmes Aléatoires";
	public static final String OPEN_ORGANIZATION_NAME = "Open";
	public static final String OPEN_ORGANIZATION_DESCRIPTION = "Welcome to your high‑digital place";
	public static final String RM_ORGANIZATION_NAME = "Rennes Métropole";
	public static final String RM_ORGANIZATION_DESCRIPTION = "Municipalité de Rennes et alentours";
	public static final String VIA_ROMA_ORGANIZATION_NAME = "Via Roma";
	public static final String VIA_ROMA_ORGANIZATION_DESCRIPTION = "Temple de la Marcello.";
	public static final String COOKING_POT_ORGANIZATION_NAME = "Cooking Pot";
	public static final String COOKING_POT_ORGANIZATION_DESCRIPTION = "Feu la succursale cachée d'OPEN.";
	public static final String BLOCK_ORGANIZATION_NAME = "Block";
	public static final String BLOCK_ORGANIZATION_DESCRIPTION = "Remplaçant du Cooking Pot.";

	private final OrganizationMemberDataFactory organizationMemberDataFactory;
	private final UserDataFactory userDataFactory;
	private final AbstractAddressDataFactory abstractAddressDataFactory;
	private final OrganizationFullMapper organizationMapper;

	public OrganizationDataFactory(OrganizationDao repository,
			OrganizationMemberDataFactory organizationMemberDataFactory, UserDataFactory userDataFactory,
			AbstractAddressDataFactory abstractAddressDataFactory, OrganizationFullMapper organizationFullMapper) {
		super(repository, OrganizationEntity.class);
		this.organizationMemberDataFactory = organizationMemberDataFactory;
		this.userDataFactory = userDataFactory;
		this.abstractAddressDataFactory = abstractAddressDataFactory;
		this.organizationMapper = organizationFullMapper;
	}

	@Override
	protected void assignData(OrganizationEntity item) {
		item.setOrganizationStatus(OrganizationStatus.VALIDATED);
	}

	public OrganizationEntity create(UUID uuid, String processDefinitionKey, Status status, String functionnalStatus,
			String initiator, LocalDateTime creationDate, String description, OrganizationStatus organizationStatus,
			LocalDateTime openingDate, LocalDateTime closingDate, Geometry position, String name) {

		try {
			OrganizationEntity item = new OrganizationEntity();
			item.setUuid(uuid);
			item.setProcessDefinitionKey(processDefinitionKey);
			item.setStatus(status);
			item.setFunctionalStatus(functionnalStatus);
			item.setInitiator(initiator);
			item.setCreationDate(handleDate(creationDate));
			item.setDescription(description);
			item.setOrganizationStatus(organizationStatus);
			item.setOpeningDate(handleDate(openingDate));
			item.setClosingDate(handleDate(closingDate));
			item.setPosition(position);
			item.setName(name);

			assignData(item);
			return repository.save(item);
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to create item for OrganizationEntity", e);
		}
	}

	public OrganizationEntity getOrCreateOrganization(UUID uuid) {
		if (uuid == null) {
			uuid = UUID.randomUUID();
		} else {
			OrganizationEntity organization = repository.findByUuid(uuid);
			if (organization != null) {
				return organization;
			}
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime yesterday = now.minusDays(1);
		return create(uuid, PROCESS_DEFINITION_KEY, Status.COMPLETED, FUNCTIONAL_STATUS_KEY, randomString(20), now,
				randomString(60), OrganizationStatus.VALIDATED, yesterday, null, null, randomString(20));
	}

	/**
	 * COMPLETED // VALIDATED
	 *
	 * Récupère ou crée une organization IRISA (idempotente)
	 * Utilise les constantes : IRISA_ORGANIZATION_NAME, IRISA_ORGANIZATION_DESCRIPTION
	 *
	 * @return organization IRISA avec données pré-configurées
	 */
	public OrganizationEntity getOrCreateIRISAOrganization() {
		OrganizationEntity existing = repository.findAll().stream()
			.filter(org -> IRISA_ORGANIZATION_NAME.equals(org.getName()))
			.findFirst()
			.orElse(null);

		if (existing != null) {
			return existing;
		}

		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName(IRISA_ORGANIZATION_NAME);
		organization.setDescription(IRISA_ORGANIZATION_DESCRIPTION);
		organization.setInitiator("initiator@mail.fr");
		organization.setCreationDate(LocalDateTime.now());
		organization.setFunctionalStatus("Validée");
		organization.setStatus(Status.COMPLETED);
		organization.setOrganizationStatus(OrganizationStatus.VALIDATED);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
		organization.setOpeningDate(LocalDateTime.of(1975, Month.JANUARY, 1, 23, 38, 12, 0));

		return repository.save(organization);
	}

	/**
	 * COMPLETED // VALIDATED
	 * 
	 * Récupère ou crée une organization Open (idempotente)
	 * Utilise les constantes : OPEN_ORGANIZATION_NAME, OPEN_ORGANIZATION_DESCRIPTION
	 *
	 * @return organization Open avec données pré-configurées
	 */
	public OrganizationEntity getOrCreateOpenOrganization() {
		OrganizationEntity existing = repository.findAll().stream()
			.filter(org -> OPEN_ORGANIZATION_NAME.equals(org.getName()))
			.findFirst()
			.orElse(null);

		if (existing != null) {
			return existing;
		}

		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName(OPEN_ORGANIZATION_NAME);
		organization.setDescription(OPEN_ORGANIZATION_DESCRIPTION);
		organization.setInitiator("initiator@mail.fr");
		organization.setCreationDate(LocalDateTime.now());
		organization.setFunctionalStatus("Validée");
		organization.setStatus(Status.COMPLETED);
		organization.setOrganizationStatus(OrganizationStatus.VALIDATED);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
		organization.setOpeningDate(LocalDateTime.of(1989, Month.FEBRUARY, 1, 23, 38, 12, 0));

		return repository.save(organization);
	}

	/**
	 * COMPLETED // VALIDATED
	 * 
	 * Récupère ou crée une organization Rennes Métropole (idempotente)
	 * Utilise les constantes : RM_ORGANIZATION_NAME, RM_ORGANIZATION_DESCRIPTION
	 *
	 * @return organization Rennes Métropole avec données pré-configurées
	 */
	public OrganizationEntity getOrCreateRMOrganization() {
		OrganizationEntity existing = repository.findAll().stream()
			.filter(org -> RM_ORGANIZATION_NAME.equals(org.getName()))
			.findFirst()
			.orElse(null);

		if (existing != null) {
			return existing;
		}

		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName(RM_ORGANIZATION_NAME);
		organization.setDescription(RM_ORGANIZATION_DESCRIPTION);
		organization.setInitiator("initiator@mail.fr");
		organization.setCreationDate(LocalDateTime.now());
		organization.setFunctionalStatus("Validée");
		organization.setStatus(Status.COMPLETED);
		organization.setOrganizationStatus(OrganizationStatus.VALIDATED);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
		organization.setOpeningDate(LocalDateTime.of(2015, Month.JANUARY, 14, 23, 38, 12, 0));

		return repository.save(organization);
	}

	/**
	 * COMPLETED // VALIDATED
	 * 
	 * Récupère ou crée une organization Via Roma (idempotente)
	 * Utilise les constantes : VIA_ROMA_ORGANIZATION_NAME, VIA_ROMA_ORGANIZATION_DESCRIPTION
	 *
	 * @return organization Via Roma avec données pré-configurées
	 */
	public OrganizationEntity getOrCreateViaRomaOrganization() {
		OrganizationEntity existing = repository.findAll().stream()
			.filter(org -> VIA_ROMA_ORGANIZATION_NAME.equals(org.getName()))
			.findFirst()
			.orElse(null);

		if (existing != null) {
			return existing;
		}

		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName(VIA_ROMA_ORGANIZATION_NAME);
		organization.setDescription(VIA_ROMA_ORGANIZATION_DESCRIPTION);
		organization.setInitiator("initiator@mail.fr");
		organization.setCreationDate(LocalDateTime.now());
		organization.setFunctionalStatus("Validée");
		organization.setStatus(Status.COMPLETED);
		organization.setOrganizationStatus(OrganizationStatus.VALIDATED);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
		organization.setOpeningDate(LocalDateTime.of(2009, Month.JUNE, 4, 23, 38, 12, 0));

		return repository.save(organization);
	}

	/**
	 * DELETED // DISENGAGED
	 * 
	 * Récupère ou crée une organization Cooking Pot (idempotente)
	 * Utilise les constantes : COOKING_POT_ORGANIZATION_NAME, COOKING_POT_ORGANIZATION_DESCRIPTION
	 *
	 * @return organization Cooking Pot avec données pré-configurées (statut DELETED)
	 */
	public OrganizationEntity getOrCreateCookingPotOrganization() {
		OrganizationEntity existing = repository.findAll().stream()
			.filter(org -> COOKING_POT_ORGANIZATION_NAME.equals(org.getName()))
			.findFirst()
			.orElse(null);

		if (existing != null) {
			return existing;
		}

		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName(COOKING_POT_ORGANIZATION_NAME);
		organization.setDescription(COOKING_POT_ORGANIZATION_DESCRIPTION);
		organization.setInitiator("initiator@mail.fr");
		organization.setCreationDate(LocalDateTime.now());
		organization.setFunctionalStatus("Fermée");
		organization.setStatus(Status.DELETED);
		organization.setOrganizationStatus(OrganizationStatus.DISENGAGED);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
		organization.setOpeningDate(LocalDateTime.of(2017, Month.JANUARY, 7, 23, 38, 12, 0));
		organization.setClosingDate(LocalDateTime.of(2022, Month.APRIL, 14, 23, 38, 12, 0));

		return repository.save(organization);
	}

	/**
	 * DRAFT // DRAFT
	 * 
	 * Récupère ou crée une organization Block (idempotente)
	 * Utilise les constantes : BLOCK_ORGANIZATION_NAME, BLOCK_ORGANIZATION_DESCRIPTION
	 *
	 * @return organization Block avec données pré-configurées (statut DRAFT)
	 */
	public OrganizationEntity getOrCreateBlockOrganization() {
		OrganizationEntity existing = repository.findAll().stream()
			.filter(org -> BLOCK_ORGANIZATION_NAME.equals(org.getName()))
			.findFirst()
			.orElse(null);

		if (existing != null) {
			return existing;
		}

		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName(BLOCK_ORGANIZATION_NAME);
		organization.setDescription(BLOCK_ORGANIZATION_DESCRIPTION);
		organization.setInitiator("initiator@mail.fr");
		organization.setCreationDate(LocalDateTime.now());
		organization.setFunctionalStatus("En rodage");
		organization.setStatus(Status.DRAFT);
		organization.setOrganizationStatus(OrganizationStatus.DRAFT);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
		organization.setOpeningDate(LocalDateTime.of(2024, Month.JUNE, 25, 23, 38, 12, 0));

		return repository.save(organization);
	}

	/**
	 * Récupère ou crée une organization minimale avec email - persistée en BDD
	 *
	 * L'adresse email utilise les constantes d'AbstractAddressDataFactory :
	 * - AbstractAddressDataFactory.EMAIL_EXPECTED_VALUE
	 *
	 * Idempotente : cherche d'abord si une organisation existe avec ce nom et une EmailAddress.
	 *
	 * @return organisation persistée en BDD avec une EmailAddress (Entity)
	 */
	public OrganizationEntity getOrCreateMinimalOrganizationWithEmail() {
		// Vérifier si une organisation avec ce nom existe déjà avec une email
		OrganizationEntity existing = repository.findAll().stream()
				.filter(org -> MINIMAL_ORGANIZATION_NAME.equals(org.getName()))
				.filter(org -> MINIMAL_ORGANIZATION_INITIATOR.equals(org.getInitiator()))
				.filter(org -> org.getAddresses() != null && org.getAddresses().stream()
						.anyMatch(addr -> addr instanceof EmailAddressEntity))
				.findFirst()
				.orElse(null);

		if (existing != null) {
			return existing;
		}

		// Créer l'organisation de base
		LocalDateTime now = LocalDateTime.now();
		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName(MINIMAL_ORGANIZATION_NAME);
		organization.setDescription(MINIMAL_ORGANIZATION_DESCRIPTION);
		organization.setInitiator(MINIMAL_ORGANIZATION_INITIATOR);
		organization.setCreationDate(now);
		organization.setFunctionalStatus(FUNCTIONAL_STATUS_KEY);
		organization.setStatus(Status.COMPLETED);
		organization.setOrganizationStatus(OrganizationStatus.VALIDATED);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
		organization.setOpeningDate(now);

		// Créer et ajouter l'adresse email directement
		organization.setAddresses(new java.util.HashSet<>(List.of(
				abstractAddressDataFactory.createEmailAddressEntity(null)
		)));

		// Persister l'organisation avec son adresse
		return repository.save(organization);
	}

	/**
	 * Récupère ou crée une organization minimale avec email - DTO persisté en BDD
	 *
	 * Utilise getOrCreateMinimalOrganizationWithEmail() et la convertit en DTO via le mapper.
	 * Idempotente (délégation). L'UUID retourné existe en BDD.
	 *
	 * À utiliser uniquement pour les tests qui récupèrent l'organisation par getOrganization().
	 *
	 * @return organisation persistée en BDD avec une EmailAddress (DTO)
	 */
	public Organization getOrCreateMinimalOrganizationWithEmailDto() {
		return organizationMapper.entityToDto(getOrCreateMinimalOrganizationWithEmail());
	}

	/**
	 * Crée une organization minimale avec email - DTO NON persisté en BDD
	 *
	 * Retourne un DTO non persisté avec UUID aléatoire.
	 * À utiliser uniquement pour les tests qui appellen organizationService.createOrganization().
	 *
	 * @return organisation NON persistée en BDD avec une EmailAddress (DTO)
	 */
	public Organization getMinimalOrganizationWithEmailDtoForCreation() {
		Organization dto = new Organization();
		dto.setUuid(UUID.randomUUID());
		dto.setName(MINIMAL_ORGANIZATION_NAME);
		dto.setDescription(MINIMAL_ORGANIZATION_DESCRIPTION);
		dto.setInitiator(MINIMAL_ORGANIZATION_INITIATOR);
		dto.setCreationDate(LocalDateTime.now());
		dto.setAddresses(List.of(abstractAddressDataFactory.getOrCreateEmailAddressDto()));
		return dto;
	}

	/**
	 * Récupère ou crée une organization minimale avec site web - persistée en BDD
	 *
	 * L'adresse web utilise les constantes d'AbstractAddressDataFactory :
	 * - AbstractAddressDataFactory.WEBSITE_EXPECTED_VALUE
	 *
	 * Idempotente : cherche d'abord si une organisation existe avec ce nom et une WebsiteAddress.
	 *
	 * @return organisation persistée en BDD avec une WebsiteAddress (Entity)
	 */
	public OrganizationEntity getOrCreateMinimalOrganizationWithWebsite() {
		// Vérifier si une organisation avec ce nom existe déjà avec une website
		OrganizationEntity existing = repository.findAll().stream()
				.filter(org -> MINIMAL_ORGANIZATION_NAME.equals(org.getName()))
				.filter(org -> MINIMAL_ORGANIZATION_INITIATOR.equals(org.getInitiator()))
				.filter(org -> org.getAddresses() != null && org.getAddresses().stream()
						.anyMatch(addr -> addr instanceof WebsiteAddressEntity))
				.findFirst()
				.orElse(null);

		if (existing != null) {
			return existing;
		}

		// Créer l'organisation de base
		LocalDateTime now = LocalDateTime.now();
		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName(MINIMAL_ORGANIZATION_NAME);
		organization.setDescription(MINIMAL_ORGANIZATION_DESCRIPTION);
		organization.setInitiator(MINIMAL_ORGANIZATION_INITIATOR);
		organization.setCreationDate(now);
		organization.setFunctionalStatus(FUNCTIONAL_STATUS_KEY);
		organization.setStatus(Status.COMPLETED);
		organization.setOrganizationStatus(OrganizationStatus.VALIDATED);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
		organization.setOpeningDate(now);

		// Créer et ajouter l'adresse web directement
		organization.setAddresses(new java.util.HashSet<>(List.of(
				abstractAddressDataFactory.createWebsiteAddressEntity(null)
		)));

		// Persister l'organisation avec son adresse
		return repository.save(organization);
	}

	/**
	 * Récupère ou crée une organization minimale avec site web - DTO persisté en BDD
	 *
	 * Utilise getOrCreateMinimalOrganizationWithWebsite() et la convertit en DTO via le mapper.
	 * Idempotente (délégation). L'UUID retourné existe en BDD.
	 *
	 * À utiliser uniquement pour les tests qui récupèrent l'organisation par getOrganization().
	 *
	 * @return organisation persistée en BDD avec une WebsiteAddress (DTO)
	 */
	public Organization getOrCreateMinimalOrganizationWithWebsiteDto() {
		return organizationMapper.entityToDto(getOrCreateMinimalOrganizationWithWebsite());
	}

	/**
	 * Crée une organization minimale avec site web - DTO NON persisté en BDD
	 *
	 * Retourne un DTO non persisté avec UUID aléatoire.
	 * À utiliser uniquement pour les tests qui appellen organizationService.createOrganization().
	 *
	 * @return organisation NON persistée en BDD avec une WebsiteAddress (DTO)
	 */
	public Organization getMinimalOrganizationWithWebsiteDtoForCreation() {
		Organization dto = new Organization();
		dto.setUuid(UUID.randomUUID());
		dto.setName(MINIMAL_ORGANIZATION_NAME);
		dto.setDescription(MINIMAL_ORGANIZATION_DESCRIPTION);
		dto.setInitiator(MINIMAL_ORGANIZATION_INITIATOR);
		dto.setCreationDate(LocalDateTime.now());
		dto.setAddresses(List.of(abstractAddressDataFactory.getOrCreateWebsiteAddressDto()));
		return dto;
	}

	/**
	 * Récupère ou crée une organization minimale avec téléphone - persistée en BDD
	 *
	 * L'adresse téléphone utilise les constantes d'AbstractAddressDataFactory :
	 * - AbstractAddressDataFactory.TELEPHONE_EXPECTED_VALUE
	 *
	 * Idempotente : cherche d'abord si une organisation existe avec ce nom et une TelephoneAddress.
	 *
	 * @return organisation persistée en BDD avec une TelephoneAddress (Entity)
	 */
	public OrganizationEntity getOrCreateMinimalOrganizationWithPhone() {
		// Vérifier si une organisation avec ce nom existe déjà avec un téléphone
		OrganizationEntity existing = repository.findAll().stream()
				.filter(org -> MINIMAL_ORGANIZATION_NAME.equals(org.getName()))
				.filter(org -> MINIMAL_ORGANIZATION_INITIATOR.equals(org.getInitiator()))
				.filter(org -> org.getAddresses() != null && org.getAddresses().stream()
						.anyMatch(addr -> addr instanceof TelephoneAddressEntity))
				.findFirst()
				.orElse(null);

		if (existing != null) {
			return existing;
		}

		// Créer l'organisation de base
		LocalDateTime now = LocalDateTime.now();
		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName(MINIMAL_ORGANIZATION_NAME);
		organization.setDescription(MINIMAL_ORGANIZATION_DESCRIPTION);
		organization.setInitiator(MINIMAL_ORGANIZATION_INITIATOR);
		organization.setCreationDate(now);
		organization.setFunctionalStatus(FUNCTIONAL_STATUS_KEY);
		organization.setStatus(Status.COMPLETED);
		organization.setOrganizationStatus(OrganizationStatus.VALIDATED);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
		organization.setOpeningDate(now);

		// Créer et ajouter l'adresse téléphone directement
		organization.setAddresses(new java.util.HashSet<>(List.of(
				abstractAddressDataFactory.createPhoneAddressEntity(null)
		)));

		// Persister l'organisation avec son adresse
		return repository.save(organization);
	}

	/**
	 * Récupère ou crée une organization minimale avec téléphone - DTO persisté en BDD
	 *
	 * Utilise getOrCreateMinimalOrganizationWithPhone() et la convertit en DTO via le mapper.
	 * Idempotente (délégation). L'UUID retourné existe en BDD.
	 *
	 * À utiliser uniquement pour les tests qui récupèrent l'organisation par getOrganization().
	 *
	 * @return organisation persistée en BDD avec une TelephoneAddress (DTO)
	 */
	public Organization getOrCreateMinimalOrganizationWithPhoneDto() {
		return organizationMapper.entityToDto(getOrCreateMinimalOrganizationWithPhone());
	}

	/**
	 * Crée une organization minimale avec téléphone - DTO NON persisté en BDD
	 *
	 * Retourne un DTO non persisté avec UUID aléatoire.
	 * À utiliser uniquement pour les tests qui appellen organizationService.createOrganization().
	 *
	 * @return organisation NON persistée en BDD avec une TelephoneAddress (DTO)
	 */
	public Organization getMinimalOrganizationWithPhoneDtoForCreation() {
		Organization dto = new Organization();
		dto.setUuid(UUID.randomUUID());
		dto.setName(MINIMAL_ORGANIZATION_NAME);
		dto.setDescription(MINIMAL_ORGANIZATION_DESCRIPTION);
		dto.setInitiator(MINIMAL_ORGANIZATION_INITIATOR);
		dto.setCreationDate(LocalDateTime.now());
		dto.setAddresses(List.of(abstractAddressDataFactory.getOrCreateTelephoneAddressDto()));
		return dto;
	}

	/**
	 * Récupère ou crée une organization avec toutes les adresses
	 *
	 * Inclut une EmailAddress, une WebsiteAddress et une TelephoneAddress.
	 * Utilise les constantes d'AbstractAddressDataFactory.
	 * Basée sur MINIMAL_ORGANIZATION_NAME pour simplicité.
	 *
	 * @return organization avec toutes les adresses pré-configurées
	 */
	public Organization getOrCreateOrganizationWithAllAddressesDtoDto() {
		Organization dto = new Organization();
		dto.setUuid(UUID.randomUUID());
		dto.setName(MINIMAL_ORGANIZATION_NAME);
		dto.setDescription(MINIMAL_ORGANIZATION_DESCRIPTION);
		dto.setInitiator(MINIMAL_ORGANIZATION_INITIATOR);
		dto.setCreationDate(LocalDateTime.now());
		dto.setAddresses(List.of(
			abstractAddressDataFactory.getOrCreateEmailAddressDto(),
			abstractAddressDataFactory.getOrCreateWebsiteAddressDto(),
			abstractAddressDataFactory.getOrCreateTelephoneAddressDto()
		));
		return dto;
	}

	/**
	 * Récupère ou crée une organization minimale avec toutes les adresses - persistée en BDD
	 *
	 * Idempotente : cherche d'abord si une organisation avec ce nom existe et possède 3 adresses.
	 * Sinon, crée une nouvelle organisation avec EmailAddress, WebsiteAddress et TelephoneAddress
	 * directement en BDD via AbstractAddressDataFactory.
	 *
	 * Utilise les constantes :
	 * - MINIMAL_ORGANIZATION_NAME
	 * - MINIMAL_ORGANIZATION_DESCRIPTION
	 * - MINIMAL_ORGANIZATION_INITIATOR
	 *
	 * Et les adresses via AbstractAddressDataFactory.createXxxAddressEntity()
	 *
	 * @return organisation persistée en BDD avec 3 adresses de contact (Entity)
	 */
	public OrganizationEntity getOrCreateMinimalOrganizationWithAllAddresses() {
		// Vérifier si une organisation avec ce nom existe déjà avec 3 adresses
		OrganizationEntity existing = repository.findAll().stream()
				.filter(org -> MINIMAL_ORGANIZATION_NAME.equals(org.getName()))
				.filter(org -> MINIMAL_ORGANIZATION_INITIATOR.equals(org.getInitiator()))
				.filter(org -> org.getAddresses() != null && org.getAddresses().size() == 3)
				.findFirst()
				.orElse(null);

		if (existing != null) {
			return existing;
		}

		// Créer l'organisation de base
		LocalDateTime now = LocalDateTime.now();
		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName(MINIMAL_ORGANIZATION_NAME);
		organization.setDescription(MINIMAL_ORGANIZATION_DESCRIPTION);
		organization.setInitiator(MINIMAL_ORGANIZATION_INITIATOR);
		organization.setCreationDate(now);
		organization.setFunctionalStatus(FUNCTIONAL_STATUS_KEY);
		organization.setStatus(Status.COMPLETED);
		organization.setOrganizationStatus(OrganizationStatus.VALIDATED);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);
		organization.setOpeningDate(now);

		// Créer et ajouter les 3 adresses directement
		organization.setAddresses(new java.util.HashSet<>(List.of(
				abstractAddressDataFactory.createEmailAddressEntity(null),
				abstractAddressDataFactory.createWebsiteAddressEntity(null),
				abstractAddressDataFactory.createPhoneAddressEntity(null)
		)));

		// Persister l'organisation avec ses adresses
		return repository.save(organization);
	}

	/**
	 * Récupère ou crée une organization minimale avec toutes les adresses - DTO
	 *
	 * Utilise getOrCreateMinimalOrganizationWithAllAddresses() et la convertit en DTO via le mapper.
	 * Idempotente (délégation).
	 *
	 * @return organisation persistée en BDD avec 3 adresses de contact (DTO)
	 */
	public Organization getOrCreateMinimalOrganizationWithAllAddressesDto() {
		return organizationMapper.entityToDto(getOrCreateMinimalOrganizationWithAllAddresses());
	}

	public void deleteAllOrganizationMembers() {
		repository.findAll().forEach(organization -> {
			if (organization.getMembers() != null) {
				organization.getMembers().clear();
				repository.save(organization);
			}
		});
	}

	public void deleteOrganizationMembers(UUID organizationUuid) {
		OrganizationEntity organization = repository.findByUuid(organizationUuid);
		if (organization != null && organization.getMembers() != null) {
			organization.getMembers().clear();
			repository.save(organization);
		}
	}

	public void deleteAllOrganizations() {
		repository.deleteAll();
	}

}
