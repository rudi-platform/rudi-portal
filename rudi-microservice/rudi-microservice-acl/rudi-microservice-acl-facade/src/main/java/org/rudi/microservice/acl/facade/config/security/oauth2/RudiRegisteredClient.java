/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.oauth2;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.util.SpringAuthorizationServerVersion;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author FNI18300
 *
 */
@NoArgsConstructor
@Getter
public class RudiRegisteredClient extends RegisteredClient {

	private static final long serialVersionUID = 5282558586069906883L;

	private String id;
	private String clientId;
	private Instant clientIdIssuedAt;
	private String clientSecret;
	private Instant clientSecretExpiresAt;
	private String clientName;
	private Set<ClientAuthenticationMethod> clientAuthenticationMethods;
	private Set<AuthorizationGrantType> authorizationGrantTypes;
	private Set<String> redirectUris;
	private Set<String> postLogoutRedirectUris;
	private Set<String> scopes;
	private ClientSettings clientSettings;
	private TokenSettings tokenSettings;
	private List<String> autorities;

	/**
	 * A builder for {@link RegisteredClient}.
	 */
	public static class Builder implements Serializable {
		private static final long serialVersionUID = SpringAuthorizationServerVersion.SERIAL_VERSION_UID;
		private String id;
		private String clientId;
		private Instant clientIdIssuedAt;
		private String clientSecret;
		private Instant clientSecretExpiresAt;
		private String clientName;
		private final Set<ClientAuthenticationMethod> clientAuthenticationMethods = new HashSet<>();
		private final Set<AuthorizationGrantType> authorizationGrantTypes = new HashSet<>();
		private final Set<String> redirectUris = new HashSet<>();
		private final Set<String> postLogoutRedirectUris = new HashSet<>();
		private final Set<String> scopes = new HashSet<>();
		private ClientSettings clientSettings;
		private TokenSettings tokenSettings;
		private List<String> authorities = new ArrayList<>();

		protected Builder(String id) {
			this.id = id;
		}

		protected Builder(RegisteredClient registeredClient) {
			this.id = registeredClient.getId();
			this.clientId = registeredClient.getClientId();
			this.clientIdIssuedAt = registeredClient.getClientIdIssuedAt();
			this.clientSecret = registeredClient.getClientSecret();
			this.clientSecretExpiresAt = registeredClient.getClientSecretExpiresAt();
			this.clientName = registeredClient.getClientName();
			if (!CollectionUtils.isEmpty(registeredClient.getClientAuthenticationMethods())) {
				this.clientAuthenticationMethods.addAll(registeredClient.getClientAuthenticationMethods());
			}
			if (!CollectionUtils.isEmpty(registeredClient.getAuthorizationGrantTypes())) {
				this.authorizationGrantTypes.addAll(registeredClient.getAuthorizationGrantTypes());
			}
			if (!CollectionUtils.isEmpty(registeredClient.getRedirectUris())) {
				this.redirectUris.addAll(registeredClient.getRedirectUris());
			}
			if (!CollectionUtils.isEmpty(registeredClient.getPostLogoutRedirectUris())) {
				this.postLogoutRedirectUris.addAll(registeredClient.getPostLogoutRedirectUris());
			}
			if (!CollectionUtils.isEmpty(registeredClient.getScopes())) {
				this.scopes.addAll(registeredClient.getScopes());
			}
			this.clientSettings = ClientSettings.withSettings(registeredClient.getClientSettings().getSettings())
					.build();
			this.tokenSettings = TokenSettings.withSettings(registeredClient.getTokenSettings().getSettings()).build();
			this.authorities = List.of();
		}

		/**
		 * Sets the identifier for the registration.
		 *
		 * @param id the identifier for the registration
		 * @return the {@link Builder}
		 */
		public Builder id(String id) {
			this.id = id;
			return this;
		}

		/**
		 * Sets the client identifier.
		 *
		 * @param clientId the client identifier
		 * @return the {@link Builder}
		 */
		public Builder clientId(String clientId) {
			this.clientId = clientId;
			return this;
		}

		/**
		 * Sets the time at which the client identifier was issued.
		 *
		 * @param clientIdIssuedAt the time at which the client identifier was issued
		 * @return the {@link Builder}
		 */
		public Builder clientIdIssuedAt(Instant clientIdIssuedAt) {
			this.clientIdIssuedAt = clientIdIssuedAt;
			return this;
		}

		/**
		 * Sets the client secret.
		 *
		 * @param clientSecret the client secret
		 * @return the {@link Builder}
		 */
		public Builder clientSecret(String clientSecret) {
			this.clientSecret = clientSecret;
			return this;
		}

		/**
		 * Sets the time at which the client secret expires or {@code null} if it does not expire.
		 *
		 * @param clientSecretExpiresAt the time at which the client secret expires or {@code null} if it does not expire
		 * @return the {@link Builder}
		 */
		public Builder clientSecretExpiresAt(Instant clientSecretExpiresAt) {
			this.clientSecretExpiresAt = clientSecretExpiresAt;
			return this;
		}

