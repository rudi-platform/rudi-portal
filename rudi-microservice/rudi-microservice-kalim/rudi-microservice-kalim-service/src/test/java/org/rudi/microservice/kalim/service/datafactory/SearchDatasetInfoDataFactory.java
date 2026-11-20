package org.rudi.microservice.kalim.service.datafactory;

import org.rudi.facet.dataverse.bean.SearchDatasetInfo;

public class SearchDatasetInfoDataFactory {

	public SearchDatasetInfo createSearchDatasetInfoWithGlobalId(String globalId) {
		return new SearchDatasetInfo().globalId(globalId);
	}

}
