package org.rudi.microservice.acl.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.microservice.acl.core.bean.AbstractAddress;
import org.rudi.microservice.acl.core.bean.AddressRole;
import org.rudi.microservice.acl.core.bean.AddressType;
import org.rudi.microservice.acl.core.bean.EmailAddress;
import org.rudi.microservice.acl.core.bean.PasswordUpdate;
import org.rudi.microservice.acl.core.bean.PostalAddress;
import org.rudi.microservice.acl.core.bean.Role;
import org.rudi.microservice.acl.core.bean.RoleSearchCriteria;
import org.rudi.microservice.acl.core.bean.TelephoneAddress;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.core.bean.UserSearchCriteria;
import org.rudi.microservice.acl.core.bean.UserType;
import org.rudi.microservice.acl.service.AclSpringBootTest;
import org.rudi.microservice.acl.service.address.AddressRoleService;
import org.rudi.microservice.acl.service.password.IdenticalNewPasswordException;
import org.rudi.microservice.acl.service.password.InvalidCredentialsException;
import org.rudi.microservice.acl.service.password.PasswordLengthException;
import org.rudi.microservice.acl.service.role.RoleService;
import org.rudi.microservice.acl.storage.dao.address.AbstractAddressDao;
import org.rudi.microservice.acl.storage.dao.address.AddressRoleDao;
import org.rudi.microservice.acl.storage.dao.user.UserDao;
import org.rudi.microservice.acl.storage.entity.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import lombok.val;

/**
 * Class de test de la couche service de user
 */
@AclSpringBootTest
class UserServiceUT {

	@MockitoBean
	private UtilContextHelper mockedUtilContextHelper;

	@Autowired
	private UserService userService;

	@Autowired
	AddressRoleService addressRoleService;

	@Autowired
	RoleService roleService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private AbstractAddressDao abstractAddressDao;

	@Autowired
	private AddressRoleDao addressRoleDao;

	// donnees existant en base par défaut
	private Role roleUtilisateur;
	private Role roleAdmin;
	private Role roleModuleProvider;

	// données insérées en base before test
	private User userRobot;
	private AddressRole roleTelephonePro;
	private AddressRole roleMailHotline;

	private String password = "Azertyuiop+2";

	@BeforeEach
	public void initData() {
		// roleUtilisateur
		RoleSearchCriteria roleSearchCriteria = new RoleSearchCriteria();
		roleSearchCriteria.setActive(true);
		roleSearchCriteria.setCode("USER");
		List<Role> roles = roleService.searchRoles(roleSearchCriteria);
		assertEquals(1, roles.size());
		roleUtilisateur = roles.get(0);

		// roleAdmin
		roleSearchCriteria.setCode("ADMINISTRATOR");
		roles = roleService.searchRoles(roleSearchCriteria);
		assertEquals(1, roles.size());
		roleAdmin = roles.get(0);

		// roleModuleProvider
		roleSearchCriteria.setCode("MODULE_STRUKTURE");
		roles = roleService.searchRoles(roleSearchCriteria);
		assertEquals(1, roles.size());
		roleModuleProvider = roles.get(0);

		// userRobot
		userRobot = userService.createUser(initUserRobot());
		assertNotNull(userRobot);

		// création en base des roles d'adresse
		roleTelephonePro = addressRoleService.createAddressRole(initRoleTelephonePro());
		roleMailHotline = addressRoleService.createAddressRole(initRoleAdresseMailHotline());
	}

	@AfterEach
	public void cleanData() {
		userDao.deleteAll();
		abstractAddressDao.deleteAll();
		addressRoleDao.deleteAll();
	}

	private User initUserCitoyen() {
		User user = new User();
		user.setType(UserType.PERSON);
		user.setLogin("PaulDupont");
		user.setPassword(password);
		user.setFirstname("Paul");
		user.setLastname("Dupont");
		user.setCompany("association de quartier");
		user.addRolesItem(roleUtilisateur);

		return user;
	}

	private UserEntity initUserEntityCitoyen() {
		User user = initUserCitoyen();
		userService.createUser(user);
		UserEntity userEntity = userDao.findByLogin(user.getLogin());
		assertNotNull(userEntity);
		return userEntity;
	}

	private User initUserRobot() {
		User user = new User();
		user.setType(UserType.ROBOT);
		user.setLogin("providerModule");
		user.setPassword("topSecret");
		user.addRolesItem(roleModuleProvider);
		return user;
	}

