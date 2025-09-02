package org.rudi.microservice.strukture.service.workflow;

import static org.rudi.microservice.strukture.service.helper.organization.OrganizationWorkflowHelper.DRAFT_TYPE_FORM_ARCHIVE_VALUE;
import static org.rudi.microservice.strukture.service.workflow.StruktureWorkflowConstants.FIELD_NAME_IMAGE_ORGANIZATION;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.service.exception.AppServiceForbiddenException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.common.service.util.ApplicationContext;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.facet.bpmn.service.FormService;
import org.rudi.facet.bpmn.service.InitializationService;
import org.rudi.facet.bpmn.service.TaskService;
import org.rudi.facet.bpmn.service.impl.AbstractTaskServiceImpl;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.DatasetSearchCriteria;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.projekt.helper.ProjektHelper;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.service.helper.LinkedProducerHelper;
import org.rudi.microservice.strukture.service.helper.StruktureAuthorisationHelper;
import org.rudi.microservice.strukture.service.helper.attachments.AttachmentsHelper;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationAssignmentHelper;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationWorkflowContext;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationWorkflowHelper;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrganizationTaskServiceImpl extends
		AbstractTaskServiceImpl<OrganizationEntity, Organization, OrganizationDao, OrganizationWorkflowHelper, OrganizationAssignmentHelper, OrganizationWorkflowContext> {

	public static final String PROCESS_DEFINITION_ID = "organization-process";

	public static final String WORFKLOW_CONTEXT_BEAN_NAME = "organizationWorkflowContext";

	private final FormService formService;
	private final AttachmentsHelper attachmentsHelper;
	private final StruktureAuthorisationHelper struktureAuthorisationHelper;
	private final OrganizationWorkflowHelper organizationWorkflowHelper;
	private final DatasetService datasetService;
	private final ProjektHelper projektHelper;
	private final TaskService<LinkedProducer> linkedProducerTaskService;
	private final LinkedProducerHelper linkedProducerHelper;

	public OrganizationTaskServiceImpl(ProcessEngine processEngine, FormHelper formHelper, BpmnHelper bpmnHelper,
			UtilContextHelper utilContextHelper, InitializationService initializationService,
			OrganizationDao assetDescriptionDao, OrganizationWorkflowHelper assetDescriptionHelper,
			OrganizationAssignmentHelper assignmentHelper, OrganizationWorkflowContext workflowContext,
			ProcessEngineConfiguration processEngineConfiguration, FormService formService,
			StruktureAuthorisationHelper struktureAuthorisationHelper, AttachmentsHelper attachmentsHelper,
			OrganizationWorkflowHelper organizationWorkflowHelper, DatasetService datasetService,
			ProjektHelper projektHelper, TaskService<LinkedProducer> linkedProducerTaskService,
			LinkedProducerHelper linkedProducerHelper) {
		super(processEngine, formHelper, bpmnHelper, utilContextHelper, initializationService, assetDescriptionDao,
				assetDescriptionHelper, assignmentHelper, workflowContext, processEngineConfiguration);
		this.formService = formService;
		this.struktureAuthorisationHelper = struktureAuthorisationHelper;
		this.attachmentsHelper = attachmentsHelper;
		this.organizationWorkflowHelper = organizationWorkflowHelper;
		this.datasetService = datasetService;
		this.projektHelper = projektHelper;
		this.linkedProducerTaskService = linkedProducerTaskService;
		this.linkedProducerHelper = linkedProducerHelper;
	}

	@Override
	protected String getWorkflowContextBeanName() {
		return WORFKLOW_CONTEXT_BEAN_NAME;
	}

	@Override
	public String getProcessDefinitionKey() {
		return PROCESS_DEFINITION_ID;
	}

	@Override
	protected AbstractTaskServiceImpl<OrganizationEntity, Organization, OrganizationDao, OrganizationWorkflowHelper, OrganizationAssignmentHelper, OrganizationWorkflowContext> lookupMe() {
		return ApplicationContext.getBean(OrganizationTaskServiceImpl.class);
	}

	@Override
	@PostConstruct
	public void loadBpmn() throws IOException {
		super.loadBpmn();
		Map<String, Object> properties = formService.getFormTemplateProperties();
		formService.createOrUpdateAllSectionAndFormDefinitions(properties);
	}

	@Override
	protected void checkRightsOnInitEntity(OrganizationEntity assetDescriptionEntity) throws IllegalArgumentException {
		try {
			struktureAuthorisationHelper.checkRightsOnInitOrganization(assetDescriptionEntity);
		} catch (AppServiceUnauthorizedException e) {
			throw new IllegalArgumentException(
					"Erreur lors de la vérification des droits pour le traitement de la tache d'organisation", e);
		}
	}

	/**
	 * pour surcharge éventuelle
	 *
	 * @param assetDescriptionEntity
	 */
	@Override
	protected void beforeStart(OrganizationEntity assetDescriptionEntity) throws InvalidDataException {

		// On réhydrate les data pour voir s'il y a une image
		Map<String, Object> data = getFormHelper().hydrateData(assetDescriptionEntity.getData());
		try {
			// Si l'image est présente, on récupère ses metadatas
			if (data != null && data.containsKey(FIELD_NAME_IMAGE_ORGANIZATION)) {
				UUID mediaUuid = UUID.fromString((String) data.get(FIELD_NAME_IMAGE_ORGANIZATION));
				// Pour vérifier que l'initiator est bien le "créateur de l'image"
				attachmentsHelper.checkIfAuthenticatedUserCanDeleteDocument(mediaUuid);
			}
		} catch (AppServiceNotFoundException e) {
			throw new InvalidDataException("Image not found");
		} catch (AppServiceForbiddenException | AppServiceUnauthorizedException e) {
			throw new InvalidDataException("Unauthorized to add this image");
		}
	}

	/**
	 * Méthode pour ajouter des données dans les variables du processus
	 *
	 * @param variables
	 * @param assetDescriptionEntity
	 */
	@Override
	protected void fillProcessVariables(Map<String, Object> variables, OrganizationEntity assetDescriptionEntity)
			throws InvalidDataException {
		if (assetDescriptionEntity.getOrganizationStatus() != null) {
			variables.put(StruktureWorkflowConstants.ORGANIZATION_STATUS,
					assetDescriptionEntity.getOrganizationStatus());
		}

		if (assetDescriptionEntity.getData() != null) {
			variables.put(StruktureWorkflowConstants.ORGANIZATION_DRAFT_TYPE,
					organizationWorkflowHelper.getDraftType(assetDescriptionEntity));
		}
	}

	/**
	 * Controle de l'état de l'entité
	 *
	 * @param assetDescriptionEntity l'entité
	 * @throws IllegalArgumentException si l'argument est incorrect
	 * @throws InvalidDataException     si les données (data) de l'entité ne peuvent être désérialisée
	 */
	@Override
	protected void checkEntityStatus(OrganizationEntity assetDescriptionEntity)
			throws IllegalArgumentException, InvalidDataException {
		if (assetDescriptionEntity == null || getBpmnHelper().queryTaskByAssetId(assetDescriptionEntity.getClass(),
				assetDescriptionEntity.getId()) != null
				&& (assetDescriptionEntity.getStatus().equals(Status.DRAFT)
						|| assetDescriptionEntity.getStatus().equals(Status.COMPLETED))) {
			throw new IllegalArgumentException("Asset is already linked to a task");
		}

		// Vérifie si l'état de l'asset est DRAFT (création de project)
		boolean isDraft = assetDescriptionEntity.getStatus().equals(Status.DRAFT);
		String draftType = organizationWorkflowHelper.getDraftType(assetDescriptionEntity);

		if (isDraft && StringUtils.isEmpty(draftType)) {
			log.debug("Cas de création d'organisation, pas d'information supplémentaire à vérifier");
			return;
		}

		boolean isArchive = Strings.CS.equals(DRAFT_TYPE_FORM_ARCHIVE_VALUE, draftType);

		if (isArchive) {
			checkExistingDataset(assetDescriptionEntity); // refus si l'organisation a des JDD
			checkStatusLinkedProducers(assetDescriptionEntity); // refus si l'organisation a des sous workflow en cours
			hasProjectOwnerRunningTask(assetDescriptionEntity); // refus si l'organisation a des projets en cours ou en attente de validation
		}
		// Else cas de la modification
	}

	private void hasProjectOwnerRunningTask(OrganizationEntity assetDescriptionEntity) {
		if (projektHelper.hasProjectOwnerRunningTask(assetDescriptionEntity.getUuid())) {
			throw new IllegalArgumentException(
					"Impossible d'archiver une organisation avec des projets en cours ou en attente de validation."
							+ assetDescriptionEntity.getUuid());
		}

	}

	private void checkExistingDataset(OrganizationEntity assetDescriptionEntity) {

		DatasetSearchCriteria datasetSearchCriteria = new DatasetSearchCriteria();
		datasetSearchCriteria.setProducerUuids(List.of(assetDescriptionEntity.getUuid()));
		try {
			if (datasetService.datasetExists(datasetSearchCriteria)) {
				throw new IllegalArgumentException("Impossible d'archiver une organisation ayant publié des JDD."
						+ assetDescriptionEntity.getUuid());
			}
		} catch (DataverseAPIException e) {
			log.error("Error while checking if JDD are attached to the organization {}",
					assetDescriptionEntity.getUuid(), e);
			throw new IllegalArgumentException(
					"Impossible d'archiver une organisation : impossible de savoir si des JDD y sont attachés."
							+ assetDescriptionEntity.getUuid(),
					e);
		}

	}

	private void checkStatusLinkedProducers(OrganizationEntity assetDescriptionEntity) {
		// Si l'organisation a des linkedProducers, on vérifie qu'il n'y a pas de workflow en cours
		List<LinkedProducerEntity> linkedProducers = linkedProducerHelper
				.getLinkedProducersFromOrganizationUuid(assetDescriptionEntity.getUuid());
		if (CollectionUtils.isNotEmpty(linkedProducers)) {
			for (final LinkedProducerEntity linkedProducerEntity : linkedProducers) {
				if (linkedProducerTaskService.hasTask(linkedProducerEntity.getUuid())) {
					throw new IllegalArgumentException(
							"Impossible d'archiver une organisation avec un rattachement ou un détachement en cours."
									+ assetDescriptionEntity.getUuid());
				}
			}
		}

	}
}
