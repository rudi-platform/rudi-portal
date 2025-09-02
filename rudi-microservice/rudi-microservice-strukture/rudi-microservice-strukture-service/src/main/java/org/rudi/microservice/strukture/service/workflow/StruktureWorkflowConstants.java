package org.rudi.microservice.strukture.service.workflow;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StruktureWorkflowConstants {



	public static final String LINKED_PRODUCER_STATUS = "linkedProducerStatus";
	public static final String ORGANIZATION_STATUS = "organizationStatus";
	public static final String ORGANIZATION_DRAFT_TYPE = "draftType";

	public static final String SECTION_NAME_IMAGE_ORGANIZATION = "image-organization";
	public static final String FIELD_NAME_IMAGE_ORGANIZATION = "organizationImage";

	public static final String DRAFT_ARCHIVE_FORM_SECTION_NAME = "organization-process-draft/archive-organization";
}
