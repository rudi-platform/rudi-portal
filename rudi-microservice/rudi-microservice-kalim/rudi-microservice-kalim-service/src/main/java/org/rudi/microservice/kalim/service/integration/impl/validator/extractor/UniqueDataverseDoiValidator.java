package org.rudi.microservice.kalim.service.integration.impl.validator.extractor;

import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.microservice.kalim.service.integration.impl.validator.metadata.AbstractUniqueMetadataIdValidator;
import org.springframework.stereotype.Component;

@Component
public class UniqueDataverseDoiValidator extends AbstractUniqueMetadataIdValidator<String> {

	private final DatasetService datasetService;

	public UniqueDataverseDoiValidator(DataverseDoiExtractor fieldExtractor, DatasetService datasetService) {
		super(fieldExtractor);
		this.datasetService = datasetService;
	}

	@Override
	protected boolean datasetAlreadyExistsWithFieldValue(String dataverseDoi) throws DataverseAPIException {
		return datasetService.datasetExists(dataverseDoi);
	}
}
