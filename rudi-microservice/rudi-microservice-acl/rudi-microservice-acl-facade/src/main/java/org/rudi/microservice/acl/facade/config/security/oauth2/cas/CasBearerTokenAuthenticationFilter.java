/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.oauth2.cas;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Override of spring behavior to set authenticationManagerResolver
 * 
 * @see org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
 */
public class CasBearerTokenAuthenticationFilter extends BearerTokenAuthenticationFilter {

	private final AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver;

	private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
			.getContextHolderStrategy();

	private AuthenticationEntryPoint authenticationEntryPoint = new BearerTokenAuthenticationEntryPoint();

	private AuthenticationFailureHandler authenticationFailureHandler = new AuthenticationEntryPointFailureHandler(
			(request, response, exception) -> this.authenticationEntryPoint.commence(request, response, exception));

	private BearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();

	private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();

	private SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

	/**
	 * Construct a {@code CasBearerTokenAuthenticationFilter} using the provided parameter(s)
	 *
	 * @param authenticationManagerResolver resolver used to select the appropriate {@link AuthenticationManager}
	 *                                      for the current HTTP request context
	 */
	public CasBearerTokenAuthenticationFilter(
			AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver/* , JwtDecoder jwtDecoder */) {
		super(authenticationManagerResolver);
		this.authenticationManagerResolver = authenticationManagerResolver;
	}

	/**
	 * Extract any <a href="https://tools.ietf.org/html/rfc6750#section-1.2" target="_blank">Bearer Token</a> from the request and attempt an
	 * authentication.
	 *
	 * @param request current HTTP request from which the bearer token is extracted
	 * @param response current HTTP response used to return authentication errors when needed
	 * @param filterChain chain used to continue request processing after this filter
	 * @throws ServletException if the filter chain fails while processing the request
	 * @throws IOException if an I/O error occurs while delegating to the chain or entry point
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (!tokenHasNotAlreadyBeenChecked(response)) {
			filterChain.doFilter(request, response);
			return;
		}

		String token;
		try {
			token = this.bearerTokenResolver.resolve(request);
		} catch (OAuth2AuthenticationException invalid) {
			this.logger.trace("Sending to authentication entry point since failed to resolve bearer token", invalid);
			this.authenticationEntryPoint.commence(request, response, invalid);
			return;
		}

		if (token == null) {
			this.logger.trace("Did not process request since did not find bearer token");
			filterChain.doFilter(request, response);
			return;
		}

		BearerTokenAuthenticationToken authenticationRequest = new BearerTokenAuthenticationToken(token);
		authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));

		try {
			AuthenticationManager authenticationManager = this.authenticationManagerResolver.resolve(request);
			Authentication authenticationResult = authenticationManager.authenticate(authenticationRequest);
			if (isDPoPBoundAccessToken(authenticationResult)) {
				// Prevent downgraded usage of DPoP-bound access tokens,
				// by rejecting a DPoP-bound access token received as a bearer token.
				BearerTokenError error = BearerTokenErrors.invalidToken("Invalid bearer token");
				throw new OAuth2AuthenticationException(error);
			}
			SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
			context.setAuthentication(authenticationResult);
			this.securityContextHolderStrategy.setContext(context);
			this.securityContextRepository.saveContext(context, request, response);
			if (this.logger.isDebugEnabled()) {
				this.logger.debug(LogMessage.format("Set SecurityContextHolder to %s", authenticationResult));
			}
			filterChain.doFilter(request, response);
		} catch (InvalidBearerTokenException e) {
			this.logger.trace("Failed to process authentication request. Continue");
			filterChain.doFilter(request, response);
		} catch (AuthenticationException failed) {
			this.logger.trace("Failed to process authentication request.", failed);
			this.securityContextHolderStrategy.clearContext();
			this.authenticationFailureHandler.onAuthenticationFailure(request, response, failed);
		}
	}

	/**
	 * Sets the {@link SecurityContextHolderStrategy} to use. The default action is to use the {@link SecurityContextHolderStrategy} stored in
	 * {@link SecurityContextHolder}.
	 *
	 * @param securityContextHolderStrategy strategy used to create, store and clear the {@link SecurityContext}
	 *                                      for the current execution thread
	 * @since 5.8
	 */
	@Override
	public void setSecurityContextHolderStrategy(SecurityContextHolderStrategy securityContextHolderStrategy) {
		Assert.notNull(securityContextHolderStrategy, "securityContextHolderStrategy cannot be null");
		this.securityContextHolderStrategy = securityContextHolderStrategy;
	}

