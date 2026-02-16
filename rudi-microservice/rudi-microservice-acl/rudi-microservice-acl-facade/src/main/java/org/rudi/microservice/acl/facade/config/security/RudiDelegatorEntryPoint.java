/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rudi.microservice.acl.facade.config.security.jwt.JwtAuthenticationEntryPoint;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 
 */
@Component
@RequiredArgsConstructor
public class RudiDelegatorEntryPoint implements AuthenticationEntryPoint {

	private static final Pattern AUTHENTICATOR_PATTERN = Pattern
			.compile("/acl/v1/oauth2-authenticators/([a-zA-Z0-9]+)/*");

	private static final String LOGIN = "login";

	private LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint;

	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		String uri = request.getRequestURI();
		Matcher matcher = AUTHENTICATOR_PATTERN.matcher(uri);
		String action = request.getParameter("action");
		if (matcher.matches() && LOGIN.equals(action)) {
			String target = matcher.group(1);
			loginUrlAuthenticationEntryPoint = new LoginUrlAuthenticationEntryPoint("/oauth2/authorization/" + target);
			loginUrlAuthenticationEntryPoint.commence(request, response, authException);
		} else {
			jwtAuthenticationEntryPoint.commence(request, response, authException);
		}
	}
}
