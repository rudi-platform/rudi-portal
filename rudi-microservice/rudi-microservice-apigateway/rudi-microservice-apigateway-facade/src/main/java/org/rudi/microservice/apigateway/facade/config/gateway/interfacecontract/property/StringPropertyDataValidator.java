/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.property;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.SwaggerFormat;
import org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.SwaggerType;

import io.swagger.models.properties.Property;

/**
 * @author FNI18300
 *
 */
public class StringPropertyDataValidator extends AbstractPropertyDataValidator {

	public StringPropertyDataValidator() {
		super(SwaggerType.STRING);
	}

	@Override
	protected boolean internalValidate(Property property, String value) {
		boolean result = false;
		SwaggerFormat format = SwaggerFormat.lookupType(property.getFormat());
		switch (format) {
		case UUID:
			try {
				UUID.fromString(value);
				result = true;
			} catch (Exception e) {
				// rien
			}
			break;
		case DATE_TIME:
			try {
				LocalDateTime.parse(value);
				result = true;
			} catch (Exception e) {
				// rien
			}
			break;
		case DATE:
			try {
				LocalDate.parse(value);
				result = true;
			} catch (Exception e) {
				// rien
			}
			break;
		default:
			result = true;
			break;
		}
		return result;
	}

}
