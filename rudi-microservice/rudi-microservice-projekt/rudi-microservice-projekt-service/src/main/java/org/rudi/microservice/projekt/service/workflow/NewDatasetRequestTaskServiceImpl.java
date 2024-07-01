/**
 * RUDI Portail
 */
package org.rudi.microservice.projekt.service.workflow;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.activiti.engine.ProcessEngine;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.common.service.exception.MissingParameterException;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.common.service.util.ApplicationContext;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.facet.bpmn.service.InitializationService;
import org.rudi.facet.bpmn.service.impl.AbstractTaskServiceImpl;
import org.rudi.facet.organization.bean.Organization;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationException;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationMembersException;
import org.rudi.microservice.projekt.core.bean.NewDatasetRequest;
import org.rudi.microservice.projekt.service.helper.ProjektAuthorisationHelper;
import org.rudi.microservice.projekt.service.helper.newdatasetrequest.NewDatasetRequestAssigmentHelper;
import org.rudi.microservice.projekt.service.helper.newdatasetrequest.NewDatasetRequestWorkflowHelper;
import org.rudi.microservice.projekt.storage.dao.newdatasetrequest.NewDatasetRequestDao;
import org.rudi.microservice.projekt.storage.dao.project.ProjectCustomDao;
import org.rudi.microservice.projekt.storage.entity.OwnerType;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 *
 */
@Service
@Slf4j
public class NewDatasetRequestTaskServiceImpl extends
		AbstractTaskServiceImpl<NewDatasetRequestEntity, NewDatasetRequest, NewDatasetRequestDao, NewDatasetRequestWorkflowHelper, NewDatasetRequestAssigmentHelper> {

	private final ProjectCustomDao projectCustomDao;

	@Autowired
	private ProjektAuthorisationHelper projektAuthorisationHelper;

	@Autowired
	private OrganizationHelper organizationHelper;

	public NewDatasetRequestTaskServiceImpl(ProcessEngine processEngine, FormHelper formHelper, BpmnHelper bpmnHelper,
			UtilContextHelper utilContextHelper, InitializationService initializationService,
			NewDatasetRequestDao assetDescriptionDao, NewDatasetRequestWorkflowHelper assetDescriptionHelper,
			NewDatasetRequestAssigmentHelper assigmentHelper, ProjectCustomDao projectCustomDao) {
		super(processEngine, formHelper, bpmnHelper, utilContextHelper, initializationService, assetDescriptionDao,
				assetDescriptionHelper, assigmentHelper);
		this.projectCustomDao = projectCustomDao;
	}

	@Override
	protected void fillProcessVariables(Map<String, Object> variables, NewDatasetRequestEntity assetDescriptionEntity) {
		variables.put(ProjektWorkflowConstants.TITLE, assetDescriptionEntity.getTitle());

		ProjectEntity projectEntity = projectCustomDao
				.findProjectByNewDatasetRequestUuid(assetDescriptionEntity.getUuid());
		if (projectEntity != null) {
			variables.put(ProjektWorkflowConstants.OWNER_PROJECT_UUID, projectEntity.getUuid());
		}
	}

	@Override
	public String getProcessDefinitionKey() {
		return "new-dataset-request-process";
	}

	@PostConstruct
	@Override
	public void loadBpmn() throws IOException {
		super.loadBpmn();
	}

	@Override
	protected AbstractTaskServiceImpl<NewDatasetRequestEntity, NewDatasetRequest, NewDatasetRequestDao, NewDatasetRequestWorkflowHelper, NewDatasetRequestAssigmentHelper> lookupMe() {
		return ApplicationContext.getBean(NewDatasetRequestTaskServiceImpl.class);
	}

	/**
	 * @param assetDescriptionEntity newDatasetRequestEntity
	 */
	@Override
	protected void updateAssetCreation(NewDatasetRequestEntity assetDescriptionEntity) {
		super.updateAssetCreation(assetDescriptionEntity);

		// Réécriture de l'initiator : initator de la demande de nouveau jeux de donnée = owner du projet associé
		ProjectEntity projectEntity = projectCustomDao
				.findProjectByNewDatasetRequestUuid(assetDescriptionEntity.getUuid());
		if (projectEntity != null) {
			if(projectEntity.getOwnerType().equals(OwnerType.USER)){
				User user = getAssignmentHelper().getUserByUuid(projectEntity.getOwnerUuid());
				if (user != null) {
					assetDescriptionEntity.setInitiator(user.getLogin());
				}
			}
			else {
				//chargement de l'orga
				try{
					Organization organization = organizationHelper.getOrganization(projectEntity.getOwnerUuid());
					if(organization != null){
						assetDescriptionEntity.setInitiator(organization.getUuid().toString());
					}
				}catch (GetOrganizationException e){
					log.error("Une erreur est survenue lors du chargement de l'organization d'uuid {} owner du projet {}",projectEntity.getUuid(), projectEntity.getUuid(),e);
				}
			}
		}
	}

	@Override
	protected void checkRightsOnInitEntity(NewDatasetRequestEntity assetDescriptionEntity)
			throws IllegalArgumentException {
		ProjectEntity projectEntity = projectCustomDao
				.findProjectByNewDatasetRequestUuid(assetDescriptionEntity.getUuid());
		if (projectEntity != null) {
			try {
				projektAuthorisationHelper.checkRightsAdministerProject(projectEntity);
			} catch (GetOrganizationMembersException | MissingParameterException | AppServiceUnauthorizedException e) {
				throw new IllegalArgumentException(
						"Erreur lors de la vérification des droits pour le traitement de la tache de new dataset request",
						e);
			}
		}

	}

}
