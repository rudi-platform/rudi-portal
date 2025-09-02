/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.anonymous;

import java.io.IOException;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 *
 */
@Slf4j
public class AnonymousAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

	private String loginAnonymous;

	/**
	 *
	 * Constructor
	 *
	 * @param successHandler - Success Handler
	 * @param failureHandler - Failure Handler
	 */
	public AnonymousAuthenticationProcessingFilter(String loginAnonymous,
			final AuthenticationSuccessHandler successHandler, final AuthenticationFailureHandler failureHandler,
			AuthenticationManager manager) {
		super(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/anonymous"));
		setAuthenticationSuccessHandler(successHandler);
		setAuthenticationFailureHandler(failureHandler);
		setAuthenticationManager(manager);
		this.loginAnonymous = loginAnonymous;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (!HttpMethod.POST.name().equals(request.getMethod())) {
			if (log.isDebugEnabled()) {
				log.debug("Authentication method not supported. Request method: {}", request.getMethod());
			}
			throw new AuthenticationCredentialsNotFoundException("Authentication method not supported");
		}

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginAnonymous,
				loginAnonymous, null);
		log.debug("login {} founded : try to authenticate", token);
		return getAuthenticationManager().authenticate(token);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		SecurityContextHolder.clearContext();
		getFailureHandler().onAuthenticationFailure(request, response, failed);
	}

}
