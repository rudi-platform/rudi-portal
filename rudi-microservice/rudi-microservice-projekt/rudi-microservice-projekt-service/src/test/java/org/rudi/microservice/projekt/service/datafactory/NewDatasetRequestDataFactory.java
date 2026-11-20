package org.rudi.microservice.projekt.service.datafactory;

import java.time.LocalDateTime;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Status;
import org.rudi.facet.bpmn.datafactory.AbstractAssetDescriptionDataFactory;
import org.rudi.microservice.projekt.storage.dao.newdatasetrequest.NewDatasetRequestDao;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestEntity;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class NewDatasetRequestDataFactory
		extends AbstractAssetDescriptionDataFactory<NewDatasetRequestEntity, NewDatasetRequestDao> {

	public NewDatasetRequestDataFactory(NewDatasetRequestDao repository) {
		super(repository, NewDatasetRequestEntity.class);
	}

	public NewDatasetRequestEntity createNotPersisted(String title, NewDatasetRequestStatus status) {
		return createNotPersisted(title, "Description de " + randomString(15), status);
	}

	public NewDatasetRequestEntity createNotPersisted(String title, String description, NewDatasetRequestStatus status) {
		NewDatasetRequestEntity request = new NewDatasetRequestEntity();
		request.setUuid(UUID.randomUUID());
		request.setTitle(title != null ? title : "Demande de " + randomString(15));
		request.setDescription(description != null ? description : "Description de " + randomString(15));
		request.setNewDatasetRequestStatus(status != null ? status : NewDatasetRequestStatus.DRAFT);
		request.setStatus(Status.DRAFT);
		request.setFunctionalStatus("Demande");
		request.setInitiator("datafactory");
		request.setProcessDefinitionKey("new-dataset-request-process");
		request.setCreationDate(LocalDateTime.now());
		request.setUpdatedDate(LocalDateTime.now());
		return request;
	}

	public NewDatasetRequestEntity createPersisted(String title, String description, NewDatasetRequestStatus status) {
		return repository.save(createNotPersisted(title, description, status));
	}
}

