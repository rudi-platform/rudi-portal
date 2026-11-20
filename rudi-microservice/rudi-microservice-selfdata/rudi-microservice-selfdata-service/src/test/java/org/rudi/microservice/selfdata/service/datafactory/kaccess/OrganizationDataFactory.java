package org.rudi.microservice.selfdata.service.datafactory.kaccess;

import java.util.UUID;

import org.rudi.facet.kaccess.bean.Organization;
import org.springframework.stereotype.Component;

@Component
public class OrganizationDataFactory {

	public Organization create(UUID uuid, String name) {
		return new Organization(uuid, name);
	}

}
