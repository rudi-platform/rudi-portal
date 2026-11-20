package org.rudi.microservice.kalim.service.datafactory;

import java.util.UUID;

import org.rudi.facet.kaccess.bean.Organization;

public class KaccessOrganizationDataFactory {

	public Organization createOrganizationWithId(UUID organizationId) {
		final Organization organization = new Organization();
		organization.setOrganizationId(organizationId);
		return organization;
	}

}
