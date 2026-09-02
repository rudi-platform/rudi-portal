package org.rudi.microservice.strukture.service.organization.impl.fields;

import java.util.UUID;

import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UUIDProcessor implements CreateOrganizationFieldProcessor {
	private final UtilContextHelper utilContextHelper;

	@Override
	public void processBeforeCreate(OrganizationEntity organization, Organization organizationDto) {
		if (organization != null && (organization.getUuid() == null || !userCanCreateOrganizationWithUuid())) {
			organization.setUuid(UUID.randomUUID());
		}
	}

	private boolean userCanCreateOrganizationWithUuid() {
		final var authenticatedUser = utilContextHelper.getAuthenticatedUser();
		if (authenticatedUser == null) {
			return false;
		}
		final var roles = authenticatedUser.getRoles();
		if (roles == null) {
			return false;
		}
		return roles.contains("MODULE_KALIM");
	}

}
