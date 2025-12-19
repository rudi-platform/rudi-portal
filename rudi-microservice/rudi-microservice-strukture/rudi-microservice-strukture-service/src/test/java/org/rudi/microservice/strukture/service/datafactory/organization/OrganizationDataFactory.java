package org.rudi.microservice.strukture.service.datafactory.organization;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.facet.acl.datafactory.UserDataFactory;
import org.rudi.facet.bpmn.datafactory.AbstractAssetDescriptionDataFactory;
import org.rudi.microservice.strukture.service.datafactory.organizationmember.OrganizationMemberDataFactory;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class OrganizationDataFactory extends AbstractAssetDescriptionDataFactory<OrganizationEntity, OrganizationDao> {

	public static String PROCESS_DEFINITION_KEY = "organization-process";
	public static String FUNCTIONAL_STATUS_KEY = "Créée";

	private final OrganizationMemberDataFactory organizationMemberDataFactory;
	private final UserDataFactory userDataFactory;

	public OrganizationDataFactory(OrganizationDao repository, OrganizationMemberDataFactory organizationMemberDataFactory, UserDataFactory userDataFactory) {
		super(repository, OrganizationEntity.class);
		this.organizationMemberDataFactory = organizationMemberDataFactory;
		this.userDataFactory = userDataFactory;
	}

	@Override
	protected void assignData(OrganizationEntity item) {
		item.setOrganizationStatus(OrganizationStatus.VALIDATED);
	}


	public OrganizationEntity create(
			UUID uuid, String processDefinitionKey, Status status, String functionnalStatus, String initiator,
			LocalDateTime creationDate, String description, OrganizationStatus organizationStatus, LocalDateTime openingDate,
			LocalDateTime closingDate, Geometry position, String name
	) {

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

	public OrganizationEntity createTestOrganizationLinkedProducer(UUID uuid) {
		if(uuid == null) {
			uuid = UUID.randomUUID();
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime yesterday = now.minusDays(1);
		return create(uuid, PROCESS_DEFINITION_KEY, Status.COMPLETED, FUNCTIONAL_STATUS_KEY, randomString(20),
				now, randomString(60), OrganizationStatus.VALIDATED, yesterday, null, null,
				randomString(20));
	}

	/**
	 * COMPLETED // VALIDATED
	 *
	 * @param initiator (optional)
	 * @return organization
	 */
	public OrganizationEntity createIRISAOrganization(String initiator) {
		if (StringUtils.isEmpty(initiator)) {
			initiator = "initiator@mail.fr";
		}

		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName("IRISA");
		organization.setDescription("Institut de Recherche en Informatique et Systèmes Aléatoires");
		organization.setUrl("https://www.irisa.fr/");
		organization.setInitiator(initiator);
		organization.setCreationDate(LocalDateTime.now());
		organization.setFunctionalStatus("Validée");
		organization.setStatus(Status.COMPLETED);
		organization.setOrganizationStatus(OrganizationStatus.VALIDATED);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);

		LocalDateTime date = LocalDateTime.of(1975, Month.JANUARY, 1, 23, 38, 12, 0);
		organization.setOpeningDate(date);


		return repository.save(organization);
	}

	/**
	 * COMPLETED // VALIDATED
	 * @param initiator (optional)
	 * @return organization
	 */
	public OrganizationEntity createOpenOrganization(String initiator) {
		if (StringUtils.isEmpty(initiator)) {
			initiator = "initiator@mail.fr";
		}
		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName("Open");
		organization.setDescription("Welcome to your high‑digital place");
		organization.setUrl("https://www.open.global/");
		organization.setInitiator(initiator);
		organization.setCreationDate(LocalDateTime.now());
		organization.setFunctionalStatus("Validée");
		organization.setStatus(Status.COMPLETED);
		organization.setOrganizationStatus(OrganizationStatus.VALIDATED);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);

		LocalDateTime date = LocalDateTime.of(1989, Month.FEBRUARY, 1, 23, 38, 12, 0);
		organization.setOpeningDate(date);


		return repository.save(organization);

	}

	/**
	 * COMPLETED // VALIDATED
	 * @param initiator (optional)
	 * @return organization
	 */
	public OrganizationEntity createRMOrganization(String initiator) {
		if (StringUtils.isEmpty(initiator)) {
			initiator = "initiator@mail.fr";
		}
		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName("Rennes Métropole");
		organization.setDescription("Municipalité de Rennes et alentours");
		organization.setUrl("https://www.open.global/");
		organization.setInitiator(initiator);
		organization.setCreationDate(LocalDateTime.now());
		organization.setFunctionalStatus("Validée");
		organization.setStatus(Status.COMPLETED);
		organization.setOrganizationStatus(OrganizationStatus.VALIDATED);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);

		LocalDateTime date = LocalDateTime.of(2015, Month.JANUARY, 14, 23, 38, 12, 0);
		organization.setOpeningDate(date);

		return repository.save(organization);
	}

	/**
	 * COMPLETED // VALIDATED
	 * @param initiator (optional)
	 * @return organization
	 */
	public OrganizationEntity createViaRomaOrganization(String initiator) {
		if (StringUtils.isEmpty(initiator)) {
			initiator = "initiator@mail.fr";
		}
		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName("Via Roma");
		organization.setDescription("Temple de la Marcello.");
		organization.setUrl("https://www.la-via-roma-rennes.fr/");
		organization.setInitiator(initiator);
		organization.setCreationDate(LocalDateTime.now());
		organization.setFunctionalStatus("Validée");
		organization.setStatus(Status.COMPLETED);
		organization.setOrganizationStatus(OrganizationStatus.VALIDATED);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);

		LocalDateTime date = LocalDateTime.of(2009, Month.JUNE, 4, 23, 38, 12, 0);
		organization.setOpeningDate(date);


		return repository.save(organization);
	}

	/**
	 * DELETED // DISENGAGED
	 * @param initiator (optional)
	 * @return organization
	 */
	public OrganizationEntity createCookingPotOrganization(String initiator) {
		if (StringUtils.isEmpty(initiator)) {
			initiator = "initiator@mail.fr";
		}
		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName("Cooking Pot");
		organization.setDescription("Feu la succursale cachée d'OPEN.");
		organization.setUrl("https://www.google.com/search?q=cooking+pot+restaurant+rennes");
		organization.setInitiator(initiator);
		organization.setCreationDate(LocalDateTime.now());
		organization.setFunctionalStatus("Fermée");
		organization.setStatus(Status.DELETED);
		organization.setOrganizationStatus(OrganizationStatus.DISENGAGED);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);

		LocalDateTime date = LocalDateTime.of(2017, Month.JANUARY, 7, 23, 38, 12, 0);
		organization.setOpeningDate(date);

		LocalDateTime date2 = LocalDateTime.of(2022, Month.APRIL, 14, 23, 38, 12, 0);
		organization.setClosingDate(date2);

		return repository.save(organization);
	}

	/**
	 * DRAFT // DRAFT
	 * @param initiator (optional)
	 * @return organization
	 */
	public OrganizationEntity createBlockOrganization(String initiator) {
		if (StringUtils.isEmpty(initiator)) {
			initiator = "initiator@mail.fr";
		}
		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName("Block");
		organization.setDescription("Remplaçant du Cooking Pot.");
		organization.setUrl("https://www.blockrennes.com/");
		organization.setInitiator(initiator);
		organization.setCreationDate(LocalDateTime.now());
		organization.setFunctionalStatus("En rodage");
		organization.setStatus(Status.DRAFT);
		organization.setOrganizationStatus(OrganizationStatus.DRAFT);
		organization.setProcessDefinitionKey(PROCESS_DEFINITION_KEY);

		LocalDateTime date = LocalDateTime.of(2024, Month.JUNE, 25, 23, 38, 12, 0);
		organization.setOpeningDate(date);


		return repository.save(organization);
	}


}

