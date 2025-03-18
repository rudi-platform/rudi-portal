package org.rudi.microservice.strukture.service.workflow;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.common.service.util.ApplicationContext;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.facet.bpmn.service.FormService;
import org.rudi.facet.bpmn.service.InitializationService;
import org.rudi.facet.bpmn.service.impl.AbstractTaskServiceImpl;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationAssignmentHelper;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationWorkflowContext;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationWorkflowHelper;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.stereotype.Service;

@Service
public class OrganizationTaskServiceImpl extends
		AbstractTaskServiceImpl<OrganizationEntity, Organization, OrganizationDao, OrganizationWorkflowHelper, OrganizationAssignmentHelper, OrganizationWorkflowContext> {

	public static final String PROCESS_DEFINITION_ID = "organization-process";

	public static final String WORFKLOW_CONTEXT_BEAN_NAME = "organizationWorkflowContext";

	private final FormService formService;

	public OrganizationTaskServiceImpl(ProcessEngine processEngine, FormHelper formHelper, BpmnHelper bpmnHelper,
			UtilContextHelper utilContextHelper, InitializationService initializationService,
			OrganizationDao assetDescriptionDao, OrganizationWorkflowHelper assetDescriptionHelper,
			OrganizationAssignmentHelper assignmentHelper, OrganizationWorkflowContext workflowContext,
			ProcessEngineConfiguration processEngineConfiguration, FormService formService) {
		super(processEngine, formHelper, bpmnHelper, utilContextHelper, initializationService, assetDescriptionDao,
				assetDescriptionHelper, assignmentHelper, workflowContext, processEngineConfiguration);
		this.formService = formService;
	}

	@Override
	protected String getWorkflowContextBeanName() {
		return WORFKLOW_CONTEXT_BEAN_NAME;
	}

	@Override
	public String getProcessDefinitionKey() {
		return PROCESS_DEFINITION_ID;
	}

	@Override
	protected AbstractTaskServiceImpl<OrganizationEntity, Organization, OrganizationDao, OrganizationWorkflowHelper, OrganizationAssignmentHelper, OrganizationWorkflowContext> lookupMe() {
		return ApplicationContext.getBean(OrganizationTaskServiceImpl.class);
	}

	@Override
	@PostConstruct
	public void loadBpmn() throws IOException {
		super.loadBpmn();
		Map<String, Object> properties = formService.getFormTemplateProperties();
		formService.createOrUpdateAllSectionAndFormDefinitions(properties);
	}
}
