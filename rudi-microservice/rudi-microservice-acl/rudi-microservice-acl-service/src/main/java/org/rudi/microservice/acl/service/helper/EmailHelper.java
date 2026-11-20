/**
 * 
 */
package org.rudi.microservice.acl.service.helper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.common.core.DocumentContent;
import org.rudi.common.core.security.RoleCodes;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.email.exception.EMailException;
import org.rudi.facet.email.model.EMailDescription;
import org.rudi.facet.generator.exception.GenerationException;
import org.rudi.facet.generator.exception.GenerationModelNotFoundException;
import org.rudi.facet.generator.text.TemplateGenerator;
import org.rudi.microservice.acl.core.bean.RoleSearchCriteria;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.core.bean.UserSearchCriteria;
import org.rudi.microservice.acl.service.mapper.user.UserMapper;
import org.rudi.microservice.acl.storage.dao.role.RoleCustomDao;
import org.rudi.microservice.acl.storage.dao.user.UserCustomDao;
import org.rudi.microservice.acl.storage.dao.user.UserDao;
import org.rudi.microservice.acl.storage.entity.accountregistration.AccountRegistrationEntity;
import org.rudi.microservice.acl.storage.entity.accountupdate.ResetPasswordRequestEntity;
import org.rudi.microservice.acl.storage.entity.address.AbstractAddressEntity;
import org.rudi.microservice.acl.storage.entity.address.AddressType;
import org.rudi.microservice.acl.storage.entity.address.EmailAddressEntity;
import org.rudi.microservice.acl.storage.entity.role.RoleEntity;
import org.rudi.microservice.acl.storage.entity.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fni18300
 *
 */
@Component
@Slf4j
public class EmailHelper {

	public static final String NO_EMAIL_ADDRESS_FOR = "No email address for {}";
	private static final String EMAIL_PATTERN = "^(.+)@(\\S+)$";

	@Value("${email.accountcreation.activation.subject}")
	private String accountCreationActivationSubject;

	@Value("${email.accountcreation.activation.body}")
	private String accountCreationActivationBody;

	@Value("${email.accountcreation.registration.subject}")
	private String accountCreationRegistrationSubject;

	@Value("${email.accountcreation.registration.body}")
	private String accountCreationRegistrationBody;

	@Value("${email.urlServer}")
	private String urlServer;

	@Value("${email.projectName}")
	private String projectName;

	@Value("${email.teamName}")
	private String teamName;

	@Value("${email.urlContact}")
	private String urlContact;

	@Value("${email.accountvalidation.path:/login/accountValidation}")
	private String accountValidationPath;

	@Autowired
	private TemplateGenerator templateGenerator;

	@Autowired
	private EMailService eMailService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private UserCustomDao userCustomDao;

	@Autowired
	private RoleCustomDao roleCustomDao;

	@Autowired
	private UserMapper userMapper;

	@Value("${email.resetpassword.subject}")
	private String changePasswordSubject;

	@Value("${email.resetpassword.body}")
	private String changePasswordBody;

	@Value("${email.resetPasswordPath:/login/reset-password}")
	private String resetPasswordPath;

	@Value("${email.confirmationresetpassword.subject}")
	private String confirmationChangePasswordSubject;

	@Value("${email.confirmationresetpassword.body}")
	private String confirmationChangePasswordBody;

	@Value("${email.accountdeletionwarning.subject}")
	private String accountDeletionWarningSubject;

	@Value("${email.accountdeletionwarning.body}")
	private String accountDeletionWarningBody;

	@Value("${email.accountdeleted.subject}")
	private String accountDeletedSubject;

	@Value("${email.accountdeleted.body}")
	private String accountDeletedBody;

	@Value("${email.accountdeletedanimator.subject}")
	private String accountDeletedAnimatorSubject;

	@Value("${email.accountdeletedanimator.body}")
	private String accountDeletedAnimatorBody;

