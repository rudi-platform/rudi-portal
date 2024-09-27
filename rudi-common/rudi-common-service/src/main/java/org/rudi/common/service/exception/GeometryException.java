/**
 * RUDI Portail
 */
package org.rudi.common.service.exception;

/**
 * @author FNI18300
 *
 */
public class GeometryException extends AppServiceException {

	private static final long serialVersionUID = 4237843540707672236L;

	public GeometryException(String message) {
		super(message, AppServiceExceptionsStatus.BAD_REQUEST);
	}

	public GeometryException(String message, Throwable cause) {
		super(message, cause, AppServiceExceptionsStatus.BAD_REQUEST);
	}
}
