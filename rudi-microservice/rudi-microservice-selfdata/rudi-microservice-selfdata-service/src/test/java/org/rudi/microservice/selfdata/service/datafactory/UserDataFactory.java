package org.rudi.microservice.selfdata.service.datafactory;

import java.util.UUID;

import org.rudi.facet.acl.bean.User;
import org.springframework.stereotype.Component;

@Component
public class UserDataFactory {

	public User createSimpleUser(UUID uuid, String login) {
		return new User().uuid(uuid).login(login);
	}

}
