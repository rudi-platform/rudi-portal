package org.rudi.microservice.strukture.service.organization.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.AppServiceForbiddenException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.projekt.helper.ProjektHelper;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.core.bean.OrganizationMember;
import org.rudi.microservice.strukture.core.bean.OrganizationSearchCriteria;
import org.rudi.microservice.strukture.core.bean.OrganizationUserMember;
import org.rudi.microservice.strukture.core.bean.OwnerInfo;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationMembersSearchCriteria;
import org.rudi.microservice.strukture.service.exception.CannotRemoveLastAdministratorException;
import org.rudi.microservice.strukture.service.exception.UserIsNotOrganizationAdministratorException;
import org.rudi.microservice.strukture.service.helper.OwnerInfoHelper;
import org.rudi.microservice.strukture.service.helper.ProviderHelper;
import org.rudi.microservice.strukture.service.helper.StruktureAuthorisationHelper;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationHelper;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationMembersHelper;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationMembersPartitionerHelper;
import org.rudi.microservice.strukture.service.mapper.OrganizationMapper;
import org.rudi.microservice.strukture.service.mapper.OrganizationMemberMapper;
import org.rudi.microservice.strukture.service.organization.OrganizationService;
import org.rudi.microservice.strukture.service.organization.impl.fields.CreateOrganizationFieldProcessor;
import org.rudi.microservice.strukture.service.organization.impl.fields.UpdateOrganizationFieldProcessor;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationCustomDao;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationMemberEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
	private final OrganizationMapper organizationMapper;
	private final Collection<CreateOrganizationFieldProcessor> createOrganizationFieldProcessors;
	private final Collection<UpdateOrganizationFieldProcessor> updateOrganizationFieldProcessors;

	private final OrganizationMemberMapper organizationMemberMapper;
	private final ProjektHelper projektHelper;
	private final ACLHelper aclHelper;
	private final StruktureAuthorisationHelper struktureAuthorisationHelper;
	private final OrganizationMembersHelper organizationMembersHelper;
	private final OrganizationMembersPartitionerHelper organizationMembersPartitionerHelper;
	private final OrganizationHelper organizationHelper;
	private final ProviderHelper providerHelper;
	private final OwnerInfoHelper ownerInfoHelper;

	@Value("${default.organization.roles:USER,ORGANIZATION}")
	private List<String> defaultOrganizationRoles;

	@Override
	public OwnerInfo getOrganizationOwnerInfo(UUID uuid) throws AppServiceBadRequestException, IllegalArgumentException {
		OrganizationEntity entity = organizationDao.findByUuid(uuid);
		if (entity == null) {
			throw new AppServiceBadRequestException(String.format("No organization for the uuid : %s", uuid));
		}
		return ownerInfoHelper.getAssetDescriptionOwnerInfo(entity);
	}

	@Override
	public Organization getOrganization(UUID uuid) throws AppServiceNotFoundException {
		OrganizationEntity entity = organizationHelper.getOrganizationEntity(uuid);
		return organizationMapper.entityToDto(entity);
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
		val entity = organizationMapper.dtoToEntity(organization);
		for (final CreateOrganizationFieldProcessor processor : createOrganizationFieldProcessors) {
			processor.processBeforeCreate(entity);
		}

		final LocalDateTime now = LocalDateTime.now();
		entity.setCreationDate(now);
		entity.setUpdatedDate(now);

		return organizationMapper.entityToDto(organizationDao.save(entity));
	}

	@Override
	@Transactional // (readOnly = false)
	public void updateOrganization(Organization organization) throws AppServiceException {
		val existingEntity = organizationHelper.getOrganizationEntity(organization.getUuid());
		for (final UpdateOrganizationFieldProcessor processor : updateOrganizationFieldProcessors) {
			processor.processBeforeUpdate(organization, existingEntity);
		}
		organizationMapper.dtoToEntity(organization, existingEntity);
	}

	@Override
	@Transactional // (readOnly = false)
	public void deleteOrganization(UUID uuid) throws AppServiceNotFoundException {
		val entity = organizationHelper.getOrganizationEntity(uuid);
		organizationDao.delete(entity);
	}

	@Override
	public Page<Organization> searchOrganizations(OrganizationSearchCriteria searchCriteria, Pageable pageable) {
		return organizationMapper.entitiesToDto(organizationCustomDao.searchOrganizations(searchCriteria, pageable),
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
	@Transactional(rollbackFor = {CannotRemoveLastAdministratorException.class, RuntimeException.class})
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

		// Pour Jules : Un mapper.dtoToEntity(dto, entity) ne fait pas de modification de la BD, il FAUT un dao.save(entity)
		organizationDao.save(existingOrganization);

		return organizationMemberMapper.entityToDto(member);
	}


	private boolean isLastAdministrator(OrganizationEntity organization, UUID userUuid) {
		val adminMembers = organization.getMembers().stream()
				.filter(orgaMember -> OrganizationRole.ADMINISTRATOR.equals(orgaMember.getRole()))
				.collect(Collectors.toList());
		return adminMembers.size() == 1 && adminMembers.get(0).getUserUuid().equals(userUuid);
	}
}
