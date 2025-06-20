package org.rudi.microservice.strukture.service.helper.organization;

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
import org.rudi.bpmn.core.bean.Task;
import org.rudi.common.core.security.RoleCodes;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.bean.UserType;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.bean.workflow.EMailData;
import org.rudi.facet.bpmn.bean.workflow.EMailDataModel;
import org.rudi.facet.bpmn.exception.FormConvertException;
import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.AbstractWorkflowContext;
import org.rudi.facet.bpmn.service.TaskService;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.generator.text.TemplateGenerator;
import org.rudi.microservice.strukture.core.bean.IntegrationStatus;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.core.bean.Method;
import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.core.bean.Report;
import org.rudi.microservice.strukture.core.bean.ReportError;
import org.rudi.microservice.strukture.service.helper.LinkedProducerHelper;
import org.rudi.microservice.strukture.service.helper.NodeProviderUserHelper;
import org.rudi.microservice.strukture.service.helper.OwnerInfoHelper;
import org.rudi.microservice.strukture.service.helper.ProviderHelper;
import org.rudi.microservice.strukture.service.helper.ReportHelper;
import org.rudi.microservice.strukture.service.helper.ReportSendExecutor;
import org.rudi.microservice.strukture.service.helper.attachments.AttachmentsHelper;
import org.rudi.microservice.strukture.service.integration.errors.IntegrationError;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationMemberEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationRole;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import static org.rudi.microservice.strukture.service.workflow.StruktureWorkflowConstants.FIELD_NAME_IMAGE_ORGANIZATION;

