package org.rudi.microservice.strukture.service.organization.impl.fields;

import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.bpmn.service.TaskService;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatusProcessor implements CreateOrganizationFieldProcessor {

	private final UtilContextHelper utilContextHelper;
	private final TaskService<Organization> organizationTaskService;

	@Override
	public void processBeforeCreate(OrganizationEntity organization) throws AppServiceBadRequestException {
		organization.setProcessDefinitionKey(organizationTaskService.getProcessDefinitionKey());
		organization.setStatus(Status.DRAFT);
		organization.setOrganizationStatus(OrganizationStatus.DRAFT);
		if(StringUtils.isEmpty(organization.getFunctionalStatus())){
			organization.setFunctionalStatus("Organisation en cours de création");
		}

		AuthenticatedUser authenticatedUser = utilContextHelper.getAuthenticatedUser();
		if(authenticatedUser != null){
			organization.setInitiator(authenticatedUser.getLogin());
		}
	}
}
