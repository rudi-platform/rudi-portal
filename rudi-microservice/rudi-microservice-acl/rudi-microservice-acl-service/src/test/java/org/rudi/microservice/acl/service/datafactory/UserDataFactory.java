package org.rudi.microservice.acl.service.datafactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.rudi.common.core.security.AccountState;
import org.rudi.common.service.datafactory.AbstractDataFactory;
import org.rudi.microservice.acl.core.bean.Role;
import org.rudi.microservice.acl.core.bean.RoleSearchCriteria;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.core.bean.UserSearchCriteria;
import org.rudi.microservice.acl.core.bean.UserType;
import org.rudi.microservice.acl.service.role.RoleService;
import org.rudi.microservice.acl.service.user.UserService;
import org.rudi.microservice.acl.storage.dao.role.RoleCustomDao;
import org.rudi.microservice.acl.storage.dao.user.UserDao;
import org.rudi.microservice.acl.storage.entity.role.RoleEntity;
import org.rudi.microservice.acl.storage.entity.user.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Factory pour créer des objets User de test (DTO et Entity).
 *
 * Fournit deux niveaux de création :
 * - Niveau Service (DTO) : getOrCreateTestUser() - pour tests métier
 * - Niveau Storage (Entity) : getOrCreateInactiveUser() - pour tests d'intégration
 *
 * La factory utilise les services métier quand possible, puis accède directement
 * aux entités JPA pour les besoins des tests d'intégration.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class UserDataFactory extends AbstractDataFactory {

	private final UserService userService;
	private final RoleDataFactory roleDataFactory;
	private final UserDao userDao;

	/**
	 * Récupère ou crée un utilisateur de test
	 */
	public User getOrCreateTestUser(String login) {
		try {
			// Tenter de charger l'utilisateur existant
			List<User> users = userService
					.searchUsers(UserSearchCriteria.builder().login(login).build(), Pageable.unpaged()).getContent();

			if (!users.isEmpty()) {
				return users.get(0);
			}
		} catch (Exception e) {
			// L'utilisateur n'existe pas, on va le créer
		}

		// Création d'un nouvel utilisateur
		Role roleUtilisateur = roleDataFactory.getOrCreateRole("USER");

		User user = new User();
		user.setType(UserType.PERSON);
		user.setLogin(login);
		user.setPassword("TestPassword123!");
		user.setFirstname("Test");
		user.setLastname("Token");
		user.addRolesItem(roleUtilisateur);

		return userService.createUser(user);
	}

	// ===== NIVEAU STORAGE (Entity) - Pour tests d'intégration =====

	/**
	 * Crée ou récupère un utilisateur avec statut ACTIVE et rôle USER (niveau Entity).
	 * 
	 * Utilisée dans les tests d'intégration pour avoir accès direct aux entités JPA.
	 *
	 * @param login l'email/login de l'utilisateur
	 * @return UserEntity persistée en base
	 */
	public UserEntity getOrCreateActiveUserEntity(String login) {
		return getOrCreateUserEntity(login, AccountState.ACTIVE);
	}

	/**
	 * Crée ou récupère un utilisateur avec le statut spécifié (niveau Entity).
	 *
	 * @param login l'email/login de l'utilisateur
	 * @param accountState le statut du compte
	 * @return UserEntity persistée en base
	 */
	public UserEntity getOrCreateUserEntity(String login, AccountState accountState) {
		// Vérifier si l'utilisateur existe déjà
		UserEntity existing = userDao.findByLogin(login);
		if (existing != null) {
			return existing;
		}

		// Créer un nouvel utilisateur au niveau Entity
		UserEntity user = new UserEntity();
		user.setUuid(UUID.randomUUID());
		user.setLogin(login);
		user.setPassword("TestPassword123!");
		user.setFirstname("Test");
		user.setLastname(randomString(10));
		user.setType(org.rudi.common.core.security.UserType.PERSON);
		user.setAccountState(accountState);
		user.setRoles(new HashSet<>());

		// Ajouter le rôle USER par défaut
		RoleEntity userRole = roleDataFactory.getOrCreateRoleEntity("USER");
		user.getRoles().add(userRole);

		return userDao.save(user);
	}

	/**
	 * Crée ou récupère un utilisateur inactif depuis N mois (niveau Entity).
	 * Utilisé pour tester les avertissements de suppression de compte.
	 * L'utilisateur est en AccountState.ACTIVE avec une lastConnexion ancienne.
	 *
	 * @param login l'email/login de l'utilisateur
	 * @param inactiveMonths nombre de mois depuis la dernière connexion
	 * @return UserEntity inactif persisté en base
	 */
	public UserEntity getOrCreateInactiveUserEntity(String login, int inactiveMonths) {
		UserEntity user = getOrCreateActiveUserEntity(login);

		// Mettre à jour la dernière connexion
		user.setLastConnexion(LocalDateTime.now().minusMonths(inactiveMonths).minusDays(1));
		return userDao.save(user);
	}

	/**
	 * Crée ou récupère un utilisateur inactif par défaut (10 mois, niveau Entity).
	 *
	 * @param login l'email/login de l'utilisateur
	 * @return UserEntity inactif persisté en base
	 */
	public UserEntity getOrCreateInactiveUserEntity(String login) {
		return getOrCreateInactiveUserEntity(login, 10);
	}

	/**
	 * Crée ou récupère un utilisateur en état INACTIVE (suppression imminente) depuis N mois (niveau Entity).
	 * Utilisé pour tester la suppression automatique de compte après avertissement.
	 * L'utilisateur est en AccountState.INACTIVE avec une lastConnexion ancienne.
	 *
	 * @param login l'email/login de l'utilisateur
	 * @param inactiveMonths nombre de mois depuis la dernière connexion
	 * @return UserEntity en état INACTIVE persisté en base
	 */
	public UserEntity getOrCreateInactiveStateUserEntity(String login, int inactiveMonths) {
		// Chercher si l'utilisateur existe déjà
		UserEntity existing = userDao.findByLogin(login);
		if (existing != null) {
			return existing;
		}

		// Créer un nouvel utilisateur avec AccountState.INACTIVE
		UserEntity user = new UserEntity();
		user.setUuid(UUID.randomUUID());
		user.setLogin(login);
		user.setPassword("TestPassword123!");
		user.setFirstname("Test");
		user.setLastname(randomString(10));
		user.setType(org.rudi.common.core.security.UserType.PERSON);
		user.setAccountState(AccountState.INACTIVE);  // État INACTIVE
		user.setLastConnexion(LocalDateTime.now().minusMonths(inactiveMonths).minusDays(1));
		user.setRoles(new HashSet<>());

		// Ajouter le rôle USER par défaut
		RoleEntity userRole = getOrCreateUserRoleEntity();
		user.getRoles().add(userRole);

		return userDao.save(user);
	}

	/**
	 * Crée ou récupère un utilisateur en état INACTIVE par défaut (24 mois, niveau Entity).
	 *
	 * @param login l'email/login de l'utilisateur
	 * @return UserEntity en état INACTIVE persisté en base
	 */
	public UserEntity getOrCreateInactiveStateUserEntity(String login) {
		return getOrCreateInactiveStateUserEntity(login, 24);
	}

	/**
	 * Crée ou récupère un utilisateur de type ROBOT inactif depuis N mois (niveau Entity).
	 * Les utilisateurs ROBOT ne doivent pas être rendus inactifs.
	 *
	 * @param login l'email/login de l'utilisateur robot
	 * @param inactiveMonths nombre de mois depuis la dernière connexion
	 * @return UserEntity de type ROBOT inactif persisté en base
	 */
	public UserEntity getOrCreateRobotUserEntity(String login, int inactiveMonths) {
		// Chercher si l'utilisateur existe déjà
		UserEntity existing = userDao.findByLogin(login);
		if (existing != null) {
			return existing;
		}

		// Créer un nouvel utilisateur de type ROBOT
		UserEntity user = new UserEntity();
		user.setUuid(UUID.randomUUID());
		user.setLogin(login);
		user.setPassword("RobotPassword123!");
		user.setFirstname("Robot");
		user.setLastname(randomString(10));
		user.setType(org.rudi.common.core.security.UserType.ROBOT);  // Type ROBOT
		user.setAccountState(AccountState.ACTIVE);  // État ACTIVE mais inactif
		user.setLastConnexion(LocalDateTime.now().minusMonths(inactiveMonths).minusDays(1));
		user.setRoles(new HashSet<>());

		// Ajouter le rôle USER par défaut
		RoleEntity userRole = getOrCreateUserRoleEntity();
		user.getRoles().add(userRole);

		return userDao.save(user);
	}

	/**
	 * Crée ou récupère un utilisateur avec le rôle MODERATOR inactif depuis N mois (niveau Entity).
	 * Les utilisateurs avec le rôle MODERATOR ne doivent pas être rendus inactifs.
	 *
	 * @param login l'email/login de l'utilisateur
	 * @param inactiveMonths nombre de mois depuis la dernière connexion
	 * @param addUserRole si true, ajoute aussi le rôle USER en plus de MODERATOR
	 * @return UserEntity avec rôle MODERATOR persisté en base
	 */
	public UserEntity getOrCreateModeratorUserEntity(String login, int inactiveMonths, boolean addUserRole) {
		// Chercher si l'utilisateur existe déjà
		UserEntity existing = userDao.findByLogin(login);
		if (existing != null) {
			return existing;
		}

		// Créer un nouvel utilisateur
		UserEntity user = new UserEntity();
		user.setUuid(UUID.randomUUID());
		user.setLogin(login);
		user.setPassword("ModeratorPassword123!");
		user.setFirstname("Moderator");
		user.setLastname(randomString(10));
		user.setType(org.rudi.common.core.security.UserType.PERSON);
		user.setAccountState(AccountState.ACTIVE);  // État ACTIVE mais inactif
		user.setLastConnexion(LocalDateTime.now().minusMonths(inactiveMonths).minusDays(1));
		user.setRoles(new HashSet<>());

		// Ajouter le rôle MODERATOR
		RoleEntity moderatorRole = roleDataFactory.getOrCreateRoleEntity("MODERATOR");
		user.getRoles().add(moderatorRole);

		// Optionnellement, ajouter aussi le rôle USER
		if (addUserRole) {
			RoleEntity userRole = getOrCreateUserRoleEntity();
			user.getRoles().add(userRole);
		}

		return userDao.save(user);
	}

	/**
	 * Crée ou récupère le rôle USER (standard pour les utilisateurs, niveau Entity).
	 *
	 * @return RoleEntity du rôle USER
	 */
	private RoleEntity getOrCreateUserRoleEntity() {
		return roleDataFactory.getOrCreateRoleEntity("USER");
	}
}
