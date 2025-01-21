/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.oauth2;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
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
@Getter
@NoArgsConstructor
public class RudiOAuth2Authorization extends OAuth2Authorization {

	private static final long serialVersionUID = 2034470506176251749L;

	private String id;
	private String registeredClientId;
	private String principalName;
	private AuthorizationGrantType authorizationGrantType;
	private Set<String> authorizedScopes;
	private Map<Class<? extends OAuth2Token>, Token<?>> tokens;
	private transient Map<String, Object> attributes;

	public static class Token<T extends OAuth2Token> implements Serializable {
		private static final long serialVersionUID = SpringAuthorizationServerVersion.SERIAL_VERSION_UID;
		protected static final String TOKEN_METADATA_NAMESPACE = "metadata.token.";

		/**
		 * The name of the metadata that indicates if the token has been invalidated.
		 */
		public static final String INVALIDATED_METADATA_NAME = TOKEN_METADATA_NAMESPACE.concat("invalidated");

		/**
		 * The name of the metadata used for the claims of the token.
		 */
		public static final String CLAIMS_METADATA_NAME = TOKEN_METADATA_NAMESPACE.concat("claims");

		private final transient T oauth2token;
		private final transient Map<String, Object> metadata;

		protected Token(T token) {
			this(token, defaultMetadata());
		}

		protected Token(T token, Map<String, Object> metadata) {
			this.oauth2token = token;
			this.metadata = Collections.unmodifiableMap(metadata);
		}

		/**
		 * Returns the token of type {@link OAuth2Token}.
		 *
		 * @return the token of type {@link OAuth2Token}
		 */
		public T getToken() {
			return this.oauth2token;
		}

		/**
		 * Returns {@code true} if the token has been invalidated (e.g. revoked). The default is {@code false}.
		 *
		 * @return {@code true} if the token has been invalidated, {@code false} otherwise
		 */
		public boolean isInvalidated() {
			return Boolean.TRUE.equals(getMetadata(INVALIDATED_METADATA_NAME));
		}

		/**
		 * Returns {@code true} if the token has expired.
		 *
		 * @return {@code true} if the token has expired, {@code false} otherwise
		 */
		public boolean isExpired() {
			return getToken().getExpiresAt() != null && Instant.now().isAfter(getToken().getExpiresAt());
		}

		/**
		 * Returns {@code true} if the token is before the time it can be used.
		 *
		 * @return {@code true} if the token is before the time it can be used, {@code false} otherwise
		 */
		public boolean isBeforeUse() {
			Instant notBefore = null;
			Map<String, Object> claims = getClaims();
			if (!CollectionUtils.isEmpty(claims)) {
				notBefore = (Instant) claims.get("nbf");
			}
			return notBefore != null && Instant.now().isBefore(notBefore);
		}

		/**
		 * Returns {@code true} if the token is currently active.
		 *
		 * @return {@code true} if the token is currently active, {@code false} otherwise
		 */
		public boolean isActive() {
			return !isInvalidated() && !isExpired() && !isBeforeUse();
		}

		/**
		 * Returns the claims associated to the token.
		 *
		 * @return a {@code Map} of the claims, or {@code null} if not available
		 */
		@Nullable
		public Map<String, Object> getClaims() {
			return getMetadata(CLAIMS_METADATA_NAME);
		}

		/**
		 * Returns the value of the metadata associated to the token.
		 *
		 * @param name the name of the metadata
		 * @param <V>  the value type of the metadata
		 * @return the value of the metadata, or {@code null} if not available
		 */
		@Nullable
		@SuppressWarnings("unchecked")
		public <V> V getMetadata(String name) {
			Assert.hasText(name, "name cannot be empty");
			return (V) this.metadata.get(name);
		}

		/**
		 * Returns the metadata associated to the token.
		 *
		 * @return a {@code Map} of the metadata
		 */
		public Map<String, Object> getMetadata() {
			return this.metadata;
		}

