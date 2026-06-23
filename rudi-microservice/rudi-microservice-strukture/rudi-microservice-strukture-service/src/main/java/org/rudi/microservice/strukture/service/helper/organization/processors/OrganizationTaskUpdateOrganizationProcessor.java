package org.rudi.microservice.strukture.service.helper.organization.processors;

import org.rudi.bpmn.core.bean.Field;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;

public interface OrganizationTaskUpdateOrganizationProcessor {

	String getAcceptedField();

	default boolean accept(Field field) {
		return field != null && field.getDefinition() != null
				&& getAcceptedField().equals(field.getDefinition().getName());
	}

	void process(Field field, OrganizationEntity organizationEntity);
}


