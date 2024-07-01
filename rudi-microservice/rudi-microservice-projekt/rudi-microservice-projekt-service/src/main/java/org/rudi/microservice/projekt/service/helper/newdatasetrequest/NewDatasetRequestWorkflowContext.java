/**
 * RUDI Portail
 */
package org.rudi.microservice.projekt.service.helper.newdatasetrequest;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.script.ScriptContext;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.bean.UserType;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.bean.workflow.EMailData;
import org.rudi.facet.bpmn.bean.workflow.EMailDataModel;
import org.rudi.facet.bpmn.entity.workflow.AssetDescriptionEntity;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.generator.text.impl.TemplateGeneratorImpl;
import org.rudi.facet.organization.bean.Organization;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationException;
import org.rudi.microservice.projekt.service.helper.AbstractProjektWorkflowContext;
import org.rudi.microservice.projekt.storage.dao.newdatasetrequest.NewDatasetRequestDao;
import org.rudi.microservice.projekt.storage.dao.project.ProjectCustomDao;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestEntity;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestStatus;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 *
 */
@Component(value = "newDatasetRequestWorkflowContext")
@Transactional(readOnly = true)
@Slf4j
public class NewDatasetRequestWorkflowContext extends
		AbstractProjektWorkflowContext<NewDatasetRequestEntity, NewDatasetRequestDao, NewDatasetRequestAssigmentHelper> {

	private final ProjectCustomDao projectCustomDao;
	private final OrganizationHelper organizationHelper;

	public NewDatasetRequestWorkflowContext(EMailService eMailService, TemplateGeneratorImpl templateGenerator,
			NewDatasetRequestDao assetDescriptionDao, NewDatasetRequestAssigmentHelper assignmentHelper,
			ACLHelper aclHelper, FormHelper formHelper, ProjectCustomDao projectCustomDao, OrganizationHelper organizationHelper) {
		super(eMailService, templateGenerator, assetDescriptionDao, assignmentHelper, aclHelper, formHelper);
		this.projectCustomDao = projectCustomDao;
		this.organizationHelper = organizationHelper;
	}

	@Transactional(readOnly = false)
	public void addData(ExecutionEntity executionEntity, String key, Object value) {
		NewDatasetRequestEntity datasetRequestEntity;
		try {
			datasetRequestEntity = super.injectData(executionEntity, key, value);
			getAssetDescriptionDao().save(datasetRequestEntity);
			log.debug("WkC - Update data {} for asset {}", key, datasetRequestEntity);
		} catch (AppServiceException e) {
			log.error("WkC - Error in update data {}", key, e);

		}

	}

	@Transactional(readOnly = false)
	public void updateStatus(ScriptContext scriptContext, ExecutionEntity executionEntity, String statusValue,
			String newDatasetRequestStatusValue, String functionalStatusValue) {
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		log.debug("WkC - Update {} to status {}", processInstanceBusinessKey, statusValue);
		Status status = Status.valueOf(statusValue);
		NewDatasetRequestStatus assetStatus = NewDatasetRequestStatus.valueOf(newDatasetRequestStatusValue);
		if (processInstanceBusinessKey != null && status != null && functionalStatusValue != null) {
			UUID uuid = UUID.fromString(processInstanceBusinessKey);
			NewDatasetRequestEntity assetDescription = getAssetDescriptionDao().findByUuid(uuid);
			if (assetDescription != null) {
				assetDescription.setStatus(status);
				assetDescription.setNewDatasetRequestStatus(assetStatus);
				assetDescription.setFunctionalStatus(functionalStatusValue);
				assetDescription.setUpdatedDate(LocalDateTime.now());
				getAssetDescriptionDao().save(assetDescription);
				log.debug("WkC - Update {} to status {}/{}/{} done.", processInstanceBusinessKey, statusValue,
						newDatasetRequestStatusValue, functionalStatusValue);
			} else {
				log.debug("WkC - Unkown {} skipped.", processInstanceBusinessKey);
			}
		} else {
			log.debug("WkC - Update {} to status {} skipped.", processInstanceBusinessKey, statusValue);
		}
	}

	@Override
	protected void addEmailDataModelData(
			EMailDataModel<NewDatasetRequestEntity, NewDatasetRequestAssigmentHelper> eMailDataModel) {
		super.addEmailDataModelData(eMailDataModel);
		ProjectEntity projectEntity = projectCustomDao
				.findProjectByNewDatasetRequestUuid(eMailDataModel.getAssetDescription().getUuid());
		if (projectEntity != null) {
			eMailDataModel.addData("project", projectEntity);
		}
	}

	/**
	 * Envoi de courriel
	 *
	 * @param scriptContext   le context du script
	 * @param executionEntity le context d'execution
	 * @param eMailData
	 * @param roleCode        le code d'un rôle destinataire
	 */
	@Override
	public void sendEMailToRole(ScriptContext scriptContext, ExecutionEntity executionEntity, EMailData eMailData, String roleCode) {
		AssetDescriptionEntity assetDescription = lookupAssetDescriptionEntity(executionEntity);

		try{
			//On rajoute le nom de l'utilisateur ou de l'organisateur ayant initié le projet.
			injectData(executionEntity,"userName", getUserDenomination(assetDescription.getInitiator()));
		}catch (AppServiceException e) {
			log.warn("une erreur est survenur lors du chargement de l'organisation: {}",assetDescription.getInitiator());
		}

		super.sendEMailToRole(scriptContext, executionEntity, eMailData, roleCode);
	}

	private String getUserDenomination(String inititator) throws GetOrganizationException {
		User u = getAclHelper().getUserByLogin(inititator);
		if (u != null && u.getType().equals(UserType.PERSON)) {
			return String.format("%s %s",u.getLastname(), u.getFirstname()).trim();
		}
		else {
			Organization o = organizationHelper.getOrganization(UUID.fromString(inititator));
			if(o != null){
				return o.getName();
			}
		}
		log.error("Aucun utilisateur trouvé et aucune organisation non plus {}",inititator);
		return StringUtils.EMPTY;
	}
}