		/**
		 * Sets the client name.
		 *
		 * @param clientName the client name
		 * @return the {@link Builder}
		 */
		public Builder clientName(String clientName) {
			this.clientName = clientName;
			return this;
		}

		/**
		 * Adds an {@link ClientAuthenticationMethod authentication method} the client may use when authenticating with the authorization server.
		 *
		 * @param clientAuthenticationMethod the authentication method
		 * @return the {@link Builder}
		 */
		public Builder clientAuthenticationMethod(ClientAuthenticationMethod clientAuthenticationMethod) {
			this.clientAuthenticationMethods.add(clientAuthenticationMethod);
			return this;
		}

		/**
		 * A {@code Consumer} of the {@link ClientAuthenticationMethod authentication method(s)} allowing the ability to add, replace, or remove.
		 *
		 * @param clientAuthenticationMethodsConsumer a {@code Consumer} of the authentication method(s)
		 * @return the {@link Builder}
		 */
		public Builder clientAuthenticationMethods(
				Consumer<Set<ClientAuthenticationMethod>> clientAuthenticationMethodsConsumer) {
			clientAuthenticationMethodsConsumer.accept(this.clientAuthenticationMethods);
			return this;
		}

		/**
		 * Adds an {@link AuthorizationGrantType authorization grant type} the client may use.
		 *
		 * @param authorizationGrantType the authorization grant type
		 * @return the {@link Builder}
		 */
		public Builder authorizationGrantType(AuthorizationGrantType authorizationGrantType) {
			this.authorizationGrantTypes.add(authorizationGrantType);
			return this;
		}

		/**
		 * A {@code Consumer} of the {@link AuthorizationGrantType authorization grant type(s)} allowing the ability to add, replace, or remove.
		 *
		 * @param authorizationGrantTypesConsumer a {@code Consumer} of the authorization grant type(s)
		 * @return the {@link Builder}
		 */
		public Builder authorizationGrantTypes(Consumer<Set<AuthorizationGrantType>> authorizationGrantTypesConsumer) {
			authorizationGrantTypesConsumer.accept(this.authorizationGrantTypes);
			return this;
		}

		/**
		 * Adds a redirect URI the client may use in a redirect-based flow.
		 *
		 * @param redirectUri the redirect URI
		 * @return the {@link Builder}
		 */
		public Builder redirectUri(String redirectUri) {
			this.redirectUris.add(redirectUri);
			return this;
		}

		/**
		 * A {@code Consumer} of the redirect URI(s) allowing the ability to add, replace, or remove.
		 *
		 * @param redirectUrisConsumer a {@link Consumer} of the redirect URI(s)
		 * @return the {@link Builder}
		 */
		public Builder redirectUris(Consumer<Set<String>> redirectUrisConsumer) {
			redirectUrisConsumer.accept(this.redirectUris);
			return this;
		}

		/**
		 * Adds a post logout redirect URI the client may use for logout. The {@code post_logout_redirect_uri} parameter is used by the client when requesting
		 * that the End-User's User Agent be redirected to after a logout has been performed.
		 *
		 * @param postLogoutRedirectUri the post logout redirect URI
		 * @return the {@link Builder}
		 * @since 1.1
		 */
		public Builder postLogoutRedirectUri(String postLogoutRedirectUri) {
			this.postLogoutRedirectUris.add(postLogoutRedirectUri);
			return this;
		}

		/**
		 * A {@code Consumer} of the post logout redirect URI(s) allowing the ability to add, replace, or remove.
		 *
		 * @param postLogoutRedirectUrisConsumer a {@link Consumer} of the post logout redirect URI(s)
		 * @return the {@link Builder}
		 * @since 1.1
		 */
		public Builder postLogoutRedirectUris(Consumer<Set<String>> postLogoutRedirectUrisConsumer) {
			postLogoutRedirectUrisConsumer.accept(this.postLogoutRedirectUris);
			return this;
		}

		/**
		 * Adds a scope the client may use.
		 *
		 * @param scope the scope
		 * @return the {@link Builder}
		 */
		public Builder scope(String scope) {
			this.scopes.add(scope);
			return this;
		}

		/**
		 * A {@code Consumer} of the scope(s) allowing the ability to add, replace, or remove.
		 *
		 * @param scopesConsumer a {@link Consumer} of the scope(s)
		 * @return the {@link Builder}
		 */
		public Builder scopes(Consumer<Set<String>> scopesConsumer) {
			scopesConsumer.accept(this.scopes);
			return this;
		}

		/**
		 * Sets the {@link ClientSettings client configuration settings}.
		 *
		 * @param clientSettings the client configuration settings
		 * @return the {@link Builder}
		 */
		public Builder clientSettings(ClientSettings clientSettings) {
			this.clientSettings = clientSettings;
			return this;
		}

		/**
		 * Sets the {@link TokenSettings token configuration settings}.
		 *
		 * @param tokenSettings the token configuration settings
		 * @return the {@link Builder}
		 */
		public Builder tokenSettings(TokenSettings tokenSettings) {
			this.tokenSettings = tokenSettings;
			return this;
		}

