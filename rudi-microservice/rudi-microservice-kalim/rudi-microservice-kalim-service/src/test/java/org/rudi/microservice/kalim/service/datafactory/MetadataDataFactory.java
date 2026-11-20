package org.rudi.microservice.kalim.service.datafactory;

import java.util.List;
import java.util.UUID;

import org.rudi.facet.kaccess.bean.Media;
import org.rudi.facet.kaccess.bean.Metadata;

public class MetadataDataFactory {

	private final KaccessOrganizationDataFactory kaccessOrganizationDataFactory = new KaccessOrganizationDataFactory();

	public Metadata createMetadataWithMedias(UUID globalId, UUID producerOrganizationId, List<Media> medias) {
		final Metadata metadata = new Metadata();
		metadata.setGlobalId(globalId);
		metadata.setAvailableFormats(medias);
		metadata.setProducer(kaccessOrganizationDataFactory.createOrganizationWithId(producerOrganizationId));
		return metadata;
	}

	public Metadata createMetadataWithMedias(UUID globalId, List<Media> medias) {
		final Metadata metadata = new Metadata();
		metadata.setGlobalId(globalId);
		metadata.setAvailableFormats(medias);
		return metadata;
	}

}
