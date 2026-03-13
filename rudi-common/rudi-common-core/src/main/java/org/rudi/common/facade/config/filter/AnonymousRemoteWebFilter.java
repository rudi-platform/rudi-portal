/**
 * RUDI Portail
 */
package org.rudi.common.facade.config.filter;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.core.security.UserType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.authentication.AnonymousAuthenticationWebFilter;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 
 */
@Slf4j
public class AnonymousRemoteWebFilter extends AnonymousAuthenticationWebFilter {

	private final RestTemplate restTemplate;

	private Object principal;

	private List<GrantedAuthority> authorities;

	private List<String> excludeContexts;

	private List<String> includeContexts;

	private String anonymousAuthenticateUrl;

	/**
	 * Creates a filter with a principal named "anonymousUser" and the single authority "ROLE_ANONYMOUS".
	 * 
	 * @param key the key to identify tokens created by this filter
	 */
	public AnonymousRemoteWebFilter(RestTemplate restTemplate, String anonymousAuthenticateUrl,
			List<String> excludeContexts, List<String> includeContexts) {
		this(restTemplate, anonymousAuthenticateUrl, "anonymous",
				AuthorityUtils.createAuthorityList("ANONYMOUS", "USER"), excludeContexts, includeContexts);
	}

	/**
	 * @param key         key the key to identify tokens created by this filter
	 * @param principal   the principal which will be used to represent anonymous users
	 * @param authorities the authority list for anonymous users
	 */
	public AnonymousRemoteWebFilter(RestTemplate restTemplate, String anonymousAuthenticateUrl, Object principal,
			List<GrantedAuthority> authorities, List<String> excludeContexts, List<String> includeContexts) {
		super(principal.toString(), principal, authorities);
		Assert.notNull(principal, "Anonymous authentication principal must be set");
		Assert.notNull(authorities, "Anonymous authorities must be set");
		this.principal = principal;
		this.authorities = authorities;
		this.restTemplate = restTemplate;
		this.excludeContexts = excludeContexts;
		this.includeContexts = includeContexts;
		this.anonymousAuthenticateUrl = anonymousAuthenticateUrl;
		log.info("Exclude contexts : {}/ Include contexts: {}", excludeContexts, includeContexts);
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return ReactiveSecurityContextHolder.getContext().switchIfEmpty(Mono.defer(() -> {

			if (CollectionUtils.isNotEmpty(excludeContexts)
					&& excludeContexts.stream().anyMatch(exchange.getRequest().getPath().value()::matches)) {
				log.info("AnonymousWebFilter : {} exclude", exchange.getRequest().getPath());
				return chain.filter(exchange).then(Mono.empty());
			}
			if (CollectionUtils.isEmpty(includeContexts) || (CollectionUtils.isNotEmpty(includeContexts)
					&& includeContexts.stream().anyMatch(exchange.getRequest().getPath().value()::matches))) {

				// Si pas de contexte de sécurité, on crée un nouveau contexte
				Authentication authentication = createAuthentication(exchange);
				SecurityContext securityContext = new SecurityContextImpl(authentication);
				try {
					Tokens tokens = remoteAnonymousAuthentication();
					if (tokens != null) {
						exchange.getRequest().getHeaders().add(CommonSecurityConstants.HEADER_TOKEN_JWT_AUTHENT_KEY,
								tokens.getJwtToken());
						exchange.getRequest().getHeaders().add(CommonSecurityConstants.HEADER_X_TOKEN_KEY,
								tokens.getRefreshToken());
					}
				} catch (Exception e) {
					log.warn("Failed to generate tokens", e);
				}
				log.debug("Populated SecurityContext with anonymous token: {}", authentication);
				return chain.filter(exchange)
						.contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
						.then(Mono.empty());
			} else {
				log.info("AnonymousWebFilter : {} not included", exchange.getRequest().getPath());
				return chain.filter(exchange).then(Mono.empty());
			}
		})).flatMap(securityContext -> {
			log.info("SecurityContext already contains token: {}", securityContext.getAuthentication());
			return chain.filter(exchange);
		});
	}

	protected Tokens remoteAnonymousAuthentication() {
		ResponseEntity<Tokens> tokens = restTemplate.postForEntity(anonymousAuthenticateUrl, null, Tokens.class);
		if (tokens.getStatusCode() == HttpStatus.OK) {
			return tokens.getBody();
		} else {
			// On considère que le token est invalide
			log.warn("Remote anonymous anthentication failed {}", tokens.getStatusCode());
			return null;
		}
	}

	/**
	 * 
	 * @param exchange le webexchange en cours
	 * @return
	 */
	@Override
	protected Authentication createAuthentication(ServerWebExchange exchange) {
		AuthenticatedUser user = createAuthenticatedUser();
		// Application des authorités
		final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				user.getLogin(), user.getLogin(), authorities);
		usernamePasswordAuthenticationToken.setDetails(user);
		return usernamePasswordAuthenticationToken;

	}

	protected AuthenticatedUser createAuthenticatedUser() {
		AuthenticatedUser user = new AuthenticatedUser(principal.toString(), UserType.PERSON);
		user.setRoles(authorities.stream().map(GrantedAuthority::getAuthority).toList());
		return user;
	}
}