	private AddressRole initRoleTelephonePro() {
		AddressRole addressRole = new AddressRole();
		addressRole.setCode("PRO");
		addressRole.setLabel("téléphone professionnel");
		addressRole.setType(AddressType.PHONE);
		addressRole.setOrder(1);
		LocalDateTime today = LocalDateTime.now();
		LocalDateTime openingDate = LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 0,
				0);
		addressRole.setOpeningDate(openingDate);

		LocalDateTime closingDate = LocalDateTime.of(openingDate.getYear() + 10, openingDate.getMonthValue(),
				openingDate.getDayOfMonth(), 0, 0);
		addressRole.setClosingDate(closingDate);

		return addressRole;
	}

	private AddressRole initRoleAdresseMailHotline() {
		AddressRole addressRole = new AddressRole();
		addressRole.setCode("HOTLINE");
		addressRole.setLabel("mail hotline");
		addressRole.setType(AddressType.EMAIL);
		addressRole.setOrder(1);
		LocalDateTime today = LocalDateTime.now();
		LocalDateTime openingDate = LocalDateTime.of(today.getYear(), today.getMonthValue(), today.getDayOfMonth(), 0,
				0);
		addressRole.setOpeningDate(openingDate);

		LocalDateTime closingDate = LocalDateTime.of(openingDate.getYear() + 10, openingDate.getMonthValue(),
				openingDate.getDayOfMonth(), 0, 0);
		addressRole.setClosingDate(closingDate);

		return addressRole;
	}

	@Test
	void testCRUDUser() {

		assertNotNull(userService);
		assertNotNull(roleUtilisateur);

		long nbUser = userDao.count();

		// creation
		// ---------
		// initialisation userCitoyen
		User userCitoyen = initUserCitoyen();

		// creation de userCitoyen
		User userCree = userService.createUser(userCitoyen);

		assertNotNull(userCree);
		assertNotNull(userCree.getUuid());
		assertEquals(UserType.PERSON, userCree.getType());
		assertEquals("PaulDupont", userCree.getLogin());
		// le password est enregistré en base mais n'est pas mappé dans le user pour sécurité
		assertNull(userCree.getPassword());
		assertEquals("Paul", userCree.getFirstname());
		assertEquals("Dupont", userCree.getLastname());
		assertEquals("association de quartier", userCree.getCompany());

		assertTrue(CollectionUtils.isEmpty(userCree.getAddresses()));
		assertEquals(1, userCree.getRoles().size());
		assertTrue(userCree.getRoles().contains(roleUtilisateur));

		assertEquals(nbUser + 1, userDao.count());

		// chargement d'un user full
		// --------------------------
		User userFull = userService.getUser(userCree.getUuid());
		assertNotNull(userFull);
		assertNull(userFull.getPassword());
		assertEquals(userCree.getUuid(), userFull.getUuid());
		assertFalse(CollectionUtils.isEmpty(userFull.getRoles()));
		assertTrue(userFull.getAddresses().isEmpty());

		// chargement d'un user en fonction de son login
		// ---------------------------------------------
		userFull = userService.getUserByLogin("PaulDupont", false);
		assertNotNull(userFull);
		assertEquals("PaulDupont", userFull.getLogin());
		assertEquals(1, userFull.getRoles().size());
		assertNull(userFull.getPassword());

		// recherche
		// ---------
		// recherche sans critères
		Page<User> pageResult = userService.searchUsers(new UserSearchCriteria(), PageRequest.of(0, 100));
		assertEquals(nbUser + 1, pageResult.getNumberOfElements());

		// recherche tous critères
		UserSearchCriteria criteriaFull = new UserSearchCriteria();
		criteriaFull.setLogin("PaulDupont");
		criteriaFull.setFirstname("Paul");
		criteriaFull.setLastname("Dupont");
		criteriaFull.setCompany("association de quartier");
		criteriaFull.setType(UserType.PERSON);
		pageResult = userService.searchUsers(criteriaFull, PageRequest.of(0, 100));
		assertEquals(1, pageResult.getNumberOfElements());
		User selectedUser = pageResult.toList().get(0);
		assertEquals("PaulDupont", selectedUser.getLogin());
		assertNull(selectedUser.getPassword());

		// modification
		// ------------
		userFull.setLogin("newLogin");
		userFull.setFirstname("Zinedine");
		userFull.setLastname("Zidane");
		userFull.setCompany("Real Madrid");
		userFull.addRolesItem(roleAdmin);

		User userModifie = userService.updateUser(userFull);
		assertNotNull(userModifie);
		assertEquals("newLogin", userModifie.getLogin());
		assertEquals("Zinedine", userModifie.getFirstname());
		assertEquals("Zidane", userModifie.getLastname());
		assertEquals("Real Madrid", userModifie.getCompany());
		// ajout d'un role
		assertEquals(2, userModifie.getRoles().size());

		// modification du user avec suppression d'un role
		User user = userService.getUser(userCree.getUuid());
		assertNotNull(user);
		assertEquals(userCree.getUuid(), user.getUuid());
		user.getRoles().remove(roleUtilisateur);

		User userModifie2 = userService.updateUser(user);
		assertEquals(1, userModifie2.getRoles().size());
		assertFalse(userModifie2.getRoles().contains(roleUtilisateur));
		assertNull(userModifie.getPassword());

		user = userService.getUser(userModifie2.getUuid());
		assertNotNull(user);
		assertFalse(user.getRoles().contains(roleUtilisateur));

		// getMe : récupération de l'utilisateur connecté
		// ----------------------------------------------
		AuthenticatedUser authenticatedUser = new AuthenticatedUser();
		authenticatedUser.setLogin(userModifie.getLogin());
		authenticatedUser.setFirstname("Zinedine");
		authenticatedUser.setLastname("Zidane");

		when(mockedUtilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);

		User connectedUser = userService.getMe();

		assertNotNull(connectedUser);
		assertNull(connectedUser.getPassword());
		assertEquals("newLogin", connectedUser.getLogin());
		assertEquals("Zinedine", connectedUser.getFirstname());
		assertEquals("Zidane", connectedUser.getLastname());
		assertEquals("Real Madrid", connectedUser.getCompany());
		assertEquals(UserType.PERSON, connectedUser.getType());
		assertEquals("ADMINISTRATOR", connectedUser.getRoles().get(0).getCode());

		// suppression d'un user (sans adressses)
		// ----------------------------------------------------
		userService.deleteUser(userModifie.getUuid());

		assertEquals(nbUser, userDao.count());
	}

	@Test
	void testCRUDAddresses() {

		assertNotNull(userRobot);

		// creation de userCitoyen
		User userCitoyen = userService.createUser(initUserCitoyen());

		// Création des différents types d'adresses
		// ----------------------------------------

		// création d'une adresse postale (pas de role d'adresse)
		PostalAddress postalAddress = new PostalAddress();
		postalAddress.setType(AddressType.POSTAL);
		postalAddress.setRecipientIdentification("Musée des Beaux-Arts");
		postalAddress.setStreetNumber("20 Quai Emile Zola");
		postalAddress.setLocality("35000 Rennes");
		postalAddress.setAdditionalIdentification("bâtiment B");
		postalAddress.setDistributionService("lieu dit les Glénans");

		AbstractAddress abstractAddress2 = userService.createAddress(userCitoyen.getUuid(), postalAddress);

		assertNotNull(abstractAddress2);
		assertNotNull(abstractAddress2.getUuid());
		assertTrue(abstractAddress2 instanceof PostalAddress);
		PostalAddress adressePostaleCreee = (PostalAddress) abstractAddress2;

		assertEquals(AddressType.POSTAL, adressePostaleCreee.getType());
		assertEquals("Musée des Beaux-Arts", adressePostaleCreee.getRecipientIdentification());
		assertEquals("20 Quai Emile Zola", adressePostaleCreee.getStreetNumber());
		assertEquals("35000 Rennes", adressePostaleCreee.getLocality());
		assertEquals("bâtiment B", adressePostaleCreee.getAdditionalIdentification());
		assertEquals("lieu dit les Glénans", adressePostaleCreee.getDistributionService());

		// création d'une adresse mail
		EmailAddress emailAddress = new EmailAddress();
		emailAddress.setType(AddressType.EMAIL);
		emailAddress.setEmail("harry.cover@free.fr");
		emailAddress.setAddressRole(roleMailHotline);

		AbstractAddress abstractAddress3 = userService.createAddress(userCitoyen.getUuid(), emailAddress);

		assertNotNull(abstractAddress3);
		assertNotNull(abstractAddress3.getUuid());
		assertTrue(abstractAddress3 instanceof EmailAddress);
		EmailAddress adresseMailCreee = (EmailAddress) abstractAddress3;
		assertEquals(AddressType.EMAIL, adresseMailCreee.getType());
		assertEquals("harry.cover@free.fr", adresseMailCreee.getEmail());
		assertEquals(roleMailHotline.getUuid(), adresseMailCreee.getAddressRole().getUuid());

		// création d'une adresse téléphone
		TelephoneAddress telephoneAddress = new TelephoneAddress();
		telephoneAddress.setType(AddressType.PHONE);
		telephoneAddress.setPhoneNumber("02 33 78 55 00");
		telephoneAddress.setAddressRole(roleTelephonePro);

		AbstractAddress abstractAddress4 = userService.createAddress(userCitoyen.getUuid(), telephoneAddress);

		assertNotNull(abstractAddress4);
		assertNotNull(abstractAddress4.getUuid());
		assertTrue(abstractAddress4 instanceof TelephoneAddress);
		TelephoneAddress adresseTelCreee = (TelephoneAddress) abstractAddress4;
		assertEquals(AddressType.PHONE, adresseTelCreee.getType());
		assertEquals("02 33 78 55 00", adresseTelCreee.getPhoneNumber());
		assertEquals(roleTelephonePro.getUuid(), adresseTelCreee.getAddressRole().getUuid());

		int nbAddresses = 3;

		// chargement d'une adresse d'un fournisseur
		// ----------------------------------------
		abstractAddress3 = userService.getAddress(userCitoyen.getUuid(), adresseMailCreee.getUuid());
		assertNotNull(abstractAddress3);
		assertEquals(adresseMailCreee.getUuid(), abstractAddress3.getUuid());
		assertTrue(abstractAddress3 instanceof EmailAddress);

		// chargement de la liste des adresses d'un fournisseur
		// ----------------------------------------------------
		List<AbstractAddress> providerAddresses = userService.getAddresses(userCitoyen.getUuid());
		assertNotNull(providerAddresses);
		assertEquals(nbAddresses, providerAddresses.size());

		// modification d'une adresse
		// --------------------------
		adresseTelCreee.setPhoneNumber("06 05 06 00 00");
		AbstractAddress abstractAddressModifiee = userService.updateAddress(userCitoyen.getUuid(), adresseTelCreee);
		TelephoneAddress adresseModifee = (TelephoneAddress) abstractAddressModifiee;
		assertEquals("06 05 06 00 00", adresseModifee.getPhoneNumber());

		// chargement d'un user full (avec roles et adresses)
		// --------------------------------------------
		User userCitoyenFull = userService.getUser(userCitoyen.getUuid());
		assertNotNull(userCitoyenFull);
		assertEquals(userCitoyenFull.getUuid(), userCitoyen.getUuid());
		assertEquals(nbAddresses, userCitoyenFull.getAddresses().size());

		// suppression d'une adresse
		// -------------------------
		assertTrue(userCitoyenFull.getAddresses().contains(adresseModifee));

		userService.deleteAddress(userCitoyen.getUuid(), adresseModifee.getUuid());

		userCitoyenFull = userService.getUser(userCitoyen.getUuid());
		assertNotNull(userCitoyenFull);
		assertEquals(userCitoyenFull.getUuid(), userCitoyen.getUuid());
		assertEquals(nbAddresses - 1, userCitoyenFull.getAddresses().size());
		assertFalse(userCitoyenFull.getAddresses().contains(adresseModifee));

		// suppression d'un user qui a des adresses
		// -----------------------------------------------
		long nbProvider = userDao.count();

		userService.deleteUser(userCitoyen.getUuid());

		assertEquals(nbProvider - 1, userDao.count());

	}

	@Test
	void testRecordAuthentification() {
		// creation de userCitoyen
		User u = userService.createUser(initUserCitoyen());
		assertNull(u.getLastConnexion());
		assertNull(u.getLastFailedAttempt());

		userService.recordAuthentication(u.getUuid(), true);
		User u1 = userService.getUser(u.getUuid());
		assertEquals(false, u1.getAccountLocked());
		assertNotNull(u1.getLastConnexion());
		assertNull(u1.getLastFailedAttempt());

		userService.recordAuthentication(u.getUuid(), false);
		User u2 = userService.getUser(u.getUuid());
		assertEquals(false, u1.getAccountLocked());
		assertNotNull(u2.getLastConnexion());
		assertNotNull(u2.getLastFailedAttempt());

		userService.recordAuthentication(u.getUuid(), true);
		User u3 = userService.getUser(u.getUuid());
		assertEquals(false, u1.getAccountLocked());
		assertNotNull(u3.getLastConnexion());
		assertNull(u3.getLastFailedAttempt());

	}

	@Test
	void testLock() {
		// creation de userCitoyen
		User u = userService.createUser(initUserCitoyen());
		assertNull(u.getLastConnexion());
		assertNull(u.getLastFailedAttempt());

		for (int i = 0; i < userService.getMaxFailedAttempt(); i++) {
			userService.recordAuthentication(u.getUuid(), false);
			User u1 = userService.getUser(u.getUuid());
			assertEquals(false, u1.getAccountLocked());
			assertNotNull(u1.getLastFailedAttempt());
		}

		userService.recordAuthentication(u.getUuid(), false);
		User u2 = userService.getUser(u.getUuid());
		assertEquals(true, u2.getAccountLocked());

	}

	@Test
	void searchUsers_by_denomination_works() {
		UserEntity user1 = new UserEntity();
		user1.setLogin("user1@mail.com");
		user1.setFirstname("Alk13");
		user1.setLastname("Begode");

		UserEntity user2 = new UserEntity();
		user2.setLogin("user2@mail.com");
		user2.setFirstname("ALK13 and things");
		user2.setLastname("Gotway");

		UserEntity user3 = new UserEntity();
		user3.setLogin("user3@mail.com");
		user3.setFirstname("Kingsong");
		user3.setLastname("alK13");

		UserEntity user4 = new UserEntity();
		user4.setLogin("alk13@mail.com");
		user4.setFirstname("Kingsong");
		user4.setLastname("S22");

		UserEntity user5 = new UserEntity();
		user5.setLogin("uiiii@mail.com");
		user5.setFirstname("Veteran");
		user5.setLastname("Sherman");

		List<UserEntity> dataUsers = List.of(user1, user2, user3, user4, user5);
		dataUsers.forEach(user -> {
			user.setUuid(UUID.randomUUID());
			user.setPassword("dfjdsklfjdsklfdsjl");
			user.setType(org.rudi.common.core.security.UserType.PERSON);
		});
		userDao.saveAll(dataUsers);

		UserSearchCriteria criteria = new UserSearchCriteria();
		criteria.setLoginAndDenomination("Alk13");
		Page<User> users = userService.searchUsers(criteria, PageRequest.of(0, 100));
		assertNotNull(users);
		assertFalse(CollectionUtils.isEmpty(users.getContent()));
		assertEquals(4, users.getTotalElements());
		assertTrue(users.stream().noneMatch(user -> user.getLogin().equalsIgnoreCase("uiiii@mail.com")));
	}

	@Test
	void updateUserPasswordOldPasswordKO() {

		AuthenticatedUser authorizedUser = new AuthenticatedUser();
		when(mockedUtilContextHelper.getAuthenticatedUser()).thenReturn(authorizedUser);

		val user = initUserEntityCitoyen();

		String newPassword = "Th1sis@newPasword!";
		val passwordUpdate = new PasswordUpdate();
		passwordUpdate.setNewPassword(newPassword);
		passwordUpdate.setOldPassword("rudi@132");

		assertThrows(InvalidCredentialsException.class,
				() -> userService.updateUserPassword(user.getLogin(), passwordUpdate));
	}

	@Test
	void updateUserPasswordOldPasswordEqualsNewPasswordKO() {

		AuthenticatedUser authorizedUser = new AuthenticatedUser();
		when(mockedUtilContextHelper.getAuthenticatedUser()).thenReturn(authorizedUser);

		val user = initUserEntityCitoyen();

		val passwordUpdate = new PasswordUpdate();
		passwordUpdate.setNewPassword(password);
		passwordUpdate.setOldPassword(password);

		assertThrows(IdenticalNewPasswordException.class,
				() -> userService.updateUserPassword(user.getLogin(), passwordUpdate));
	}

	@Test
	void updateUserPasswordLoginNullKO() {

		AuthenticatedUser authorizedUser = new AuthenticatedUser();
		when(mockedUtilContextHelper.getAuthenticatedUser()).thenReturn(authorizedUser);

		initUserEntityCitoyen();

		String newPassword = "Th1sis@newPasword!";
		val passwordUpdate = new PasswordUpdate();
		passwordUpdate.setNewPassword(newPassword);
		passwordUpdate.setOldPassword(password);

		assertThrows(InvalidCredentialsException.class, () -> userService.updateUserPassword(null, passwordUpdate));
	}

	@Test
	void updateUserPasswordNewPasswordNotSecureEnoughKO() {

		AuthenticatedUser authorizedUser = new AuthenticatedUser();
		when(mockedUtilContextHelper.getAuthenticatedUser()).thenReturn(authorizedUser);

		val user = initUserEntityCitoyen();

		String newPassword = "Hello";
		val passwordUpdate = new PasswordUpdate();
		passwordUpdate.setNewPassword(newPassword);
		passwordUpdate.setOldPassword(password);

		assertThrows(PasswordLengthException.class,
				() -> userService.updateUserPassword(user.getLogin(), passwordUpdate));
	}
}
