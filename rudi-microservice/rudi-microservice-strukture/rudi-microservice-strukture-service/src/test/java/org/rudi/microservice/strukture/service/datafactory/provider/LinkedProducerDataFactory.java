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
public class LinkedProducerDataFactory extends AbstractAssetDescriptionDataFactory<LinkedProducerEntity, LinkedProducerDao> {
	public final String PROCESS_DEFINITION_KEY = "linked-producer-process";

	@Autowired
	private OrganizationDataFactory organizationDataFactory;

	public LinkedProducerDataFactory(LinkedProducerDao repository) {
		super(repository, LinkedProducerEntity.class);
	}

	public LinkedProducerEntity createValidatedLinkedProducer(UUID providerUuid, UUID orgnizationUuid, UUID nodeProviderUuid) {
		LocalDateTime now = LocalDateTime.now();
		LinkedProducerEntity linkedProducer = create(UUID.randomUUID(), PROCESS_DEFINITION_KEY, Status.COMPLETED, "Validé", nodeProviderUuid.toString(), now, randomString(100));
		linkedProducer.setLinkedProducerStatus(LinkedProducerStatus.VALIDATED);

		OrganizationEntity organization = organizationDataFactory.createTestOrganizationLinkedProducer(orgnizationUuid);
		linkedProducer.setOrganization(organization);

		return repository.save(linkedProducer);
	}
}
