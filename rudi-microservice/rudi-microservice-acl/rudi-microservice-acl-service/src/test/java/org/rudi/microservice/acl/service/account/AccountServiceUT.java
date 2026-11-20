package org.rudi.microservice.acl.service.account;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.common.core.security.AccountState;
import org.rudi.common.core.security.UserType;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.projekt.helper.ProjektHelper;
import org.rudi.facet.selfdata.helper.SelfdataHelper;
import org.rudi.microservice.acl.core.bean.Account;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.service.AclSpringBootTest;
import org.rudi.microservice.acl.service.datafactory.UserDataFactory;
import org.rudi.microservice.acl.service.helper.EmailHelper;
import org.rudi.microservice.acl.service.password.PasswordLengthException;
import org.rudi.microservice.acl.service.password.PasswordNotMatchingRegexException;
import org.rudi.microservice.acl.storage.dao.accountregistration.AccountRegistrationDao;
import org.rudi.microservice.acl.storage.dao.user.UserDao;
import org.rudi.microservice.acl.storage.entity.accountregistration.AccountRegistrationEntity;
import org.rudi.microservice.acl.storage.entity.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@AclSpringBootTest
class AccountServiceUT {

	@Autowired
	private AccountService accountService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private AccountRegistrationDao accountRegistrationDao;

	@Autowired
	private UserDataFactory userDataFactory;

	@MockitoBean
	private JavaMailSenderImpl javaMailSender;

	@MockitoBean
	private EmailHelper emailHelper;

	// ===== Mocks pour les facets externes (appels inter-microservices) =====
	@MockitoBean
	private ProjektHelper projektHelper;

	@MockitoBean
	private OrganizationHelper organizationHelper;

	@MockitoBean
	private SelfdataHelper selfdataHelper;


	@Test
	void test_checkAccount_KO_when_missingField() {

		// Aucune information de compte remplie
		Account account = new Account();

		// Les infos sont KO : champ manquant
		assertThrows(MissingFieldException.class, () -> accountService.checkAccountCreation(account));

		// On met un login
		account.setLogin("stuff");

		// Les infos sont toujours KO : champ manquant pour mot de passe
		assertThrows(MissingFieldException.class, () -> accountService.checkAccountCreation(account));

		// On met un mot de passe mais pas de login
		account.setLogin(null);
		account.setPassword("stuff2");

		// Les infos sont toujours KO : champ manquant pour login
		assertThrows(MissingFieldException.class, () -> accountService.checkAccountCreation(account));
	}

	@Test
	void test_checkAccount_KO_when_loginNotMail() {

		// Création info de compte
		Account account = new Account();
		account.setPassword("Pas$word123456789456123");

		// On met un login qui n'est pas un mail
		account.setLogin("stuff");

		// Les infos sont KO : on attend un mail
		assertThrows(LoginNotMailException.class, () -> accountService.checkAccountCreation(account));
	}

	@Test
	void test_checkAccount_KO_when_passwordLengthIncorrect() {

		// Création info de compte
		Account account = new Account();
		account.setLogin("someone@gmail.com");

		// mot de passe trop court
		account.setPassword("p");

		// Les infos sont KO : mot de passe trop court
		assertThrows(PasswordLengthException.class, () -> accountService.checkAccountCreation(account));

		// mot de passe trop long
		account.setPassword("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
				+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
				+ "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");

		// Les infos sont KO : mot de passe trop long
		assertThrows(PasswordLengthException.class, () -> accountService.checkAccountCreation(account));
	}

	@Test
	void test_checkAccount_KO_when_loginAlreadyExists() {

		// Création d'un user dans la BDD
		UserEntity user = new UserEntity();
		user.setUuid(UUID.randomUUID());
		user.setLogin("alreadyhere@gmail.com");
		user.setPassword("1Passwordd@mn");
		user.setType(UserType.PERSON);
		userDao.save(user);

		// Création info de compte avec le même login
		Account account = new Account();
		account.setLogin("alreadyhere@gmail.com");
		account.setPassword("soMeth1ngudmngayeahw@za");

		// Les infos sont KO : le login existe déjà
		assertThrows(LoginAlreadyExistsException.class, () -> accountService.checkAccountCreation(account));
	}

	@Test
	void test_registerAccount() throws AppServiceException {
		// Login de l'user de test créé
		String login = "someone@gmail.com";

		// Informations pour la création de compte
		Account account = new Account();
		account.setLogin(login);
		account.setPassword("soMeth1ngudmngayeahw@za");
		account.setFirstname("prénom");
		account.setLastname("nomdefamille");

		doCallRealMethod().when(javaMailSender).createMimeMessage();
		doNothing().when(javaMailSender).send((MimeMessage) any());

		accountService.registerAccount(account);
		assertNotNull(accountRegistrationDao.findByLogin(login));
	}

