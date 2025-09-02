package org.rudi.microservice.projekt.service.project.impl.fields.linkeddataset;

import javax.annotation.Nullable;

import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity;

/**
 * 
 * @author FNI18300
 *
 */
public interface DeleteLinkedDatasetFieldProcessor {
	void process(@Nullable LinkedDatasetEntity existingLinkedDataset, @Nullable Boolean force)
			throws AppServiceException;
}
