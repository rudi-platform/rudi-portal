package org.rudi.microservice.kalim.service.integration.impl.validator.metadata;

import org.rudi.microservice.kalim.service.integration.impl.handlers.AbstractIntegrationRequestTreatmentHandler;
import org.rudi.microservice.kalim.service.integration.impl.handlers.PostIntegrationRequestTreatmentHandler;
import org.rudi.microservice.kalim.service.integration.impl.validator.Error304Builder;
import org.rudi.microservice.kalim.service.integration.impl.validator.extractor.FieldExtractor;

public abstract class AbstractUniqueMetadataIdValidator<T> extends AbstractMetadataIdValidator<T> {

	protected AbstractUniqueMetadataIdValidator(FieldExtractor<T> fieldExtractor) {
		super(fieldExtractor);
	}

	@Override
	public boolean canBeUsedBy(AbstractIntegrationRequestTreatmentHandler handler) {
		return handler instanceof PostIntegrationRequestTreatmentHandler;
	}

	@Override
	protected boolean validationSucceedsIfDatasetAlreadyExists() {
		return false;
	}

	@Override
	protected Error304Builder getErrorBuilderForFieldValue(T fieldValue) {
		return new Error304Builder().field(getField()).fieldValue(fieldValue);
	}
}
