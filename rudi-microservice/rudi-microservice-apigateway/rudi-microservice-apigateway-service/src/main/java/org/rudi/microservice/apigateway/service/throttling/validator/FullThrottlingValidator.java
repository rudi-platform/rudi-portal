package org.rudi.microservice.apigateway.service.throttling.validator;

import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.apigateway.core.bean.Throttling;
import org.springframework.stereotype.Component;

@Component
class FullThrottlingValidator implements ThrottlingValidator {

	@Override
	public void validateCreate(Throttling throttling) throws AppServiceException {
		validateCommon(throttling);
	}

	@SuppressWarnings("java:S2583") // Bien qu'il y ait des @NotNull auto générés, on ne peut pas garantir qu'ils soient toujours respectés dans ce contexte
	private void validateCommon(Throttling throttling) {
		if (throttling.getCode() == null) {
			throw new IllegalArgumentException("Code required");
		}
		if (throttling.getOrder() == null) {
			throw new IllegalArgumentException("Order id required");
		}
	}

	@Override
	public void validateUpdate(Throttling throttling) throws AppServiceException {
		if (throttling.getUuid() == null) {
			throw new IllegalArgumentException("UUID required");
		}
		validateCommon(throttling);
	}
}
