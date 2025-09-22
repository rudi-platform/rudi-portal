/**
 * RUDI Portail
 */
package org.rudi.microservice.projekt.service.workflow;

import java.io.IOException;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.common.service.exception.MissingParameterException;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.common.service.util.ApplicationContext;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.facet.bpmn.service.InitializationService;
import org.rudi.facet.bpmn.service.impl.AbstractTaskServiceImpl;
import org.rudi.facet.organization.bean.Organization;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationException;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationMembersException;
import org.rudi.microservice.projekt.core.bean.LinkedDataset;
import org.rudi.microservice.projekt.service.helper.ProjektAuthorisationHelper;
import org.rudi.microservice.projekt.service.helper.linkeddataset.LinkedDatasetAssigmentHelper;
import org.rudi.microservice.projekt.service.helper.linkeddataset.LinkedDatasetWorkflowContext;
import org.rudi.microservice.projekt.service.helper.linkeddataset.LinkedDatasetWorkflowHelper;
import org.rudi.microservice.projekt.storage.dao.linkeddataset.LinkedDatasetDao;
import org.rudi.microservice.projekt.storage.dao.project.ProjectCustomDao;
import org.rudi.microservice.projekt.storage.entity.OwnerType;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 */
@Service
@Slf4j
public class LinkedDatasetTaskServiceImpl extends
		AbstractTaskServiceImpl<LinkedDatasetEntity, LinkedDataset, LinkedDatasetDao, LinkedDatasetWorkflowHelper, LinkedDatasetAssigmentHelper, LinkedDatasetWorkflowContext> {

	public static final String PROCESS_DEFINITION_ID = "linked-dataset-process";

	public static final String WORFKLOW_CONTEXT_BEAN_NAME = "linkedDatasetWorkflowContext";

	private final ProjectCustomDao projectCustomDao;

	@Autowired
	private ProjektAuthorisationHelper projektAuthorisationHelper;
	@Autowired
	private OrganizationHelper organizationHelper;

	public LinkedDatasetTaskServiceImpl(ProcessEngine processEngine, FormHelper formHelper, BpmnHelper bpmnHelper,
			UtilContextHelper utilContextHelper, InitializationService initializationService,
			LinkedDatasetDao assetDescriptionDao, LinkedDatasetWorkflowHelper assetDescriptionHelper,
			LinkedDatasetAssigmentHelper assigmentHelper, LinkedDatasetWorkflowContext workflowContext,
			ProcessEngineConfiguration processEngineConfiguration, ProjectCustomDao projectCustomDao) {
		super(processEngine, formHelper, bpmnHelper, utilContextHelper, initializationService, assetDescriptionDao,
				assetDescriptionHelper, assigmentHelper, workflowContext, processEngineConfiguration);
		this.projectCustomDao = projectCustomDao;
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
	protected AbstractTaskServiceImpl<LinkedDatasetEntity, LinkedDataset, LinkedDatasetDao, LinkedDatasetWorkflowHelper, LinkedDatasetAssigmentHelper, LinkedDatasetWorkflowContext> lookupMe() {
		return ApplicationContext.getBean(LinkedDatasetTaskServiceImpl.class);
	}

	@Override
	protected void fillProcessVariables(Map<String, Object> variables, LinkedDatasetEntity assetDescriptionEntity) {
		ProjectEntity projectEntity = projectCustomDao.findProjectByLinkedDatasetUuid(assetDescriptionEntity.getUuid());
		if (projectEntity != null) {
			variables.put(ProjektWorkflowConstants.OWNER_PROJECT_UUID, projectEntity.getUuid());
		}

		variables.put(ProjektWorkflowConstants.DATASET_PRODUCER_UUID,
				assetDescriptionEntity.getDatasetOrganisationUuid());
	}

	@PostConstruct
	@Override
	public void loadBpmn() throws IOException {
		super.loadBpmn();
	}

	/**
	 * @param assetDescriptionEntity linkedDatasetEntity
	 */
	@Override
	protected void updateAssetCreation(LinkedDatasetEntity assetDescriptionEntity) {
		super.updateAssetCreation(assetDescriptionEntity);

		// Réécriture de l'initiator : initator de la demande de jeux de donnée = owner du projet associé
		ProjectEntity projectEntity = projectCustomDao.findProjectByLinkedDatasetUuid(assetDescriptionEntity.getUuid());
		if (projectEntity != null) {
			if (projectEntity.getOwnerType().equals(OwnerType.USER)) {
				User user = getAssignmentHelper().getUserByUuid(projectEntity.getOwnerUuid());
				if (user != null) {
					assetDescriptionEntity.setInitiator(user.getLogin());
				}
			} else {
				// chargement de l'orga
				try {
					Organization organization = organizationHelper.getOrganization(projectEntity.getOwnerUuid());
					if (organization != null) {
						assetDescriptionEntity.setInitiator(organization.getUuid().toString());
					}
				} catch (GetOrganizationException e) {
					log.error(
							"Une erreur est survenue lors du chargement de l'organization d'uuid {} owner du projet {}",
							projectEntity.getUuid(), projectEntity.getUuid(), e);
				}
			}
		}
	}

	@Override
	protected void checkRightsOnInitEntity(LinkedDatasetEntity assetDescriptionEntity) throws IllegalArgumentException {

		ProjectEntity projectEntity = projectCustomDao.findProjectByLinkedDatasetUuid(assetDescriptionEntity.getUuid());
		if (projectEntity != null) {
			try {
				projektAuthorisationHelper.checkRightsAdministerProject(projectEntity);
			} catch (GetOrganizationMembersException | MissingParameterException | AppServiceUnauthorizedException e) {
				throw new IllegalArgumentException(
						"Erreur lors de la vérification des droits pour le traitement de la tache de linkeddataset", e);
			}
		}
	}

	/**
	 * @param assetDescriptionEntity
	 */
	@Override
	protected void checkEntityStatus(LinkedDatasetEntity assetDescriptionEntity)
			throws AppServiceException, InvalidDataException {
		super.checkEntityStatus(assetDescriptionEntity);

		ProjectEntity projectEntity = projectCustomDao.findProjectByLinkedDatasetUuid(assetDescriptionEntity.getUuid());

		if (projectEntity == null
				|| getBpmnHelper().queryTaskByAssetId(projectEntity.getClass(), projectEntity.getId()) != null
						&& (projectEntity.getStatus().equals(Status.DRAFT)
								|| projectEntity.getStatus().equals(Status.PENDING))) {
			throw new AppServiceBadRequestException("Projekt is linked to a running task");
		}
	}

}
