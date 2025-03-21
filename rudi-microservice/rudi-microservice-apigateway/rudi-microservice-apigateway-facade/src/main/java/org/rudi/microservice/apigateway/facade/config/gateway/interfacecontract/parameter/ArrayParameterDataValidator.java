/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.parameter;

import java.util.EnumMap;
import java.util.Map;

import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.SwaggerType;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.property.ArrayPropertyDataValidator;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.property.BooleanPropertyDataValidator;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.property.IntegerPropertyDataValidator;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.property.NumberPropertyDataValidator;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.property.PropertyDataValidator;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.property.StringPropertyDataValidator;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

/**
 * @author FNI18300
 *
 */
public class ArrayParameterDataValidator extends AbstractParameterDataValidator {

	private static final Map<SwaggerType, PropertyDataValidator> DATA_VALIDATORS = new EnumMap<>(SwaggerType.class);

	public ArrayParameterDataValidator() {
		super(SwaggerType.ARRAY);
	}

	@Override
	protected boolean internalValidate(OpenAPI openAPI, Parameter parameter, String arrayValue) {
		boolean result = false;
		Schema property = parameter.getSchema().getItems();
		String[] values = arrayValue.split(",");
		SwaggerType type = SwaggerType.lookupType(property.getType());
		PropertyDataValidator parameterDataValidator = DATA_VALIDATORS.get(type);
		if (parameterDataValidator != null) {
			for (String value : values) {
				result &= parameterDataValidator.validate(openAPI, property, value);
			}
		} else {
			// cas non prévu on bloque pas
			result = true;
		}
		return result;
	}

	static {
		DATA_VALIDATORS.put(SwaggerType.STRING, new StringPropertyDataValidator());
		DATA_VALIDATORS.put(SwaggerType.BOOLEAN, new BooleanPropertyDataValidator());
		DATA_VALIDATORS.put(SwaggerType.NUMBER, new NumberPropertyDataValidator());
		DATA_VALIDATORS.put(SwaggerType.INTEGER, new IntegerPropertyDataValidator());
		DATA_VALIDATORS.put(SwaggerType.ARRAY, new ArrayPropertyDataValidator(DATA_VALIDATORS));
	}

}
