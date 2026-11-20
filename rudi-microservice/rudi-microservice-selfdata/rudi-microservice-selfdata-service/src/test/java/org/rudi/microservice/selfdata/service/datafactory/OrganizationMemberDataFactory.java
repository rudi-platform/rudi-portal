package org.rudi.microservice.selfdata.service.datafactory;

import java.util.UUID;

import org.rudi.facet.organization.bean.OrganizationMember;
import org.springframework.stereotype.Component;

@Component
public class OrganizationMemberDataFactory {

	public OrganizationMember create(UUID userUuid) {
		return new OrganizationMember().userUuid(userUuid);
	}

}
