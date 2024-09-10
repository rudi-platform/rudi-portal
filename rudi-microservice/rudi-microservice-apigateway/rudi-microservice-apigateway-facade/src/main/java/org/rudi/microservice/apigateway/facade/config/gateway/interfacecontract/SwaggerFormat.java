/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author FNI18300
 *
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SwaggerFormat {

	FLOAT("float"), DOUBLE("double"), INT32("int32"), INT64("int64"), DATE("date"), DATE_TIME("date-time"),
	BYTE("byte"), BINARY("binary"), UUID("uuid");

	@Getter
	private String name;

	public static SwaggerFormat lookupType(String name) {
		SwaggerFormat result = null;
		for (SwaggerFormat type : values()) {
			if (type.getName().equals(name)) {
				result = type;
				break;
			}
		}
		return result;
	}
}