	@Test
	void test_registerAccountKOminLength() throws AppServiceException {
		// Login de l'user de test créé
		String login = "someone@gmail.com";

		// Informations pour la création de compte
		Account account = new Account();
		account.setLogin(login);
		account.setPassword("soMeth1ng@");
		account.setFirstname("prénom");
		account.setLastname("nomdefamille");

		doCallRealMethod().when(javaMailSender).createMimeMessage();
		doNothing().when(javaMailSender).send((MimeMessage) any());

		assertThrows(PasswordLengthException.class, () -> accountService.registerAccount(account));
	}

	@Test
	void test_registerAccountKOPattern() throws AppServiceException {
		// Login de l'user de test créé
		String login = "someone@gmail.com";

		// Informations pour la création de compte
		Account account = new Account();
		account.setLogin(login);
		account.setPassword("somethingudmngayeahwaza");
		account.setFirstname("prénom");
		account.setLastname("nomdefamille");

		doCallRealMethod().when(javaMailSender).createMimeMessage();
		doNothing().when(javaMailSender).send((MimeMessage) any());

		assertThrows(PasswordNotMatchingRegexException.class, () -> accountService.registerAccount(account));

		account.setPassword("someth1ngudmngayeahwaza");
		assertThrows(PasswordNotMatchingRegexException.class, () -> accountService.registerAccount(account));

		account.setPassword("soMeth1ngudmngayeahwaza");
		assertThrows(PasswordNotMatchingRegexException.class, () -> accountService.registerAccount(account));

		account.setPassword("soMeth1ngudmngaye@hwaza");
		accountService.registerAccount(account);
		assertNotNull(accountRegistrationDao.findByLogin(login));
	}

	@Test
	void test_validateAccount() throws AppServiceException {

		User user = null;
		String login = "someone@gmail.com";

		Account account = new Account();
		account.setLogin(login);
		account.setPassword("soMeth1ngudmngayeahw@za");
		account.setFirstname("prénom");
		account.setLastname("nomdefamille");

		doCallRealMethod().when(javaMailSender).createMimeMessage();
		doNothing().when(javaMailSender).send((MimeMessage) any());

		accountService.registerAccount(account);
		AccountRegistrationEntity accountRegistrationEntity = accountRegistrationDao.findByLogin(account.getLogin());

		user = accountService.validateAccount(accountRegistrationEntity.getToken());

		// s'il existe pas => 400
		assertNotNull(user, "La validation du user a échoué");

		assertNull(accountRegistrationDao.findByLogin(account.getLogin()));

	}

	@Test
	@DisplayName("Nettoyage des comptes d'enregistrement périmés")
	void testCleanUp() {
		AccountRegistrationEntity accountRegistrationEntity = new AccountRegistrationEntity();
		accountRegistrationEntity.setUuid(UUID.randomUUID());
		accountRegistrationEntity.setLogin("test@test.fr");
		accountRegistrationEntity.setPassword("test@test.fr");

		accountRegistrationEntity.setToken(UUID.randomUUID().toString());
		accountRegistrationEntity.setCreationDate(LocalDateTime.now().minusMonths(1));

		accountRegistrationDao.save(accountRegistrationEntity);

		accountService.cleanExpiredAccounts();

		assertThat(accountService.getAccountByToken(accountRegistrationEntity.getToken())).isNull();
	}

	@Test
	@Transactional  // Isoler ce test pour éviter l'interférence avec d'autres tests utilisant les mêmes logins
	@DisplayName("sendAccountDeletionWarnings : cherche les tâches des utilisateurs inactifs")
	void test_sendAccountDeletionWarnings_searchesTasksForInactiveUsers() {
		// GIVEN : utilisateur ACTIF avec dernière connexion très ancienne (23 mois)
		userDataFactory.getOrCreateInactiveUserEntity("inactive@example.com", 23);

		// Mock les helpers pour qu'ils retournent pas de tâches
		when(projektHelper.searchTasks(any())).thenReturn(new ArrayList<>());
		when(organizationHelper.searchTasks(any())).thenReturn(new ArrayList<>());
		when(selfdataHelper.searchTasks(any())).thenReturn(new ArrayList<>());

		// WHEN
		accountService.sendAccountDeletionWarnings();

		// THEN : les 3 helpers doivent être appelés pour chercher les tâches
		verify(projektHelper, times(1)).searchTasks(any());
		verify(organizationHelper, times(1)).searchTasks(any());
		verify(selfdataHelper, times(1)).searchTasks(any());
	}

