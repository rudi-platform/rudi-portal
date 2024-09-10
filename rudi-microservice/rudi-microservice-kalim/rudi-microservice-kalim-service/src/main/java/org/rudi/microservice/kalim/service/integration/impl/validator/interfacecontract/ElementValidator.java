package org.rudi.microservice.kalim.service.integration.impl.validator.interfacecontract;

import java.util.Set;

import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;

public interface ElementValidator<T> {
	Set<IntegrationRequestErrorEntity> validate(T t, String interfaceContractToUse);

	default boolean accept(T t) {
		return false;
	}
}
