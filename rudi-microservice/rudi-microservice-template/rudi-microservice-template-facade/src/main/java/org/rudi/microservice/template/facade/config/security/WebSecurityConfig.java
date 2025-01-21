package org.rudi.microservice.template.facade.config.security;

import java.util.Arrays;

import jakarta.servlet.Filter;
import org.rudi.common.facade.config.filter.JwtRequestFilter;
import org.rudi.common.facade.config.filter.OAuth2RequestFilter;
import org.rudi.common.facade.config.filter.PreAuthenticationFilter;
import org.rudi.common.service.helper.UtilContextHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig {

	private static final String ACTUATOR_URL = "/actuator/**";

	private static final String[] SB_PERMIT_ALL_URL = {
			// URL public
			"/template/v1/application-information", "/template/v1/healthCheck",
			// swagger ui / openapi
			"/template/v3/api-docs/**", "/template/swagger-ui/**", "/template/swagger-ui.html",
			"/template/swagger-resources/**", "/configuration/ui", "/configuration/security", "/webjars/**" };

	@Value("${application.role.administrateur.code}")
	private String administrateurRoleCode;

	@Value("${module.oauth2.check-token-uri}")
	private String checkTokenUri;

	@Value("${rudi.template.security.authentication.disabled:false}")
	private boolean disableAuthentification = false;

	@Value("${rudi.template.security.pre-authentication.disabled:false}")
	private boolean disablePreAuthentification = false;

	private final UtilContextHelper utilContextHelper;
	private final RestTemplate oAuth2RestTemplate;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		if (!disableAuthentification) {
			http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(AbstractHttpConfigurer::disable)
					.authorizeHttpRequests(authorizeHttpReq -> {
						// starts authorizing configurations
						authorizeHttpReq.requestMatchers(SB_PERMIT_ALL_URL).permitAll();
						// autorisatio des actuators aux seuls role admin
						authorizeHttpReq.requestMatchers(ACTUATOR_URL).hasRole(administrateurRoleCode);
						// authenticate all remaining URLS
						authorizeHttpReq.anyRequest().fullyAuthenticated();
					}).exceptionHandling(exception -> exception.configure(http))
					// installation du filtre de type header
					.addFilterBefore(createOAuth2Filter(), UsernamePasswordAuthenticationFilter.class)
					.addFilterBefore(createJwtRequestFilter(), UsernamePasswordAuthenticationFilter.class)
					// configuring the session on the server
					.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

			if (!disablePreAuthentification) {
				log.warn("Template pre-authentication is enabled");
				http.addFilterAfter(createPreAuthenticationFilter(), BasicAuthenticationFilter.class);
			}
		} else {
			http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(AbstractHttpConfigurer::disable)
					.authorizeHttpRequests(authorizeHttpReq -> authorizeHttpReq.anyRequest().permitAll());
		}
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		final var configuration = new CorsConfiguration();
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
	GrantedAuthorityDefaults grantedAuthorityDefaults() {
		// Remove the ROLE_ prefix
		return new GrantedAuthorityDefaults("");
	}

	@Bean
	public JwtRequestFilter createJwtRequestFilter() {
		return new JwtRequestFilter(SB_PERMIT_ALL_URL, utilContextHelper, oAuth2RestTemplate);
	}

	private Filter createOAuth2Filter() {
		return new OAuth2RequestFilter(SB_PERMIT_ALL_URL, checkTokenUri, utilContextHelper, oAuth2RestTemplate);
	}

	private Filter createPreAuthenticationFilter() {
		return new PreAuthenticationFilter();
	}
}
