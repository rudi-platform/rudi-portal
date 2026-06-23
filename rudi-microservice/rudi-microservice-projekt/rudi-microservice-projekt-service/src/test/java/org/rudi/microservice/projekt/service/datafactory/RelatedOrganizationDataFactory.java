package org.rudi.microservice.projekt.service.datafactory;

import java.util.UUID;

import org.rudi.microservice.projekt.storage.entity.relatedorganization.RelatedOrganizationEntity;
import org.rudi.microservice.projekt.storage.entity.relatedorganization.RelationStatus;
import org.springframework.stereotype.Component;

@Component
public class RelatedOrganizationDataFactory {

	public RelatedOrganizationEntity createNotPersisted(UUID organizationUuid, RelationStatus relationStatus) {
		RelatedOrganizationEntity relatedOrganization = new RelatedOrganizationEntity();
		relatedOrganization.setUuid(UUID.randomUUID());
		relatedOrganization.setOrganizationUuid(organizationUuid != null ? organizationUuid : UUID.randomUUID());
		relatedOrganization.setRelationStatus(relationStatus != null ? relationStatus : RelationStatus.PENDING);
		return relatedOrganization;
	}
}

