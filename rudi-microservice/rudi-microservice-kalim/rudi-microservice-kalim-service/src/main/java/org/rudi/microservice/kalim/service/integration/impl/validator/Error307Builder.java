package org.rudi.microservice.kalim.service.integration.impl.validator;

import org.rudi.facet.dataverse.fields.FieldSpec;
import org.rudi.microservice.kalim.service.IntegrationError;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;

public class Error307Builder extends ErrorBuilder {
	private String fieldValue;

	@Override
	public Error307Builder field(FieldSpec fieldSpec) {
		super.field(fieldSpec);
		return this;
	}

	public Error307Builder fieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
		return this;
	}

	@Override
	protected IntegrationError getIntegrationError() {
		return IntegrationError.ERR_307;
	}

	@Override
	protected Object[] getFormattedMessageParameters() {
		return new Object[] { fieldValue, fieldSpec.getLocalName() };
	}

	@Override
	public IntegrationRequestErrorEntity build() {
		if (fieldSpec == null) {
			throw new IllegalArgumentException("Missing fieldSpec");
		}
		if (fieldValue == null) {
			throw new IllegalArgumentException("Missing fieldValue");
		}
		return super.build();
	}
}