	/**
	 * Sets the {@link SecurityContextRepository} to save the {@link SecurityContext} on authentication success. The default action is not to save the
	 * {@link SecurityContext}.
	 * 
	 * @param securityContextRepository the {@link SecurityContextRepository} to use. Cannot be null.
	 */
	@Override
	public void setSecurityContextRepository(SecurityContextRepository securityContextRepository) {
		Assert.notNull(securityContextRepository, "securityContextRepository cannot be null");
		this.securityContextRepository = securityContextRepository;
	}

	/**
	 * Set the {@link BearerTokenResolver} to use. Defaults to {@link DefaultBearerTokenResolver}.
	 * 
	 * @param bearerTokenResolver the {@code BearerTokenResolver} to use
	 */
	@Override
	public void setBearerTokenResolver(BearerTokenResolver bearerTokenResolver) {
		Assert.notNull(bearerTokenResolver, "bearerTokenResolver cannot be null");
		this.bearerTokenResolver = bearerTokenResolver;
	}

	/**
	 * Set the {@link AuthenticationEntryPoint} to use. Defaults to {@link BearerTokenAuthenticationEntryPoint}.
	 * 
	 * @param authenticationEntryPoint the {@code AuthenticationEntryPoint} to use
	 */
	@Override
	public void setAuthenticationEntryPoint(final AuthenticationEntryPoint authenticationEntryPoint) {
		Assert.notNull(authenticationEntryPoint, "authenticationEntryPoint cannot be null");
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	/**
	 * Set the {@link AuthenticationFailureHandler} to use. Default implementation invokes {@link AuthenticationEntryPoint}.
	 * 
	 * @param authenticationFailureHandler the {@code AuthenticationFailureHandler} to use
	 * @since 5.2
	 */
	@Override
	public void setAuthenticationFailureHandler(final AuthenticationFailureHandler authenticationFailureHandler) {
		Assert.notNull(authenticationFailureHandler, "authenticationFailureHandler cannot be null");
		this.authenticationFailureHandler = authenticationFailureHandler;
	}

	/**
	 * Set the {@link AuthenticationDetailsSource} to use. Defaults to {@link WebAuthenticationDetailsSource}.
	 * 
	 * @param authenticationDetailsSource the {@code AuthenticationConverter} to use
	 * @since 5.5
	 */
	@Override
	public void setAuthenticationDetailsSource(
			AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
		Assert.notNull(authenticationDetailsSource, "authenticationDetailsSource cannot be null");
		this.authenticationDetailsSource = authenticationDetailsSource;
	}

	/**
	 * Checks whether the authenticated token contains a DPoP confirmation (`cnf.jkt`) claim.
	 *
	 * @param authentication authentication result to inspect
	 * @return {@code true} when the access token is bound to a DPoP proof key, otherwise {@code false}
	 */
	private static boolean isDPoPBoundAccessToken(Authentication authentication) {
		if (!(authentication instanceof AbstractOAuth2TokenAuthenticationToken<?> accessTokenAuthentication)) {
			return false;
		}
		ClaimAccessor accessTokenClaims = accessTokenAuthentication::getTokenAttributes;
		String jwkThumbprintClaim = null;
		Map<String, Object> confirmationMethodClaim = accessTokenClaims.getClaimAsMap("cnf");
		if (!CollectionUtils.isEmpty(confirmationMethodClaim) && confirmationMethodClaim.containsKey("jkt")) {
			jwkThumbprintClaim = (String) confirmationMethodClaim.get("jkt");
		}
		return StringUtils.hasText(jwkThumbprintClaim);
	}

	/**
	 * Indicates if bearer token validation should run for the current response/context state.
	 *
	 * @param response HTTP response used to detect a previous unauthorized status
	 * @return {@code true} if no authentication is present or if the current status is {@code 401},
	 *         meaning token validation may be attempted
	 */
	protected boolean tokenHasNotAlreadyBeenChecked(HttpServletResponse response) {
		return response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED
				|| SecurityContextHolder.getContext().getAuthentication() == null;
	}
}
