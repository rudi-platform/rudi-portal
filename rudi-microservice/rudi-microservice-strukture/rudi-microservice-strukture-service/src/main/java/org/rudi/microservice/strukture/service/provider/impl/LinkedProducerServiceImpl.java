package org.rudi.microservice.strukture.service.provider.impl;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.core.bean.OwnerInfo;
import org.rudi.microservice.strukture.service.helper.OwnerInfoHelper;
import org.rudi.microservice.strukture.service.helper.ProviderHelper;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationHelper;
import org.rudi.microservice.strukture.service.mapper.LinkedProducerMapper;
import org.rudi.microservice.strukture.service.provider.LinkedProducerService;
import org.rudi.microservice.strukture.storage.dao.provider.LinkedProducerDao;
import org.rudi.microservice.strukture.storage.dao.provider.ProviderDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerStatus;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LinkedProducerServiceImpl implements LinkedProducerService {

	private final OrganizationHelper organizationHelper;
	private final LinkedProducerMapper linkedProducerMapper;
	private final ProviderHelper providerHelper;
	private final ProviderDao providerDao;
	private final LinkedProducerDao linkedProducerDao;
	private final OwnerInfoHelper ownerInfoHelper;

	@Override
	public OwnerInfo getLinkedProducerOwnerInfo(UUID uuid) throws AppServiceBadRequestException, IllegalArgumentException {
		LinkedProducerEntity entity = linkedProducerDao.findByUuid(uuid);
		if (entity == null) {
			throw new AppServiceBadRequestException(String.format("No linked producer for the uuid : %s", uuid));
		}
		return ownerInfoHelper.getAssetDescriptionOwnerInfo(entity);
	}

	@Override
	public LinkedProducer getLinkedProducer(UUID linkedProducerUuid) {
		return linkedProducerMapper.entityToDto(linkedProducerDao.findByUuid(linkedProducerUuid));
	}

	@Override
	@Transactional
	public LinkedProducer createLinkedProducer(UUID organizationUuid) throws AppServiceNotFoundException, AppServiceUnauthorizedException, AppServiceBadRequestException {
		// Récupération du provider concerné
		ProviderEntity provider = providerHelper.getMyProvider();

		// Récupération de l'organization concernée
		OrganizationEntity organization = getOrganizationEntityValidatedFromUuid(organizationUuid);

		// Rattachement de l'organization au provider
		provider.getLinkedProducers().add(createLinkedProducer(organization, provider));

		// Récupération de l'entité sauvegardée en base
		ProviderEntity savedProvider = providerDao.save(provider);
		Optional<LinkedProducerEntity> createdLinkedProducer = savedProvider.getLinkedProducers().stream().filter(LinkedProducerEntity -> LinkedProducerEntity.getOrganization().getUuid().equals(organizationUuid)).findFirst();

		if (createdLinkedProducer.isEmpty()) {
			throw new AppServiceBadRequestException(String.format("Une erreur est survenue lors du rattachement de l'organisation %s au provider %s", organizationUuid, provider.getUuid()));
		}

		return linkedProducerMapper.entityToDto(createdLinkedProducer.get());
	}

	private LinkedProducerEntity createLinkedProducer(OrganizationEntity organization, ProviderEntity provider) {
		LocalDateTime now = LocalDateTime.now();

		// Création de l'objet LinkedProducer
		LinkedProducerEntity linkedProducerEntity = new LinkedProducerEntity();
		linkedProducerEntity.setUuid(UUID.randomUUID());
		linkedProducerEntity.setDescription(String.format("Rattachement de l'organsiation %s au provider %s", organization.getName(), provider.getLabel()));
		linkedProducerEntity.setFunctionalStatus("Lien créé");
		linkedProducerEntity.setProcessDefinitionKey("linked-producer-process");
		linkedProducerEntity.setOrganization(organization);
		linkedProducerEntity.setLinkedProducerStatus(LinkedProducerStatus.DRAFT);
		linkedProducerEntity.setStatus(Status.DRAFT);
		linkedProducerEntity.setCreationDate(now);
		linkedProducerEntity.setUpdatedDate(now);
		linkedProducerEntity.setInitiator(provider.getUuid().toString());
		linkedProducerEntity.setUpdator(provider.getUuid().toString());

		return linkedProducerEntity;
	}

	private OrganizationEntity getOrganizationEntityValidatedFromUuid(UUID uuid) throws AppServiceNotFoundException {
		// Vérifie que l'organisation existe bien et qu'elle a bien été validée par un moderateur.
		OrganizationEntity organizationEntity = organizationHelper.getOrganizationEntity(uuid);
		if (!organizationEntity.getOrganizationStatus().equals(OrganizationStatus.VALIDATED)) {
			throw new InvalidParameterException("Orgnanization is not in a valid state");
		}

		return organizationEntity;
	}
}
