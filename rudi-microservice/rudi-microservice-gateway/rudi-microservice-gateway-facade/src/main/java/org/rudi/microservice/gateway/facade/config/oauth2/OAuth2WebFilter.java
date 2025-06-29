/**
 * RUDI Portail
 */
package org.rudi.microservice.gateway.facade.config.oauth2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.core.security.UserType;
import org.rudi.microservice.gateway.facade.config.AbstractAuthenticationWebFilter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Filtre pour les tokens OAuth2
 * 
 * @author FNI18300
 *
 */
@Slf4j
public class OAuth2WebFilter extends AbstractAuthenticationWebFilter {

	private RestTemplate restTemplate = null;

	private String checkTokenUri;

	public OAuth2WebFilter(final String[] excludeUrlPatterns, String checkTokenUri, RestTemplate restTemplate) {
		super(excludeUrlPatterns);
		this.checkTokenUri = checkTokenUri;
		this.restTemplate = restTemplate;
	}

	protected Mono<Authentication> handleToken(final String token) {
		try {
			ResponseEntity<OAuth2TokenData> checkToken = restTemplate.postForEntity(checkTokenUri,
					buildFomEntity("token", token), OAuth2TokenData.class);
			if (checkToken.getStatusCode() == HttpStatus.OK) {
				return authenticationSuccess(checkToken);
			} else {
				// On considère que le token est invalide
				log.warn("Le token OAuth2 est invalide {}", checkToken.getStatusCode());
				return Mono.empty();
			}
		} catch (HttpClientErrorException.BadRequest e) {
			// c'est la cas d'un token jwt reçu
			log.warn(
					"OAuth2 token check by Gateway failed. JWT token check should be done afterward by another filter. See ACL logs for details.");
			return Mono.empty();
		} catch (Exception e) {
			log.warn("OAuth2 authentication failed", e);
			return Mono.empty();
		}
	}

	private Mono<Authentication> authenticationSuccess(ResponseEntity<OAuth2TokenData> checkToken) {
		OAuth2TokenData tokenData = checkToken.getBody();
		if (tokenData != null && tokenData.isActive()) {
			// Validation du token
			AuthenticatedUser user = createAuthenticatedUser(tokenData);
			// Application des authorités
			final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
					user.getLogin(), null, collectAuthorities(user));
			usernamePasswordAuthenticationToken.setDetails(user);

			return Mono.just(usernamePasswordAuthenticationToken);
		} else {
			// On considère que le token est invalide
			if (tokenData != null) {
				log.warn("Le token OAuth2 pour {} est inactif", tokenData.getClientId());
			} else {
				log.warn("Aucune donnée trouvée dans le token");
			}
			return Mono.empty();
		}
	}

	private AuthenticatedUser createAuthenticatedUser(OAuth2TokenData tokenData) {
		AuthenticatedUser user = new AuthenticatedUser(tokenData.getClientId(), UserType.ROBOT);
		user.setRoles(new ArrayList<>());
		if (CollectionUtils.isNotEmpty(tokenData.getAuthorities())) {
			for (String role : tokenData.getAuthorities()) {
				user.getRoles().add(role);
			}
		}
		return user;
	}

	public static HttpEntity<MultiValueMap<String, String>> buildFomEntity(String param, String value) {
		Map<String, String> map = new HashMap<>();
		map.put(param, value);
		return buildFomEntity(map);
	}

	public static HttpEntity<MultiValueMap<String, String>> buildFomEntity(Map<String, String> parameters) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		if (MapUtils.isNotEmpty(parameters)) {
			parameters.entrySet().forEach(item -> map.add(item.getKey(), item.getValue()));
		}

		return new HttpEntity<>(map, headers);
	}

}
