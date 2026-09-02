package org.rudi.microservice.strukture.service.helper.organization.processor;

import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;

public interface OrganizationTaskUpdateProcessor {

	void process(Organization organization, OrganizationEntity existingOrganization) throws InvalidDataException;
}

