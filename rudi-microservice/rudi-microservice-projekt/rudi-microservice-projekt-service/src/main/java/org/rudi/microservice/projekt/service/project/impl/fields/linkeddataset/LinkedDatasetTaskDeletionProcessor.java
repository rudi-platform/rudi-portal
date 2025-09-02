package org.rudi.microservice.projekt.service.project.impl.fields.linkeddataset;

import org.jetbrains.annotations.Nullable;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.service.TaskService;
import org.rudi.microservice.projekt.core.bean.LinkedDataset;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetStatus;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class LinkedDatasetTaskDeletionProcessor implements DeleteLinkedDatasetFieldProcessor {

	private final TaskService<LinkedDataset> linkedDatasetTaskService;

	@Override
	public void process(@Nullable LinkedDatasetEntity existingLinkedDataset, @Nullable Boolean force) throws AppServiceException {
		if (existingLinkedDataset == null) {
			return;
		}

		existingLinkedDataset.setLinkedDatasetStatus(LinkedDatasetStatus.DISENGAGED);

		if (Boolean.TRUE.equals(force)) {

			try {
				if(linkedDatasetTaskService.hasTask(existingLinkedDataset.getUuid())) {
					linkedDatasetTaskService.stopTaskByByAssetUuid(existingLinkedDataset.getUuid());
				}
				else {
					log.info("There is no task for {}", existingLinkedDataset.getUuid());
				}
			} catch (InvalidDataException e) {
				throw new AppServiceException(String.format("Could not stop linked dataset task %s", existingLinkedDataset.getUuid()), e);
			}
		}
	}
}
