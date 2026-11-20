package org.rudi.microservice.kalim.service.datafactory;

import java.util.List;

import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.MetadataList;

public class MetadataListDataFactory {

	public MetadataList createMetadataList(Long offset, List<Metadata> items) {
		return new MetadataList().offset(offset).items(items);
	}

}
