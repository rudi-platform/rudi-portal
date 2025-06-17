/**
 * RUDI Portail
 */
package org.rudi.common.facade.config.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.core.security.UserType;
import org.rudi.common.service.helper.UtilContextHelper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtre pour les tokens JWT
 *
 * @author FNI18300
 */
@Slf4j
public class OAuth2RequestFilter extends BearerTokenFilter {

	// Controle des patterns des URL
	private AntPathMatcher pathMatcher;

	// Liste des URL à exclure
	private Collection<String> excludeUrlPatterns;

	private String checkTokenUri;

	public OAuth2RequestFilter(final String[] excludeUrlPatterns, String checkTokenUri,
			final UtilContextHelper utilContextHelper, final RestTemplate oAuth2RestTemplate) {
		super(utilContextHelper, oAuth2RestTemplate);
		this.excludeUrlPatterns = Arrays.asList(excludeUrlPatterns);
		pathMatcher = new AntPathMatcher();
		this.checkTokenUri = checkTokenUri;
	}

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain chain) throws ServletException, IOException {
		// Request token header
		String requestAuthentTokenHeader = request.getHeader(AbstractJwtTokenUtil.HEADER_TOKEN_JWT_AUTHENT_KEY);

		if (log.isDebugEnabled()) {
			log.debug("Header: {}", requestAuthentTokenHeader);
		}
		if (requestAuthentTokenHeader == null) {
			// on a pas de contexte donc on n'est pas authentifié
			setTokenIsInvalid(response);
		} else if (!requestAuthentTokenHeader.startsWith(AbstractJwtTokenUtil.HEADER_TOKEN_JWT_PREFIX)) {
			log.error("Le token ne commence pas avec la chaine Bearer");
			setTokenIsInvalid(response);
		} else {
			final String tokenJwt = requestAuthentTokenHeader
					.substring(AbstractJwtTokenUtil.HEADER_TOKEN_JWT_PREFIX.length());

			handleToken(response, tokenJwt);
		}

		chain.doFilter(request, response);
	}

	private void handleToken(final HttpServletResponse response, final String tokenJwt) {
		try {
			HttpEntity<MultiValueMap<String, String>> request = EntityHelper.buildFomEntity("token", tokenJwt);
			ResponseEntity<OAuth2TokenData> checkToken = oAuth2RestTemplate.postForEntity(checkTokenUri, request,
					OAuth2TokenData.class);
			if (checkToken.getStatusCode() == HttpStatus.OK) {
				handleToken(checkToken, response);
			} else {
				// On considère que le token est invalide
				log.warn("Le token OAuth2 est invalide {}", checkToken.getStatusCode());
				setTokenIsInvalid(response);
			}
		} catch (HttpClientErrorException.BadRequest e) {
			log.warn(
					"OAuth2 token check failed. JWT token check should be done afterward by another filter. See ACL logs for details.");
			// c'est la cas d'un token jwt reçu
			setTokenIsInvalid(response);
		} catch (Exception e) {
			log.warn("OAuth2 authentication failed", e);
			setTokenIsInvalid(response);
		}
	}

	private void handleToken(ResponseEntity<OAuth2TokenData> checkToken, final HttpServletResponse response) {
		OAuth2TokenData tokenData = checkToken.getBody();
		if (tokenData != null && tokenData.isActive()) {
			// Validation du token
			final var authenticatedUser = createAuthenticatedUser(tokenData);
			setTokenIsValid(authenticatedUser, response);
		} else {
			// On considère que le token est invalide
			log.warn("Le token OAuth2 pour {} est inactif", tokenData);
			setTokenIsInvalid(response);
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

	@Override
	protected boolean shouldNotFilter(final HttpServletRequest request) throws ServletException {
		// Contrôle si l'URL n'est pas dans le liste d'exclusion. Si c'est le cas, elle
		// ne passera pas dans ce filtre
		return excludeUrlPatterns.stream().anyMatch(p -> pathMatcher.match(p, request.getServletPath()));
	}

}
