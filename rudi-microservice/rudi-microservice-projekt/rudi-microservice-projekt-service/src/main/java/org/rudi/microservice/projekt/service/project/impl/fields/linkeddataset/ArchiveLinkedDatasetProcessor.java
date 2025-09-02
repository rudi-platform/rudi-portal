package org.rudi.microservice.projekt.service.project.impl.fields.linkeddataset;

import javax.annotation.Nullable;

import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity;

public interface ArchiveLinkedDatasetProcessor {
	void process(@Nullable LinkedDatasetEntity existingLinkedDataset)
			throws AppServiceException;
}
