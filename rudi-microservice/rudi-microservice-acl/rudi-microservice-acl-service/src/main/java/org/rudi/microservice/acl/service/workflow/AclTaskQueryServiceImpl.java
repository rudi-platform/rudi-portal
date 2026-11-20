package org.rudi.microservice.acl.service.workflow;

import org.activiti.engine.ProcessEngine;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.bpmn.bean.workflow.HistoricSearchCriteria;
import org.rudi.facet.bpmn.bean.workflow.TaskSearchCriteria;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.facet.bpmn.helper.workflow.HistoricHelper;
import org.rudi.facet.bpmn.service.impl.AbstractTaskQueryServiceImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Service générique de consultation des tâches du microservice ACL (recherche, historique)
 */
@Service
public class AclTaskQueryServiceImpl extends AbstractTaskQueryServiceImpl<TaskSearchCriteria, HistoricSearchCriteria> {

	protected AclTaskQueryServiceImpl(ProcessEngine processEngine, FormHelper formHelper, BpmnHelper bpmnHelper,
			HistoricHelper historicHelper, UtilContextHelper utilContextHelper, ApplicationContext applicationContext) {
		super(processEngine, formHelper, bpmnHelper, historicHelper, utilContextHelper, applicationContext);
	}

}

