package org.rudi.microservice.strukture.service.helper.provider;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.script.ScriptContext;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.core.security.RoleCodes;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.bean.UserType;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.bean.workflow.EMailData;
import org.rudi.facet.bpmn.bean.workflow.EMailDataModel;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.AbstractWorkflowContext;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.generator.text.TemplateGenerator;
import org.rudi.microservice.strukture.core.bean.IntegrationStatus;
import org.rudi.microservice.strukture.core.bean.Method;
import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.core.bean.Report;
import org.rudi.microservice.strukture.core.bean.ReportError;
import org.rudi.microservice.strukture.service.helper.NodeProviderUserHelper;
import org.rudi.microservice.strukture.service.helper.OwnerInfoHelper;
import org.rudi.microservice.strukture.service.helper.ProviderHelper;
import org.rudi.microservice.strukture.service.helper.ReportHelper;
import org.rudi.microservice.strukture.service.helper.ReportSendExecutor;
import org.rudi.microservice.strukture.service.integration.errors.IntegrationError;
import org.rudi.microservice.strukture.storage.dao.provider.LinkedProducerDao;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Component(value = "linkedProducerWorkflowContext")
@Transactional
@Slf4j
public class LinkedProducerWorkflowContext
		extends AbstractWorkflowContext<LinkedProducerEntity, LinkedProducerDao, LinkedProducerAssignmentHelper> {

	private static final String ATTACH_COMMENT_KEY = "messageToAttachmentInitiator";
	private static final String DETACH_COMMENT_KEY = "messageToDetachmentInitiator";

	@Value("${rudi.linked-producer.report.version:v1}")
	private String version;
	@Value("${rudi.linked-producer.report.attempts:3}")
	private int attempts;

	private final NodeProviderUserHelper nodeProviderUserHelper;
	private final ProviderHelper providerHelper;
	private final ReportHelper reportHelper;
	private final OwnerInfoHelper ownerInfoHelper;

	protected LinkedProducerWorkflowContext(EMailService eMailService, TemplateGenerator templateGenerator,
			LinkedProducerDao assetDescriptionDao, LinkedProducerAssignmentHelper assignmentHelper, ACLHelper aclHelper,
			FormHelper formHelper, NodeProviderUserHelper nodeProviderUserHelper, ProviderHelper providerHelper,
			ReportHelper reportHelper, OwnerInfoHelper ownerInfoHelper) {
		super(eMailService, templateGenerator, assetDescriptionDao, assignmentHelper, aclHelper, formHelper);
		this.nodeProviderUserHelper = nodeProviderUserHelper;
		this.providerHelper = providerHelper;
		this.reportHelper = reportHelper;
		this.ownerInfoHelper = ownerInfoHelper;
	}

	@Transactional(readOnly = false)
	@SuppressWarnings("unused") // Utilisé par linked-producer-process.bpmn20.xml
	public void updateStatus(ScriptContext scriptContext, ExecutionEntity executionEntity, String statusValue,
			String linkedProducerStatusValue, String functionalStatusValue) {
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		log.debug("WkC - Update {} to status {}, {}, {}", processInstanceBusinessKey, statusValue,
				linkedProducerStatusValue, functionalStatusValue);
		Status status = Status.valueOf(statusValue);
		LinkedProducerStatus linkedProducerStatus = StringUtils.isEmpty(linkedProducerStatusValue) ? null
				: LinkedProducerStatus.valueOf(linkedProducerStatusValue);

		if (!StringUtils.isEmpty(processInstanceBusinessKey) && status != null && functionalStatusValue != null) {
			UUID organizationUuid = UUID.fromString(processInstanceBusinessKey);
			LinkedProducerEntity assetDescriptionEntity = getAssetDescriptionDao().findByUuid(organizationUuid);
			if (assetDescriptionEntity != null) {
				assetDescriptionEntity.setStatus(status);
				assetDescriptionEntity.setLinkedProducerStatus(
						linkedProducerStatus == null ? assetDescriptionEntity.getLinkedProducerStatus()
								: linkedProducerStatus);
				assetDescriptionEntity.setFunctionalStatus(functionalStatusValue);
				assetDescriptionEntity.setUpdatedDate(LocalDateTime.now());
				getAssetDescriptionDao().save(assetDescriptionEntity);
				log.debug("WkC - Update {} to status {} done.", processInstanceBusinessKey, statusValue);
			} else {
				log.debug("WkC - Unkown {} skipped.", processInstanceBusinessKey);
			}
		} else {
			log.debug("WkC - Update {} to status {} skipped.", processInstanceBusinessKey, statusValue);
		}
	}

	@SuppressWarnings("unused") // Utilisé par linked-producer-process.bpmn20.xml
	public void contactAttachmentInitator(ScriptContext scriptContext, ExecutionEntity executionEntity,
			EMailData eMailData, boolean isValidated) {
		LinkedProducerEntity assetDescriptionEntity = lookupAssetDescriptionEntity(executionEntity);
		contactInitiator(executionEntity, eMailData, buildAttachReport(assetDescriptionEntity, isValidated));
	}

	@SuppressWarnings("unused") // Utilisé par linked-producer-process.bpmn20.xml
	public void contactDetachmentInitator(ScriptContext scriptContext, ExecutionEntity executionEntity,
			EMailData eMailData, boolean isValidated) {
		LinkedProducerEntity assetDescriptionEntity = lookupAssetDescriptionEntity(executionEntity);

		contactInitiator(executionEntity, eMailData, buildDetachReport(assetDescriptionEntity, isValidated));
	}

	@SuppressWarnings("unused") // Utilisé par linked-producer-process.bpmn20.xml
	public void detach(ScriptContext scriptContext, ExecutionEntity executionEntity) {
		LinkedProducerEntity assetDescriptionEntity = lookupAssetDescriptionEntity(executionEntity);
		if (assetDescriptionEntity != null
				&& assetDescriptionEntity.getLinkedProducerStatus().equals(LinkedProducerStatus.DISENGAGED)) {
			getAssetDescriptionDao().delete(assetDescriptionEntity);
		}
	}

	private void contactInitiator(ExecutionEntity executionEntity, EMailData eMailData, Report report) {
		LinkedProducerEntity assetDescriptionEntity = lookupAssetDescriptionEntity(executionEntity);

		User initiator = lookupUser(assetDescriptionEntity.getInitiator());

		String email = lookupEMailAddress(initiator);
		if (initiator != null && initiator.getType().equals(UserType.ROBOT)
				&& initiator.getRoles().stream().anyMatch(role -> role.getCode().equals(RoleCodes.PROVIDER))) {
			// On est dans le cas d'un provider : donc rapport
			NodeProvider nodeProvider = nodeProviderUserHelper.getNodeProviderFromUser(initiator);
			if (nodeProvider == null) {
				throw new InvalidParameterException("Node introuvable");
			}

			String providerContactEmail = providerHelper.getContactEmail(nodeProvider);
			if (providerContactEmail != null) {
				email = providerContactEmail;
			}

			// L'identifiant utilisé ccôté noeud est celui de l'organisation
			Thread thread = new Thread(new ReportSendExecutor(reportHelper, report, nodeProvider, attempts,
					assetDescriptionEntity.getOrganization().getUuid()));
			thread.start();
		}
		try {
			sendEMail(executionEntity, assetDescriptionEntity, eMailData, List.of(email));
		} catch (Exception e) {
			log.warn("WkC - Failed to send mail for " + executionEntity.getProcessDefinitionKey(), e);
		}

	}

	private Report buildAttachReport(LinkedProducerEntity assetDescription, boolean isValidated) {

		Map<String, Object> data = null;
		try {
			data = getFormHelper().hydrateData(assetDescription.getData());
		} catch (InvalidDataException e) {
			log.warn("Impossible de récupérer les datas");
		}

		String comment = data != null && data.containsKey(ATTACH_COMMENT_KEY) ? data.get(ATTACH_COMMENT_KEY).toString()
				: null;
		IntegrationStatus status = isValidated ? IntegrationStatus.OK : IntegrationStatus.KO;
		List<IntegrationError> integrationErrors = new ArrayList<>();
		if (status.equals(IntegrationStatus.KO)) {
			integrationErrors.add(IntegrationError.ERR_101);
		}

		// Le resourceId est l'UUID de l'organisation concernée par l'action
		return new Report().reportId(UUID.randomUUID()).submissionDate(assetDescription.getCreationDate())
				.treatmentDate(LocalDateTime.now()).method(Method.ATTACH).version(version)
				.resourceId(assetDescription.getOrganization().getUuid())
				.resourceTitle(assetDescription.getOrganization().getName()).integrationStatus(status)
				.integrationErrors(getErrorsFromIntegrationError(integrationErrors)).comment(comment);

	}

	private Report buildDetachReport(LinkedProducerEntity assetDescription, boolean isValidated) {

		Map<String, Object> data = null;
		try {
			data = getFormHelper().hydrateData(assetDescription.getData());
		} catch (InvalidDataException e) {
			log.warn("Impossible de récupérer les datas");
		}

		String comment = data != null && data.containsKey(DETACH_COMMENT_KEY) ? data.get(DETACH_COMMENT_KEY).toString()
				: null;
		IntegrationStatus status = isValidated ? IntegrationStatus.OK : IntegrationStatus.KO;
		List<IntegrationError> integrationErrors = new ArrayList<>();
		if (status.equals(IntegrationStatus.KO)) {
			integrationErrors.add(IntegrationError.ERR_101);
		}

		// Le resourceId est l'UUID de l'organisation concernée par l'action
		return new Report().reportId(UUID.randomUUID()).submissionDate(assetDescription.getCreationDate())
				.treatmentDate(LocalDateTime.now()).method(Method.DETACH).version(version)
				.resourceId(assetDescription.getOrganization().getUuid())
				.resourceTitle(assetDescription.getOrganization().getName()).integrationStatus(status)
				.integrationErrors(getErrorsFromIntegrationError(integrationErrors)).comment(comment);
	}

	private List<ReportError> getErrorsFromIntegrationError(List<IntegrationError> integrationErrors) {
		ArrayList<ReportError> errors = new ArrayList<>();

		for (IntegrationError integrationError : integrationErrors) {
			errors.add(new ReportError().errorCode(integrationError.getCode())
					.errorMessage(integrationError.getMessage()));
		}

		return errors;
	}

	@Override
	protected void addEmailDataModelData(
			EMailDataModel<LinkedProducerEntity, LinkedProducerAssignmentHelper> eMailDataModel) {
		super.addEmailDataModelData(eMailDataModel);
		eMailDataModel.addData("denomination",
				ownerInfoHelper.getAssetDescriptionOwnerInfo(eMailDataModel.getAssetDescription()).getName());
	}
}
