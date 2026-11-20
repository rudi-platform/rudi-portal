package org.rudi.microservice.acl.service.datafactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.rudi.common.service.datafactory.AbstractDataFactory;
import org.rudi.microservice.acl.core.bean.RoleSearchCriteria;
import org.rudi.microservice.acl.service.role.RoleService;
import org.rudi.microservice.acl.storage.dao.role.RoleCustomDao;
import org.rudi.microservice.acl.storage.entity.role.RoleEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class RoleDataFactory extends AbstractDataFactory {

	private final RoleService roleService;
	private final RoleCustomDao roleCustomDao;

	/**
	 * Récupère ou crée un rôle
	 */
	public org.rudi.microservice.acl.core.bean.Role getOrCreateRole(String code) {
		RoleSearchCriteria roleSearchCriteria = new RoleSearchCriteria();
		roleSearchCriteria.setActive(true);
		roleSearchCriteria.setCode(code);
		List<org.rudi.microservice.acl.core.bean.Role> roles = roleService.searchRoles(roleSearchCriteria);

		if (!roles.isEmpty()) {
			return roles.get(0);
		}

		// Si le rôle n'existe pas, le créer (normalement les rôles de base existent)
		org.rudi.microservice.acl.core.bean.Role role = new org.rudi.microservice.acl.core.bean.Role();
		role.setCode(code);
		role.setLabel(code);
		role.setOrder(999);
		LocalDateTime now = LocalDateTime.now();
		role.setOpeningDate(now);

		return roleService.createRole(role);
	}

	/**
	 * Crée ou récupère un rôle par code (niveau Entity).
	 *
	 * @param code le code du rôle (ex: "MODERATOR", "ADMIN", "USER")
	 * @return RoleEntity du rôle cherché ou créé
	 */
	public RoleEntity getOrCreateRoleEntity(String code) {
		// Chercher le rôle existant
		RoleSearchCriteria criteria = new RoleSearchCriteria();
		criteria.setCode(code);

		List<RoleEntity> existingRoles = roleCustomDao.searchRoles(criteria);
		if (!existingRoles.isEmpty()) {
			return existingRoles.get(0);
		}

		// Créer le rôle s'il n'existe pas
		RoleEntity role = new RoleEntity();
		role.setUuid(UUID.randomUUID());
		role.setCode(code);
		role.setLabel(code);
		role.setOrder(code.equals("MODERATOR") ? 50 : 100);
		role.setOpeningDate(LocalDateTime.now());

		return role;
	}
}
