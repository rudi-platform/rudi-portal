package org.rudi.microservice.strukture.service.workflow;

import org.activiti.engine.ProcessEngine;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.bpmn.bean.workflow.HistoricSearchCriteria;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.facet.bpmn.helper.workflow.HistoricHelper;
import org.rudi.facet.bpmn.service.impl.AbstractTaskQueryServiceImpl;
import org.rudi.microservice.strukture.core.bean.workflow.StruktureTaskSearchCriteria;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class StruktureTaskQueryServiceImpl
		extends AbstractTaskQueryServiceImpl<StruktureTaskSearchCriteria, HistoricSearchCriteria> {

	public StruktureTaskQueryServiceImpl(ProcessEngine processEngine, FormHelper formHelper, BpmnHelper bpmnHelper,
			HistoricHelper historicHelper, UtilContextHelper utilContextHelper, ApplicationContext applicationContext) {
		super(processEngine, formHelper, bpmnHelper, historicHelper, utilContextHelper, applicationContext);
	}

}
