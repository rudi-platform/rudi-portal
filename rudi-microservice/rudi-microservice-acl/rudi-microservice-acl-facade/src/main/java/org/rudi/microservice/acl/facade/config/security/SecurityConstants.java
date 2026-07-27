/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security;

import org.bouncycastle.util.Arrays;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author FNI18300
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityConstants {

	public static final String AUTHENTICATE_URL = "/authenticate";

	public static final String AUTHENTICATE1_URL = "/authenticate1";

	public static final String ANONYMOUS_URL = "/anonymous";

	public static final String CHECK_CREDENTIAL_URL = "/check_credential";

	public static final String ACTUATOR_URL = "/actuator/**";

	public static final String LOGOUT_URL = "/acl/v1/account/logout";

	public static final String OAUTH2_AUTHENTICATORS_URL = "/acl/v1/oauth2-authenticators/**";

	protected static final String[] AUTHENTICATION_PERMIT_URL = { AUTHENTICATE_URL, CHECK_CREDENTIAL_URL };

	// En autorisant une URL ici, il faut l'autoriser dans le WebSecuConfig de la gateway pour les appels front
	protected static final String[] SB_PERMIT_ALL_URL = {
			// URL public
			"/acl/v1/application-information", "/acl/v1/healthCheck", "/acl/v1/oauth2-authenticators",
			/* "/acl/v1/oauth2-authenticators/**", */ "/acl/v1/oauth2-authenticators/icons/**", "/oauth2/*token*",
			"/oauth2/logout", "/oauth2/jwks", AUTHENTICATE_URL, AUTHENTICATE1_URL,
			// swagger ui / openapi
			"/acl/v3/api-docs/**", "/acl/swagger-ui/**", "/acl/swagger-ui.html", "/acl/swagger-resources/**",
			"/configuration/ui", "/configuration/security", "/webjars/**", "/error", "/acl/v1/kaptcha/**" };

	protected static final String[] SB_PERMIT_ALL_URL2 = Arrays.append(SB_PERMIT_ALL_URL, OAUTH2_AUTHENTICATORS_URL);
}
