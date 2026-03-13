/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security;

import org.rudi.common.service.util.ApplicationContext;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtTokenUtil;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 
 */
@RequiredArgsConstructor
public abstract class AbstractAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private JwtTokenUtil jwtTokenUtil;

	@Getter(value = AccessLevel.PROTECTED)
	private ObjectMapper objectMapper = new ObjectMapper();

	protected JwtTokenUtil getJwtTokenUtil() {
		if (jwtTokenUtil == null) {
			jwtTokenUtil = ApplicationContext.getBean(JwtTokenUtil.class);
		}
		return jwtTokenUtil;
	}
}
