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
public abstract class AbstractPropertyDataValidator implements PropertyDataValidator {

	private SwaggerType type;

	protected AbstractPropertyDataValidator(SwaggerType type) {
		this.type = type;
	}

	@Override
	public boolean validate(Property property, String value) {
		boolean result = false;
		if (type == SwaggerType.lookupType(property.getType())) {
			result = internalValidate(property, value);
		}
		return result;
	}

	protected abstract boolean internalValidate(Property property, String value);

}
