package org.rudi.facet.bpmn.datafactory;

import java.util.List;

import org.rudi.common.core.security.AuthenticatedUser;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserDataFactory {

	public AuthenticatedUser createWithRoles(String login, List<String> roles) {
		AuthenticatedUser authenticatedUser = new AuthenticatedUser(login);
		authenticatedUser.setRoles(roles);
		return authenticatedUser;
	}

}
