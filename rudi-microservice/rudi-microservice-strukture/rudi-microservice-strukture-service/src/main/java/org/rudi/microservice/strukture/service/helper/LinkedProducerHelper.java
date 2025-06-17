package org.rudi.microservice.strukture.service.helper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.service.mapper.LinkedProducerMapper;
import org.rudi.microservice.strukture.storage.dao.provider.ProviderDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerStatus;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LinkedProducerHelper {
	public static final String INITIAL_FUNCTIONNAL_STATUS = "Lien créé";

	private final ProviderDao providerDao;
	private final LinkedProducerMapper linkedProducerMapper;

	/**
	 * Crée un linkedDataset en base de donnée
	 *
	 * @param organization entité organization à lier
	 * @param provider entité provider à qui l'organisation doit être liée
	 * @return le linkedProducerCréé
	 * @throws AppServiceBadRequestException si le linkedProducer n'est pas correctement créé
	 */
	public LinkedProducer createLinkedProducer(OrganizationEntity organization, ProviderEntity provider, NodeProvider nodeProvider) throws AppServiceBadRequestException {
		// Rattachement de l'organization au provider
		provider.getLinkedProducers().add(createDraftLinkedProducerEntity(organization, provider, nodeProvider));

		// Récupération de l'entité sauvegardée en base
		ProviderEntity savedProvider = providerDao.save(provider);
		Optional<LinkedProducerEntity> createdLinkedProducer = savedProvider.getLinkedProducers().stream().filter(
						linkedProducerEntity -> linkedProducerEntity.getOrganization().getUuid().equals(organization.getUuid()))
				.findFirst();

		if (createdLinkedProducer.isEmpty()) {
			throw new AppServiceBadRequestException(
					String.format("Une erreur est survenue lors du rattachement de l'organisation %s au provider %s",
							organization.getUuid(), provider.getUuid()));
		}

		return linkedProducerMapper.entityToDto(createdLinkedProducer.get());
	}

	/**
	 *	Crée une version draft du linkedDataset avec tous les champs correctement pré-saisis.
	 *
	 * @param organization entité organization à lier
	 * @param provider entité provider à qui l'organisation doit être liée
	 * @return le linkedProducerCréé
	 */
	private LinkedProducerEntity createDraftLinkedProducerEntity(OrganizationEntity organization, ProviderEntity provider, NodeProvider nodeProvider) {
		LocalDateTime now = LocalDateTime.now();

		// Création de l'objet LinkedProducer
		LinkedProducerEntity linkedProducerEntity = new LinkedProducerEntity();
		linkedProducerEntity.setUuid(UUID.randomUUID());
		linkedProducerEntity.setDescription(String.format("Rattachement de l'organsiation %s au provider %s",
				organization.getName(), provider.getLabel()));
		linkedProducerEntity.setFunctionalStatus(INITIAL_FUNCTIONNAL_STATUS);
		linkedProducerEntity.setProcessDefinitionKey("linked-producer-process");
		linkedProducerEntity.setOrganization(organization);
		linkedProducerEntity.setLinkedProducerStatus(LinkedProducerStatus.DRAFT);
		linkedProducerEntity.setStatus(Status.DRAFT);
		linkedProducerEntity.setCreationDate(now);
		linkedProducerEntity.setUpdatedDate(now);
		linkedProducerEntity.setInitiator(nodeProvider.getUuid().toString());
		// s'assurer que dans les deux cas il s'agit du NP
		linkedProducerEntity.setUpdator(nodeProvider.getUuid().toString());

		return linkedProducerEntity;
	}
}
