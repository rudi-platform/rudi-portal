/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.security;

import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLException;

import org.rudi.common.core.webclient.HttpClientHelper;
import org.rudi.common.facade.config.filter.AbstractJwtTokenUtil;
import org.rudi.common.facade.config.filter.AnonymousWebFilter;
import org.rudi.microservice.apigateway.facade.config.gateway.exception.GenericErrorWebExceptionHandler;
import org.rudi.microservice.apigateway.facade.config.security.jwt.JwtWebFilter;
import org.rudi.microservice.apigateway.facade.config.security.oauth2.OAuth2WebFilter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.AuthenticatedPrincipalServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.WebFilter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;

/**
 * @author FNI18300
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig {

	private static final List<String> SB_INCLUDE_URLS = List.of("\\/medias\\/.*", "\\/apigateway\\/datasets\\/.*");

	@Value("${application.role.administrateur.code}")
	private String administrateurRoleCode;

	@Value("${module.oauth2.check-token-uri}")
	private String checkTokenUri;

	@Getter
	@Value("${module.oauth2.client-id}")
	private String clientId;

	@Getter
	@Value("${module.oauth2.client-secret}")
	private String clientSecret;

	@Getter
	@Value("${module.oauth2.provider-uri}")
	private String tokenUri;

	@Value("${module.oauth2.scope}")
	private String[] scopes;

	@Value("${rudi.apigateway.security.authentication.disabled:false}")
	private boolean disableAuthentification = false;

	private final RestTemplate internalRestTemplate;

	private final HttpClientHelper httpClientHelper;

	@Qualifier("JwtTokenUtilApiGateway")
	@Autowired
	private AbstractJwtTokenUtil jwtTokenUtil;

	@Bean
	public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
		http.authorizeExchange(exchanges -> {
			exchanges.matchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class)).permitAll();
			if (!disableAuthentification) {
				exchanges.pathMatchers(SecurityConstants.SB_PERMIT_ALL_URL).permitAll();
				exchanges.pathMatchers(SecurityConstants.ACTUATOR_URL).authenticated();
				exchanges.anyExchange().authenticated();
			} else {
				log.warn("/!\\ Authentification is disabled");
				exchanges.anyExchange().permitAll();
			}
		});

		http.httpBasic(Customizer.withDefaults()).formLogin(Customizer.withDefaults())
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.anonymous(anonymous -> anonymous
						.authenticationFilter(new AnonymousWebFilter(jwtTokenUtil, null, SB_INCLUDE_URLS)))
				.exceptionHandling(e -> e.authenticationEntryPoint(new HttpBearerServerAuthenticationEntryPoint()));

		if (!disableAuthentification) {
			http.addFilterBefore(createOAuth2Filter(), SecurityWebFiltersOrder.AUTHENTICATION)
					.addFilterBefore(createJwtRequestFilter(), SecurityWebFiltersOrder.AUTHENTICATION);
		}
		return http.build();
	}

	@Bean(name = "rudi_oauth2_repository")
	public ReactiveClientRegistrationRepository getRegistration() {
		ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(SecurityConstants.REGISTRATION_ID)
				.tokenUri(tokenUri).clientId(clientId).clientSecret(clientSecret)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS).scope(scopes).build();
		return new InMemoryReactiveClientRegistrationRepository(clientRegistration);
	}

	@Bean(name = "rudi_oauth2_client_service")
	public ReactiveOAuth2AuthorizedClientService clientService(
			@Qualifier("rudi_oauth2_repository") ReactiveClientRegistrationRepository clientRegistrationRepository) {
		return new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
	}

	@Bean(name = "rudi_oauth2_client_repository")
	public ServerOAuth2AuthorizedClientRepository clientRepository(
			@Qualifier("rudi_oauth2_client_service") ReactiveOAuth2AuthorizedClientService clientService) {
		return new AuthenticatedPrincipalServerOAuth2AuthorizedClientRepository(clientService);
	}

	@Bean(defaultCandidate = true)
	public HttpClient httpClient() throws SSLException {
		return httpClientHelper.createReactorHttpClient(true, false, false);
	}

	@Bean
	protected CorsConfigurationSource corsConfigurationSource() {
		final CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "PUT", "DELETE"));
		configuration.addAllowedHeader("*");
		configuration.addExposedHeader("Authorization");
		configuration.addExposedHeader("X-TOKEN");
		configuration.setAllowCredentials(true);

		// Url autorisées
		// 4200 pour les développement | 8080 pour le déploiement
		configuration.setAllowedOriginPatterns(Arrays.asList("*"));

		final var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	protected GrantedAuthorityDefaults grantedAuthorityDefaults() {
		// Remove the ROLE_ prefix
		return new GrantedAuthorityDefaults("");
	}

	public WebFilter createJwtRequestFilter() {
		return new JwtWebFilter(SecurityConstants.SB_PERMIT_ALL_URL, jwtTokenUtil);
	}

	private WebFilter createOAuth2Filter() {
		return new OAuth2WebFilter(SecurityConstants.SB_PERMIT_ALL_URL, checkTokenUri, internalRestTemplate);
	}

	@Bean
	@Order(-2)
	public ErrorWebExceptionHandler errorWebExceptionHandler(ErrorAttributes errorAttributes,
			WebProperties webProperties, ServerProperties serverProperties,
			ObjectProvider<org.springframework.web.reactive.result.view.ViewResolver> viewResolvers,
			ServerCodecConfigurer serverCodecConfigurer, ApplicationContext applicationContext) {
		GenericErrorWebExceptionHandler exceptionHandler = new GenericErrorWebExceptionHandler(errorAttributes,
				webProperties.getResources(), serverProperties.getError(), applicationContext);
		exceptionHandler.setViewResolvers(viewResolvers.orderedStream().toList());
		exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
		exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
		return exceptionHandler;
	}
}
