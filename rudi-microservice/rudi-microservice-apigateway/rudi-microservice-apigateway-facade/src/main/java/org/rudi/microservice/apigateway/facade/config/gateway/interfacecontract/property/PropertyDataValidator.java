/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.property;

import io.swagger.models.properties.Property;

/**
 * @author FNI18300
 *
 */
public interface PropertyDataValidator {

	boolean validate(Property property, String value);
}
