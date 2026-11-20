package org.rudi.microservice.projekt.service.datafactory;

import java.util.List;
import java.util.UUID;

import org.rudi.facet.acl.bean.Role;
import org.rudi.facet.acl.bean.User;
import org.springframework.stereotype.Component;

@Component
public class UserDataFactory {

	public User createSimpleUser(UUID uuid, String login) {
		return new User().uuid(uuid).login(login);
	}

	public User createUser(String login, UUID uuid, List<Role> roles) {
		return new User().login(login).uuid(uuid).roles(roles);
	}

}
