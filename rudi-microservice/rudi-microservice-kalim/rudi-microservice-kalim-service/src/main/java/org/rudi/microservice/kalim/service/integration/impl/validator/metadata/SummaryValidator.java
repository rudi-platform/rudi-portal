package org.rudi.microservice.kalim.service.integration.impl.validator.metadata;

import java.util.List;

import org.rudi.facet.dataverse.fields.FieldSpec;
import org.rudi.facet.kaccess.bean.DictionaryEntry;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.constant.RudiMetadataField;
import org.springframework.stereotype.Component;

@Component
public class SummaryValidator extends AbstractNotEmptyDictionaryEntryListValidator {

	@Override
	protected FieldSpec getFieldName() {

		return RudiMetadataField.SUMMARY;
	}

	@Override
	protected List<DictionaryEntry> getMetadataElementToValidate(Metadata metadata) {
		return metadata.getSummary();
	}

}
