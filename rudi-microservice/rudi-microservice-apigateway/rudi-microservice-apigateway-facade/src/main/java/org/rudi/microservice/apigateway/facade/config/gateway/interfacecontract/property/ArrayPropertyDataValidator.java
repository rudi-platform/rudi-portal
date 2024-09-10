/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.property;

import java.util.Map;

import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.SwaggerType;

import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;

/**
 * @author FNI18300
 *
 */
public class ArrayPropertyDataValidator extends AbstractPropertyDataValidator {

	private Map<SwaggerType, PropertyDataValidator> validators;

	public ArrayPropertyDataValidator(Map<SwaggerType, PropertyDataValidator> validators) {
		super(SwaggerType.ARRAY);
		this.validators = validators;
	}

	@Override
	protected boolean internalValidate(Property property, String arrayValue) {
		boolean result = false;
		Property subProperty = ((ArrayProperty) property).getItems();
		String[] values = arrayValue.split(",");
		SwaggerType type = SwaggerType.lookupType(property.getType());
		PropertyDataValidator parameterDataValidator = validators.get(type);
		if (parameterDataValidator != null) {
			for (String value : values) {
				result &= parameterDataValidator.validate(subProperty, value);
			}
		} else {
			// cas non prévu on bloque pas
			result = true;
		}
		return result;
	}

}
