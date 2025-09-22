package org.rudi.microservice.strukture.service.exception;

import org.rudi.common.service.exception.BusinessException;

public class InvalidStateException extends BusinessException {
	public InvalidStateException(String message) {
		super(message);
	}
}
