package org.rudi.microservice.acl.service.workflow;

import java.io.IOException;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.common.service.util.ApplicationContext;
import org.rudi.facet.bpmn.exception.FormConvertException;
import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.facet.bpmn.service.FormService;
import org.rudi.facet.bpmn.service.InitializationService;
import org.rudi.facet.bpmn.service.impl.AbstractTaskServiceImpl;
import org.rudi.microservice.acl.core.bean.ExternalAccountRequest;
import org.rudi.microservice.acl.service.helper.externalaccountrequest.ExternalAccountRequestAssignmentHelper;
import org.rudi.microservice.acl.service.helper.externalaccountrequest.ExternalAccountRequestWorkflowContext;
import org.rudi.microservice.acl.service.helper.externalaccountrequest.ExternalAccountRequestWorkflowHelper;
import org.rudi.microservice.acl.storage.dao.externalaccountrequest.ExternalAccountRequestDao;
import org.rudi.microservice.acl.storage.entity.externalaccountrequest.ExternalAccountRequestEntity;
import org.springframework.stereotype.Service;

/**
 * Service de gestion des tâches du workflow de demande de création de compte dans le système d'authentification externe
 */
@Service
public class ExternalAccountRequestTaskServiceImpl extends
		AbstractTaskServiceImpl<ExternalAccountRequestEntity, ExternalAccountRequest, ExternalAccountRequestDao, ExternalAccountRequestWorkflowHelper, ExternalAccountRequestAssignmentHelper, ExternalAccountRequestWorkflowContext> {

	public static final String PROCESS_DEFINITION_ID = "external-account-request-process";

	public static final String WORFKLOW_CONTEXT_BEAN_NAME = "externalAccountRequestWorkflowContext";

	private final FormService formService;

	public ExternalAccountRequestTaskServiceImpl(ProcessEngine processEngine, FormHelper formHelper,
			BpmnHelper bpmnHelper, UtilContextHelper utilContextHelper, InitializationService initializationService,
			ExternalAccountRequestDao assetDescriptionDao, ExternalAccountRequestWorkflowHelper assetDescriptionHelper,
			ExternalAccountRequestAssignmentHelper assignmentHelper,
			ExternalAccountRequestWorkflowContext workflowContext,
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
	protected AbstractTaskServiceImpl<ExternalAccountRequestEntity, ExternalAccountRequest, ExternalAccountRequestDao, ExternalAccountRequestWorkflowHelper, ExternalAccountRequestAssignmentHelper, ExternalAccountRequestWorkflowContext> lookupMe() {
		return ApplicationContext.getBean(ExternalAccountRequestTaskServiceImpl.class);
	}

	@Override
	@PostConstruct
	public void loadBpmn() throws IOException {
		super.loadBpmn();
		Map<String, Object> properties = formService.getFormTemplateProperties();
		formService.createOrUpdateAllSectionAndFormDefinitions(properties);
	}
	
	@Override
	protected void updateDraftAssetData(ExternalAccountRequest assetDescription,
			ExternalAccountRequestEntity assetDescriptionEntity)
			throws FormDefinitionException, FormConvertException, InvalidDataException {
		super.updateDraftAssetData(assetDescription, assetDescriptionEntity);
		if (StringUtils.isNotBlank(assetDescription.getIdentityProvider())) {
			Map<String, Object> datas = getFormHelper().hydrateData(assetDescriptionEntity.getData());
			datas.put(AclWorkflowConstants.IDENTITY_PROVIDER_FIELD, assetDescription.getIdentityProvider());
			assetDescriptionEntity.setData(getFormHelper().deshydrateData(datas));
		}
	}

	@Override
	protected void fillProcessVariables(Map<String, Object> variables,
			ExternalAccountRequestEntity assetDescriptionEntity) {
		if (assetDescriptionEntity.getExternalAccountRequestStatus() != null) {
			variables.put(AclWorkflowConstants.EXTERNAL_ACCOUNT_REQUEST_STATUS,
					assetDescriptionEntity.getExternalAccountRequestStatus().name());
		}
	}

}

