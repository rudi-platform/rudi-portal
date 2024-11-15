package org.rudi.microservice.strukture.service.integration.errors;

import com.fasterxml.jackson.annotation.JsonValue;

public enum IntegrationError {
	ERR_101("ERR_101", "Action non autorisée par le modérateur.");

	private final String message;
	private final String code;

	IntegrationError(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@JsonValue
	public String getMessage() {
		return message;
	}

	@JsonValue
	public String getCode() {
		return code;
	}

	@Override
	public String toString() {

		return code.concat(" ").concat(message);
	}
}
