/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.util.ApplicationContext;
import org.rudi.microservice.acl.core.bean.Token;
import org.rudi.microservice.acl.core.bean.TokenSearchCritera;
import org.rudi.microservice.acl.core.bean.TokenType;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtTokenUtil;
import org.rudi.microservice.acl.service.token.TokenService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;

/**
 * @author FNI18300
 */
@Component
@RequiredArgsConstructor
public class TokenManager {

	private JwtTokenUtil jwtTokenUtil;

	private final TokenService tokenService;

	private final ObjectMapper objectMapper;

	public Token saveToken(TokenType type, AuthenticatedUser user, String tokenValue)
			throws ParseException, JsonProcessingException, AppServiceException {
		Token token = buildToken(type, user, tokenValue);
		return tokenService.saveToken(token);
	}

	public String getTokenValue(String tokenId) {
		Token token = tokenService.getToken(Long.valueOf(tokenId));
		if (token != null) {
			return token.getValue();
		} else {
			return null;
		}
	}

	public Token buildToken(TokenType type, User user, OAuth2Authorization authorization,
			org.springframework.security.oauth2.server.authorization.OAuth2Authorization.Token<? extends OAuth2Token> oauthToken)
			throws JsonProcessingException {
		Token token = new Token();
		token.setUserId(user.getLogin());
		token.setAuthorizationGrantType(authorization.getAuthorizationGrantType().getValue());
		token.setType(type);
		token.setRegisteredClientId(authorization.getRegisteredClientId());
		token.setValue(oauthToken.getToken().getTokenValue());
		token.setIssuedAt(LocalDateTime.ofInstant(oauthToken.getToken().getIssuedAt(), ZoneOffset.UTC));
		token.setExpriresAt(LocalDateTime.ofInstant(oauthToken.getToken().getExpiresAt(), ZoneOffset.UTC));
		token.setAttributes(objectMapper.writeValueAsString(authorization.getAttributes()));
		token.setMetadata(objectMapper.writeValueAsString(oauthToken.getMetadata()));
		return token;
	}

	public Token buildToken(TokenType type, AuthenticatedUser user, String tokenValue)
			throws ParseException, JsonProcessingException {
		String nakedTokenValue = getJwtTokenUtil().removePrefix(tokenValue);
		SignedJWT jwt = getJwtTokenUtil().getJWS(nakedTokenValue);
		Token token = new Token();
		token.setType(type);
		token.setAuthorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS.getValue());
		token.setUserId(jwt.getJWTClaimsSet().getSubject());
		token.setRegisteredClientId(jwt.getJWTClaimsSet().getSubject());

		token.setExpriresAt(LocalDateTime
				.ofInstant(getJwtTokenUtil().getExpirationDateFromToken(nakedTokenValue).toInstant(), ZoneOffset.UTC));
		token.setIssuedAt(LocalDateTime
				.ofInstant(getJwtTokenUtil().getIssuedAtDateFromToken(nakedTokenValue).toInstant(), ZoneOffset.UTC));

		token.setValue(nakedTokenValue);
		if (user != null) {
			token.setAttributes(objectMapper.writeValueAsString(user.getData()));
		}
		token.setMetadata(objectMapper.writeValueAsString(jwt.getJWTClaimsSet()));
		return token;
	}

	public boolean existsRefreshToken(String refreshToken) {
		return existsToken(TokenType.REFRESH_TOKEN, refreshToken);
	}

	public boolean existsToken(String token) {
		return existsToken(TokenType.ACCESS_TOKEN, token);
	}

	protected boolean existsToken(TokenType type, String token) {
		if (token == null) {
			return false;
		}
		String nakedTokenValue = getJwtTokenUtil().removePrefix(token);
		TokenSearchCritera searchCriteria = TokenSearchCritera.builder().types(List.of(type)).token(nakedTokenValue)
				.build();
		return !tokenService.searchTokens(searchCriteria).isEmpty();
	}

	public boolean remove(String refreshToken) {
		if (refreshToken == null) {
			return false;
		}
		return tokenService.removeTokenByValue(refreshToken);
	}

	protected JwtTokenUtil getJwtTokenUtil() {
		if (jwtTokenUtil == null) {
			jwtTokenUtil = ApplicationContext.getBean(JwtTokenUtil.class);
		}
		return jwtTokenUtil;
	}

}
