/**
 * RUDI Portail
 */
package org.rudi.common.facade.gateway.config.exception;

import org.rudi.common.facade.config.filter.CommonSecurityConstants;
import org.springframework.security.core.AuthenticationException;

import lombok.Getter;

/**
 * @author FNI18300
 */
public class ExpiredAuthenticationException extends AuthenticationException {

	private static final long serialVersionUID = -3010535079958085888L;

	@Getter
	private final int errorCode;

	public ExpiredAuthenticationException() {
		this("Token expired", CommonSecurityConstants.HTTP_CODE_TOKEN_EXPIRED, null);
	}

	public ExpiredAuthenticationException(String message, int errorCode, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public ExpiredAuthenticationException(String message, int errorCode) {
		this(message, errorCode, null);
	}

}
