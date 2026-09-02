package org.rudi.microservice.strukture.service.organization.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.AppServiceForbiddenException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.projekt.helper.ProjektHelper;
import org.rudi.microservice.strukture.core.bean.NodeOrganization;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.core.bean.OrganizationMember;
import org.rudi.microservice.strukture.core.bean.OrganizationUserMember;
import org.rudi.microservice.strukture.core.bean.OwnerInfo;
import org.rudi.microservice.strukture.core.bean.criteria.NodeOrganizationSearchCriteria;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationMembersSearchCriteria;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationSearchCriteria;
import org.rudi.microservice.strukture.service.exception.CannotRemoveLastAdministratorException;
import org.rudi.microservice.strukture.service.exception.UserIsNotOrganizationAdministratorException;
import org.rudi.microservice.strukture.service.helper.OwnerInfoHelper;
import org.rudi.microservice.strukture.service.helper.ProviderHelper;
import org.rudi.microservice.strukture.service.helper.StruktureAuthorisationHelper;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationHelper;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationMembersHelper;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationMembersPartitionerHelper;
import org.rudi.microservice.strukture.service.mapper.NodeOrganizationMapper;
import org.rudi.microservice.strukture.service.mapper.OrganizationFullMapper;
import org.rudi.microservice.strukture.service.mapper.OrganizationMemberMapper;
import org.rudi.microservice.strukture.service.mapper.OrganizationSimpleMapper;
import org.rudi.microservice.strukture.service.organization.OrganizationService;
import org.rudi.microservice.strukture.service.organization.impl.fields.CreateOrganizationFieldProcessor;
import org.rudi.microservice.strukture.service.organization.impl.fields.UpdateOrganizationFieldProcessor;
import org.rudi.microservice.strukture.storage.bean.NodeOrganizationProjectionBean;
import org.rudi.microservice.strukture.storage.dao.address.AbstractAddressDao;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationCustomDao;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationDao;
import org.rudi.microservice.strukture.storage.entity.address.AbstractAddressEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationMemberEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationRole;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {

	// Le nombre maximum d'UUIDs de membres qu'on veut exploiter pour croiser avec ACL afin de ne pas avoir
	// une requête HTTP vers ACL trop longue
	private static final int MAX_AMOUNT_OF_ORGANIZATION_MEMBERS = 50;

	private final OrganizationDao organizationDao;
	private final OrganizationCustomDao organizationCustomDao;
	private final AbstractAddressDao abstractAddressDao;
	private final OrganizationSimpleMapper organizationSimpleMapper;
	private final OrganizationFullMapper organizationFullMapper;
	private final NodeOrganizationMapper nodeOrganizationMapper;
	private final Collection<CreateOrganizationFieldProcessor> createOrganizationFieldProcessors;
	private final Collection<UpdateOrganizationFieldProcessor> updateOrganizationFieldProcessors;

	private final OrganizationMemberMapper organizationMemberMapper;
	private final ProjektHelper projektHelper;
	private final ACLHelper aclHelper;
	private final StruktureAuthorisationHelper struktureAuthorisationHelper;
	private final OrganizationMembersHelper organizationMembersHelper;
	private final OrganizationMembersPartitionerHelper organizationMembersPartitionerHelper;
	private final OrganizationHelper organizationHelper;
	private final OwnerInfoHelper ownerInfoHelper;

	@Value("${default.organization.roles:USER,ORGANIZATION}")
	private List<String> defaultOrganizationRoles;

	@Autowired
	private ProviderHelper providerHelper;

	@Override
	public OwnerInfo getOrganizationOwnerInfo(UUID uuid)
			throws AppServiceBadRequestException, IllegalArgumentException {
		OrganizationEntity entity = organizationDao.findByUuid(uuid);
		if (entity == null) {
			throw new AppServiceBadRequestException(String.format("No organization for the uuid : %s", uuid));
		}
		return ownerInfoHelper.getAssetDescriptionOwnerInfo(entity);
	}

	@Override
	public Organization getOrganization(UUID uuid, boolean full) throws AppServiceException {
		OrganizationEntity entity = organizationHelper.getOrganizationEntity(uuid);
		if (OrganizationStatus.DISENGAGED.equals(entity.getOrganizationStatus())) {
			struktureAuthorisationHelper.checkRightsAdminsterOrganization(entity);
		}
		if (full) {
			return organizationFullMapper.entityToDto(entity);
		}
		return organizationSimpleMapper.entityToDto(entity);
	}

	@Override
	public User getOrganizationUserFromOrganizationUuid(UUID organizationUuid) throws AppServiceForbiddenException {
		Map<String, Boolean> accessRightsRoles = StruktureAuthorisationHelper
				.getADMINISTRATOR_MODULE_STRUKTURE_ACCESS();

		if (!(struktureAuthorisationHelper.isAccessGrantedByRole(accessRightsRoles)
				|| struktureAuthorisationHelper.isAccessGrantedForUserOnOrganization(organizationUuid))) {
			throw new AppServiceForbiddenException(
					String.format("Authenticated user is not member of organization %s", organizationUuid));
		}

		final var organizationUserLogin = getOrganizationUserLoginFromOrganizationUuid(organizationUuid);
		return aclHelper.getUserByLogin(organizationUserLogin);
	}

	private String getOrganizationUserLoginFromOrganizationUuid(UUID organizationUuid) {
		return organizationUuid.toString();
	}

	@Override
	@Transactional // (readOnly = false)
	public Organization createOrganization(Organization organization) throws AppServiceBadRequestException {
		val entity = organizationFullMapper.dtoToEntity(organization);
		for (final CreateOrganizationFieldProcessor processor : createOrganizationFieldProcessors) {
			processor.processBeforeCreate(entity, organization);
		}

		final LocalDateTime now = LocalDateTime.now();
		entity.setCreationDate(now);
		entity.setUpdatedDate(now);

		return organizationFullMapper.entityToDto(organizationDao.save(entity));
	}

	@Override
	@Transactional // (readOnly = false)
	public void updateOrganization(Organization organization) throws AppServiceException {
		val existingEntity = organizationHelper.getOrganizationEntity(organization.getUuid());
		for (final UpdateOrganizationFieldProcessor processor : updateOrganizationFieldProcessors) {
			processor.processBeforeUpdate(organization, existingEntity);
		}
		organizationSimpleMapper.dtoToEntity(organization, existingEntity);
	}

	@Override
	@Transactional // (readOnly = false)
	public void deleteOrganization(UUID uuid) throws AppServiceNotFoundException {
		val entity = organizationHelper.getOrganizationEntity(uuid);
		final Set<AbstractAddressEntity> addressesToDelete = new HashSet<>(entity.getAddresses());
		entity.getAddresses().clear();
		organizationDao.save(entity);
		organizationDao.delete(entity);
		abstractAddressDao.deleteAll(addressesToDelete);
	}

	@Override
	public Page<NodeOrganization> searchNodeOrganizations(NodeOrganizationSearchCriteria searchCriteria,
			Pageable pageable) throws AppServiceException {
		// On ne veut que les organisations dont le status BPMN est COMPLETED
		searchCriteria.setStatus(Status.COMPLETED);

		// Récupération du provider concerné
		ProviderEntity provider = providerHelper.getMyProvider();
		if (provider != null) {
			searchCriteria.setProviderUUID(provider.getUuid());
		}
		return nodeOrganizationMapper
				.beansToNodeDto(organizationCustomDao.searchNodeOrganizations(searchCriteria, pageable), pageable);
	}

	@Override
	public Page<Organization> searchOrganizations(OrganizationSearchCriteria searchCriteria, Pageable pageable) {
		if (searchCriteria != null && Boolean.TRUE.equals(searchCriteria.getFull())) {
			return organizationFullMapper.entitiesToDto(organizationCustomDao.searchOrganizations(searchCriteria, pageable),
					pageable);
		}
		return organizationSimpleMapper.entitiesToDto(organizationCustomDao.searchOrganizations(searchCriteria, pageable),
				pageable);
	}

	@Override
	@Transactional // readOnly = false
	public OrganizationMember addOrganizationMember(UUID organizationUuid, OrganizationMember organizationMember)
			throws AppServiceException {
		Map<String, Boolean> accessRightsRoles = StruktureAuthorisationHelper
				.getADMINISTRATOR_MODULE_STRUKTURE_ACCESS();

		// Verifier que l'utilisateur connecté a le droit d'agir
		if (!(struktureAuthorisationHelper.isAccessGrantedByRole(accessRightsRoles) || struktureAuthorisationHelper
				.isAccessGrantedForUserOnOrganizationAsAdministrator(organizationUuid))) {
			throw new UserIsNotOrganizationAdministratorException(String.format(
					"L'utilisateur connecté n'est pas autorisé à agir sur l'organisation %s", organizationUuid));
		}

		OrganizationEntity organizationEntity = organizationHelper.getOrganizationEntity(organizationUuid);
		// Verifier que le membre qu'on ajoute est user ACL
		User correspondingUser = organizationMembersHelper.getUserByLoginOrByUuid(organizationMember.getLogin(),
				organizationMember.getUserUuid());
		organizationMember.setUserUuid(correspondingUser.getUuid()); // Utile si le DTO ne contenait que le login
		OrganizationMemberEntity organizationMemberEntity = organizationMemberMapper.dtoToEntity(organizationMember);
		organizationMemberEntity.setAddedDate(LocalDateTime.now());
		organizationMembersHelper.checkUserIsNotMember(organizationEntity, organizationMemberEntity);
		organizationEntity.getMembers().add(organizationMemberEntity);
		// Permet d'attendre l'ajout effectif du user à l'organisation avant d'envoyer la notif à projekt
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				// Notify projekt member has been added to update tasks candidatesex
				projektHelper.notifyUserHasBeenAdded(organizationUuid, organizationMember.getUserUuid());
			}
		});
		return organizationMemberMapper.entityToDto(organizationMemberEntity);
	}

	@Override
	public List<OrganizationMember> getOrganizationMembers(UUID organizationUuid) throws AppServiceException {
		Map<String, Boolean> accessRightsRoles = StruktureAuthorisationHelper
				.getADMINISTRATOR_MODERATOR_PROJEKT_ACCESS();

		// Verifier que l'utilisateur connecté a le droit d'agir
		if (!(struktureAuthorisationHelper.isAccessGrantedByRole(accessRightsRoles) || struktureAuthorisationHelper
				.isAccessGrantedForUserOnOrganizationAsAdministrator(organizationUuid))) {
			throw new UserIsNotOrganizationAdministratorException(String.format(
					"L'utilisateur connecté n'est pas autorisé à agir sur l'organisation %s", organizationUuid));
		}
		val organizationEntity = organizationHelper.getOrganizationEntity(organizationUuid);
		return organizationMemberMapper.entitiesToDto(organizationEntity.getMembers());
	}

	@Override
	@Transactional(rollbackFor = { CannotRemoveLastAdministratorException.class, RuntimeException.class })
	// readOnly = false
	public void removeOrganizationMembers(UUID organizationUuid, UUID userUuid) throws AppServiceException {
		Map<String, Boolean> accessRightsRoles = StruktureAuthorisationHelper
				.getADMINISTRATOR_MODULE_STRUKTURE_ACCESS();

		// Vérification des droits pour l'utilisation de cette fonction
		if (!(struktureAuthorisationHelper.isAccessGrantedByRole(accessRightsRoles) || struktureAuthorisationHelper
				.isAccessGrantedForUserOnOrganizationAsAdministrator(organizationUuid))) {
			throw new AppServiceUnauthorizedException(
					"L'utilisateur connecté n'a pas le droit de manipuler cette organisation");
		}

		val organizationEntity = organizationHelper.getOrganizationEntity(organizationUuid);

		final var anyAdministratorBeforeRemovingMember = organizationEntity.getMembers().stream()
				.filter(member -> member.getRole() == OrganizationRole.ADMINISTRATOR).findAny();

		organizationEntity.getMembers().removeIf(member -> member.getUserUuid().equals(userUuid));

		final var anyAdministratorAfterRemovingMember = organizationEntity.getMembers().stream()
				.filter(member -> member.getRole() == OrganizationRole.ADMINISTRATOR).findAny();

		if (anyAdministratorBeforeRemovingMember.isPresent() && anyAdministratorAfterRemovingMember.isEmpty()) {
			log.debug(String.format(
					"Il n'est pas possible de supprimer le dernier administrateur (userUuid = %s) de l'organisation %s",
					userUuid, organizationUuid));

			throw new CannotRemoveLastAdministratorException(
					"Il n'est pas possible de supprimer le dernier administrateur.");
		}

		// Permet d'attendre la suppression effective du user à l'organisation avant d'envoyer la notif à projekt
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				// Notify projekt member has been deleted to update tasks candidates
				projektHelper.notifyUserHasBeenRemoved(organizationUuid, userUuid);
			}
		});

	}

	@Override
	public Page<OrganizationUserMember> searchOrganizationMembers(OrganizationMembersSearchCriteria searchCriteria,
			Pageable pageable) throws AppServiceException {

		Map<String, Boolean> accessRightsRoles = StruktureAuthorisationHelper
				.getADMINISTRATOR_MODULE_STRUKTURE_ACCESS();

		// Vérification des droits pour l'utilisation de cette fonction
		if (!(struktureAuthorisationHelper.isAccessGrantedByRole(accessRightsRoles) || struktureAuthorisationHelper
				.isAccessGrantedForUserOnOrganizationAsAdministrator(searchCriteria.getOrganizationUuid()))) {
			throw new AppServiceUnauthorizedException(
					"L'utilisateur connecté n'a pas le droit de chercher des membres pour cette organisation");
		}

		// Gestion de la taille de la partition, inutile de charger 50 membres si on a une limite à 10
		int partitionSize = Math.min(MAX_AMOUNT_OF_ORGANIZATION_MEMBERS, searchCriteria.getLimit());

		List<Pageable> partitions = organizationMembersPartitionerHelper.getOrganizationMembersPartition(searchCriteria,
				partitionSize);

		List<OrganizationUserMember> members = new ArrayList<>();
		for (Pageable partition : partitions) {
			members.addAll(organizationMembersPartitionerHelper.partitionToEnrichedMembers(partition, searchCriteria));
		}

		return organizationMembersPartitionerHelper.extractPage(members, pageable);
	}

	@Override
	public Boolean isAuthenticatedOrganizationAdministrator(UUID organizationUuid) throws AppServiceException {
		return organizationMembersHelper.isAuthenticatedUserOrganizationAdministrator(organizationUuid);
	}

	@Override
	@Transactional
	public OrganizationMember updateOrganizationMember(UUID organizationUuid, UUID userUuid,
			OrganizationMember organizationMember) throws AppServiceException {
		Map<String, Boolean> accessRightsRoles = StruktureAuthorisationHelper.getADMINISTRATOR_ACCESS();

		// Vérifie que l'utilisateur connecté est bien administrateur de l'organisation
		if (!(struktureAuthorisationHelper.isAccessGrantedByRole(accessRightsRoles)
				|| struktureAuthorisationHelper.isAccessGrantedForUserOnOrganization(organizationUuid))) {
			throw new AppServiceUnauthorizedException(
					"L'utilisateur connecté n'a pas le droit de chercher des membres pour cette organisation");
		}

		// Vérifier que l'UUID de l'organisation passée correspond bien à une organisation connue, sinon throw une exception
		val existingOrganization = organizationHelper.getOrganizationEntity(organizationUuid);

		// Vérifier que l'UUID du membre passé est bien lié à l'organisation passée en paramètre.
		var member = existingOrganization.getMembers().stream()
				.filter(orgaMember -> orgaMember.getUserUuid().equals(userUuid)).findFirst().orElse(null);
		if (member == null) {
			throw new AppServiceBadRequestException(
					"Les paramètres fournis ne permettent pas de réaliser une opération logique");
		}

		// Vérifier la cohérence entre les paramètres passés.
		if (!organizationUuid.equals(organizationMember.getUuid())
				|| !userUuid.equals(organizationMember.getUserUuid())) {
			throw new AppServiceBadRequestException(
					"Les paramètres fournis ne permettent pas de réaliser une opération logique");
		}

		// Vérifier qu'on modifie bien le role.
		if (member.getRole().equals(organizationMember.getRole())) {
			return organizationMemberMapper.entityToDto(member); // throw exception ?
		}

		// Vérifier que l'on ne modifie pas le dernier administrateur.
		if (isLastAdministrator(existingOrganization, userUuid)) {
			log.debug(String.format(
					"Il n'est pas possible de modifier le dernier administrateur (userUuid = %s) de l'organisation %s en éditeur",
					userUuid, organizationUuid));
			throw new CannotRemoveLastAdministratorException(
					"Il n'est pas possible de supprimer le dernier administrateur.");
		}

		organizationMemberMapper.dtoToEntity(organizationMember, member);

		// Un mapper.dtoToEntity(dto, entity) ne fait pas de modification de la BD, il FAUT un dao.save(entity)
		organizationDao.save(existingOrganization);

		return organizationMemberMapper.entityToDto(member);
	}

	@Override
	public NodeOrganization getNodeOrganization(UUID uuid) throws AppServiceException {
		// Récupération du provider concerné
		ProviderEntity provider = providerHelper.getMyProvider();
		if (provider == null) {
			throw new AppServiceBadRequestException("No provider found");
		}

		NodeOrganizationSearchCriteria criteria = NodeOrganizationSearchCriteria.builder().uuids(List.of(uuid)).providerUUID(provider.getUuid()).build();

		NodeOrganizationProjectionBean nodeOrganizationProjectionBean = organizationCustomDao.searchNodeOrganizations(criteria, Pageable.unpaged()).stream().findFirst().orElseThrow(() -> new AppServiceBadRequestException("Organization not found"));

		return nodeOrganizationMapper.beanToNodeDto(nodeOrganizationProjectionBean);
	}

	@Override
	public Page<Organization> searchMyOrganizations(OrganizationSearchCriteria criteria, Pageable pageable) throws AppServiceException {
		// On force les valeurs liées au contexte du "my".
		criteria.setOrganizationStatus(List.of(org.rudi.microservice.strukture.core.bean.OrganizationStatus.VALIDATED));
		criteria.setStatus(Status.COMPLETED);
		criteria.setUserUuid(aclHelper.getAuthenticatedUserUuid());

		// Appel du search classique et pas du searchMy car pas de restriction supplémentaires
		if (Boolean.TRUE.equals(criteria.getFull())) {
			return organizationFullMapper.entitiesToDto(organizationCustomDao.searchMyOrganizations(criteria, pageable), pageable);
		}
		return organizationSimpleMapper.entitiesToDto(organizationCustomDao.searchMyOrganizations(criteria, pageable), pageable);
	}

	private boolean isLastAdministrator(OrganizationEntity organization, UUID userUuid) {
		val adminMembers = organization.getMembers().stream()
				.filter(orgaMember -> OrganizationRole.ADMINISTRATOR.equals(orgaMember.getRole()))
				.toList();
		return adminMembers.size() == 1 && adminMembers.get(0).getUserUuid().equals(userUuid);
	}

	private void deleteOrphanAddresses(Set<AbstractAddressEntity> previousAddresses,
			Set<AbstractAddressEntity> currentAddresses) {
		if (previousAddresses == null || previousAddresses.isEmpty()) {
			return;
		}

		// L'association est en ManyToMany : on conserve un nettoyage manuel des adresses détachées.
		final Set<UUID> currentAddressUuids = currentAddresses == null ? Set.of()
				: currentAddresses.stream().map(AbstractAddressEntity::getUuid).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		for (final AbstractAddressEntity previousAddress : previousAddresses) {
			if (previousAddress.getUuid() != null && !currentAddressUuids.contains(previousAddress.getUuid())) {
				abstractAddressDao.delete(previousAddress);
			}
		}
	}
}