	/**
	 * Envoie un courriel de confirmation de création de compte
	 *
	 * @param user   l'utilisateur destinataire
	 * @param locale la langue
	 */
	public void sendAccountCreationConfirmation(UserEntity user, Locale locale) {
		try {

			// recherche de l'email de destination
			String to = lookupEmail(user);
			if (to == null) {
				log.info(NO_EMAIL_ADDRESS_FOR, user.getLogin());
				return;
			}

			Map<String, Object> additionalProperties = computeAdditionnalProperties(null);

			// génération du sujet
			AccountCreationConfirmationDataModel dataModelSubject = new AccountCreationConfirmationDataModel(user,
					additionalProperties, locale, accountCreationActivationSubject);
			DocumentContent subject = templateGenerator.generateDocument(dataModelSubject);

			// génération du corps
			AccountCreationConfirmationDataModel dataModelBody = new AccountCreationConfirmationDataModel(user,
					additionalProperties, locale, accountCreationActivationBody);
			DocumentContent body = templateGenerator.generateDocument(dataModelBody);

			// Création du modèle de courrier
			EMailDescription eMailDescription = new EMailDescription(to,
					FileUtils.readFileToString(subject.getFile(), StandardCharsets.UTF_8), body);
			// envoie du courriel
			eMailService.sendMailAndCatchException(eMailDescription);

		} catch (GenerationModelNotFoundException | GenerationException | IOException e) {
			// On ne renvoie pas d'exception car on ne veut pas bloquer la création de compte (mail informatif seulement)
			log.error("Cannot send account creation confirmation mail", e);
		}
	}

	/**
	 * Méthode d'envoie du courriel de création de compte avec demande de confirmation d'adresse email
	 *
	 * @param account le compte
	 * @param locale
	 * @throws GenerationModelNotFoundException
	 * @throws GenerationException
	 * @throws IOException
	 * @throws EMailException
	 */
	public void sendAccountRegistration(AccountRegistrationEntity account, Locale locale)
			throws GenerationModelNotFoundException, GenerationException, IOException, EMailException {
		// recherche de l'email de destination
		String to = lookupEmail(account);
		if (to == null) {
			log.info(NO_EMAIL_ADDRESS_FOR, account.getLogin());
			return;
		}

		Map<String, Object> additionalProperties = computeAdditionnalProperties(accountValidationPath);

		// génération du sujet
		AccountRegistrationDataModel dataModelSubject = new AccountRegistrationDataModel(account, additionalProperties,
				locale, accountCreationRegistrationSubject);
		DocumentContent subject = templateGenerator.generateDocument(dataModelSubject);

		// génération du corps
		AccountRegistrationDataModel dataModelBody = new AccountRegistrationDataModel(account, additionalProperties,
				locale, accountCreationRegistrationBody);
		DocumentContent body = templateGenerator.generateDocument(dataModelBody);

		// Création du modèle de courrier
		EMailDescription eMailDescription = new EMailDescription(to,
				FileUtils.readFileToString(subject.getFile(), StandardCharsets.UTF_8), body);
		// envoie du courriel
		eMailService.sendMailAndCatchException(eMailDescription);
	}

	private String lookupEmail(AccountRegistrationEntity account) {
		return account.getLogin();
	}

	/**
	 * Extraction de l'adresse courriel cible
	 *
	 * @param user
	 * @return
	 */
	public String lookupEmail(UserEntity user) {
		if (user.getLogin().matches(EMAIL_PATTERN)) {
			return user.getLogin();
		} else {
			Optional<AbstractAddressEntity> emailAddressEntity = user.getAddresses().stream()
					.filter(a -> a.getType() == AddressType.EMAIL).findFirst();
			if (emailAddressEntity.isPresent()) {
				return ((EmailAddressEntity) emailAddressEntity.get()).getEmail();
			} else {
				return null;
			}
		}
	}

	public User lookupUserByEmail(String email) {
		UserEntity currentUser = userDao.findByLogin(email);
		// On part du principe que l'email est le login
		if (currentUser == null) {
			UserSearchCriteria criteria = new UserSearchCriteria();
			criteria.setUserEmail(email);
			Pageable pageable = PageRequest.of(0, 1);
			Page<UserEntity> userPage = userCustomDao.searchUsers(criteria, pageable);
			if (userPage != null) {
				currentUser = userPage.stream().findFirst().orElse(null);
			}
		}
		if (currentUser != null) {
			return userMapper.entityToDto(currentUser);
		}
		return null;
	}

