/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.parameter;

import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.SwaggerType;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;

/**
 * @author FNI18300
 *
 */
public class IntegerParameterDataValidator extends AbstractParameterDataValidator {

	public IntegerParameterDataValidator() {
		super(SwaggerType.INTEGER);
	}

	@Override
	protected boolean internalValidate(OpenAPI openapi, Parameter parameter, String value) {
		boolean result = false;
		try {
			Long.parseLong(value);
		} catch (Exception e) {
			// rien
		}
		return result;
	}

}
