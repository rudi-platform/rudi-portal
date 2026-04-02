package org.rudi.microservice.strukture.service.datafactory.provider;

import java.time.LocalDateTime;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Status;
import org.rudi.facet.bpmn.datafactory.AbstractAssetDescriptionDataFactory;
import org.rudi.microservice.strukture.service.datafactory.organization.OrganizationDataFactory;
import org.rudi.microservice.strukture.storage.dao.provider.LinkedProducerDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Component
public class LinkedProducerDataFactory
		extends AbstractAssetDescriptionDataFactory<LinkedProducerEntity, LinkedProducerDao> {
	public final String PROCESS_DEFINITION_KEY = "linked-producer-process";

	@Autowired
	private OrganizationDataFactory organizationDataFactory;

	public LinkedProducerDataFactory(LinkedProducerDao repository) {
		super(repository, LinkedProducerEntity.class);
	}


	public LinkedProducerEntity createValidatedLinkedProducer(UUID providerUuid, UUID orgnizationUuid,
			UUID nodeProviderUuid) {
		return createLinkedProducer(providerUuid, orgnizationUuid, nodeProviderUuid, Status.COMPLETED, LinkedProducerStatus.VALIDATED);
	}

	public LinkedProducerEntity createDraftLinkedProducer(UUID providerUuid, UUID orgnizationUuid, UUID nodeProviderUuid) {
		return createLinkedProducer(providerUuid, orgnizationUuid, nodeProviderUuid, Status.DRAFT, LinkedProducerStatus.DRAFT);
	}

	public LinkedProducerEntity createAttachLinkedProducer(UUID providerUuid, UUID orgnizationUuid, UUID nodeProviderUuid) {
		return createLinkedProducer(providerUuid, orgnizationUuid, nodeProviderUuid, Status.PENDING, LinkedProducerStatus.IN_PROGRESS);
	}

	public LinkedProducerEntity createDetachLinkedProducer(UUID providerUuid, UUID orgnizationUuid, UUID nodeProviderUuid) {
		return createLinkedProducer(providerUuid, orgnizationUuid, nodeProviderUuid, Status.DRAFT, LinkedProducerStatus.VALIDATED);
	}

	public LinkedProducerEntity createLinkedProducer(UUID providerUuid, UUID orgnizationUuid, UUID nodeProviderUuid,
			Status status, LinkedProducerStatus linkedProducerStatus) {
		LocalDateTime now = LocalDateTime.now();
		LinkedProducerEntity linkedProducer = create(UUID.randomUUID(), PROCESS_DEFINITION_KEY, status,
				"En cours", nodeProviderUuid.toString(), now, randomString(100));
		linkedProducer.setLinkedProducerStatus(linkedProducerStatus);

		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(orgnizationUuid);
		linkedProducer.setOrganization(organization);

		return repository.save(linkedProducer);
	}

	public void deleteAllLinkedProducer() {
		repository.deleteAll();
	}
}
