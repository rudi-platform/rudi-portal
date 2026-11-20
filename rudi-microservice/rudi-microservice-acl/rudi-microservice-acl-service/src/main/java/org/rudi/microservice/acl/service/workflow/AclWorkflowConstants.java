package org.rudi.microservice.acl.service.workflow;

import org.rudi.facet.bpmn.helper.workflow.WorkflowConstants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AclWorkflowConstants extends WorkflowConstants {

	public static final String EXTERNAL_ACCOUNT_REQUEST_STATUS = "externalAccountRequestStatus";

	public static final String IDENTITY_PROVIDER_FIELD = "identityProvider";

}