	@Test
	@Transactional  // Isoler ce test pour éviter l'interférence avec d'autres tests utilisant les mêmes logins
	@DisplayName("sendAccountDeletionWarnings : Vérifie le passage du flag a inactifs sur les users concernés")
	void test_sendAccountDeletionWarnings_setsInactiveFlagOnUsers() {
		// GIVEN : utilisateurs ACTIFS avec dernière connexion très ancienne (23 mois)
		// et un utilisateur actif avec connexion récente
		userDataFactory.getOrCreateInactiveUserEntity("inactive@example.com", 23);
		userDataFactory.getOrCreateInactiveUserEntity("inactive2@example.com", 23);
		userDataFactory.getOrCreateActiveUserEntity("active@example.com");

		// Mock les helpers pour qu'ils retournent pas de tâches
		when(projektHelper.searchTasks(any())).thenReturn(new ArrayList<>());
		when(organizationHelper.searchTasks(any())).thenReturn(new ArrayList<>());
		when(selfdataHelper.searchTasks(any())).thenReturn(new ArrayList<>());

		// WHEN
		accountService.sendAccountDeletionWarnings();

		// THEN : vérifier les states des utilisateurs
		UserEntity foundInactive1 = userDao.findByLogin("inactive@example.com");
		UserEntity foundInactive2 = userDao.findByLogin("inactive2@example.com");
		UserEntity foundActive = userDao.findByLogin("active@example.com");

		assertNotNull(foundInactive1, "Utilisateur inactif 1 doit exister en BD");
		assertNotNull(foundInactive2, "Utilisateur inactif 2 doit exister en BD");
		assertNotNull(foundActive, "Utilisateur actif doit exister en BD");

		// Les utilisateurs inactifs doivent passer en état INACTIVE
		assertThat(foundInactive1.getAccountState()).isEqualTo(AccountState.INACTIVE);
		assertThat(foundInactive2.getAccountState()).isEqualTo(AccountState.INACTIVE);
		// L'utilisateur actif reste ACTIVE
		assertThat(foundActive.getAccountState()).isEqualTo(AccountState.ACTIVE);

		// Les helpers sont appelés pour chercher les tâches de chaque utilisateur inactif (2 appels chacun)
		verify(projektHelper, times(2)).searchTasks(any());
		verify(organizationHelper, times(2)).searchTasks(any());
		verify(selfdataHelper, times(2)).searchTasks(any());
	}

	@Test
	@DisplayName("Suppression de comptes inactifs : aucun utilisateur à supprimer")
	void testDeleteInactiveUsersAccountShouldNotAnonymizeWhenNoUsersFound() {
		// GIVEN : créer un utilisateur en état INACTIVE mais qui n'a pas atteint le seuil de 24 mois
		// (par ex: inactif depuis 20 mois seulement)
		userDataFactory.getOrCreateInactiveStateUserEntity("notyet@example.com", 20);

		// Créer aussi un utilisateur ACTIF pour s'assurer qu'il n'est pas touché
		UserEntity activeUser = userDataFactory.getOrCreateActiveUserEntity("active@example.com");
		String originalLogin = activeUser.getLogin();
		String originalFirstname = activeUser.getFirstname();

		// WHEN : appeler la méthode qui supprime les utilisateurs inactifs (seuil: 24 mois)
		accountService.deleteInactiveUsersAccount();

		// THEN : aucun utilisateur ne doit être anonymisé
		UserEntity foundInactiveNotYet = userDao.findByLogin("notyet@example.com");
		UserEntity foundActive = userDao.findByLogin(originalLogin);

		// L'utilisateur inactif depuis 20 mois ne doit pas être anonymisé
		assertThat(foundInactiveNotYet)
			.isNotNull()
			.extracting(UserEntity::getLastname)
			.isNotEqualTo("Utilisateur Inconnu");

		// L'utilisateur actif ne doit pas être modifié
		assertThat(foundActive)
			.isNotNull()
			.extracting(UserEntity::getLogin, UserEntity::getFirstname)
			.containsExactly(originalLogin, originalFirstname);
	}

