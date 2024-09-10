/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.core.common;

import lombok.Builder;
import lombok.Data;

/**
 * @author FNI18300
 *
 */
@Data
@Builder
public class ParameterInformation {

	private String parameterName;

	private boolean valid;

	private String message;
}
