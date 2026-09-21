package org.rudi.microservice.strukture.service.workflow;


import org.rudi.facet.bpmn.helper.workflow.WorkflowConstants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StruktureWorkflowConstants extends WorkflowConstants {

	public static final String LINKED_PRODUCER_STATUS = "linkedProducerStatus";
	public static final String ORGANIZATION_STATUS = "organizationStatus";

	public static final String ORGANIZATION_DRAFT_ARCHIVE_FORM_SECTION_NAME = "organization-process-draft/archive-organization";
	public static final String ORGANIZATION_DRAFT_UPDATE_FORM_SECTION_NAME = "organization-process-draft/update-organization";

	public static final String ORGANIZATION_CREATION_COMMENT_KEY = "messageToOrganizationCreator";
	public static final String ORGANIZATION_ARCHIVAGE_COMMENT_KEY = "messageToOrganizationArchiver";
	public static final String ORGANIZATION_ARCHIVE_MODE_KEY = "organizationArchiveMode";
	public static final String ORGANIZATION_ARCHIVAGE_TYPE_DISENGAGED_VALUE = "DISENGAGED";
}
