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
public class NumberPropertyDataValidator extends AbstractPropertyDataValidator {

	public NumberPropertyDataValidator() {
		super(SwaggerType.NUMBER);
	}

	@Override
	protected boolean internalValidate(OpenAPI openapi, Schema<?> property, String value) {
		boolean result = false;
		try {
			Double.parseDouble(value);
		} catch (Exception e) {
			// rien
		}
		return result;
	}

}
