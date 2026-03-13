/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.oauth2.authenticator;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.common.core.DocumentContent;
import org.rudi.microservice.acl.core.bean.OAuth2AuthenticatorDescription;
import org.rudi.microservice.acl.core.bean.OAuth2AuthenticatorDescription.AuthorizationGrantTypeEnum;
import org.rudi.microservice.acl.core.bean.OAuth2Provider;
import org.rudi.microservice.acl.core.bean.TokenManagement;
import org.rudi.microservice.acl.core.bean.ViewSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ClientSettings;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticatorHelper {

	private static final String AUTHENTICATORS_ICONS_URL = "/acl/v1/oauth2-authenticators/icons/";

	@Value("${rudi.oauth2.authenticator.configuration:authenticators/oauth2-authenticator.json}")
	private String oauh2AuthenticatorConfigurationFile;

	@Value("${module.oauth2.check-token-uri}")
	private String checkTokenUri;

	private String localServerUrl = null;

	private final ObjectMapper objectMapper;

	private final ResourceLoader resourceLoader;

	private List<OAuth2AuthenticatorDescription> oAuth2AuthenticatorDescriptions = null;

	public List<OAuth2AuthenticatorDescription> getOAuth2AuthenticatorDescriptions(boolean full) {
		return filterValues(getOAuth2AuthenticatorDescriptions(), full);
	}

	public OAuth2AuthenticatorDescription getOAuth2AuthenticatorDescription(String authenticatorName, boolean full) {
		return filterValues(oAuth2AuthenticatorDescriptions, full).stream()
				.filter(authenticator -> authenticator.getName().equalsIgnoreCase(authenticatorName)).findFirst()
				.orElse(null);
	}

	protected List<OAuth2AuthenticatorDescription> getOAuth2AuthenticatorDescriptions() {
		if (oAuth2AuthenticatorDescriptions == null) {
			try {
				oAuth2AuthenticatorDescriptions = loadOAuth2AuthenticatorDescriptions();
			} catch (Exception e) {
				log.error("Failed to load OAuth2 authenticator descriptions from file: {}",
						oauh2AuthenticatorConfigurationFile, e);
			}
		}
		return oAuth2AuthenticatorDescriptions;
	}

	protected List<OAuth2AuthenticatorDescription> loadOAuth2AuthenticatorDescriptions() throws IOException {
		List<OAuth2AuthenticatorDescription> result = null;
		File f = new File(oauh2AuthenticatorConfigurationFile);

		if (f.exists() && f.isFile()) {
			try (JsonParser p = objectMapper.createParser(f)) {
				result = p.readValueAs(new TypeReference<List<OAuth2AuthenticatorDescription>>() {
				});
			}
		} else {
			try (JsonParser p = objectMapper.createParser(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(oauh2AuthenticatorConfigurationFile))) {
				result = p.readValueAs(new TypeReference<List<OAuth2AuthenticatorDescription>>() {
				});
			}
		}
		if (result == null) {
			result = new ArrayList<>();
		} else {
			fillDefaultValues(result);
		}
		return result;
	}

	protected void fillDefaultValues(List<OAuth2AuthenticatorDescription> result) {
		result.forEach(authenticator -> {
			fillAuthenticatorDefaultValues(authenticator);
			fillProviderDefaultValues(authenticator);
		});

	}

	private void fillProviderDefaultValues(OAuth2AuthenticatorDescription authenticator) {
		if (authenticator.getProvider() == null) {
			authenticator.setProvider(new OAuth2Provider());
		}
		if (authenticator.getProvider().getTokenManagement() == null) {
			authenticator.getProvider().setTokenManagement(TokenManagement.IF_POSSIBLE_PROVIDER);
		}
		if (authenticator.getProvider().getAuthorizationUri() == null) {
			authenticator.getProvider().setAuthorizationUri("${serverUrl}/oauth2/authorize");
		}
		if (authenticator.getProvider().getTokenUri() == null) {
			authenticator.getProvider().setTokenUri("${serverUrl}/oauth2/token");
		}
		if (authenticator.getProvider().getUserInfoUri() == null) {
			authenticator.getProvider().setUserInfoUri("${serverUrl}/oauth2/userinfo");
		}
		if (authenticator.getProvider().getJwkSetUri() == null) {
			authenticator.getProvider().setJwkSetUri("${serverUrl}/oauth2/jwks");
		}
		if (authenticator.getProvider().getUserNameAttribute() == null) {
			authenticator.getProvider().setUserNameAttribute("sub");
		}
		if (authenticator.getProvider().getRoleConvertExpression() == null) {
			authenticator.getProvider().setRoleConvertExpression("ROLE_*(.*)");
		}
	}

	private void fillAuthenticatorDefaultValues(OAuth2AuthenticatorDescription authenticator) {
		if (authenticator.getAuthorizationGrantType() == null) {
			authenticator.setAuthorizationGrantType(AuthorizationGrantTypeEnum.AUTHORIZATION_CODE);
		}
		if (authenticator.getRedirectUrl() == null) {
			authenticator.setRedirectUrl("${serverUrl}/login/oauth2/code/");
		}
	}

	protected List<OAuth2AuthenticatorDescription> filterValues(
			List<OAuth2AuthenticatorDescription> authenticatorDescriptions, boolean full) {
		if (full) {
			return authenticatorDescriptions;
		}
		return authenticatorDescriptions.stream().map(authenticator -> {
			OAuth2AuthenticatorDescription item = new OAuth2AuthenticatorDescription().name(authenticator.getName())
					.label(authenticator.getLabel()).description(authenticator.getDescription())
					.viewSettings(new ViewSettings());
			if (authenticator.getViewSettings() != null) {
				item.getViewSettings().setIsolated(authenticator.getViewSettings().getIsolated());
				item.getViewSettings().setCssClass(authenticator.getViewSettings().getCssClass());
				if (StringUtils.isNotEmpty(authenticator.getViewSettings().getIconUrl())) {
					item.getViewSettings().setIconUrl(AUTHENTICATORS_ICONS_URL + authenticator.getName());
				}
			}
			return item;
		}).toList();
	}

	public DocumentContent getOAuth2AuthenticatorDescriptionIcon(String authenticatorName) throws IOException {
		OAuth2AuthenticatorDescription oAuth2AuthenticatorDescription = getOAuth2AuthenticatorDescriptions().stream()
				.filter(authenticator -> authenticator.getName().equalsIgnoreCase(authenticatorName)).findFirst()
				.orElse(null);
		if (oAuth2AuthenticatorDescription != null && oAuth2AuthenticatorDescription.getViewSettings() != null
				&& StringUtils.isNotEmpty(oAuth2AuthenticatorDescription.getViewSettings().getIconUrl())) {
			final Resource resource = resourceLoader
					.getResource(oAuth2AuthenticatorDescription.getViewSettings().getIconUrl());
			return DocumentContent.fromResource(resource, false);
		}
		return null;
	}

	public void authenticateThroughAuthenticator(String authenticatorName) throws IOException {
		OAuth2AuthenticatorDescription oAuth2AuthenticatorDescription = getOAuth2AuthenticatorDescriptions().stream()
				.filter(authenticator -> authenticator.getName().equalsIgnoreCase(authenticatorName)).findFirst()
				.orElse(null);
		if (oAuth2AuthenticatorDescription != null) {
			ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder
					.getRequestAttributes();
			if (servletRequestAttributes == null || servletRequestAttributes.getResponse() == null) {
				throw new IllegalStateException("No HTTP response available to redirect");
			}
			servletRequestAttributes.getResponse().sendRedirect(oAuth2AuthenticatorDescription.getServerUrl());
		} else {
			throw new IllegalArgumentException("Authenticator not found: " + authenticatorName);
		}
	}

	@Bean
	public InMemoryClientRegistrationRepository createClientRegistrationRepository() {
		OAuth2ClientProperties properties = new OAuth2ClientProperties();
		// récupère les descriptions des authentificateurs pour initialiser les providers et registrations à partir de ces descriptions. Les descriptions sont
		// ensuite utilisées pour construire les ClientRegistration à partir des propriétés.
		List<OAuth2AuthenticatorDescription> auth2AuthenticatorDescriptions = getOAuth2AuthenticatorDescriptions();
		if (CollectionUtils.isNotEmpty(auth2AuthenticatorDescriptions)) {
			// initialise les providers et registrations à partir des descriptions des authentificateurs.
			initializeProviders(auth2AuthenticatorDescriptions, properties);
			initializeRegistrations(auth2AuthenticatorDescriptions, properties);
		}
		// on reparcourt la liste des descriptions des authentificateurs pour prendre en compte les éventuelles exigences de PKCE et construire les
		// ClientRegistration en conséquence.
		Map<String, ClientRegistration> registrationMaps = new OAuth2ClientPropertiesMapper(properties)
				.asClientRegistrations();
		List<ClientRegistration> registrations = new ArrayList<>();
		for (Map.Entry<String, ClientRegistration> registration : registrationMaps.entrySet()) {
			OAuth2AuthenticatorDescription oAuth2AuthenticatorDescription = getOAuth2AuthenticatorDescription(
					registration.getKey(), true);
			if (oAuth2AuthenticatorDescription != null
					&& Boolean.TRUE.equals(oAuth2AuthenticatorDescription.getRequireProofKey())) {
				ClientRegistration original = registration.getValue();
				registrations.add(ClientRegistration.withClientRegistration(original)
						.clientSettings(ClientSettings.builder().requireProofKey(true).build()).build());
			} else {
				registrations.add(registration.getValue());
			}
		}
		return new InMemoryClientRegistrationRepository(registrations);
	}

	@Bean
	public AuthenticationManagerResolver<HttpServletRequest> trustedIssuerJwtAuthenticationManagerResolver() {
		List<OAuth2AuthenticatorDescription> auth2AuthenticatorDescriptions = getOAuth2AuthenticatorDescriptions();
		List<String> issuers = auth2AuthenticatorDescriptions.stream().map(OAuth2AuthenticatorDescription::getServerUrl)
				.toList();
		return JwtIssuerAuthenticationManagerResolver.fromTrustedIssuers(issuers);
	}

	private void initializeRegistrations(List<OAuth2AuthenticatorDescription> auth2AuthenticatorDescriptions,
			OAuth2ClientProperties properties) {
		auth2AuthenticatorDescriptions.forEach(authenticator -> {
			OAuth2ClientProperties.Registration registration = new OAuth2ClientProperties.Registration();
			registration.setAuthorizationGrantType(authenticator.getAuthorizationGrantType() != null
					? authenticator.getAuthorizationGrantType().getValue()
					: null);
			registration.setClientAuthenticationMethod(authenticator.getClientAuthenticationMethod());
			registration.setClientId(authenticator.getClientId());
			registration.setClientName(authenticator.getClientId());
			registration.setClientSecret(authenticator.getClientSecret());
			registration.setProvider(authenticator.getName());
			registration.setRedirectUri(convertUrl(authenticator.getRedirectUrl(), authenticator.getServerUrl()));
			registration.setScope(convertScope(authenticator.getScope()));
			properties.getRegistration().put(authenticator.getName(), registration);
		});
	}

	private void initializeProviders(List<OAuth2AuthenticatorDescription> auth2AuthenticatorDescriptions,
			OAuth2ClientProperties properties) {
		auth2AuthenticatorDescriptions.forEach(authenticator -> {
			if (authenticator.getProvider() != null) {
				OAuth2ClientProperties.Provider provider = new OAuth2ClientProperties.Provider();
				provider.setAuthorizationUri(
						convertUrl(authenticator.getProvider().getAuthorizationUri(), authenticator.getServerUrl()));
				provider.setUserInfoUri(
						convertUrl(authenticator.getProvider().getUserInfoUri(), authenticator.getServerUrl()));
				provider.setIssuerUri(
						convertUrl(authenticator.getProvider().getIssuerUri(), authenticator.getServerUrl()));
				provider.setJwkSetUri(
						convertUrl(authenticator.getProvider().getJwkSetUri(), authenticator.getServerUrl()));
				provider.setTokenUri(
						convertUrl(authenticator.getProvider().getTokenUri(), authenticator.getServerUrl()));
				provider.setUserInfoAuthenticationMethod(authenticator.getProvider().getUserInfoAuthenticationMethod());
				provider.setUserNameAttribute(authenticator.getProvider().getUserNameAttribute());
				properties.getProvider().put(authenticator.getName(), provider);
			}
		});
	}

	protected String convertUrl(String url, String serverUrl) {
		String result = url;
		if (StringUtils.isNotEmpty(result)) {
			if (StringUtils.isNotEmpty(getLocalServerUrl())) {
				result = result.replace("${localServerUrl}", getLocalServerUrl());
				serverUrl = serverUrl.replace("${localServerUrl}", getLocalServerUrl());
			}
			if (StringUtils.isNotEmpty(serverUrl)) {
				result = result.replace("${serverUrl}", serverUrl);
			}
			if (result != null && !(result.startsWith("http") || result.startsWith("https"))) {
				result = serverUrl + result;
			}
		}
		return result;
	}

	protected Set<String> convertScope(String scope) {
		if (StringUtils.isBlank(scope)) {
			return Set.of();
		}
		return Set.of(StringUtils.split(scope, ",")).stream().map(String::trim).filter(StringUtils::isNotBlank)
				.collect(Collectors.toSet());
	}

	/**
	 * Extrait l'URL du serveur local à partir de la check-token-uri. ça pourrait fonctionner avec n'importe quelle url déjà défini dans le fichier
	 * properties.
	 *
	 * @return L'URL du serveur local ou null si elle ne peut pas être déterminée
	 */
	protected String getLocalServerUrl() {
		if (localServerUrl != null) {
			return localServerUrl;
		}
		try {
			URL url = URI.create(checkTokenUri).toURL();
			String path = url.getPath();
			localServerUrl = checkTokenUri.substring(0, checkTokenUri.length() - path.length());
		} catch (Exception e) {
			log.warn("Error parsing check-token-uri: {}", checkTokenUri, e);
		}
		return localServerUrl;
	}
}
