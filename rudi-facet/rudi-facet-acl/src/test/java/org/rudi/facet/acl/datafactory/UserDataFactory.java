package org.rudi.facet.acl.datafactory;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.rudi.common.service.datafactory.AbstractDataFactory;
import org.rudi.facet.acl.bean.Role;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.bean.UserType;
import org.rudi.facet.acl.helper.ACLHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class UserDataFactory extends AbstractDataFactory {

	private final ACLHelper helper;
	private final RoleDataFactory roleDataFactory;

	public User getOrCreateUser(String login, UserType type, List<Role> roles) {
		User user = null;

		if (StringUtils.isNotEmpty(login)) {
			user = helper.getUserByLogin(login);
		}

		if (user != null) {
			return user;
		}

		user = new User();
		user.setUuid(UUID.randomUUID());
		user.setLogin(login);
		user.setPassword(randomString(10)+"Az!1");
		user.setCompany(randomString(20));
		user.setFirstname(randomString(20));
		user.setLastname(randomString(20));
		user.setType(type);
		user.setRoles(roles);

		return user;
	}

	public User createUserNodeProvider(String login) {
		return getOrCreateUser(login, UserType.ROBOT, List.of(roleDataFactory.getOrCreate("PROVIDER", null)));
	}

	public User createUser(String login){
		return getOrCreateUser(login, UserType.PERSON, List.of(roleDataFactory.getOrCreate("USER", null)));
	}

	public User createUserAdmin(String login){
		return getOrCreateUser(login, UserType.PERSON, List.of(roleDataFactory.getOrCreate("USER", null), roleDataFactory.getOrCreate("ADMINISTRATOR", null)));
	}

	public User createUserModerator(String login){
		return getOrCreateUser(login, UserType.PERSON, List.of(roleDataFactory.getOrCreate("USER", null), roleDataFactory.getOrCreate("MODERATOR", null)));
	}
}
