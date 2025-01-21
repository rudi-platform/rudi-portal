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
public class BooleanPropertyDataValidator extends AbstractPropertyDataValidator {

	public BooleanPropertyDataValidator() {
		super(SwaggerType.BOOLEAN);
	}

	@Override
	protected boolean internalValidate(OpenAPI openapi, Schema<?> property, String value) {
		boolean result = false;
		if (Boolean.TRUE.toString().equals(value) || Boolean.FALSE.toString().equals(value)) {
			result = true;
		}
		return result;
	}

}
