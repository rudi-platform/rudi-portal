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
public class BooleanParameterDataValidator extends AbstractParameterDataValidator {

	public BooleanParameterDataValidator() {
		super(SwaggerType.BOOLEAN);
	}

	@Override
	protected boolean internalValidate(SerializableParameter parameter, String value) {
		boolean result = false;
		if (Boolean.TRUE.toString().equals(value) || Boolean.FALSE.toString().equals(value)) {
			result = true;
		}
		return result;
	}

}
