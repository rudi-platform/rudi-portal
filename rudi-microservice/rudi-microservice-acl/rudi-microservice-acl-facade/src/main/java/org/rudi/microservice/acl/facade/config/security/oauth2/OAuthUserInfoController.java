/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.oauth2;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author FNI18300
 *
 */
@RestController
public class OAuthUserInfoController {

	@GetMapping("/oauth2/userinfo")
	public Principal user(Principal user) {
		return user;
	}

}
