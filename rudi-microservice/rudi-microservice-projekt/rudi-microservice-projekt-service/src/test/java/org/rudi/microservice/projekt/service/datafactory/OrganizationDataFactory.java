package org.rudi.microservice.projekt.service.datafactory;

import java.util.UUID;

import org.rudi.facet.organization.bean.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationDataFactory {

	public Organization createWithName(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		return organization;
	}

	public Organization createWithUuid(UUID uuid) {
		Organization organization = new Organization();
		organization.setUuid(uuid);
		return organization;
	}

}
