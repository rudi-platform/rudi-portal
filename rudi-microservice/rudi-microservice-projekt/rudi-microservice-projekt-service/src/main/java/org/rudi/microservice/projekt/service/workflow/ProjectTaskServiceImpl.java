/**
 * RUDI Portail
 */
package org.rudi.microservice.projekt.service.workflow;

import static org.rudi.microservice.projekt.service.helper.project.ProjectWorkflowHelper.DRAFT_TYPE_FORM_ARCHIVE_VALUE;
import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.DRAFT_FORM_SECTION_NAME;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.rudi.bpmn.core.bean.Form;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.common.service.exception.MissingParameterException;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.common.service.util.ApplicationContext;
import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.facet.bpmn.service.FormService;
import org.rudi.facet.bpmn.service.InitializationService;
import org.rudi.facet.bpmn.service.impl.AbstractTaskServiceImpl;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationMembersException;
import org.rudi.microservice.projekt.core.bean.Project;
import org.rudi.microservice.projekt.service.helper.ProjektAuthorisationHelper;
import org.rudi.microservice.projekt.service.helper.project.ProjectAssigmentHelper;
import org.rudi.microservice.projekt.service.helper.project.ProjectWorkflowContext;
import org.rudi.microservice.projekt.service.helper.project.ProjectWorkflowHelper;
import org.rudi.microservice.projekt.storage.dao.project.ProjectDao;
import org.rudi.microservice.projekt.storage.entity.DatasetConfidentiality;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 */
@Service
@Slf4j
public class ProjectTaskServiceImpl extends
		AbstractTaskServiceImpl<ProjectEntity, Project, ProjectDao, ProjectWorkflowHelper, ProjectAssigmentHelper, ProjectWorkflowContext> {

	private static final String PROCESS_DEFINITION_ID = "project-process";

	public static final String WORFKLOW_CONTEXT_BEAN_NAME = "projectWorkflowContext";

	@Value("${rudi.project.task.allowed.status.administrator}")
	private List<String> administratorAllowedModificationStatus;

	@Value("${rudi.project.task.allowed.status.owner}")
	private List<String> projectOwnerAllowedModificationStatus;

	@Autowired
	private ProjektFormEnhancerHelper projektFormEnhancerHelper;

	@Autowired
	private ProjektAuthorisationHelper projektAuthorisationHelper;

	@Autowired
	private FormService formService;
	@Autowired
	private ProjectWorkflowHelper projectWorkflowHelper;

	public ProjectTaskServiceImpl(ProcessEngine processEngine, FormHelper formHelper, BpmnHelper bpmnHelper,
			UtilContextHelper utilContextHelper, InitializationService initializationService,
			ProjectDao assetDescriptionDao, ProjectWorkflowHelper assetDescriptionHelper,
			ProjectAssigmentHelper assigmentHelper, ProjectWorkflowContext workflowContext,
			ProcessEngineConfiguration processEngineConfiguration) {
		super(processEngine, formHelper, bpmnHelper, utilContextHelper, initializationService, assetDescriptionDao,
				assetDescriptionHelper, assigmentHelper, workflowContext, processEngineConfiguration);
	}

	@Override
	public String getProcessDefinitionKey() {
		return PROCESS_DEFINITION_ID;
	}

	@Override
	protected String getWorkflowContextBeanName() {
		return WORFKLOW_CONTEXT_BEAN_NAME;
	}

	@Override
	protected AbstractTaskServiceImpl<ProjectEntity, Project, ProjectDao, ProjectWorkflowHelper, ProjectAssigmentHelper, ProjectWorkflowContext> lookupMe() {
		return ApplicationContext.getBean(ProjectTaskServiceImpl.class);
	}

	@Override
	protected void fillProcessVariables(Map<String, Object> variables, ProjectEntity assetDescriptionEntity)
			throws InvalidDataException {
		variables.put(ProjektWorkflowConstants.TITLE, assetDescriptionEntity.getTitle());
		if (assetDescriptionEntity.getProjectStatus() != null) {
			variables.put(ProjektWorkflowConstants.PROJECT_STATUS, assetDescriptionEntity.getProjectStatus().name());
		}
		if (assetDescriptionEntity.getData() != null) {
			variables.put(ProjektWorkflowConstants.PROJECT_DRAFT_TYPE,
					projectWorkflowHelper.getDraftType(assetDescriptionEntity));
		}
	}

	@Override
	@PostConstruct
	public void loadBpmn() throws IOException {
		super.loadBpmn();
		Map<String, Object> properties = formService.getFormTemplateProperties();
		formService.createOrUpdateAllSectionAndFormDefinitions(properties);
	}

	/**
	 * @param assetDescriptionEntity
	 */
	@Override
	protected boolean checkUpdate(ProjectEntity assetDescriptionEntity) {
		boolean result = super.checkUpdate(assetDescriptionEntity);
		List<String> allowedStatus = isCurrentAdmin() ? administratorAllowedModificationStatus
				: projectOwnerAllowedModificationStatus;

		return allowedStatus.contains(assetDescriptionEntity.getProjectStatus().name()) && result;
	}

	@Override
	protected void checkRightsOnInitEntity(ProjectEntity assetDescriptionEntity) throws IllegalArgumentException {
		try {
			projektAuthorisationHelper.checkRightsInitProject(assetDescriptionEntity);
		} catch (GetOrganizationMembersException | MissingParameterException | AppServiceUnauthorizedException e) {
			throw new IllegalArgumentException(
					"Erreur lors de la vérification des droits pour le traitement de la tache de projet", e);
		}
	}

	/**
	 * @param assetDescriptionEntity
	 */
	@Override
	protected void checkEntityStatus(ProjectEntity assetDescriptionEntity)
			throws IllegalArgumentException, InvalidDataException {
		if (assetDescriptionEntity == null || getBpmnHelper().queryTaskByAssetId(assetDescriptionEntity.getClass(),
				assetDescriptionEntity.getId()) != null
				&& (assetDescriptionEntity.getStatus().equals(Status.DRAFT)
						|| assetDescriptionEntity.getStatus().equals(Status.COMPLETED))) {
			throw new IllegalArgumentException("Asset is already linked to a task");
		}

		// Vérifie si l'état de l'asset est DRAFT (création de project)
		boolean isDraft = assetDescriptionEntity.getStatus().equals(Status.DRAFT);

		if (isDraft) {
			log.debug("Cas de création de projet, pas d'information supplémentaire à vérifier");
			return;
		}

		String draftType = projectWorkflowHelper.getDraftType(assetDescriptionEntity);
		boolean isArchive = !StringUtils.equals(DRAFT_TYPE_FORM_ARCHIVE_VALUE, draftType);

		if (isArchive) {
			checkStatusLinkedDataset(assetDescriptionEntity);
			checkStatusNewDatasetRequest(assetDescriptionEntity);
		} else {
			// entrée dans le workflow pour modification de la reutilisation
			// Vérifie si l'état est COMPLETED (pas de workflow en cours, le dernier workflow sur le projet est terminé) et s'il n'y a pas de dataset restreint
			boolean isCompletedAndNoRestrictedDataset = assetDescriptionEntity.getStatus().equals(Status.COMPLETED)
					&& !isAnyRestrictedDatasetOnProjekt(assetDescriptionEntity);

			// Si l'état n'est ni DRAFT, ni [COMPLETED avec un dataset non restreint], on lève une exception
			if (!isCompletedAndNoRestrictedDataset) {
				log.error(
						"Invalid status for project {} : project is draft = {}, project is completed without restricted dataset = {}",
						assetDescriptionEntity.getUuid(), isDraft, isCompletedAndNoRestrictedDataset);
				throw new IllegalArgumentException(
						"Invalid status or dataset type for project " + assetDescriptionEntity.getUuid());
			}
		}
	}

	private boolean isAnyRestrictedDatasetOnProjekt(ProjectEntity assetDescriptionEntity) {
		return assetDescriptionEntity.getLinkedDatasets().stream()
				.anyMatch(a -> CollectionUtils.containsAny(
						List.of(DatasetConfidentiality.RESTRICTED, DatasetConfidentiality.SELFDATA),
						a.getDatasetConfidentiality()));
	}

	/**
	 * @return
	 * @throws FormDefinitionException
	 */
	@Nullable
	@Override
	public Form lookupDraftForm(String formType) throws FormDefinitionException {
		Form form = super.lookupDraftForm(formType);

		if (form != null) {
			form.getSections().stream().filter(s -> s.getName().equals(DRAFT_FORM_SECTION_NAME)).findFirst()
					.ifPresent(value -> projektFormEnhancerHelper.enhance(value));
		}

		return form;
	}

	private void checkStatusLinkedDataset(ProjectEntity assetDescriptionEntity) {
		// vérif des etats des linkeddatasets
		// ils doivent être COMPLETED | CANCELLED | DELETED
		for (final LinkedDatasetEntity linkedDataset : assetDescriptionEntity.getLinkedDatasets()) {
			if (linkedDataset.getStatus() != Status.COMPLETED && linkedDataset.getStatus() != Status.CANCELLED
					&& linkedDataset.getStatus() != Status.DELETED) {
				throw new IllegalArgumentException(
						"Invalid dataset status type for project " + assetDescriptionEntity.getUuid());
			}
		}
	}

	private void checkStatusNewDatasetRequest(ProjectEntity assetDescriptionEntity) {
		// vérif des etats des NewDatasetRequest
		// ils doivent être COMPLETED | CANCELLED | DELETED
		for (final NewDatasetRequestEntity newDatasetRequest : assetDescriptionEntity.getDatasetRequests()) {
			if (newDatasetRequest.getStatus() != Status.COMPLETED && newDatasetRequest.getStatus() != Status.CANCELLED
					&& newDatasetRequest.getStatus() != Status.DELETED) {
				throw new IllegalArgumentException(
						"Invalid dataset status type for project " + assetDescriptionEntity.getUuid());
			}
		}
	}
}
