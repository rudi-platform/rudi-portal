package org.rudi.microservice.projekt.service.datafactory.kaccess;

import java.util.UUID;

import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.MetadataAccessCondition;
import org.rudi.facet.kaccess.bean.MetadataAccessConditionConfidentiality;
import org.rudi.facet.kaccess.bean.Organization;
import org.rudi.microservice.projekt.core.bean.DatasetConfidentiality;
import org.springframework.stereotype.Component;

@Component
public class MetadataDataFactory {

	public Metadata createWithRestrictedProducer(UUID producerOrganizationUuid) {
		Organization producer = new Organization();
		producer.setOrganizationId(producerOrganizationUuid);

		MetadataAccessConditionConfidentiality confidentiality = new MetadataAccessConditionConfidentiality();
		confidentiality.setRestrictedAccess(true);

		MetadataAccessCondition accessCondition = new MetadataAccessCondition();
		accessCondition.setConfidentiality(confidentiality);

		Metadata metadata = new Metadata();
		metadata.setAccessCondition(accessCondition);
		metadata.setProducer(producer);

		return metadata;
	}

	public Metadata createMetadataForDatasetConfidentiality(DatasetConfidentiality datasetConfidentiality) {
		MetadataAccessConditionConfidentiality confidentiality = new MetadataAccessConditionConfidentiality();
		confidentiality.setGdprSensitive(datasetConfidentiality == DatasetConfidentiality.SELFDATA);
		confidentiality.setRestrictedAccess(datasetConfidentiality == DatasetConfidentiality.RESTRICTED
				|| datasetConfidentiality == DatasetConfidentiality.SELFDATA);

		MetadataAccessCondition accessCondition = new MetadataAccessCondition();
		accessCondition.setConfidentiality(confidentiality);

		Metadata metadata = new Metadata();
		metadata.setResourceTitle("On s'en moque");
		metadata.setAccessCondition(accessCondition);

		return metadata;
	}

}
