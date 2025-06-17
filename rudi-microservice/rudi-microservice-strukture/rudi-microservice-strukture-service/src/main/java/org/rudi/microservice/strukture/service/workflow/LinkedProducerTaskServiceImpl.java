package org.rudi.microservice.strukture.service.workflow;

import java.io.IOException;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.core.security.RoleCodes;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.common.service.util.ApplicationContext;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.facet.bpmn.service.InitializationService;
import org.rudi.facet.bpmn.service.impl.AbstractTaskServiceImpl;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.service.helper.provider.LinkedProducerAssignmentHelper;
import org.rudi.microservice.strukture.service.helper.provider.LinkedProducerWorkflowContext;
import org.rudi.microservice.strukture.service.helper.provider.LinkedProducerWorkflowHelper;
import org.rudi.microservice.strukture.storage.dao.provider.LinkedProducerDao;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LinkedProducerTaskServiceImpl extends
		AbstractTaskServiceImpl<LinkedProducerEntity, LinkedProducer, LinkedProducerDao, LinkedProducerWorkflowHelper, LinkedProducerAssignmentHelper, LinkedProducerWorkflowContext> {

	public static final String PROCESS_DEFINITION_ID = "linked-producer-process";

	public static final String WORFKLOW_CONTEXT_BEAN_NAME = "linkedProducerWorkflowContext";

	protected LinkedProducerTaskServiceImpl(ProcessEngine processEngine, FormHelper formHelper, BpmnHelper bpmnHelper,
			UtilContextHelper utilContextHelper, InitializationService initializationService,
			LinkedProducerDao assetDescriptionDao, LinkedProducerWorkflowHelper assetDescriptionHelper,
			LinkedProducerAssignmentHelper assignmentHelper, LinkedProducerWorkflowContext workflowContext,
			ProcessEngineConfiguration processEngineConfiguration) {
		super(processEngine, formHelper, bpmnHelper, utilContextHelper, initializationService, assetDescriptionDao,
				assetDescriptionHelper, assignmentHelper, workflowContext, processEngineConfiguration);
	}

	@Override
	protected String getWorkflowContextBeanName() {
		return WORFKLOW_CONTEXT_BEAN_NAME;
	}

	@Override
	public String getProcessDefinitionKey() {
		return PROCESS_DEFINITION_ID;
	}

	/**
	 * @return
	 */
	@Override
	protected AbstractTaskServiceImpl<LinkedProducerEntity, LinkedProducer, LinkedProducerDao, LinkedProducerWorkflowHelper, LinkedProducerAssignmentHelper, LinkedProducerWorkflowContext> lookupMe() {
		return ApplicationContext.getBean(LinkedProducerTaskServiceImpl.class);
	}

	/**
	 * Méthode pour ajouter des données dans les variables du processus
	 *
	 * @param variables
	 * @param assetDescriptionEntity
	 */
	@Override
	protected void fillProcessVariables(Map<String, Object> variables, LinkedProducerEntity assetDescriptionEntity) {
		if (assetDescriptionEntity.getLinkedProducerStatus() != null) {
			variables.put(StruktureWorkflowConstants.LINKED_PRODUCER_STATUS,
					assetDescriptionEntity.getLinkedProducerStatus());
		}
	}

	@Override
	@PostConstruct
	public void loadBpmn() throws IOException {
		super.loadBpmn();
	}

	/**
	 * @param assetDescriptionEntity
	 * @throws IllegalArgumentException
	 */
	@Override
	protected void checkEntityStatus(LinkedProducerEntity assetDescriptionEntity) throws IllegalArgumentException {
		if (assetDescriptionEntity == null) {
			throw new IllegalArgumentException("Invalid task");
		}

		// Si le status n'est ni à DRAFT ni à COMPLETED, on lance une exception.
		if (!(assetDescriptionEntity.getStatus().equals(Status.DRAFT)
				|| assetDescriptionEntity.getStatus().equals(Status.COMPLETED))) {
			log.error("Invalid status for linkedProducer {} : status : {}", assetDescriptionEntity.getUuid(),
					assetDescriptionEntity.getStatus());
			throw new IllegalArgumentException("Invalid status on project " + assetDescriptionEntity.getUuid());
		}
	}


	@Override
	protected void updateAssetCreation(LinkedProducerEntity assetDescriptionEntity) {
		String presetInitiator = assetDescriptionEntity.getInitiator();

		super.updateAssetCreation(assetDescriptionEntity);

		if(StringUtils.isNotEmpty(presetInitiator) && getUtilContextHelper().hasRole(RoleCodes.MODERATOR)){
			assetDescriptionEntity.setInitiator(presetInitiator);
		}
	}
}
