package org.rudi.microservice.strukture.service.provider.impl;

import java.security.InvalidParameterException;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.core.bean.OwnerInfo;
import org.rudi.microservice.strukture.core.bean.criteria.LinkedProducerSearchCriteria;
import org.rudi.microservice.strukture.service.helper.LinkedProducerHelper;
import org.rudi.microservice.strukture.service.helper.OwnerInfoHelper;
import org.rudi.microservice.strukture.service.helper.ProviderHelper;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationHelper;
import org.rudi.microservice.strukture.service.mapper.LinkedProducerMapper;
import org.rudi.microservice.strukture.service.provider.LinkedProducerService;
import org.rudi.microservice.strukture.storage.dao.provider.LinkedProducerCustomDao;
import org.rudi.microservice.strukture.storage.dao.provider.LinkedProducerDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerStatus;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class LinkedProducerServiceImpl implements LinkedProducerService {
	private final LinkedProducerDao linkedProducerDao;
	private final LinkedProducerCustomDao linkedProducerCustomDao;

	private final OwnerInfoHelper ownerInfoHelper;
	private final ProviderHelper providerHelper;
	private final OrganizationHelper organizationHelper;
	private final LinkedProducerHelper linkedProducerHelper;

	private final UtilPageable utilPageable;

	private final LinkedProducerMapper linkedProducerMapper;

	@Override
	public OwnerInfo getLinkedProducerOwnerInfo(UUID uuid)
			throws AppServiceBadRequestException, IllegalArgumentException {
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
	public LinkedProducer createLinkedProducer(UUID organizationUuid) throws AppServiceNotFoundException,
			AppServiceUnauthorizedException, AppServiceBadRequestException, DataIntegrityViolationException {
		// Récupération du provider concerné
		ProviderEntity provider = providerHelper.getMyProvider();

		// Récupération de l'organization concernée
		OrganizationEntity organization = getOrganizationEntityValidatedFromUuid(organizationUuid);

		LinkedProducer linkedProducer = getMyLinkedProducerFromOrganization(organizationUuid);
		if (linkedProducer != null) {
			if(!linkedProducer.getLinkedProducerStatus().equals(org.rudi.microservice.strukture.core.bean.LinkedProducerStatus.CANCELLED)){
				log.error("Organization {} déjà liée au provider {}", organizationUuid, provider.getUuid());
				throw new DataIntegrityViolationException("Organisation déjà liée.");
			}
			/*
			Elle a été refusée donc on la renvoie pour revalidation : renouvellement de la demande de rattachement.
			Pour relancer le workflow, on réinitialise les status comme si on démarrait avec un nouvel objet
			*/
			return reinitializeLinkedProducer(linkedProducer.getUuid());
		}

		return linkedProducerHelper.createLinkedProducer(organization, provider);
	}

	@Override
	public Page<LinkedProducer> searchLinkedProducers(LinkedProducerSearchCriteria criteria, Pageable pageable) {
		return linkedProducerMapper.entitiesToDto(linkedProducerCustomDao.searchLinkedProducers(criteria, pageable),
				pageable);
	}

	@Override
	public LinkedProducer getMyLinkedProducerFromOrganizationUuid(UUID organizationUuid)
			throws AppServiceUnauthorizedException {
		LinkedProducer linkedProducer = getMyLinkedProducerFromOrganization(organizationUuid);
		if (linkedProducer == null) {
			log.error("Organization {} non liée au provider", organizationUuid);
			throw new DataIntegrityViolationException("L'organisation n'est pas liée.");
		}
		return linkedProducer;
	}

	private LinkedProducer getMyLinkedProducerFromOrganization(UUID organizationUuid)
			throws AppServiceUnauthorizedException {
		LinkedProducerSearchCriteria criteria = LinkedProducerSearchCriteria.builder()
				.providerUuid(providerHelper.getMyProvider().getUuid()).organizationUuid(organizationUuid).build();

		Pageable pageable = utilPageable.getPageable(0, 1, null);
		Page<LinkedProducer> pagedLinkedProducer = searchLinkedProducers(criteria, pageable);

		if (pagedLinkedProducer.isEmpty()) {
			return null;
		}

		return pagedLinkedProducer.getContent().get(0);
	}

	private OrganizationEntity getOrganizationEntityValidatedFromUuid(UUID uuid) throws AppServiceNotFoundException {
		// Vérifie que l'organisation existe bien et qu'elle a bien été validée par un moderateur.
		OrganizationEntity organizationEntity = organizationHelper.getOrganizationEntity(uuid);
		if (!organizationEntity.getOrganizationStatus().equals(OrganizationStatus.VALIDATED)) {
			throw new InvalidParameterException("Orgnanization is not in a valid state");
		}

		return organizationEntity;
	}

	private LinkedProducer reinitializeLinkedProducer(UUID linkedProducerUuid){
		LinkedProducerEntity linkedProducerEntity = linkedProducerDao.findByUuid(linkedProducerUuid);

		linkedProducerEntity.setLinkedProducerStatus(LinkedProducerStatus.DRAFT);
		linkedProducerEntity.setStatus(Status.DRAFT);
		linkedProducerEntity.setProcessDefinitionKey("linked-producer-process");

		return linkedProducerMapper.entityToDto(linkedProducerDao.save(linkedProducerEntity));
	}

}
