package org.rudi.microservice.kalim.service.datafactory;

import java.util.UUID;

import org.rudi.facet.organization.bean.Organization;

public class OrganizationDataFactory {

	public Organization createOrganizationWithUuid(UUID uuid) {
		final Organization organization = new Organization();
		organization.setUuid(uuid);
		return organization;
	}

}
