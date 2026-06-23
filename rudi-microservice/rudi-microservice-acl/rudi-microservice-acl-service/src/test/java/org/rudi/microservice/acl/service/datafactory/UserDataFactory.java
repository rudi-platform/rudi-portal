package org.rudi.microservice.acl.service.datafactory;

import java.time.LocalDateTime;
import java.util.List;

import org.rudi.common.service.datafactory.AbstractDataFactory;
import org.rudi.microservice.acl.core.bean.Role;
import org.rudi.microservice.acl.core.bean.RoleSearchCriteria;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.core.bean.UserSearchCriteria;
import org.rudi.microservice.acl.core.bean.UserType;
import org.rudi.microservice.acl.service.role.RoleService;
import org.rudi.microservice.acl.service.user.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Factory pour créer des objets Token et User de test
 */
@Component
@RequiredArgsConstructor
public class UserDataFactory extends AbstractDataFactory {

	private final UserService userService;
	private final RoleService roleService;

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
		Role roleUtilisateur = getOrCreateRole("USER");

		User user = new User();
		user.setType(UserType.PERSON);
		user.setLogin(login);
		user.setPassword("TestPassword123!");
		user.setFirstname("Test");
		user.setLastname("Token");
		user.addRolesItem(roleUtilisateur);

		return userService.createUser(user);
	}

	/**
	 * Récupère ou crée un rôle
	 */
	public Role getOrCreateRole(String code) {
		RoleSearchCriteria roleSearchCriteria = new RoleSearchCriteria();
		roleSearchCriteria.setActive(true);
		roleSearchCriteria.setCode(code);
		List<Role> roles = roleService.searchRoles(roleSearchCriteria);

		if (!roles.isEmpty()) {
			return roles.get(0);
		}

		// Si le rôle n'existe pas, le créer (normalement les rôles de base existent)
		Role role = new Role();
		role.setCode(code);
		role.setLabel(code);
		role.setOrder(999);
		LocalDateTime now = LocalDateTime.now();
		role.setOpeningDate(now);

		return roleService.createRole(role);
	}
}
