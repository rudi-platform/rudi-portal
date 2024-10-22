package org.rudi.microservice.strukture.service.workflow;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.activiti.engine.ProcessEngine;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.common.service.util.ApplicationContext;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.facet.bpmn.service.FormService;
import org.rudi.facet.bpmn.service.InitializationService;
import org.rudi.facet.bpmn.service.impl.AbstractTaskServiceImpl;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.service.helper.provider.LinkedProducerAssignmentHelper;
import org.rudi.microservice.strukture.service.helper.provider.LinkedProducerWorkflowHelper;
import org.rudi.microservice.strukture.storage.dao.provider.LinkedProducerDao;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.springframework.stereotype.Service;

@Service
public class LinkedProducerTaskServiceImpl extends AbstractTaskServiceImpl<LinkedProducerEntity, LinkedProducer, LinkedProducerDao, LinkedProducerWorkflowHelper, LinkedProducerAssignmentHelper> {

	public static final String PROCESS_DEFINITION_ID = "linked-producer-process";

	private final FormService formService;

	protected LinkedProducerTaskServiceImpl(ProcessEngine processEngine, FormHelper formHelper, BpmnHelper bpmnHelper, UtilContextHelper utilContextHelper, InitializationService initializationService, LinkedProducerDao assetDescriptionDao, LinkedProducerWorkflowHelper assetDescriptionHelper, LinkedProducerAssignmentHelper assignmentHelper, FormService formService) {
		super(processEngine, formHelper, bpmnHelper, utilContextHelper, initializationService, assetDescriptionDao, assetDescriptionHelper, assignmentHelper);
		this.formService = formService;
	}

	@Override
	public String getProcessDefinitionKey() {
		return PROCESS_DEFINITION_ID;
	}

	/**
	 * @return
	 */
	@Override
	protected AbstractTaskServiceImpl<LinkedProducerEntity, LinkedProducer, LinkedProducerDao, LinkedProducerWorkflowHelper, LinkedProducerAssignmentHelper> lookupMe() {
		return ApplicationContext.getBean(LinkedProducerTaskServiceImpl.class);
	}

	@Override
	@PostConstruct
	public void loadBpmn() throws IOException {
		super.loadBpmn();
	}
}
