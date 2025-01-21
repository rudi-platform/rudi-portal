/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.oauth2;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.core.bean.UserSearchCriteria;
import org.rudi.microservice.acl.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 *
 */
@Slf4j
public class RudiRegisteredClientRepository implements RegisteredClientRepository {

	@Value("${rudi.access_token.live_duration:5}")
	private int accessTokenLiveDuration;

	@Value("${rudi.refresh_token.live_duration:60}")
	private int refreshTokenLiveDuration;

	@Autowired
	private UserService userService;

	@Override
	public RegisteredClient findById(String id) {
		RegisteredClient result = null;
		UserSearchCriteria userSearchCritera = UserSearchCriteria.builder().login(id).build();
		Page<User> users = userService.searchUsers(userSearchCritera, PageRequest.ofSize(1));
		if (!users.isEmpty()) {
			User user = users.getContent().get(0);
			String encodedPassword = userService.getUserPassword(user.getUuid());
			RudiRegisteredClient.Builder builder = new RudiRegisteredClient.Builder(user.getLogin());
			builder.clientId(id);
			builder.clientSecret(encodedPassword);
			builder.clientName(id);
			builder.authorizationGrantTypes(grantTypes -> grantTypes
					.addAll(Set.of(AuthorizationGrantType.AUTHORIZATION_CODE, AuthorizationGrantType.CLIENT_CREDENTIALS,
							AuthorizationGrantType.JWT_BEARER, AuthorizationGrantType.REFRESH_TOKEN)));
			builder.clientAuthenticationMethods(
					methods -> methods.addAll(Set.of(ClientAuthenticationMethod.CLIENT_SECRET_POST,
							ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
							ClientAuthenticationMethod.CLIENT_SECRET_JWT)));
			builder.scopes(t -> {
				if (CollectionUtils.isNotEmpty(user.getRoles())) {
					user.getRoles().forEach(role -> t.add(role.getCode()));
				}
				t.add("read");
				t.add("write");
			});
			builder.authorities(t -> {
				if (CollectionUtils.isNotEmpty(user.getRoles())) {
					user.getRoles().forEach(role -> t.add(role.getCode()));
				}
			});
			builder.tokenSettings(createTokenSettings());
			builder.redirectUris(uris -> uris.addAll(List.of("/oauth2")));
			result = builder.build();
		}
		return result;
	}

	private TokenSettings createTokenSettings() {
		return TokenSettings.builder().accessTokenTimeToLive(Duration.ofMinutes(accessTokenLiveDuration))
				.refreshTokenTimeToLive(Duration.ofMinutes(refreshTokenLiveDuration)).build();
	}

	@Override
	public RegisteredClient findByClientId(String clientId) {
		return findById(clientId);
	}

	@Override
	public void save(RegisteredClient registeredClient) {
		UserSearchCriteria userSearchCritera = UserSearchCriteria.builder().login(registeredClient.getClientName())
				.build();
		Page<User> users = userService.searchUsers(userSearchCritera, PageRequest.ofSize(1));
		if (!users.isEmpty()) {
			User user = users.getContent().get(0);
			log.debug("Try to save: {}", user);
		}
	}
}
