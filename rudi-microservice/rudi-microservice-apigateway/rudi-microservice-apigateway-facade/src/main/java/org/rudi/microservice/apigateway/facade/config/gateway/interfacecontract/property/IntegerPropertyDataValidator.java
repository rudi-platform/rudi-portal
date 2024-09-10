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
public class IntegerPropertyDataValidator extends AbstractPropertyDataValidator {

	public IntegerPropertyDataValidator() {
		super(SwaggerType.INTEGER);
	}

	@Override
	protected boolean internalValidate(Property property, String value) {
		boolean result = false;
		try {
			Long.parseLong(value);
		} catch (Exception e) {
			// rien
		}
		return result;
	}

}