	public void sendTokenToChangePassword(ResetPasswordRequestEntity passwordObject, String email, Locale locale) {
		try {
			Map<String, Object> additionalProperties = computeAdditionnalProperties(resetPasswordPath);

			// Géneration du sujet
			ResetPasswordRequestDataModel dataModelSubject = new ResetPasswordRequestDataModel(passwordObject,
					additionalProperties, locale, changePasswordSubject);
			DocumentContent subject = templateGenerator.generateDocument(dataModelSubject);
			log.info(NO_EMAIL_ADDRESS_FOR, email);
			// génération du corps
			ResetPasswordRequestDataModel dataModelBody = new ResetPasswordRequestDataModel(passwordObject,
					additionalProperties, locale, changePasswordBody);
			DocumentContent body = templateGenerator.generateDocument(dataModelBody);

			// Création du modèle de courrier
			EMailDescription eMailDescription = new EMailDescription(email,
					FileUtils.readFileToString(subject.getFile(), StandardCharsets.UTF_8), body);
			// envoie du courriel
			eMailService.sendMailAndCatchException(eMailDescription);

		} catch (GenerationModelNotFoundException | GenerationException | IOException e) {
			log.error("Cannot send generate token mail", e);
		}
	}

	public void sendPasswordChangeConfirmation(UserEntity user, Locale locale) {
		try {

			// recherche de l'email de destination
			String to = lookupEmail(user);
			if (to == null) {
				log.error(NO_EMAIL_ADDRESS_FOR, user.getLogin());
				return;
			}

			Map<String, Object> additionalProperties = computeAdditionnalProperties(null);

			// génération du sujet
			final var dataModelSubject = new AccountCreationConfirmationDataModel(user, additionalProperties, locale,
					confirmationChangePasswordSubject);
			DocumentContent subject = templateGenerator.generateDocument(dataModelSubject);

			// génération du corps
			final var dataModelBody = new AccountCreationConfirmationDataModel(user, additionalProperties, locale,
					confirmationChangePasswordBody);
			DocumentContent body = templateGenerator.generateDocument(dataModelBody);

			// Création du modèle de courrier
			final var eMailDescription = new EMailDescription(to,
					FileUtils.readFileToString(subject.getFile(), StandardCharsets.UTF_8), body);
			// envoie du courriel
			eMailService.sendMailAndCatchException(eMailDescription);

		} catch (Exception e) {
			// On ne renvoie pas d'exception car on ne veut pas bloquer la création de compte (mail informatif seulement)
			log.error("Cannot send password change confirmation mail", e);
		}
	}

	protected Map<String, Object> computeAdditionnalProperties(String pathServer) {
		Map<String, Object> additionalProperties = new HashMap<>();
		additionalProperties.put("projectName", projectName);
		additionalProperties.put("teamName", teamName);
		additionalProperties.put("urlServer", urlServer);
		additionalProperties.put("urlContact", urlContact);
		if (pathServer != null) {
			additionalProperties.put("pathServer", pathServer);
		}
		return additionalProperties;
	}

	/**
	 * Envoie un courriel d'avertissement de suppression imminente de compte
	 *
	 * @param user                     l'utilisateur
	 * @param locale                   la langue
	 * @param hasPendingTasks          indique si l'utilisateur a des tâches en attente
	 * @param warningThresholdMonths le nombre de mois depuis la dernière connexion
	 */
	public void sendAccountDeletionWarning(UserEntity user, Locale locale, boolean hasPendingTasks, int warningThresholdMonths) {
		try {
			// Recherche de l'email de destination
			String to = lookupEmail(user);
			if (to == null) {
				log.warn(NO_EMAIL_ADDRESS_FOR, user.getLogin());
				return;
			}

			Map<String, Object> additionalProperties = computeAdditionnalProperties(null);
			String userDenomination = getUserDenomination(user);
			// Génération du sujet
			AccountDeletionWarningDataModel dataModelSubject = new AccountDeletionWarningDataModel(hasPendingTasks, userDenomination,
					warningThresholdMonths, additionalProperties, locale, accountDeletionWarningSubject);
			DocumentContent subject = templateGenerator.generateDocument(dataModelSubject);

			// Génération du corps
			AccountDeletionWarningDataModel dataModelBody = new AccountDeletionWarningDataModel(hasPendingTasks, userDenomination,
					warningThresholdMonths, additionalProperties, locale, accountDeletionWarningBody);
			DocumentContent body = templateGenerator.generateDocument(dataModelBody);

			// Création du modèle de courrier
			EMailDescription eMailDescription = new EMailDescription(to,
					FileUtils.readFileToString(subject.getFile(), StandardCharsets.UTF_8), body);
			// Envoi du courriel
			eMailService.sendMailAndCatchException(eMailDescription);

		} catch (GenerationModelNotFoundException | GenerationException | IOException e) {
			// On ne renvoie pas d'exception car on ne veut pas bloquer le scheduler (mail informatif seulement)
			log.error("Cannot send account deletion warning mail", e);
		}
	}

