package org.rudi.microservice.acl.service.workflow;

import org.rudi.facet.bpmn.helper.workflow.WorkflowConstants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExternalAccountRequestWorkflowConstants extends WorkflowConstants {

	public static final String MESSAGE_TO_REQUESTER_KEY = "messageToRequester";
	
}
