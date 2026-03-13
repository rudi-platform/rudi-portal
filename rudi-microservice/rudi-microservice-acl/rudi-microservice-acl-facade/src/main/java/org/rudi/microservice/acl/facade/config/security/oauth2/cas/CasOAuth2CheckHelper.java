/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.oauth2.cas;

import org.rudi.common.facade.config.filter.OAuth2TokenData;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CasOAuth2CheckHelper {

	private final AuthenticationManagerResolver<HttpServletRequest> trustedIssuerJwtAuthenticationManagerResolver;

	public OAuth2TokenData checkToken(String token) {
		BearerTokenAuthenticationToken authenticationRequest = new BearerTokenAuthenticationToken(token);

		try {
			AuthenticationManager authenticationManager = trustedIssuerJwtAuthenticationManagerResolver.resolve(null);
			Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);

			if (log.isDebugEnabled()) {
				log.debug("checkToken to {}", authenticationResult);
			}

			String clientId = authenticationResult.getName();
			if (authenticationResult.isAuthenticated()) {
				return OAuth2TokenData.builder().active(true).clientId(clientId).build();
			} else {
				return OAuth2TokenData.builder().active(false).errorCode(HttpStatus.UNAUTHORIZED.value())
						.clientId(clientId).build();
			}

		} catch (AuthenticationException failed) {
			log.trace("Failed to process authentication request.", failed);
		}

		return OAuth2TokenData.builder().active(false).errorCode(HttpStatus.UNAUTHORIZED.value()).clientId(null)
				.build();
	}
}
