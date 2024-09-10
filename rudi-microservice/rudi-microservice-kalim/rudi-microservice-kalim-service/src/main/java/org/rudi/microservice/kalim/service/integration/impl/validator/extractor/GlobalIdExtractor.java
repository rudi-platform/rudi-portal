package org.rudi.microservice.kalim.service.integration.impl.validator.extractor;

import java.util.UUID;

import org.rudi.facet.dataverse.fields.FieldSpec;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.constant.RudiMetadataField;
import org.springframework.stereotype.Component;

@Component
public class GlobalIdExtractor implements FieldExtractor<UUID> {

	@Override
	public FieldSpec getField() {
		return RudiMetadataField.GLOBAL_ID;
	}

	@Override
	public UUID getFieldValue(Metadata metadata) {
		return metadata.getGlobalId();
	}
}
