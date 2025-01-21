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
public class BooleanParameterDataValidator extends AbstractParameterDataValidator {

	public BooleanParameterDataValidator() {
		super(SwaggerType.BOOLEAN);
	}

	@Override
	protected boolean internalValidate(OpenAPI openAPI, Parameter parameter, String value) {
		boolean result = false;
		if (Boolean.TRUE.toString().equals(value) || Boolean.FALSE.toString().equals(value)) {
			result = true;
		}
		return result;
	}

}
