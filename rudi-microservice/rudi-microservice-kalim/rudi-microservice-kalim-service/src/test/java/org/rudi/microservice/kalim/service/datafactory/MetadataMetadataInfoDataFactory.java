package org.rudi.microservice.kalim.service.datafactory;

import org.rudi.facet.kaccess.bean.MetadataMetadataInfo;
import org.rudi.facet.kaccess.bean.Organization;

public class MetadataMetadataInfoDataFactory {

	public MetadataMetadataInfo createMetadataMetadataInfoWithProvider(Organization metadataProvider) {
		final MetadataMetadataInfo metadataInfo = new MetadataMetadataInfo();
		metadataInfo.setMetadataProvider(metadataProvider);
		return metadataInfo;
	}

}
