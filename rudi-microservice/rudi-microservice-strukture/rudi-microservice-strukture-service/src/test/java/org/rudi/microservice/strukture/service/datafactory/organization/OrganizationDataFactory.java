package org.rudi.microservice.strukture.service.datafactory.organization;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

import org.locationtech.jts.geom.Geometry;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.facet.bpmn.datafactory.AbstractAssetDescriptionDataFactory;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class OrganizationDataFactory extends AbstractAssetDescriptionDataFactory<OrganizationEntity, OrganizationDao> {

	public static String PROCESS_DEFINITION_KEY = "organziation-process";
	public static String FUNCTIONAL_STATUS_KEY = "Créée";

	public OrganizationDataFactory(OrganizationDao repository) {
		super(repository, OrganizationEntity.class);
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

	public OrganizationEntity createLiksiOrganization(){
		OrganizationEntity organization = new OrganizationEntity();
		organization.setUuid(UUID.randomUUID());
		organization.setName("Liksi");
		organization.setDescription("petite ESN");
		organization.setUrl("http://liksi.com");
		organization.setInitiator("initiator@mail.fr");
		organization.setCreationDate(LocalDateTime.now());
		organization.setFunctionalStatus("Validée");
		organization.setStatus(Status.COMPLETED);
		organization.setOrganizationStatus(OrganizationStatus.VALIDATED);
		organization.setProcessDefinitionKey("organization-process");

		LocalDateTime date = LocalDateTime.of(2022, Month.APRIL, 14, 23, 38, 12, 0);
		organization.setOpeningDate(date);

		LocalDateTime date2 = LocalDateTime.of(2025, Month.APRIL, 14, 23, 38, 12, 0);
		organization.setClosingDate(date2);

		return repository.save(organization);
	}





}

