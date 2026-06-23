package org.rudi.microservice.strukture.service.helper.organization.processors;

import org.rudi.bpmn.core.bean.Field;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractOrganizationTaskUpdateOrganizationStringFieldProcessor
		implements OrganizationTaskUpdateOrganizationProcessor {

	@Getter
	private final String acceptedField;

	protected abstract void assign(String value, OrganizationEntity organizationEntity);

	@Override
	public void process(Field field, OrganizationEntity organizationEntity) {
		String value = null;
		if (field != null && field.getValues() != null && !field.getValues().isEmpty()) {
			value = field.getValues().get(0);
		}
		assign(value, organizationEntity);
	}
}