	@Test
	@DisplayName("Suppression de comptes inactifs : anonymise plusieurs utilisateurs et ignore les actifs")
	void testDeleteInactiveUsersAccountShouldAnonymizeMultipleUsersAndSkipActive() {
		// GIVEN : créer plusieurs utilisateurs en état INACTIVE inactifs depuis 24+ mois
		UserEntity inactiveUser1 = userDataFactory.getOrCreateInactiveStateUserEntity("todelete1@example.com", 25);
		UserEntity inactiveUser2 = userDataFactory.getOrCreateInactiveStateUserEntity("todelete2@example.com", 26);

		// Créer un utilisateur ACTIF qui ne doit pas être modifié
		UserEntity activeUser = userDataFactory.getOrCreateActiveUserEntity("mustremain@example.com");
		String activeOriginalLogin = activeUser.getLogin();
		String activeOriginalFirstname = activeUser.getFirstname();
		String activeOriginalLastname = activeUser.getLastname();

		// Créer un utilisateur INACTIF mais pas assez longtemps
		userDataFactory.getOrCreateInactiveStateUserEntity("notyet@example.com", 23);

		UUID user1UuidBefore = inactiveUser1.getUuid();
		UUID user2UuidBefore = inactiveUser2.getUuid();

		// WHEN : appeler la méthode de suppression
		accountService.deleteInactiveUsersAccount();

		// THEN : vérifier l'anonymisation des utilisateurs supprimés
		UserEntity anonUser1 = userDao.findByUuid(user1UuidBefore);
		UserEntity anonUser2 = userDao.findByUuid(user2UuidBefore);
		UserEntity stillActive = userDao.findByLogin(activeOriginalLogin);
		UserEntity notYetDeleted = userDao.findByLogin("notyet@example.com");

		// Les utilisateurs supprimés doivent être anonymisés
		assertThat(anonUser1)
			.isNotNull()
			.extracting(UserEntity::getLastname, UserEntity::getFirstname, UserEntity::getPassword, 
					UserEntity::getAccountState)
			.containsExactly("Utilisateur Inconnu", "", "", AccountState.DELETED);

		assertThat(anonUser2)
			.isNotNull()
			.extracting(UserEntity::getLastname, UserEntity::getFirstname, UserEntity::getPassword, 
					UserEntity::getAccountState)
			.containsExactly("Utilisateur Inconnu", "", "", AccountState.DELETED);

		// Le login doit être remplacé par l'UUID
		assertThat(anonUser1.getLogin())
			.isEqualTo(user1UuidBefore.toString());
		assertThat(anonUser2.getLogin())
			.isEqualTo(user2UuidBefore.toString());

		// L'utilisateur actif doit rester inchangé
		assertThat(stillActive)
			.isNotNull()
			.extracting(UserEntity::getLogin, UserEntity::getFirstname, UserEntity::getLastname)
			.containsExactly(activeOriginalLogin, activeOriginalFirstname, activeOriginalLastname);

		// L'utilisateur inactif depuis 23 mois ne doit pas être supprimé
		assertThat(notYetDeleted)
			.isNotNull()
			.extracting(UserEntity::getLastname)
			.isNotEqualTo("Utilisateur Inconnu");
	}

