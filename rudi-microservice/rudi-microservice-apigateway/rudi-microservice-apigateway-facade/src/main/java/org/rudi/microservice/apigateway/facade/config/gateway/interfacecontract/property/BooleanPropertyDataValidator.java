/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.property;

import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.SwaggerType;

import io.swagger.models.properties.Property;

/**
 * @author FNI18300
 *
 */
public class BooleanPropertyDataValidator extends AbstractPropertyDataValidator {

	public BooleanPropertyDataValidator() {
		super(SwaggerType.BOOLEAN);
	}

	@Override
	protected boolean internalValidate(Property property, String value) {
		boolean result = false;
		if (Boolean.TRUE.toString().equals(value) || Boolean.FALSE.toString().equals(value)) {
			result = true;
		}
		return result;
	}

}