		protected static Map<String, Object> defaultMetadata() {
			Map<String, Object> metadata = new HashMap<>();
			metadata.put(INVALIDATED_METADATA_NAME, false);
			return metadata;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			Token<?> that = (Token<?>) obj;
			return Objects.equals(this.oauth2token, that.oauth2token) && Objects.equals(this.metadata, that.metadata);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.oauth2token, this.metadata);
		}
	}

	public static class Builder implements Serializable {
		private static final long serialVersionUID = SpringAuthorizationServerVersion.SERIAL_VERSION_UID;
		private String id;
		private final String registeredClientId;
		private String principalName;
		private AuthorizationGrantType authorizationGrantType;
		private Set<String> authorizedScopes;
		private Map<Class<? extends OAuth2Token>, Token<?>> tokens = new HashMap<>();
		private final Map<String, Object> attributes = new HashMap<>();

		protected Builder(String registeredClientId) {
			this.registeredClientId = registeredClientId;
		}

		/**
		 * Sets the identifier for the authorization.
		 *
		 * @param id the identifier for the authorization
		 * @return the {@link Builder}
		 */
		public Builder id(String id) {
			this.id = id;
			return this;
		}

		/**
		 * Sets the {@code Principal} name of the resource owner (or client).
		 *
		 * @param principalName the {@code Principal} name of the resource owner (or client)
		 * @return the {@link Builder}
		 */
		public Builder principalName(String principalName) {
			this.principalName = principalName;
			return this;
		}

		/**
		 * Sets the {@link AuthorizationGrantType authorization grant type} used for the authorization.
		 *
		 * @param authorizationGrantType the {@link AuthorizationGrantType}
		 * @return the {@link Builder}
		 */
		public Builder authorizationGrantType(AuthorizationGrantType authorizationGrantType) {
			this.authorizationGrantType = authorizationGrantType;
			return this;
		}

		/**
		 * Sets the authorized scope(s).
		 *
		 * @param authorizedScopes the {@code Set} of authorized scope(s)
		 * @return the {@link Builder}
		 * @since 0.4.0
		 */
		public Builder authorizedScopes(Set<String> authorizedScopes) {
			this.authorizedScopes = authorizedScopes;
			return this;
		}

		/**
		 * Sets the {@link OAuth2AccessToken access token}.
		 *
		 * @param accessToken the {@link OAuth2AccessToken}
		 * @return the {@link Builder}
		 */
		public Builder accessToken(OAuth2AccessToken accessToken) {
			return token(accessToken);
		}

		/**
		 * Sets the {@link OAuth2RefreshToken refresh token}.
		 *
		 * @param refreshToken the {@link OAuth2RefreshToken}
		 * @return the {@link Builder}
		 */
		public Builder refreshToken(OAuth2RefreshToken refreshToken) {
			return token(refreshToken);
		}

		/**
		 * Sets the {@link OAuth2Token token}.
		 *
		 * @param token the token
		 * @param <T>   the type of the token
		 * @return the {@link Builder}
		 */
		public <T extends OAuth2Token> Builder token(T token) {
			return token(token, metadata -> {
			});
		}

		/**
		 * Sets the {@link OAuth2Token token} and associated metadata.
		 *
		 * @param token            the token
		 * @param metadataConsumer a {@code Consumer} of the metadata {@code Map}
		 * @param <T>              the type of the token
		 * @return the {@link Builder}
		 */
		public <T extends OAuth2Token> Builder token(T token, Consumer<Map<String, Object>> metadataConsumer) {

			Assert.notNull(token, "token cannot be null");
			Map<String, Object> metadata = Token.defaultMetadata();
			Token<?> existingToken = this.tokens.get(token.getClass());
			if (existingToken != null) {
				metadata.putAll(existingToken.getMetadata());
			}
			metadataConsumer.accept(metadata);
			Class<? extends OAuth2Token> tokenClass = token.getClass();
			this.tokens.put(tokenClass, new Token<>(token, metadata));
			return this;
		}

		protected final Builder tokens(Map<Class<? extends OAuth2Token>, Token<?>> tokens) {
			this.tokens = new HashMap<>(tokens);
			return this;
		}

		/**
		 * Adds an attribute associated to the authorization.
		 *
		 * @param name  the name of the attribute
		 * @param value the value of the attribute
		 * @return the {@link Builder}
		 */
		public Builder attribute(String name, Object value) {
			Assert.hasText(name, "name cannot be empty");
			Assert.notNull(value, "value cannot be null");
			this.attributes.put(name, value);
			return this;
		}

		/**
		 * A {@code Consumer} of the attributes {@code Map} allowing the ability to add, replace, or remove.
		 *
		 * @param attributesConsumer a {@link Consumer} of the attributes {@code Map}
		 * @return the {@link Builder}
		 */
		public Builder attributes(Consumer<Map<String, Object>> attributesConsumer) {
			attributesConsumer.accept(this.attributes);
			return this;
		}

		/**
		 * Builds a new {@link OAuth2Authorization}.
		 *
		 * @return the {@link OAuth2Authorization}
		 */
		public OAuth2Authorization build() {
			Assert.hasText(this.principalName, "principalName cannot be empty");
			Assert.notNull(this.authorizationGrantType, "authorizationGrantType cannot be null");

			RudiOAuth2Authorization authorization = new RudiOAuth2Authorization();
			if (!StringUtils.hasText(this.id)) {
				this.id = UUID.randomUUID().toString();
			}
			authorization.id = this.id;
			authorization.registeredClientId = this.registeredClientId;
			authorization.principalName = this.principalName;
			authorization.authorizationGrantType = this.authorizationGrantType;
			authorization.authorizedScopes = Collections.unmodifiableSet(
					!CollectionUtils.isEmpty(this.authorizedScopes) ? new HashSet<>(this.authorizedScopes)
							: new HashSet<>());
			authorization.tokens = Collections.unmodifiableMap(this.tokens);
			authorization.attributes = Collections.unmodifiableMap(this.attributes);
			return authorization;
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
		RudiOAuth2Authorization other = (RudiOAuth2Authorization) obj;
		return Objects.equals(id, other.id);
	}
}
