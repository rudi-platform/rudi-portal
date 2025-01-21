/**
 * 
 */
package org.rudi.microservice.registry.facade.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author FNI18300
 *
 */
@Configuration
@EnableWebSecurity
class WebSecurityConfig {

	private static final String EUREKA_URL = "/eureka/**";

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.ignoringRequestMatchers(EUREKA_URL));

		return http.build();
	}

}