	/**
	 * Envoie un courriel à l'utilisateur dont le compte vient d'être supprimé pour inactivité
	 *
	 * @param to     l'adresse courriel de l'utilisateur (à récupérer avant anonymisation)
	 * @param locale la langue
	 */
	public void sendAccountDeletedToUser(String to, Locale locale) {
		if (StringUtils.isBlank(to)) {
			return;
		}
		sendAccountDeletionMail(to, null, locale, accountDeletedSubject, accountDeletedBody);
	}

	/**
	 * Envoie un courriel aux animateurs pour les informer de la suppression d'un compte ayant des éléments actifs
	 *
	 * @param recipients les adresses courriel des animateurs
	 * @param userUuid   l'identifiant de l'utilisateur supprimé
	 * @param locale     la langue
	 */
	public List<String> lookupModeratorEmails() {
		RoleSearchCriteria roleSearchCriteria = new RoleSearchCriteria();
		roleSearchCriteria.setCode(RoleCodes.MODERATOR);
		List<UUID> moderatorRoleUuids = roleCustomDao.searchRoles(roleSearchCriteria).stream()
				.map(RoleEntity::getUuid).toList();
		if (moderatorRoleUuids.isEmpty()) {
			log.warn("{} role not found in database, no moderator will be notified", RoleCodes.MODERATOR);
			return List.of();
		}

		UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
		userSearchCriteria.setRoleUuids(moderatorRoleUuids);
		return userCustomDao.searchUsers(userSearchCriteria, Pageable.unpaged()).getContent().stream()
				.map(this::lookupEmail).filter(StringUtils::isNotBlank).distinct().toList();
	}

	public void sendAccountDeletedToAnimators(Collection<String> recipients, UUID userUuid, Locale locale) {
		if (recipients == null) {
			return;
		}
		for (String to : recipients) {
			if (StringUtils.isNotBlank(to)) {
				sendAccountDeletionMail(to, userUuid, locale, accountDeletedAnimatorSubject, accountDeletedAnimatorBody);
			}
		}
	}

	private void sendAccountDeletionMail(String to, UUID userUuid, Locale locale, String subjectTemplate,
			String bodyTemplate) {
		try {
			Map<String, Object> additionalProperties = computeAdditionnalProperties(null);

			DocumentContent subject = templateGenerator
					.generateDocument(new AccountDeletedDataModel(userUuid, additionalProperties, locale, subjectTemplate));
			DocumentContent body = templateGenerator
					.generateDocument(new AccountDeletedDataModel(userUuid, additionalProperties, locale, bodyTemplate));

			EMailDescription eMailDescription = new EMailDescription(to,
					FileUtils.readFileToString(subject.getFile(), StandardCharsets.UTF_8), body);
			eMailService.sendMailAndCatchException(eMailDescription);
		} catch (GenerationModelNotFoundException | GenerationException | IOException e) {
			log.error("Cannot send account deleted mail", e);
		}
	}

	private String getUserDenomination(UserEntity user) {
		if(!StringUtils.isEmpty(user.getFirstname()) && !StringUtils.isEmpty(user.getLastname())) {
			return String.format("%s %s", user.getFirstname(), user.getLastname()).trim();
		}
		else if(!StringUtils.isEmpty(user.getFirstname())) {
			return user.getFirstname().trim();
		}
		else if(!StringUtils.isEmpty(user.getLastname())) {
			return user.getLastname().trim();
		}
		return null;
	}
}
