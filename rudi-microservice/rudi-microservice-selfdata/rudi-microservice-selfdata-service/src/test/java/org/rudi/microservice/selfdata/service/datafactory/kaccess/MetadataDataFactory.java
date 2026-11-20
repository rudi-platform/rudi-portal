package org.rudi.microservice.selfdata.service.datafactory.kaccess;

import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.MetadataExtMetadata;
import org.rudi.facet.kaccess.bean.MetadataExtMetadataExtSelfdata;
import org.rudi.facet.kaccess.bean.Organization;
import org.rudi.facet.kaccess.bean.SelfdataContent;
import org.springframework.stereotype.Component;

@Component
public class MetadataDataFactory {

	public Metadata createEmpty() {
		return new Metadata();
	}

	public Metadata createWithProducer(Organization producer) {
		Metadata metadata = new Metadata();
		metadata.setProducer(producer);
		return metadata;
	}

	public Metadata createWithSelfdataAccess(SelfdataContent.SelfdataAccessEnum access) {
		SelfdataContent selfdataContent = new SelfdataContent();
		selfdataContent.setSelfdataAccess(access);

		MetadataExtMetadataExtSelfdata extSelfdata = new MetadataExtMetadataExtSelfdata();
		extSelfdata.setExtSelfdataContent(selfdataContent);

		MetadataExtMetadata extMetadata = new MetadataExtMetadata();
		extMetadata.setExtSelfdata(extSelfdata);

		Metadata metadata = new Metadata();
		metadata.setExtMetadata(extMetadata);
		return metadata;
	}

}
