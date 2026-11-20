package org.rudi.microservice.kalim.service.datafactory;

import java.util.List;

import org.rudi.facet.dataverse.bean.DatasetFile;
import org.rudi.facet.dataverse.bean.DatasetMetadataBlock;
import org.rudi.facet.dataverse.bean.DatasetVersion;

public class DatasetVersionDataFactory {

	public DatasetVersion createDatasetVersion(Long id, Long datasetId, String datasetPersistentId,
			String versionState, Integer versionNumber, Integer versionMinorNumber,
			DatasetMetadataBlock metadataBlocks, List<DatasetFile> files) {
		return new DatasetVersion().id(id).datasetId(datasetId).datasetPersistentId(datasetPersistentId)
				.versionState(versionState).versionNumber(versionNumber).versionMinorNumber(versionMinorNumber)
				.metadataBlocks(metadataBlocks).files(files);
	}

}
