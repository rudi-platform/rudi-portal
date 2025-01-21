/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.parameter;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;

/**
 * @author FNI18300
 *
 */

public interface ParameterDataValidator {
	boolean validate(OpenAPI openAPI, Parameter parameter, String value);
}
