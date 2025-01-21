/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.oauth2;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.acl.core.bean.Token;
import org.rudi.microservice.acl.core.bean.TokenSearchCritera;
import org.rudi.microservice.acl.core.bean.TokenType;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.core.bean.UserSearchCriteria;
import org.rudi.microservice.acl.service.token.TokenService;
import org.rudi.microservice.acl.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 *
 */
@Slf4j
public class RudiAuthorizationService implements OAuth2AuthorizationService {

	@Autowired
	private UserService userService;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public OAuth2Authorization findById(String id) {
		OAuth2Authorization result = null;
		UserSearchCriteria userSearchCritera = UserSearchCriteria.builder().login(id).build();
		Page<User> users = userService.searchUsers(userSearchCritera, PageRequest.ofSize(1));
		if (!users.isEmpty()) {
			User user = users.getContent().get(0);

			TokenSearchCritera searchCritera = TokenSearchCritera.builder().userId(user.getLogin()).build();
			List<Token> tokens = tokenService.searchTokens(searchCritera);

			RudiOAuth2Authorization.Builder builder = new RudiOAuth2Authorization.Builder(user.getLogin());
			builder.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS);
			builder.authorizedScopes(computeScopes(user));
			builder.accessToken(createToken(user, tokens));
			builder.refreshToken(createRefreshToken(user, tokens));
			result = builder.build();
		}
		return result;
	}

	@Override
	public OAuth2Authorization findByToken(String tokenValue, OAuth2TokenType tokenType) {
		TokenSearchCritera searchCritera = TokenSearchCritera.builder().token(tokenValue)
				.types(List.of(TokenType.valueOf(tokenType.getValue().toUpperCase()))).build();
		List<Token> initialTokens = tokenService.searchTokens(searchCritera);
		if (!initialTokens.isEmpty()) {
			Token token = initialTokens.get(0);
			User user = userService.getUserByLogin(token.getUserId(), false);
			searchCritera = TokenSearchCritera.builder().userId(user.getLogin()).build();
			List<Token> tokens = tokenService.searchTokens(searchCritera);
			RudiOAuth2Authorization.Builder builder = new RudiOAuth2Authorization.Builder(user.getLogin());
			builder.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS);
			builder.authorizedScopes(computeScopes(user));
			builder.accessToken(createToken(user, tokens));
			builder.refreshToken(createRefreshToken(user, tokens));
			return builder.build();
		}
		return null;
	}

	/**
	 * 
	 * @param user   l'utilisateur
	 * @param tokens les tokens
	 * @return
	 */
	protected OAuth2RefreshToken createRefreshToken(User user, List<Token> tokens) {
		Token token = tokens.stream().filter(t -> t.getType().equals(TokenType.REFRESH_TOKEN)).findFirst().orElse(null);
		if (token != null) {
			return new OAuth2RefreshToken(token.getValue(), token.getIssuedAt().toInstant(ZoneOffset.UTC),
					token.getExpriresAt().toInstant(ZoneOffset.UTC));
		}
		return null;
	}

	protected OAuth2AccessToken createToken(User user, List<Token> tokens) {
		Token token = tokens.stream().filter(t -> t.getType().equals(TokenType.ACCESS_TOKEN)).findFirst().orElse(null);
		if (token != null) {
			return new OAuth2AccessToken(org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER,
					token.getValue(), token.getIssuedAt().toInstant(ZoneOffset.UTC),
					token.getExpriresAt().toInstant(ZoneOffset.UTC), computeScopes(user));
		}
		return null;
	}

	protected Set<String> computeScopes(User user) {
		Set<String> result = new HashSet<>();
		if (CollectionUtils.isNotEmpty(user.getRoles())) {
			user.getRoles().forEach(role -> result.add(role.getCode()));
		} else {
			result.add("read");
		}
		return result;
	}

	@Override
	public void save(OAuth2Authorization authorization) {
		log.info("RudiRegisteredClientRepository save authorization...");
		User user = lookupUser(authorization);
		if (user != null) {
			try {
				saveAccessToken(user, authorization);
				saveRefreshToken(user, authorization);
			} catch (Exception e) {
				log.warn("Failed to save token", e);
			}
		}
	}

	protected void saveAccessToken(User user, OAuth2Authorization authorization)
			throws JsonProcessingException, AppServiceException {
		org.springframework.security.oauth2.server.authorization.OAuth2Authorization.Token<OAuth2AccessToken> oauthToken = authorization
				.getAccessToken();
		if (oauthToken != null) {
			Token token = saveToken(TokenType.ACCESS_TOKEN, user, authorization, oauthToken);
			tokenService.saveToken(token);
		}
	}

	protected void saveRefreshToken(User user, OAuth2Authorization authorization)
			throws JsonProcessingException, AppServiceException {
		org.springframework.security.oauth2.server.authorization.OAuth2Authorization.Token<OAuth2RefreshToken> oauthToken = authorization
				.getRefreshToken();
		if (oauthToken != null) {
			Token token = saveToken(TokenType.REFRESH_TOKEN, user, authorization, oauthToken);
			tokenService.saveToken(token);
		}
	}

	protected Token saveToken(TokenType type, User user, OAuth2Authorization authorization,
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

	@Override
	public void remove(OAuth2Authorization authorization) {
		log.info("RudiRegisteredClientRepository remove authorization...");
		User user = lookupUser(authorization);
		if (user != null) {
			List<Token> tokens = lookupTokens(user.getLogin());
			if (tokens != null) {
				org.springframework.security.oauth2.server.authorization.OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization
						.getRefreshToken();
				for (Token token : tokens) {
					if (token.getValue().equals(authorization.getAccessToken().getToken().getTokenValue())
							|| (refreshToken != null
									&& token.getValue().equals(refreshToken.getToken().getTokenValue()))) {
						tokenService.removeToken(token.getId());
					}
				}
			}
		}
	}

	protected User lookupUser(OAuth2Authorization authorization) {
		UserSearchCriteria searchCritera = UserSearchCriteria.builder().login(authorization.getPrincipalName()).build();
		Page<User> users = userService.searchUsers(searchCritera, Pageable.ofSize(1));
		if (!users.isEmpty()) {
			return users.getContent().get(0);
		}
		return null;
	}

	protected List<Token> lookupTokens(String userId) {
		TokenSearchCritera searchCritera = TokenSearchCritera.builder().userId(userId).build();
		return tokenService.searchTokens(searchCritera);
	}
}
