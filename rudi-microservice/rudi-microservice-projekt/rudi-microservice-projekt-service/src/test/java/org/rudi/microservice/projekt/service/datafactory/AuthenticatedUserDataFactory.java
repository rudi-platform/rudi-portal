package org.rudi.microservice.projekt.service.datafactory;

import org.rudi.common.core.security.AuthenticatedUser;
import org.springframework.stereotype.Component;

@Component("projektAuthenticatedUserDataFactory")
public class AuthenticatedUserDataFactory {

	public AuthenticatedUser createAuthenticatedUser(String login) {
		AuthenticatedUser authenticatedUser = new AuthenticatedUser();
		authenticatedUser.setLogin(login);
		return authenticatedUser;
	}

}
