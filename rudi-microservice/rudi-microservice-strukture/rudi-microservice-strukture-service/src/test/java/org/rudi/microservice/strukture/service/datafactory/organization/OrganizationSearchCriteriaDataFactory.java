package org.rudi.microservice.strukture.service.datafactory.organization;

import java.util.List;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Status;
import org.rudi.microservice.strukture.core.bean.OrganizationStatus;
import org.rudi.microservice.strukture.core.bean.criteria.NodeOrganizationSearchCriteria;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationSearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class OrganizationSearchCriteriaDataFactory {

	public OrganizationSearchCriteria createForUuidsNameInitiatorExcludedAndActive(List<UUID> uuids, String name,
			String initiator, List<UUID> excludeOrganizationUuids, boolean active) {
		return OrganizationSearchCriteria.builder().uuids(uuids).name(name).initiator(initiator)
				.excludeOrganizationUuids(excludeOrganizationUuids).active(active).build();
	}

	public OrganizationSearchCriteria createForNameOrDescription(String nameOrDescription) {
		return OrganizationSearchCriteria.builder().nameOrDescription(nameOrDescription).build();
	}

	public OrganizationSearchCriteria createForUserUuidOrganizationStatusAndStatus(UUID userUuid,
			List<OrganizationStatus> organizationStatus, Status status) {
		return OrganizationSearchCriteria.builder().userUuid(userUuid).organizationStatus(organizationStatus)
				.status(status).build();
	}

	public OrganizationSearchCriteria createForUserUuidOrganizationStatusAndAdminAllowedStatus(UUID userUuid,
			List<OrganizationStatus> organizationStatus, List<OrganizationStatus> adminMemberAllowedStatus) {
		return OrganizationSearchCriteria.builder().userUuid(userUuid).organizationStatus(organizationStatus)
				.adminMemberAllowedStatus(adminMemberAllowedStatus).build();
	}

	public NodeOrganizationSearchCriteria createNodeOrganizationSearchCriteria(List<UUID> uuids, UUID providerUUID) {
		return NodeOrganizationSearchCriteria.builder().uuids(uuids).providerUUID(providerUUID).build();
	}

}
