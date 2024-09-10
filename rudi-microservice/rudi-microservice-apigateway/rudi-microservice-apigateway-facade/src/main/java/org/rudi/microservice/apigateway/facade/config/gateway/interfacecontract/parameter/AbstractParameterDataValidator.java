/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.parameter;

import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.SwaggerType;

import io.swagger.models.parameters.SerializableParameter;

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
	public boolean validate(SerializableParameter parameter, String value) {
		boolean result = false;
		if (type == SwaggerType.lookupType(parameter.getType())) {
			result = internalValidate(parameter, value);
		}
		return result;
	}

	protected abstract boolean internalValidate(SerializableParameter parameter, String value);

}
