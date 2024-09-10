package org.rudi.microservice.kalim.service.integration.impl.validator.extractor;

import org.rudi.facet.dataverse.fields.FieldSpec;
import org.rudi.facet.kaccess.bean.Metadata;

public interface FieldExtractor<T> {
	FieldSpec getField();

	T getFieldValue(Metadata metadata);
}
