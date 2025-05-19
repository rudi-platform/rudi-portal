/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.oauth2.configurer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @see org.springframework.security.oauth2.server.authorization.web.authentication.ClientSecretBasicAuthenticationConverter
 * 
 */
public class ClientSecretBasicAuthenticationConverter implements AuthenticationConverter {

	@Nullable
	@Override
	public Authentication convert(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null) {
			return null;
		}

		String[] parts = header.split("\\s");
		if (!parts[0].equalsIgnoreCase("Basic")) {
			return null;
		}

		if (parts.length != 2) {
			throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
		}

		byte[] decodedCredentials;
		try {
			decodedCredentials = Base64.getDecoder().decode(parts[1].getBytes(StandardCharsets.UTF_8));
		} catch (IllegalArgumentException ex) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST), ex);
		}

		String credentialsString = new String(decodedCredentials, StandardCharsets.UTF_8);
		String[] credentials = credentialsString.split(":", 2);
		if (credentials.length != 2 || !StringUtils.hasText(credentials[0]) || !StringUtils.hasText(credentials[1])) {
			throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
		}

		// on ne fait plus d'URL decoding
		// ça ne respecte pas vraiment la rfc mais c'est ce que tout le monde fait
		String clientID = credentials[0];
		String clientSecret = credentials[1];

		return new OAuth2ClientAuthenticationToken(clientID, ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
				clientSecret, OAuth2EndpointUtils.getParametersIfMatchesAuthorizationCodeGrantRequest(request));
	}

}
