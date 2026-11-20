package org.rudi.microservice.kalim.service.datafactory;

import org.rudi.facet.dataverse.bean.Dataset;
import org.rudi.facet.dataverse.bean.DatasetVersion;

public class DatasetDataFactory {

	public Dataset createDatasetWithLatestVersion(DatasetVersion latestVersion) {
		return new Dataset().latestVersion(latestVersion);
	}

}
