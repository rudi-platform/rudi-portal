package org.rudi.microservice.projekt.service.datafactory.dto;

import java.util.UUID;

import org.rudi.microservice.projekt.core.bean.DatasetConfidentiality;
import org.rudi.microservice.projekt.core.bean.LinkedDataset;
import org.springframework.stereotype.Component;

@Component
public class LinkedDatasetDtoDataFactory {

	public LinkedDataset create() {
		return new LinkedDataset();
	}

	public LinkedDataset createLinkedDataset(UUID uuid, String comment, DatasetConfidentiality datasetConfidentiality) {
		LinkedDataset linkedDataset = new LinkedDataset();
		linkedDataset.setComment(comment);
		linkedDataset.setUuid(uuid);
		linkedDataset.setDatasetConfidentiality(datasetConfidentiality);
		linkedDataset.setDatasetUuid(UUID.randomUUID());
		return linkedDataset;
	}

}
