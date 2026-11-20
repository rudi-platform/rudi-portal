package org.rudi.microservice.kalim.service.datafactory;

import org.rudi.facet.kaccess.bean.MetadataList;
import org.rudi.facet.kaccess.bean.MetadataListFacets;

public class MetadataListFacetsDataFactory {

	public MetadataListFacets createMetadataListFacets(MetadataList metadataList) {
		return new MetadataListFacets().metadataList(metadataList);
	}

}
