package org.rudi.microservice.acl.service.account.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Nonnull;

import bean.workflow.SelfdataTaskSearchCriteria;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.rudi.common.core.security.AccountState;
import org.rudi.common.core.security.UserType;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.MissingParameterException;
import org.rudi.facet.organization.bean.StruktureTaskSearchCriteria;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.projekt.helper.ProjektHelper;
import org.rudi.facet.selfdata.helper.SelfdataHelper;
import org.rudi.microservice.acl.core.bean.Account;
import org.rudi.microservice.acl.core.bean.PasswordChange;
import org.rudi.microservice.acl.core.bean.RoleSearchCriteria;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.core.bean.UserSearchCriteria;
import org.rudi.microservice.acl.service.account.AbstractAccountRegistrationException;
import org.rudi.microservice.acl.service.account.AccountService;
import org.rudi.microservice.acl.service.account.InvalidAccountTokenException;
import org.rudi.microservice.acl.service.account.LoginAlreadyExistsException;
import org.rudi.microservice.acl.service.account.LoginNotMailException;
import org.rudi.microservice.acl.service.account.MissingFieldException;
import org.rudi.microservice.acl.service.account.SendEmailRegistrationException;
import org.rudi.microservice.acl.service.account.TokenExpiredException;
import org.rudi.microservice.acl.service.config.UserAccountDeletionConfiguration;
import org.rudi.microservice.acl.service.helper.EmailHelper;
import org.rudi.microservice.acl.service.helper.PasswordHelper;
import org.rudi.microservice.acl.service.mapper.accountregistration.AccountRegistrationMapper;
import org.rudi.microservice.acl.service.mapper.user.UserMapper;
import org.rudi.microservice.acl.service.password.AbstractPasswordException;
import org.rudi.microservice.acl.service.user.UserService;
import org.rudi.microservice.acl.storage.dao.accountregistration.AccountRegistrationDao;
import org.rudi.microservice.acl.storage.dao.accountupdate.ResetPasswordRequestDao;
import org.rudi.microservice.acl.storage.dao.role.RoleCustomDao;
import org.rudi.microservice.acl.storage.dao.user.UserCustomDao;
import org.rudi.microservice.acl.storage.dao.user.UserDao;
import org.rudi.microservice.acl.storage.entity.accountregistration.AccountRegistrationEntity;
import org.rudi.microservice.acl.storage.entity.accountupdate.ResetPasswordRequestEntity;
import org.rudi.microservice.acl.storage.entity.role.RoleEntity;
import org.rudi.microservice.acl.storage.entity.user.UserEntity;
import org.rudi.microservice.projekt.core.bean.ProjektTaskSearchCriteria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

	private static final int HEURE_EN_SECONDE = 60 * 60;
	/**
	 * Validity duration for an account registration
	 */
	@Value("${rudi.acl.accountregistration.validity:90}")
	private int accountRegistrationValidity;

	private static final String ANONYMIZED_LAST_NAME = "Utilisateur Inconnu";

	@Value("${application.role.user.code:USER}")
	private String userRoleCode;

	private final UserDao userDao;

	private final UserCustomDao userCustomDao;

	private final AccountRegistrationDao accountRegistrationDao;

	private final AccountRegistrationMapper accountRegistrationMapper;

	private final UserMapper userMapper;

	private final RoleCustomDao roleCustomDao;

	private final UserService userService;

	private final EmailHelper emailHelper;

	private final PasswordHelper passwordHelper;

	private final ResetPasswordRequestDao resetPasswordRequestDao;

	private final UpdatePasswordTokenHelper updatePasswordTokenHelper;

	private final UserAccountDeletionConfiguration accountDeletionConfiguration;

	private final ProjektHelper projektHelper;

	private final OrganizationHelper organizationHelper;

	private final SelfdataHelper selfdataHelper;


	@Override
	public void checkAccountCreation(Account account)
			throws AbstractAccountRegistrationException, AbstractPasswordException {

		// Check des champs obligatoires
		boolean missingField = false;
		String missingFieldName = null;
		if (StringUtils.isEmpty(account.getLogin())) {
			missingField = true;
			missingFieldName = "login";
		} else if (StringUtils.isEmpty(account.getPassword())) {
			missingField = true;
			missingFieldName = "mot de passe";
		}

		// Si champ manquant : erreur
		if (missingField) {
			throw new MissingFieldException(missingFieldName);
		}

		// Contrôle de la longueur du champ mot de passe
		passwordHelper.checkPassword(account.getPassword());

		// Contrôle du login au format mail
		if (!EmailValidator.getInstance().isValid(account.getLogin())) {
			throw new LoginNotMailException(account.getLogin());
		}

		// On cherche maintenant dans la BDD si le login existe déjà
		UserSearchCriteria criteria = new UserSearchCriteria();
		criteria.setLogin(account.getLogin());
		User duplicateUser = userService.getUserByLogin(account.getLogin(), false);
		if (duplicateUser != null) {
			throw new LoginAlreadyExistsException(account.getLogin());
		}

		AccountRegistrationEntity duplicatedAccountRegistration = accountRegistrationDao
				.findByLogin(account.getLogin());
		if (duplicatedAccountRegistration != null) {
			// Si la demande a déjà été faite précédemment, on la supprime pour éviter les doublons en base
			accountRegistrationDao.delete(duplicatedAccountRegistration);
		}
	}

	@Transactional // (readOnly = false)
	@Override
	public void registerAccount(Account account)
			throws AbstractAccountRegistrationException, AbstractPasswordException {
		checkAccountCreation(account);
		// On crée l'entité accountRegistration
		AccountRegistrationEntity entity = accountRegistrationMapper.dtoToEntity(account);
		entity.setCreationDate(LocalDateTime.now());
		entity.setUuid(UUID.randomUUID());
		// On génère un token
		entity.setToken(UUID.randomUUID().toString());

		// On sauvegarde l'entité
		accountRegistrationDao.save(entity);
		// On envoie le courriel
		try {
			emailHelper.sendAccountRegistration(entity, Locale.FRENCH);
		} catch (Exception e) {
			throw new SendEmailRegistrationException();
		}

	}

	@Transactional // (readOnly = false)
	@Override
	public User validateAccount(String token) throws AbstractAccountRegistrationException {
		// On récupère l'account registration
		AccountRegistrationEntity accountRegistration = accountRegistrationDao.findByToken(token);

		// s'il existe pas => 400
		if (accountRegistration == null) {
			throw new InvalidAccountTokenException();
		}
		// s'il existe mais est périmé => 400
		LocalDateTime reference = LocalDateTime.now().minus(Duration.ofMinutes(accountRegistrationValidity));
		if (accountRegistration.getCreationDate().isBefore(reference)) {
			throw new InvalidAccountTokenException();
		}

		// si ok => création du user correspondant
		UserEntity user = createAccountUtilisateur(accountRegistration);
		userDao.save(user);

		try {
			emailHelper.sendAccountCreationConfirmation(user, Locale.FRENCH);
		} catch (Exception e) {
			log.error("L'envoi du mail d'activation du compte a échoué", e);
		}

		accountRegistrationDao.delete(accountRegistration);

		return userMapper.entityToDto(user);
	}

	@Override
	public Account getAccountByToken(String token) {
		// On récupère l'account registration
		AccountRegistrationEntity accountRegistration = accountRegistrationDao.findByToken(token);
		return accountRegistrationMapper.entityToDto(accountRegistration);
	}

	@Override
	public boolean isAccountCreatedNotValidated(String login) {
		// On récupère l'account registration dans la table si ça existe ben le compte est créé
		// mais n'est pas validé, si on trouve rien c'est que le compte n'est même pas créé
		return accountRegistrationDao.findByLogin(login) != null;
	}

	@Transactional // (readOnly = false)
	@Override
	public void cleanExpiredAccounts() {
		// date du jour - accountRegistrationValidity
		LocalDateTime reference = LocalDateTime.now().minus(Duration.ofMinutes(accountRegistrationValidity));
		// recherche des toutes les accontRegistation plus ancienne que cette date
		List<AccountRegistrationEntity> accountRegistrations = accountRegistrationDao
				.findByCreationDateBefore(reference);
		// purge
		if (CollectionUtils.isNotEmpty(accountRegistrations)) {
			for (AccountRegistrationEntity accountRegistration : accountRegistrations) {
				accountRegistrationDao.delete(accountRegistration);
			}
		}
	}

	@Override
	@Transactional // (readOnly = false)
	public void checkPasswordChangeToken(UUID token)
			throws AppServiceNotFoundException, TokenExpiredException, MissingParameterException {
		updatePasswordTokenHelper.checkTokenValidity(token);
	}

	@Override
	@Transactional // (readOnly = false)
	public void validatePasswordChange(PasswordChange passwordChange) throws AppServiceException {

		final var token = passwordChange.getToken();
		updatePasswordTokenHelper.validateToken(token, updatePasswordEntity -> {

			final var clearPassword = passwordChange.getPassword();
			passwordHelper.checkPassword(clearPassword);

			final var user = findRequiredUserByUuid(updatePasswordEntity.getUserUuid());
			user.setPassword(passwordHelper.encodePassword(clearPassword));

			emailHelper.sendPasswordChangeConfirmation(user, Locale.FRENCH);

		});

	}

	@Nonnull
	private UserEntity findRequiredUserByUuid(@Nonnull UUID userUuid) throws AppServiceNotFoundException {
		final var user = userDao.findByUuid(userUuid);
		if (user == null) {
			throw new AppServiceNotFoundException(UserEntity.class, userUuid);
		}
		return user;
	}

	private UserEntity createAccountUtilisateur(AccountRegistrationEntity account) {

		// Attribution du rôle : utilisateur à l'User
		// Appeler le role DAO pour recuperer l'entité correspondante
		RoleSearchCriteria criteria = new RoleSearchCriteria();
		criteria.setCode(userRoleCode);
		List<RoleEntity> roles = roleCustomDao.searchRoles(criteria);

		// Attribution des autres propriétés
		UserEntity userToCreate = new UserEntity();
		// Uuid à setter
		userToCreate.setUuid(account.getUuid());
		userToCreate.setFirstname(account.getFirstname());
		userToCreate.setLastname(account.getLastname());
		userToCreate.setLogin(account.getLogin());
		if (StringUtils.isNotEmpty(account.getPassword())) {
			userToCreate.setPassword(passwordHelper.encodePassword(account.getPassword()));
		}
		userToCreate.setType(UserType.PERSON);
		userToCreate.setRoles(new HashSet<>());
		userToCreate.getRoles().addAll(roles);
		userToCreate.setHasSubscribeToNotifications(account.hasSubscribeToNotifications());
		return userToCreate;
	}

	@Transactional
	@Override
	public void requestPasswordChange(String email) {
		User currentUser = emailHelper.lookupUserByEmail(email);
		if (currentUser == null) {
			return;
		}
		ResetPasswordRequestEntity passwordEntity = new ResetPasswordRequestEntity();
		passwordEntity.setUuid(UUID.randomUUID());
		passwordEntity.setUserUuid(currentUser.getUuid());
		passwordEntity.setToken(UUID.randomUUID());
		passwordEntity.setCreationDate(LocalDateTime.now());
		// Sauvegarde de la request d'update
		resetPasswordRequestDao.save(passwordEntity);
		// Send token email
		emailHelper.sendTokenToChangePassword(passwordEntity, email, Locale.FRENCH);
	}

	/**
	 * Supprime tous les tokens périmés
	 */
	@Transactional
	public void deleteAllExpiredToken() {
		resetPasswordRequestDao.findAll().forEach(element -> {
			if ((LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
					- element.getCreationDate().toEpochSecond(ZoneOffset.UTC)) / HEURE_EN_SECONDE >= 1) {
				resetPasswordRequestDao.delete(element);
			}
		});
	}

	/**
	 * Envoyer un avertissement aux utilisateurs concernant la suppression imminente de leur compte
	 * en raison d'inactivité
	 */
	@Transactional
	@Override
	public void sendAccountDeletionWarnings() {
		log.info("Sending account deletion warnings...");

		// Calculer la date seuil : maintenant - (inactivityMonthsBeforeDeletion - deletionWarningMonthsBefore) mois
		final int warningThresholdMonths = accountDeletionConfiguration.getInactivityMonthsBeforeDeletion()
				- accountDeletionConfiguration.getDeletionWarningMonthsBefore();
		final LocalDateTime thresholdDate = LocalDate.now().atStartOfDay().minusMonths(warningThresholdMonths);

		List<UserEntity> inactiveUsers = searchInactivePersonUsers(thresholdDate, List.of(AccountState.ACTIVE));

		if (CollectionUtils.isEmpty(inactiveUsers)) {
			log.info("No inactive users found for deletion warning");
			return;
		}

		// Vérifier si le user a des tâches en cours.
		for (UserEntity user : inactiveUsers) {
			emailHelper.sendAccountDeletionWarning(user, Locale.FRENCH, hasPendingTasks(user), warningThresholdMonths);
			user.setAccountState(AccountState.INACTIVE);
			userDao.save(user);
		}


		log.info("Found {} inactive users to warn", inactiveUsers.size());
	}

	private List<UUID> collectUserRoleUuids() {
		// Récupérer le rôle USER pour son UUID
		RoleSearchCriteria roleSearchCriteria = new RoleSearchCriteria();
		roleSearchCriteria.setCode(userRoleCode);
		List<RoleEntity> userRoles = roleCustomDao.searchRoles(roleSearchCriteria);

		if (CollectionUtils.isEmpty(userRoles)) {
			log.warn("{} role not found in database", userRoleCode);
		}

		// Extraire l'UUID du rôle USER
		return userRoles.stream()
				.map(RoleEntity::getUuid)
				.toList();
	}

	private List<UserEntity> searchInactivePersonUsers(LocalDateTime thresholdDate, List<AccountState> accountStates) {
		List<UUID> userRoleUuids = collectUserRoleUuids();
		if(CollectionUtils.isEmpty(userRoleUuids)) {
			log.warn("No user role UUIDs found, cannot search for inactive users");
			return List.of();
		}

		// Créer le critère de recherche pour les utilisateurs inactifs
		UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
		userSearchCriteria.setLastConnexionBefore(thresholdDate);
		userSearchCriteria.setType(
				org.rudi.microservice.acl.core.bean.UserType.PERSON);
		userSearchCriteria.setRoleUuids(userRoleUuids);
		userSearchCriteria.setAccountStates(accountStates);

		// Rechercher les utilisateurs correspondant aux critères
		return userCustomDao.searchUsers(userSearchCriteria, Pageable.unpaged())
				.getContent();
	}

	private boolean hasPendingTasks(UserEntity user) {
		ProjektTaskSearchCriteria projektTaskSearchCriteria = new ProjektTaskSearchCriteria();
		projektTaskSearchCriteria.setUserLogin(user.getLogin());
		projektTaskSearchCriteria.setAsAdmin(true);

		StruktureTaskSearchCriteria struktureTaskSearchCriteria = new StruktureTaskSearchCriteria();
		struktureTaskSearchCriteria.setUserLogin(user.getLogin());
		struktureTaskSearchCriteria.setAsAdmin(true);

		SelfdataTaskSearchCriteria selfdataTaskSearchCriteria = new SelfdataTaskSearchCriteria();
		selfdataTaskSearchCriteria.setUserLogin(user.getLogin());
		selfdataTaskSearchCriteria.setAsAdmin(true);

		return !projektHelper.searchTasks(projektTaskSearchCriteria).isEmpty()
				|| !organizationHelper.searchTasks(struktureTaskSearchCriteria).isEmpty()
				|| !selfdataHelper.searchTasks(selfdataTaskSearchCriteria).isEmpty();
	}

	@Transactional
	@Override
	public void deleteInactiveUsersAccount() {
		// Calculer la date seuil : maintenant - inactivityMonthsBeforeDeletion mois
		final LocalDateTime thresholdDate = LocalDate.now().atStartOfDay()
				.minusMonths(accountDeletionConfiguration.getInactivityMonthsBeforeDeletion());

		List<UserEntity> inactiveUsers = searchInactivePersonUsers(thresholdDate, List.of(AccountState.INACTIVE));

		for (UserEntity user : inactiveUsers) {
			String loginAvantAnonymisation = user.getLogin();
			UUID userUuid = user.getUuid();
			boolean hasActiveElements = hasPendingTasks(user);

			anonymizeUser(user);
			notifyMicroservicesOfUserDeactivation(loginAvantAnonymisation, userUuid);

			emailHelper.sendAccountDeletedToUser(loginAvantAnonymisation, Locale.FRENCH);
			if (hasActiveElements) {
				emailHelper.sendAccountDeletedToAnimators(emailHelper.lookupModeratorEmails(), userUuid, Locale.FRENCH);
			}
		}
	}

	private void notifyMicroservicesOfUserDeactivation(String login, UUID userUuid) {

		try {
			organizationHelper.notifyUserDeactivation(login, userUuid);
		} catch (Exception e) {
			log.error("Failed to notify organization service about user deactivation for user: {}", login, e);
		}

		try {
			projektHelper.notifyUserDeactivation(login, userUuid);
		} catch (Exception e) {
			log.error("Failed to notify projekt service about user deactivation for user: {}", login, e);
		}

		try {
			selfdataHelper.notifyUserDeactivation(login, userUuid);
		} catch (Exception e) {
			log.error("Failed to notify selfdata service about user deactivation for user: {}", login, e);
		}
	}

	public void anonymizeUser(UserEntity userEntity) {
		userEntity.setLastname(ANONYMIZED_LAST_NAME);
		userEntity.setFirstname(StringUtils.EMPTY);
		userEntity.setLogin(userEntity.getUuid().toString());
		userEntity.setPassword(StringUtils.EMPTY);
		userEntity.setAccountState(AccountState.DELETED);
		userEntity.getAddresses().clear();
		userDao.save(userEntity);
	}
}
