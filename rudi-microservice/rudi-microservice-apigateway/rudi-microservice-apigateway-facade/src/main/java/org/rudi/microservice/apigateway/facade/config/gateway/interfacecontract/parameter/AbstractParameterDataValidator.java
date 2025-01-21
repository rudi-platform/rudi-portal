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
public abstract class AbstractParameterDataValidator implements ParameterDataValidator {

	private SwaggerType type;

	protected AbstractParameterDataValidator(SwaggerType type) {
		this.type = type;
	}

	@Override
	public boolean validate(OpenAPI openAPI, Parameter parameter, String value) {
		boolean result = false;
		if (type == SwaggerType.lookupType(parameter.getSchema().getType())) {
			result = internalValidate(openAPI, parameter, value);
		}
		return result;
	}

	protected abstract boolean internalValidate(OpenAPI openAPI, Parameter parameter, String value);

}
