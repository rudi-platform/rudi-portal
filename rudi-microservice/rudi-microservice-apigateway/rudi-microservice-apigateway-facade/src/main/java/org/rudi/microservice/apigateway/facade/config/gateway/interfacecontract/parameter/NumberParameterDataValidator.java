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
public class NumberParameterDataValidator extends AbstractParameterDataValidator {

	public NumberParameterDataValidator() {
		super(SwaggerType.NUMBER);
	}

	@Override
	protected boolean internalValidate(OpenAPI openapi, Parameter parameter, String value) {
		boolean result = false;
		try {
			Double.parseDouble(value);
		} catch (Exception e) {
			// rien
		}
		return result;
	}

}
