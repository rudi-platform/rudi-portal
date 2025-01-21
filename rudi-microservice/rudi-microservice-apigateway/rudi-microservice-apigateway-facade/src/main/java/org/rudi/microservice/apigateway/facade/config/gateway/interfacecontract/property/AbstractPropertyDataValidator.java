/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.property;

import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.SwaggerType;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

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
	public boolean validate(OpenAPI openAPI, Schema<?> property, String value) {
		boolean result = false;
		if (type == SwaggerType.lookupType(property.getType())) {
			result = internalValidate(openAPI, property, value);
		}
		return result;
	}

	protected abstract boolean internalValidate(OpenAPI openAPI, Schema<?> property, String value);

}
