package org.rudi.microservice.strukture.service.helper.organization.processor.impl;

import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.service.helper.organization.processor.OrganizationTaskUpdateProcessor;
import org.rudi.microservice.strukture.service.organization.impl.fields.ContactAddressesProcessor;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrganizationTaskContactAddressesProcessor implements OrganizationTaskUpdateProcessor {

	private final ContactAddressesProcessor contactAddressesProcessor;

	@Override
	public void process(Organization organization, OrganizationEntity existingOrganization) throws InvalidDataException {
		try {
			contactAddressesProcessor.processBeforeUpdate(organization, existingOrganization);
		} catch (AppServiceBadRequestException e) {
			throw new InvalidDataException("Invalid organization addresses for workflow update", e);
		}
	}
}

