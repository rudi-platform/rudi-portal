package org.rudi.microservice.kalim.service.integration.impl.transformer;

import org.rudi.microservice.kalim.service.integration.impl.handlers.AbstractIntegrationRequestTreatmentHandler;

public interface ElementTransformer<T> {

	T tranform(T t);

	default boolean canBeUsedBy(AbstractIntegrationRequestTreatmentHandler handler) {
		return true;
	}
}