@Component(value = "organizationWorkflowContext")
@Transactional
@Slf4j
public class OrganizationWorkflowContext
		extends AbstractWorkflowContext<OrganizationEntity, OrganizationDao, OrganizationAssignmentHelper> {

	private final LinkedProducerHelper linkedProducerHelper;
	@Value("${rudi.organization.report.version:v1}")
	private String version;
	@Value("${rudi.organization.report.attempts:3}")
	private int attempts;

	private static final String COMMENT_KEY = "messageToOrganizationCreator";

	private final NodeProviderUserHelper nodeProviderUserHelper;
	private final ReportHelper reportHelper;
	private final ProviderHelper providerHelper;
	private final OwnerInfoHelper ownerInfoHelper;
	private final TaskService<LinkedProducer> linkedProducerTaskService;
	private final AttachmentsHelper attachmentsHelper;

	public OrganizationWorkflowContext(EMailService eMailService, TemplateGenerator templateGenerator,
			OrganizationDao assetDescriptionDao, OrganizationAssignmentHelper assignmentHelper, ACLHelper aclHelper,
			FormHelper formHelper, NodeProviderUserHelper nodeProviderUserHelper, ReportHelper reportHelper,
			ProviderHelper providerHelper, OwnerInfoHelper ownerInfoHelper, TaskService<LinkedProducer> linkedProducerTaskService, LinkedProducerHelper linkedProducerHelper, AttachmentsHelper attachmentsHelper) {
		super(eMailService, templateGenerator, assetDescriptionDao, assignmentHelper, aclHelper, formHelper);
		this.nodeProviderUserHelper = nodeProviderUserHelper;
		this.reportHelper = reportHelper;
		this.providerHelper = providerHelper;
		this.ownerInfoHelper = ownerInfoHelper;
		this.linkedProducerTaskService = linkedProducerTaskService;
		this.linkedProducerHelper = linkedProducerHelper;
		this.attachmentsHelper = attachmentsHelper;
	}

	@Transactional(readOnly = false)
	@SuppressWarnings("unused") // Utilisé par organization-process.bpmn20.xml
	public void updateStatus(ScriptContext context, ExecutionEntity executionEntity, String statusValue,
			String organizationStatusValue, String functionalStatusValue) {
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		log.debug("WkC - Update {} to status {}, {}, {}", processInstanceBusinessKey, statusValue,
				organizationStatusValue, functionalStatusValue);
		Status status = Status.valueOf(statusValue);
		OrganizationStatus organizationStatus = OrganizationStatus.valueOf(organizationStatusValue);

		if (!StringUtils.isEmpty(processInstanceBusinessKey) && status != null && functionalStatusValue != null) {
			UUID organizationUuid = UUID.fromString(processInstanceBusinessKey);
			OrganizationEntity assetDescriptionEntity = getAssetDescriptionDao().findByUuid(organizationUuid);
			if (assetDescriptionEntity != null) {
				assetDescriptionEntity.setStatus(status);
				assetDescriptionEntity.setOrganizationStatus(organizationStatus);
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

	@SuppressWarnings("unused") // Utilisé par organization-process.bpmn20.xml
	public void sendEmailToOrganizationInitiator(ScriptContext context, ExecutionEntity executionEntity,
			EMailData eMailData, boolean isValidated) {
		OrganizationEntity assetDescription = lookupAssetDescriptionEntity(executionEntity);
		User initiator = lookupUser(assetDescription.getInitiator());
		if (initiator != null) {
			String email = lookupEMailAddress(initiator);
			if (initiator.getType().equals(UserType.ROBOT)
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

				Report report = buildReport(assetDescription, isValidated);

				Thread thread = new Thread(new ReportSendExecutor(reportHelper, report, nodeProvider, attempts,
						assetDescription.getUuid()));
				thread.start();
			}
			// On envoie un mail à l'initiator
			try {
				sendEMail(executionEntity, assetDescription, eMailData, List.of(email));
			} catch (Exception e) {
				log.warn("WkC - Failed to send mail for " + executionEntity.getProcessDefinitionKey(), e);
			}

		}
	}

	@SuppressWarnings("unused") // Utilisé par organization-process.bpmn20.xml
	public void startAttachOrganizationToProvider(ScriptContext context, ExecutionEntity executionEntity) throws FormDefinitionException, FormConvertException, InvalidDataException, AppServiceBadRequestException {
		OrganizationEntity assetDescription = lookupAssetDescriptionEntity(executionEntity);
		User initiator = lookupUser(assetDescription.getInitiator());
		if (initiator != null && UserType.ROBOT.equals(initiator.getType())
				&& initiator.getRoles().stream().anyMatch(role -> role.getCode().equals(RoleCodes.PROVIDER))) {

			NodeProvider nodeProvider = nodeProviderUserHelper.getNodeProviderFromUser(initiator);

			if (nodeProvider == null) {
				throw new InvalidParameterException("Node introuvable");
			}

			ProviderEntity providerEntity = providerHelper.getProviderFromNodeProvider(nodeProvider);
			if (providerEntity == null) {
				throw new InvalidParameterException("Provider introuvable");
			}

			LinkedProducer lp = linkedProducerHelper.createLinkedProducer(assetDescription, providerEntity, nodeProvider);
			lp.setInitiator(assetDescription.getInitiator());
			Task t = linkedProducerTaskService.createDraft(lp);
			linkedProducerTaskService.startTask(t);
		}
	}

	@SuppressWarnings("unused") // Utilisé par organization-process.bpmn20.xml
	public void attachInitiatorToOrganization(ScriptContext context, ExecutionEntity executionEntity){
		OrganizationEntity assetDescriptionEntity = lookupAssetDescriptionEntity(executionEntity);
		User initiator = lookupUser(assetDescriptionEntity.getInitiator());
		if(initiator != null && UserType.PERSON.equals(initiator.getType())) {
			OrganizationMemberEntity organizationMemberEntity = new OrganizationMemberEntity();
			organizationMemberEntity.setAddedDate(LocalDateTime.now());
			organizationMemberEntity.setRole(OrganizationRole.ADMINISTRATOR);
			organizationMemberEntity.setUserUuid(initiator.getUuid());

			assetDescriptionEntity.getMembers().add(organizationMemberEntity);
		}
	}

	@SuppressWarnings("unused") // Utilisé par organization-process.bpmn20.xml
	public void saveMedia(ScriptContext context, ExecutionEntity executionEntity) throws AppServiceException {
		OrganizationEntity assetDescriptionEntity = lookupAssetDescriptionEntity(executionEntity);
		Map<String, Object> data = null;
		try {
			data = getFormHelper().hydrateData(assetDescriptionEntity.getData());
		} catch (InvalidDataException e) {
			log.warn("Impossible de récupérer les datas");
		}

		if(data != null && data.containsKey(FIELD_NAME_IMAGE_ORGANIZATION)){
			UUID mediaUuid = UUID.fromString((String) data.get(FIELD_NAME_IMAGE_ORGANIZATION));

			attachmentsHelper.saveMediaInMediaService(mediaUuid, assetDescriptionEntity.getUuid());
		}
	}

	private Report buildReport(OrganizationEntity assetDescription, boolean isValidated) {

		Map<String, Object> data = null;
		try {
			data = getFormHelper().hydrateData(assetDescription.getData());
		} catch (InvalidDataException e) {
			log.warn("Impossible de récupérer les datas");
		}

		String comment = data != null && data.containsKey(COMMENT_KEY) ? data.get(COMMENT_KEY).toString() : null;
		IntegrationStatus status = isValidated ? IntegrationStatus.OK : IntegrationStatus.KO;
		List<IntegrationError> integrationErrors = new ArrayList<>();
		if (status.equals(IntegrationStatus.KO)) {
			integrationErrors.add(IntegrationError.ERR_101);
		}

		return new Report().reportId(UUID.randomUUID()).submissionDate(assetDescription.getCreationDate())
				.treatmentDate(LocalDateTime.now()).method(Method.POST).version(version)
				.resourceId(assetDescription.getUuid()).resourceTitle(assetDescription.getName())
				.integrationStatus(status).integrationErrors(getErrorsFromIntegrationError(integrationErrors))
				.comment(comment);

	}

	private List<ReportError> getErrorsFromIntegrationError(List<IntegrationError> integrationErrors) {
		ArrayList<ReportError> errors = new ArrayList<>();

		for (IntegrationError integrationError : integrationErrors) {
			errors.add(new ReportError().errorCode(integrationError.getCode())
					.errorMessage(integrationError.getMessage()));
		}

		return errors;
	}

	/**
	 * Point d'extension pour l'alimentation du datamodel des emails
	 *
	 * @param eMailDataModel
	 */
	@Override
	protected void addEmailDataModelData(
			EMailDataModel<OrganizationEntity, OrganizationAssignmentHelper> eMailDataModel) {
		super.addEmailDataModelData(eMailDataModel);
		eMailDataModel.addData("denomination",
				ownerInfoHelper.getAssetDescriptionOwnerInfo(eMailDataModel.getAssetDescription()).getName());
	}
}