		public Builder authorities(List<String> authorities) {
			this.authorities = List.copyOf(authorities);
			return this;
		}

		public Builder authorities(Consumer<List<String>> authoritiesConsumer) {
			authoritiesConsumer.accept(this.authorities);
			return this;
		}

		/**
		 * Builds a new {@link RegisteredClient}.
		 *
		 * @return a {@link RegisteredClient}
		 */
		public RegisteredClient build() {
			Assert.hasText(this.clientId, "clientId cannot be empty");
			Assert.notEmpty(this.authorizationGrantTypes, "authorizationGrantTypes cannot be empty");
			if (this.authorizationGrantTypes.contains(AuthorizationGrantType.AUTHORIZATION_CODE)) {
				Assert.notEmpty(this.redirectUris, "redirectUris cannot be empty");
			}
			if (!StringUtils.hasText(this.clientName)) {
				this.clientName = this.id;
			}
			if (CollectionUtils.isEmpty(this.clientAuthenticationMethods)) {
				this.clientAuthenticationMethods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
			}
			if (this.clientSettings == null) {
				ClientSettings.Builder builder = ClientSettings.builder();
				if (isPublicClientType()) {
					// @formatter:off
					builder
							.requireProofKey(true)
							.requireAuthorizationConsent(true);
					// @formatter:on
				}
				this.clientSettings = builder.build();
			}
			if (this.tokenSettings == null) {
				this.tokenSettings = TokenSettings.builder().build();
			}
			validateScopes();
			validateRedirectUris();
			validatePostLogoutRedirectUris();
			return create();
		}

		private boolean isPublicClientType() {
			return this.authorizationGrantTypes.contains(AuthorizationGrantType.AUTHORIZATION_CODE)
					&& this.clientAuthenticationMethods.size() == 1
					&& this.clientAuthenticationMethods.contains(ClientAuthenticationMethod.NONE);
		}

		private RegisteredClient create() {
			RudiRegisteredClient registeredClient = new RudiRegisteredClient();

			registeredClient.id = this.id;
			registeredClient.clientId = this.clientId;
			registeredClient.clientIdIssuedAt = this.clientIdIssuedAt;
			registeredClient.clientSecret = this.clientSecret;
			registeredClient.clientSecretExpiresAt = this.clientSecretExpiresAt;
			registeredClient.clientName = this.clientName;
			registeredClient.clientAuthenticationMethods = Collections
					.unmodifiableSet(new HashSet<>(this.clientAuthenticationMethods));
			registeredClient.authorizationGrantTypes = Collections
					.unmodifiableSet(new HashSet<>(this.authorizationGrantTypes));
			registeredClient.redirectUris = Collections.unmodifiableSet(new HashSet<>(this.redirectUris));
			registeredClient.postLogoutRedirectUris = Collections
					.unmodifiableSet(new HashSet<>(this.postLogoutRedirectUris));
			registeredClient.scopes = Collections.unmodifiableSet(new HashSet<>(this.scopes));
			registeredClient.clientSettings = this.clientSettings;
			registeredClient.tokenSettings = this.tokenSettings;
			registeredClient.autorities = this.authorities;
			return registeredClient;
		}

		private void validateScopes() {
			if (CollectionUtils.isEmpty(this.scopes)) {
				return;
			}

			for (String scope : this.scopes) {
				Assert.isTrue(validateScope(scope), "scope \"" + scope + "\" contains invalid characters");
			}
		}

		private static boolean validateScope(String scope) {
			return scope == null || scope.chars().allMatch(c -> withinTheRangeOf(c, 0x21, 0x21)
					|| withinTheRangeOf(c, 0x23, 0x5B) || withinTheRangeOf(c, 0x5D, 0x7E));
		}

		private static boolean withinTheRangeOf(int c, int min, int max) {
			return c >= min && c <= max;
		}

		private void validateRedirectUris() {
			if (CollectionUtils.isEmpty(this.redirectUris)) {
				return;
			}

			for (String redirectUri : this.redirectUris) {
				Assert.isTrue(validateRedirectUri(redirectUri),
						"redirect_uri \"" + redirectUri + "\" is not a valid redirect URI or contains fragment");
			}
		}

		private void validatePostLogoutRedirectUris() {
			if (CollectionUtils.isEmpty(this.postLogoutRedirectUris)) {
				return;
			}

			for (String postLogoutRedirectUri : this.postLogoutRedirectUris) {
				Assert.isTrue(validateRedirectUri(postLogoutRedirectUri), "post_logout_redirect_uri \""
						+ postLogoutRedirectUri + "\" is not a valid post logout redirect URI or contains fragment");
			}
		}

		private static boolean validateRedirectUri(String redirectUri) {
			try {
				URI validRedirectUri = new URI(redirectUri);
				return validRedirectUri.getFragment() == null;
			} catch (URISyntaxException ex) {
				return false;
			}
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(id);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RudiRegisteredClient other = (RudiRegisteredClient) obj;
		return Objects.equals(id, other.id);
	}
}
