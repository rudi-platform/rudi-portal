package org.rudi.microservice.projekt.service.datafactory.dto;

import java.util.UUID;

import org.rudi.microservice.projekt.core.bean.NewDatasetRequest;
import org.rudi.microservice.projekt.core.bean.NewDatasetRequestStatus;
import org.springframework.stereotype.Component;

@Component
public class NewDatasetRequestDtoDataFactory {

	public NewDatasetRequest create() {
		return new NewDatasetRequest();
	}

	public NewDatasetRequest createNewDatasetRequest(String title, String description) {
		NewDatasetRequest request = new NewDatasetRequest();
		request.setTitle(title);
		request.setDescription(description);
		return request;
	}

	public NewDatasetRequest createNewDatasetRequestForUpdate(UUID uuid, String description, String title,
			String updator, String processDefinitionKey, NewDatasetRequestStatus newDatasetRequestStatus) {
		NewDatasetRequest request = new NewDatasetRequest();
		request.setUuid(uuid);
		request.setDescription(description);
		request.setTitle(title);
		request.setUpdator(updator);
		request.setProcessDefinitionKey(processDefinitionKey);
		request.setNewDatasetRequestStatus(newDatasetRequestStatus);
		return request;
	}

}
