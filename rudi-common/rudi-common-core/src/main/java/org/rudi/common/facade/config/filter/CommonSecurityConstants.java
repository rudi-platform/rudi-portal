package org.rudi.common.facade.config.filter;

import org.springframework.http.HttpStatusCode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author FNI18300
 *
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonSecurityConstants {

	/**
	 * Le code d'erreur à renvoyer au front quand un token d'authent a expiré
	 */
	public static final int HTTP_CODE_TOKEN_EXPIRED = 498;

	public static final HttpStatusCode HTTP_TOKEN_EXPIRED = HttpStatusCode.valueOf(HTTP_CODE_TOKEN_EXPIRED);

	public static final String HEADER_TOKEN_JWT_AUTHENT_KEY = "Authorization";

	public static final String HEADER_X_TOKEN_KEY = "X-TOKEN";

	public static final String HEADER_TOKEN_JWT_PREFIX = "Bearer ";

}
