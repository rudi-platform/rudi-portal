/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.jwt;

import java.io.IOException;

import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.facade.config.filter.Tokens;
import org.rudi.microservice.acl.core.bean.TokenType;
import org.rudi.microservice.acl.facade.config.security.AbstractAuthenticationSuccessHandler;
import org.rudi.microservice.acl.facade.config.security.TokenManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * @author FNI18300
 *
 */
@Component
public class JwtAuthenticationLoginSuccessHandler extends AbstractAuthenticationSuccessHandler {

	private static final String AUTHORIZATION_HEADER = "Authorization";

	private static final String X_TOKEN_HEADER = "X-TOKEN";

	private final TokenManager tokenHelper;

	public JwtAuthenticationLoginSuccessHandler(TokenManager tokenHelper) {
		super();
		this.tokenHelper = tokenHelper;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		UsernamePasswordAuthenticationToken customToken = (UsernamePasswordAuthenticationToken) authentication;
		SecurityContextHolder.getContext().setAuthentication(customToken);

		AuthenticatedUser user = (AuthenticatedUser) customToken.getDetails();
		Tokens tokens = generateTokens(user);

		response.setHeader(AUTHORIZATION_HEADER, tokens.getJwtToken());
		response.setHeader(X_TOKEN_HEADER, tokens.getRefreshToken());
		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		getObjectMapper().writeValue(response.getWriter(), tokens);

		clearAuthenticationAttributes(request);
	}

	private Tokens generateTokens(AuthenticatedUser user) throws IOException {
		Tokens tokens = null;
		try {
			tokens = getJwtTokenUtil().generateTokens(user.getLogin(), user);
			tokenHelper.saveToken(TokenType.USER_CODE, user, tokens.getJwtToken());
			tokenHelper.saveToken(TokenType.REFRESH_TOKEN, user, tokens.getRefreshToken());
		} catch (Exception e) {
			throw new IOException("Failed to generate tokens", e);
		}
		return tokens;
	}

	/**
	 * Removes temporary authentication-related data which may have been stored in the session during the authentication process..
	 *
	 * @param request - HTTP request
	 */
	protected final void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return;
		}
		session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	}

}
