package org.rudi.microservice.projekt.service.datafactory;

import java.time.LocalDateTime;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Status;
import org.rudi.facet.bpmn.datafactory.AbstractAssetDescriptionDataFactory;
import org.rudi.microservice.projekt.storage.dao.linkeddataset.LinkedDatasetDao;
import org.rudi.microservice.projekt.storage.entity.DatasetConfidentiality;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class LinkedDatasetDataFactory extends AbstractAssetDescriptionDataFactory<LinkedDatasetEntity, LinkedDatasetDao> {

	public LinkedDatasetDataFactory(LinkedDatasetDao repository) {
		super(repository, LinkedDatasetEntity.class);
	}

	public LinkedDatasetEntity createNotPersisted(UUID datasetUuid, UUID organizationUuid, LinkedDatasetStatus status,
			DatasetConfidentiality confidentiality) {
		LinkedDatasetEntity linkedDataset = new LinkedDatasetEntity();
		linkedDataset.setUuid(UUID.randomUUID());
		linkedDataset.setDatasetUuid(datasetUuid != null ? datasetUuid : UUID.randomUUID());
		linkedDataset.setDatasetOrganisationUuid(organizationUuid != null ? organizationUuid : UUID.randomUUID());
		linkedDataset.setLinkedDatasetStatus(status != null ? status : LinkedDatasetStatus.DRAFT);
		linkedDataset.setDatasetConfidentiality(confidentiality != null ? confidentiality : DatasetConfidentiality.OPENED);
		linkedDataset.setComment("Linked dataset cree par la datafactory");
		linkedDataset.setStatus(Status.COMPLETED);
		linkedDataset.setFunctionalStatus("Lie");
		linkedDataset.setInitiator("datafactory");
		linkedDataset.setProcessDefinitionKey("linked-dataset-process");
		linkedDataset.setCreationDate(LocalDateTime.now());
		linkedDataset.setUpdatedDate(LocalDateTime.now());
		return linkedDataset;
	}
}