	@Test
	@DisplayName("Suppression de comptes inactifs : vérifie l'anonymisation complète d'un utilisateur")
	void testDeleteInactiveUsersAccountShouldAnonymizeUserCorrectly() {
		// GIVEN : créer un utilisateur en état INACTIVE inactif depuis 24 mois
		UserEntity userToDelete = userDataFactory.getOrCreateInactiveStateUserEntity("delete@example.com", 24);
		UUID userUuid = userToDelete.getUuid();
		String originalLogin = userToDelete.getLogin();
		String originalFirstname = userToDelete.getFirstname();
		String originalLastname = userToDelete.getLastname();

		// Vérifier les données avant anonymisation
		assertThat(userToDelete)
			.extracting(UserEntity::getLogin, UserEntity::getFirstname, UserEntity::getLastname, 
					UserEntity::getAccountState)
			.containsExactly(originalLogin, originalFirstname, originalLastname, AccountState.INACTIVE);
		assertThat(userToDelete.getPassword()).isNotNull();

		// WHEN : appeler la méthode de suppression
		accountService.deleteInactiveUsersAccount();

		// THEN : vérifier que toutes les données sont anonymisées correctement
		UserEntity anonymizedUser = userDao.findByUuid(userUuid);

		assertThat(anonymizedUser)
			.isNotNull()
			.as("L'utilisateur doit exister après anonymisation")
			.satisfies(user -> {
				assertThat(user.getLastname())
					.as("Le nom de famille doit être remplacé par 'Utilisateur Inconnu'")
					.isEqualTo("Utilisateur Inconnu");
				assertThat(user.getFirstname())
					.as("Le prénom doit être vidé")
					.isEmpty();
				assertThat(user.getLogin())
					.as("Le login doit être remplacé par l'UUID")
					.isEqualTo(userUuid.toString());
				assertThat(user.getPassword())
					.as("Le mot de passe doit être vidé")
					.isEmpty();
				assertThat(user.getAccountState())
					.as("Le statut du compte doit être DELETED")
					.isEqualTo(AccountState.DELETED);
			});

		// Vérifier que le login original n'existe plus en base
		UserEntity notFoundByOriginalLogin = userDao.findByLogin(originalLogin);
		assertThat(notFoundByOriginalLogin)
			.as("L'utilisateur ne doit pas être trouvable par son ancien login")
			.isNull();
	}
	@Test
	@Transactional  // Isoler ce test pour éviter l'interférence avec d'autres tests
	@DisplayName("sendAccountDeletionWarnings : exclut les utilisateurs de type ROBOT")
	void test_sendAccountDeletionWarnings_robotUsersNotInactivated() {
		// GIVEN : utilisateur ROBOT inactif depuis plus de 24 mois
		userDataFactory.getOrCreateRobotUserEntity("robot@example.com", 25);

		// Créer aussi un utilisateur PERSON actif pour comparaison
		userDataFactory.getOrCreateActiveUserEntity("active@example.com");

		// Mock les helpers pour qu'ils retournent pas de tâches
		when(projektHelper.searchTasks(any())).thenReturn(new ArrayList<>());
		when(organizationHelper.searchTasks(any())).thenReturn(new ArrayList<>());
		when(selfdataHelper.searchTasks(any())).thenReturn(new ArrayList<>());

		// WHEN
		accountService.sendAccountDeletionWarnings();

		// THEN : vérifier que l'utilisateur ROBOT est toujours ACTIVE (pas rendu INACTIVE)
		UserEntity foundRobot = userDao.findByLogin("robot@example.com");
		assertNotNull(foundRobot, "L'utilisateur ROBOT doit exister en base");
		assertThat(foundRobot.getAccountState())
			.as("L'utilisateur ROBOT ne doit pas passer à AccountState.INACTIVE")
			.isEqualTo(AccountState.ACTIVE);

		// L'utilisateur actif doit rester ACTIVE
		UserEntity foundActive = userDao.findByLogin("active@example.com");
		assertNotNull(foundActive, "L'utilisateur actif doit exister en base");
		assertThat(foundActive.getAccountState())
			.isEqualTo(AccountState.ACTIVE);

		// Les helpers ne doivent être appelés que pour l'utilisateur actif (si dans le seuil)
		// Pas pour le robot
	}

	@Test
	@Transactional  // Isoler ce test pour éviter l'interférence avec d'autres tests
	@DisplayName("sendAccountDeletionWarnings : exclut les utilisateurs ayant le rôle MODERATOR")
	void test_sendAccountDeletionWarnings_moderatorUsersNotInactivated() {
		// GIVEN : utilisateur avec rôle MODERATOR inactif depuis plus de 24 mois
		userDataFactory.getOrCreateModeratorUserEntity("moderator@example.com", 25, false);

		// Créer aussi un utilisateur sans rôle MODERATOR inactif pour comparaison
		userDataFactory.getOrCreateInactiveUserEntity("regular@example.com", 25);

		// Mock les helpers pour qu'ils retournent pas de tâches
		when(projektHelper.searchTasks(any())).thenReturn(new ArrayList<>());
		when(organizationHelper.searchTasks(any())).thenReturn(new ArrayList<>());
		when(selfdataHelper.searchTasks(any())).thenReturn(new ArrayList<>());

		// WHEN
		accountService.sendAccountDeletionWarnings();

		// THEN : vérifier que l'utilisateur MODERATOR n'est pas rendu INACTIVE
		UserEntity foundModerator = userDao.findByLogin("moderator@example.com");
		assertNotNull(foundModerator, "L'utilisateur MODERATOR doit exister en base");
		assertThat(foundModerator.getAccountState())
			.as("L'utilisateur ayant le rôle MODERATOR ne doit pas passer à AccountState.INACTIVE")
			.isEqualTo(AccountState.ACTIVE);

		// L'utilisateur sans rôle MODERATOR doit passer à INACTIVE
		UserEntity foundRegular = userDao.findByLogin("regular@example.com");
		assertNotNull(foundRegular, "L'utilisateur regular doit exister en base");
		assertThat(foundRegular.getAccountState())
			.as("L'utilisateur sans rôle MODERATOR doit passer à AccountState.INACTIVE")
			.isEqualTo(AccountState.INACTIVE);

		// Les helpers doivent être appelés une fois pour l'utilisateur regular
		verify(projektHelper, times(1)).searchTasks(any());
		verify(organizationHelper, times(1)).searchTasks(any());
		verify(selfdataHelper, times(1)).searchTasks(any());
	}

	
}
