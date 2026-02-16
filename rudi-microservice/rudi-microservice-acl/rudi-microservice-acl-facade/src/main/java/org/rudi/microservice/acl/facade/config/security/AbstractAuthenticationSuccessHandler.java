/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.service.util.ApplicationContext;
import org.rudi.microservice.acl.core.bean.Token;
import org.rudi.microservice.acl.core.bean.TokenType;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtTokenUtil;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;

/**
 * 
 */
@RequiredArgsConstructor
public abstract class AbstractAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private JwtTokenUtil jwtTokenUtil;

	private ObjectMapper objectMapper = new ObjectMapper();

	protected Token buildToken(TokenType type, AuthenticatedUser user, String tokenValue)
			throws ParseException, JsonProcessingException {
		String nakedTokenValue = jwtTokenUtil.removePrefix(tokenValue);
		SignedJWT jwt = jwtTokenUtil.getJWS(nakedTokenValue);
		Token token = new Token();
		token.setType(type);
		token.setAuthorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue());
		token.setUserId(user.getLogin());
		token.setRegisteredClientId(user.getLogin());

		token.setExpriresAt(LocalDateTime
				.ofInstant(jwtTokenUtil.getExpirationDateFromToken(nakedTokenValue).toInstant(), ZoneOffset.UTC));
		token.setIssuedAt(LocalDateTime.ofInstant(jwtTokenUtil.getIssuedAtDateFromToken(nakedTokenValue).toInstant(),
				ZoneOffset.UTC));

		token.setValue(nakedTokenValue);
		token.setAttributes(objectMapper.writeValueAsString(user.getData()));
		token.setMetadata(objectMapper.writeValueAsString(jwt.getJWTClaimsSet()));
		return token;
	}

	/**
	 * @return the objectMapper
	 */
	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected JwtTokenUtil getJwtTokenUtil() {
		if (jwtTokenUtil == null) {
			jwtTokenUtil = ApplicationContext.getBean(JwtTokenUtil.class);
		}
		return jwtTokenUtil;
	}
}
