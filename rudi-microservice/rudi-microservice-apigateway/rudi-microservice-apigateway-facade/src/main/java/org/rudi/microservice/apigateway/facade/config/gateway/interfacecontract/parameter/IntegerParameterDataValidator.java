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
public class IntegerParameterDataValidator extends AbstractParameterDataValidator {

	public IntegerParameterDataValidator() {
		super(SwaggerType.INTEGER);
	}

	@Override
	protected boolean internalValidate(SerializableParameter parameter, String value) {
		boolean result = false;
		try {
			Long.parseLong(value);
		} catch (Exception e) {
			// rien
		}
		return result;
	}

}
