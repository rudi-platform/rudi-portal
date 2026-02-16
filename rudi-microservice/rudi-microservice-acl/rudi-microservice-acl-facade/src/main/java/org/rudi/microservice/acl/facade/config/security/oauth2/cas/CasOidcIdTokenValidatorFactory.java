/**
 * 
 */
package org.rudi.microservice.acl.facade.config.security.oauth2.cas;

import java.time.Duration;
import java.util.function.Function;

import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenValidator;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author FNI18300
 *
 */
@AllArgsConstructor
public class CasOidcIdTokenValidatorFactory implements Function<ClientRegistration, OAuth2TokenValidator<Jwt>> {

	@Getter
	private Integer clockSew;

	@Override
	public OAuth2TokenValidator<Jwt> apply(ClientRegistration clientRegistration) {
		OidcIdTokenValidator oidcIdTokenValidator = new OidcIdTokenValidator(clientRegistration);
		if (clockSew != null) {
			oidcIdTokenValidator.setClockSkew(Duration.ofMinutes(clockSew));
		}
		JwtTimestampValidator jwtTimestampValidator = new JwtTimestampValidator();
		return new DelegatingOAuth2TokenValidator<>(jwtTimestampValidator, oidcIdTokenValidator);
	}

}
