/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract.parameter;

import io.swagger.models.parameters.SerializableParameter;

/**
 * @author FNI18300
 *
 */
public interface ParameterDataValidator {

	boolean validate(SerializableParameter parameter, String value);
}
