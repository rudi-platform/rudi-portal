package org.rudi.microservice.kalim.service.datafactory;

import java.util.List;

import org.rudi.facet.dataverse.bean.SearchDatasetInfo;
import org.rudi.facet.dataverse.model.search.SearchElements;

public class SearchElementsDataFactory {

	public SearchElements<SearchDatasetInfo> createSearchDatasetElements(Long total, List<SearchDatasetInfo> items) {
		return new SearchElements<SearchDatasetInfo>().total(total).items(items);
	}

}
