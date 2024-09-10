/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.gateway.interfacecontract;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author FNI18300
 *
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum SwaggerType {

	STRING("string",
			List.of(SwaggerFormat.BINARY, SwaggerFormat.BYTE, SwaggerFormat.DATE_TIME, SwaggerFormat.DATE,
					SwaggerFormat.UUID)),
	NUMBER("number", List.of(SwaggerFormat.DOUBLE, SwaggerFormat.FLOAT)),
	INTEGER("integer", List.of(SwaggerFormat.INT32, SwaggerFormat.INT64)), BOOLEAN("boolean", List.of()),
	ARRAY("array", List.of()), OBJECT("object", List.of());

	@Getter
	private String name;

	@Getter
	private List<SwaggerFormat> formats;

	public static SwaggerType lookupType(String name) {
		SwaggerType result = null;
		for (SwaggerType type : values()) {
			if (type.getName().equals(name)) {
				result = type;
				break;
			}
		}
		return result;
	}

}
