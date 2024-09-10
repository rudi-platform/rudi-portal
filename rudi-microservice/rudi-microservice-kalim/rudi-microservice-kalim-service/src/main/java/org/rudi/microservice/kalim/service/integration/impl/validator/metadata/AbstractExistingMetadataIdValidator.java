package org.rudi.microservice.kalim.service.integration.impl.validator.metadata;

import org.rudi.microservice.kalim.service.integration.impl.handlers.AbstractIntegrationRequestTreatmentHandler;
import org.rudi.microservice.kalim.service.integration.impl.handlers.PutIntegrationRequestTreatmentHandler;
import org.rudi.microservice.kalim.service.integration.impl.validator.extractor.FieldExtractor;

public abstract class AbstractExistingMetadataIdValidator<T> extends AbstractMetadataIdValidator<T> {

	protected AbstractExistingMetadataIdValidator(FieldExtractor<T> fieldExtractor) {
		super(fieldExtractor);
	}

	@Override
	public boolean canBeUsedBy(AbstractIntegrationRequestTreatmentHandler handler) {
		return handler instanceof PutIntegrationRequestTreatmentHandler;
	}

	@Override
	protected boolean validationSucceedsIfDatasetAlreadyExists() {
		return true;
	}
}
