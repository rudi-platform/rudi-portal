package org.rudi.microservice.strukture.service.organization.impl.fields;

import java.time.LocalDateTime;

import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpeningDateProcessor implements CreateOrganizationFieldProcessor, UpdateOrganizationFieldProcessor {

	@Override
	public void processBeforeCreate(OrganizationEntity organization, Organization organizationDto) throws AppServiceBadRequestException {
		organization.setOpeningDate(LocalDateTime.now());
	}

	@Override
	public void processBeforeUpdate(Organization organization, OrganizationEntity existingOrganization) {
		// Rien à tester en MAJ la date peut ne pas bouger
	}
}
