package org.rudi.common.facade.config.filter;

import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.service.helper.UtilContextHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BearerTokenFilter extends OncePerRequestFilter {

	protected static final int INVALID_TOKEN_STATUS = HttpServletResponse.SC_UNAUTHORIZED;
	private final UtilContextHelper utilContextHelper;
	protected final RestTemplate oAuth2RestTemplate;

	/**
	 * <b>Avant d'utiliser cette méthode, il faut être sûr de l'ordre des filtres configuré dans la classe WebSecurityConfig du microservice</b>
	 *
	 * @return true si un autre BearerTokenFilter a déjà vérifié le token avant ce filtre dans la chaîne des filtres Spring
	 */
	protected boolean tokenHasNotAlreadyBeenChecked(HttpServletResponse response) {
		return response.getStatus() == INVALID_TOKEN_STATUS
				|| SecurityContextHolder.getContext().getAuthentication() == null;
	}

	protected void setTokenIsValid(AuthenticatedUser authenticatedUser, HttpServletResponse response) {
		utilContextHelper.setAuthenticatedUser(authenticatedUser);
		response.setHeader(HttpHeaders.WWW_AUTHENTICATE, null);
	}

	protected void setTokenIsInvalid(HttpServletResponse response) {
		setTokenIsInvalid(response, INVALID_TOKEN_STATUS);
	}

	protected void setTokenIsInvalid(HttpServletResponse response, int status) {
		response.setStatus(status);
		response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
	}

}
