/**
 * RUDI Portail
 */
package org.rudi.microservice.projekt.service.workflow;

import org.rudi.facet.bpmn.helper.workflow.WorkflowConstants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author FNI18300
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjektWorkflowConstants extends WorkflowConstants {

	public static final String PROJECT_STATUS = "projectStatus";

	public static final String TITLE = "title";

	public static final String OWNER_PROJECT_UUID = "ownerProjectUuid";

	public static final String DATASET_PRODUCER_UUID = "datasetProducerUuid";

	public static final String PROJECT_DRAFT_FORM_SECTION_NAME = "project-process-draft/modified-project";
	public static final String PROJECT_PICTURE_SECTION_NAME = "project-process-draft/modified-project-picture";

	public static final String TITLE_FIELD_NAME = "title";
	public static final String DESCRIPTION_FIELD_NAME = "description";
	public static final String EXPECTED_COMPLETION_END_DATE_FIELD_NAME = "expectedCompletionEndDate";
	public static final String EXPECTED_COMPLETION_START_DATE_FIELD_NAME = "expectedCompletionStartDate";
	public static final String TARGET_AUDIENCES_FIELD_NAME = "targetAudiences";
	public static final String TERRITORIAL_SCALE_FIELD_NAME = "territorialScale";
	public static final String DETAILED_TERRITORIAL_SCALE_FIELD_NAME = "detailedTerritorialScale";
	public static final String TYPE_FIELD_NAME = "type";
	public static final String REUTILISATION_STATUS_FIELD_NAME = "reutilisationStatus";
	public static final String DESIRED_SUPPORTS_FIELD_NAME = "desiredSupports";
	public static final String CONFIDENTIALITY_FIELD_NAME = "confidentiality";
	public static final String ACCESS_URL_FIELD_NAME = "accessUrl";



}
