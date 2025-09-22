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
import org.hsqldb.lib.StringUtil;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.common.core.security.RoleCodes;
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
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.generator.text.TemplateGenerator;
import org.rudi.facet.kmedia.bean.KindOfData;
import org.rudi.facet.kmedia.bean.MediaOrigin;
import org.rudi.facet.kmedia.service.MediaService;
import org.rudi.facet.projekt.helper.ProjektHelper;
import org.rudi.microservice.projekt.core.bean.ProjektArchiveMode;
import org.rudi.microservice.strukture.core.bean.IntegrationStatus;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.core.bean.Method;
import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.core.bean.Report;
import org.rudi.microservice.strukture.core.bean.ReportError;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationMembersSearchCriteria;
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
import static org.rudi.microservice.strukture.service.workflow.StruktureWorkflowConstants.DRAFT_ARCHIVE_FORM_SECTION_NAME;
import static org.rudi.microservice.strukture.service.workflow.StruktureWorkflowConstants.FIELD_NAME_IMAGE_ORGANIZATION;

@Component(value = "organizationWorkflowContext")
@Transactional
@Slf4j
public class OrganizationWorkflowContext
		extends AbstractWorkflowContext<OrganizationEntity, OrganizationDao, OrganizationAssignmentHelper> {

	private static final String CREATION_COMMENT_KEY = "messageToOrganizationCreator";
	private static final String ARCHIVAGE_COMMENT_KEY = "messageToOrganizationArchiver";
	private static final String ORGANIZATION_ARCHIVE_MODE_KEY = "organizationArchiveMode";
	private static final String ARCHIVAGE_TYPE_DISENGAGED_VALUE = "DISENGAGED";

	private static final String BASE_COMMENT_ARCHIVE = "Archivage et détachement de l'organisation";

	private static final String ERROR_NODE_NOT_FOUND = "Node introuvable";
	private static final String ERROR_HYDRATING_DATA = "Impossible de récupérer les datas";

	@Value("${rudi.organization.report.version:v1}")
	private String version;
	@Value("${rudi.organization.report.attempts:3}")
	private int attempts;

	private final LinkedProducerHelper linkedProducerHelper;
	private final ProjektHelper projektHelper;
	private final NodeProviderUserHelper nodeProviderUserHelper;
	private final ReportHelper reportHelper;
	private final ProviderHelper providerHelper;
	private final OwnerInfoHelper ownerInfoHelper;
	private final TaskService<LinkedProducer> linkedProducerTaskService;
	private final AttachmentsHelper attachmentsHelper;
	private final OrganizationMembersHelper organizationMembersHelper;
	private final MediaService mediaService;

	public OrganizationWorkflowContext(EMailService eMailService, TemplateGenerator templateGenerator,
			OrganizationDao assetDescriptionDao, OrganizationAssignmentHelper assignmentHelper, ACLHelper aclHelper,
			FormHelper formHelper, NodeProviderUserHelper nodeProviderUserHelper, ReportHelper reportHelper,
			ProviderHelper providerHelper, OwnerInfoHelper ownerInfoHelper,
			TaskService<LinkedProducer> linkedProducerTaskService, LinkedProducerHelper linkedProducerHelper,
			AttachmentsHelper attachmentsHelper, ProjektHelper projektHelper,
			OrganizationMembersHelper organizationMembersHelper, MediaService mediaService) {
		super(eMailService, templateGenerator, assetDescriptionDao, assignmentHelper, aclHelper, formHelper);
		this.nodeProviderUserHelper = nodeProviderUserHelper;
		this.reportHelper = reportHelper;
		this.providerHelper = providerHelper;
		this.ownerInfoHelper = ownerInfoHelper;
		this.linkedProducerTaskService = linkedProducerTaskService;
		this.linkedProducerHelper = linkedProducerHelper;
		this.attachmentsHelper = attachmentsHelper;
		this.projektHelper = projektHelper;
		this.organizationMembersHelper = organizationMembersHelper;
		this.mediaService = mediaService;
	}

	@Transactional(readOnly = false)
	@SuppressWarnings("unused") // Utilisé par organization-process.bpmn20.xml
	public void updateStatus(ScriptContext context, ExecutionEntity executionEntity, String statusValue,
			String organizationStatusValue, String functionalStatusValue) {
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		log.debug("WkC - Update {} to status {}, {}, {}", processInstanceBusinessKey, statusValue,
				organizationStatusValue, functionalStatusValue);
		Status status = Status.valueOf(statusValue);
		OrganizationStatus organizationStatus = StringUtil.isEmpty(organizationStatusValue) ? null
				: OrganizationStatus.valueOf(organizationStatusValue);

		if (!StringUtils.isEmpty(processInstanceBusinessKey) && status != null && functionalStatusValue != null) {
			UUID organizationUuid = UUID.fromString(processInstanceBusinessKey);
			OrganizationEntity assetDescriptionEntity = getAssetDescriptionDao().findByUuid(organizationUuid);
			if (assetDescriptionEntity != null) {
				assetDescriptionEntity.setStatus(status);
				assetDescriptionEntity.setOrganizationStatus(
						organizationStatus == null ? assetDescriptionEntity.getOrganizationStatus()
								: organizationStatus);
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
					throw new InvalidParameterException(ERROR_NODE_NOT_FOUND);
				}

				String providerContactEmail = providerHelper.getContactEmail(nodeProvider);
				if (providerContactEmail != null) {
					email = providerContactEmail;
				}

				Report report = buildReport(assetDescription, CREATION_COMMENT_KEY, isValidated,
						assetDescription.getCreationDate(), Method.POST, null);

				Thread thread = new Thread(new ReportSendExecutor(reportHelper, report, nodeProvider, attempts,
						assetDescription.getUuid()));
				thread.start();
			}
			// On envoie un mail à l'initiator
			try {
				sendEMail(executionEntity, assetDescription, eMailData, List.of(email));
			} catch (Exception e) {
				log.warn("WkC - Failed to send mail to initiator for " + executionEntity.getProcessDefinitionKey(), e);
			}

		}
	}

	@SuppressWarnings("unused") // Utilisé par organization-process.bpmn20.xml
	public void startAttachOrganizationToProvider(ScriptContext context, ExecutionEntity executionEntity)
			throws FormDefinitionException, FormConvertException, InvalidDataException, AppServiceException {
		OrganizationEntity assetDescription = lookupAssetDescriptionEntity(executionEntity);
		User initiator = lookupUser(assetDescription.getInitiator());
		if (initiator != null && UserType.ROBOT.equals(initiator.getType())
				&& initiator.getRoles().stream().anyMatch(role -> role.getCode().equals(RoleCodes.PROVIDER))) {

			NodeProvider nodeProvider = nodeProviderUserHelper.getNodeProviderFromUser(initiator);

			if (nodeProvider == null) {
				throw new InvalidParameterException(ERROR_NODE_NOT_FOUND);
			}

			ProviderEntity providerEntity = providerHelper.getProviderFromNodeProvider(nodeProvider);
			if (providerEntity == null) {
				throw new InvalidParameterException("Provider introuvable");
			}

			LinkedProducer lp = linkedProducerHelper.createLinkedProducer(assetDescription, providerEntity,
					nodeProvider);
			lp.setInitiator(assetDescription.getInitiator());
			Task t = linkedProducerTaskService.createDraft(lp);
			linkedProducerTaskService.startTask(t);
		}
	}

	@SuppressWarnings("unused") // Utilisé par organization-process.bpmn20.xml
	public void attachInitiatorToOrganization(ScriptContext context, ExecutionEntity executionEntity) {
		OrganizationEntity assetDescriptionEntity = lookupAssetDescriptionEntity(executionEntity);
		User initiator = lookupUser(assetDescriptionEntity.getInitiator());
		if (initiator != null && UserType.PERSON.equals(initiator.getType())) {
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
			log.warn(ERROR_HYDRATING_DATA);
		}

		if (data != null && data.containsKey(FIELD_NAME_IMAGE_ORGANIZATION)) {
			UUID mediaUuid = UUID.fromString((String) data.get(FIELD_NAME_IMAGE_ORGANIZATION));

			attachmentsHelper.saveMediaInMediaService(mediaUuid, assetDescriptionEntity.getUuid());
		}
	}

	@SuppressWarnings("unused") // Utilisé par organization-process.bpmn20.xml
	public void archiveOrganization(ScriptContext context, ExecutionEntity executionEntity) throws AppServiceException {
		OrganizationEntity assetDescription = lookupAssetDescriptionEntity(executionEntity);
		Map<String, Object> data = null;

		try {
			data = getFormHelper().hydrateData(assetDescription.getData());
		} catch (InvalidDataException e) {
			log.warn(ERROR_HYDRATING_DATA);
		}

		if (data != null && data.containsKey(ORGANIZATION_ARCHIVE_MODE_KEY)) {
			String archivageType = data.get(ORGANIZATION_ARCHIVE_MODE_KEY).toString();

			assetDescription.setClosingDate(LocalDateTime.now());
			assetDescription.setStatus(Status.DELETED);
			assetDescription.setOrganizationStatus(OrganizationStatus.DISENGAGED);
			assetDescription.setFunctionalStatus("Organisation archivée");

			getAssetDescriptionDao().save(assetDescription);

			// Appel à Projekt pour archiver les projets et leurs linkedDatasets
			projektHelper.archiveOwnerProjects(assetDescription.getUuid(),
					mapOrganizationArchiveModeToProjektArchiveMode(archivageType));

			// on detach l'organisation de tous ses providers
			linkedProducerHelper.detachOrganizationFromItsProviders(assetDescription.getUuid());

			// on archive les medias de l'organisation
			try {
				mediaService.deleteMediaFor(MediaOrigin.PRODUCER, assetDescription.getUuid(), KindOfData.LOGO);
			} catch (DataverseAPIException e) {
				log.error("Erreur lors de la suppression du logo de l'organisation d'id {}", assetDescription.getUuid(),
						e);
			}
		} else {
			throw new AppServiceException(String.format("Invalid data on %s", assetDescription.getUuid()));
		}
	}

	@SuppressWarnings("unused") // Utilisé par organization-process.bpmn20.xml
	public void sendArchivageEmail(ScriptContext context, ExecutionEntity executionEntity, EMailData eMailData,
			boolean isValidated) {
		OrganizationEntity assetDescription = lookupAssetDescriptionEntity(executionEntity);

		if (isValidated) {
			// envoi du rapport
			sendReportToLinkedProviders(assetDescription, buildReport(assetDescription, ARCHIVAGE_COMMENT_KEY,
					isValidated, assetDescription.getUpdatedDate(), Method.DELETE, BASE_COMMENT_ARCHIVE));

			sendEmailToOrganizationMembers(executionEntity, assetDescription, eMailData);

		} else {
			sendEmailToArchiveInitiator(executionEntity, assetDescription, eMailData);
		}
	}

	@SuppressWarnings("unused") // Utilisé par organization-process.bpmn20.xml
	public void notifyOrganizationMembersArchive(ScriptContext context, ExecutionEntity executionEntity,
			EMailData eMailData) {
		OrganizationEntity assetDescription = lookupAssetDescriptionEntity(executionEntity);

		sendEmailToOrganizationMembers(executionEntity, assetDescription, eMailData);
	}

	@SuppressWarnings("unused") // Utilisé par organization-process.bpmn20.xml
	public void resetArchiveDraftForm(ScriptContext context, ExecutionEntity executionEntity) {
		OrganizationEntity assetDescriptionB = lookupAssetDescriptionEntity(executionEntity);
		resetFormData(context, executionEntity, FormHelper.DRAFT_ARCHIVE_USER_TASK_ID, null,
				DRAFT_ARCHIVE_FORM_SECTION_NAME);
		OrganizationEntity assetDescriptionA = lookupAssetDescriptionEntity(executionEntity);
		log.debug("WkC - resetArchiveDraftForm - assetDescriptionA: {}", assetDescriptionA);
	}

	private ProjektArchiveMode mapOrganizationArchiveModeToProjektArchiveMode(String organizationArchiveMode) {
		return organizationArchiveMode.equals(ARCHIVAGE_TYPE_DISENGAGED_VALUE) ? ProjektArchiveMode.DISENGAGED
				: ProjektArchiveMode.ARCHIVED;
	}

	private void sendEmailToArchiveInitiator(ExecutionEntity executionEntity, OrganizationEntity assetDescription,
			EMailData eMailData) {
		User initiator = lookupUser(assetDescription.getInitiator());
		if (initiator != null) {
			String email = lookupEMailAddress(initiator);
			if (initiator.getType().equals(UserType.ROBOT)
					&& initiator.getRoles().stream().anyMatch(role -> role.getCode().equals(RoleCodes.PROVIDER))) {
				NodeProvider nodeProvider = nodeProviderUserHelper.getNodeProviderFromUser(initiator);

				if (nodeProvider == null) {
					throw new InvalidParameterException(ERROR_NODE_NOT_FOUND);
				}

				email = providerHelper.getContactEmail(nodeProvider);
			}

			try {
				sendEMail(executionEntity, assetDescription, eMailData, List.of(email));
			} catch (Exception e) {
				log.warn("WkC - Failed to send mail to archive initiator for "
						+ executionEntity.getProcessDefinitionKey(), e);
			}
		}
	}

	private void sendEmailToOrganizationMembers(ExecutionEntity executionEntity, OrganizationEntity assetDescription,
			EMailData eMailData) {
		List<String> emails = getOrganizationMembersEmails(assetDescription);

		try {
			sendEMail(executionEntity, assetDescription, eMailData, emails);
		} catch (Exception e) {
			log.warn("WkC - Failed to send mail for " + executionEntity.getProcessDefinitionKey(), e);
		}
	}

	private List<String> getOrganizationMembersEmails(OrganizationEntity assetDescription) {
		// envoi du mail à l'ensemble des membres de l'organsiation
		List<String> emails = new ArrayList<>();
		List<User> users = organizationMembersHelper.searchCorrespondingUsers(
				assetDescription.getMembers().stream().toList(), new OrganizationMembersSearchCriteria());

		for (User user : users) {

			String email = lookupEMailAddress(user);
			if (StringUtils.isNotEmpty(email) && !emails.contains(email)) {
				emails.add(email);
			}
		}

		return emails;
	}

	private Report buildReport(OrganizationEntity assetDescription, String commentKey, boolean isValidated,
			LocalDateTime submissionDate, Method method, String baseComment) {
		Map<String, Object> data = null;
		try {
			data = getFormHelper().hydrateData(assetDescription.getData());
		} catch (InvalidDataException e) {
			log.warn(ERROR_HYDRATING_DATA);
		}

		String comment = data != null && data.containsKey(commentKey) ? data.get(commentKey).toString() : "";
		comment = StringUtils.isNotEmpty(comment) && StringUtils.isNotEmpty(baseComment) ? " : " + comment : comment;
		String fullComment = StringUtils.join(baseComment, comment);

		IntegrationStatus status = isValidated ? IntegrationStatus.OK : IntegrationStatus.KO;
		List<IntegrationError> integrationErrors = new ArrayList<>();

		if (status.equals(IntegrationStatus.KO)) {
			integrationErrors.add(IntegrationError.ERR_101);
		}

		return new Report().reportId(UUID.randomUUID()).submissionDate(submissionDate)
				.treatmentDate(LocalDateTime.now()).method(method).version(version).method(method)
				.resourceId(assetDescription.getUuid()).resourceTitle(assetDescription.getName())
				.integrationStatus(status).integrationErrors(getErrorsFromIntegrationError(integrationErrors))
				.comment(fullComment);
	}

	private void sendReportToLinkedProviders(OrganizationEntity assetDescription, Report report) {
		List<NodeProvider> nodeProviders = providerHelper.getOrganizationsNodeProviders(assetDescription.getUuid());

		for (NodeProvider nodeProvider : nodeProviders) {
			Thread thread = new Thread(
					new ReportSendExecutor(reportHelper, report, nodeProvider, attempts, assetDescription.getUuid()));
			thread.start();
		}
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

		try {
			OrganizationEntity organization = eMailDataModel.getAssetDescription();
			Map<String, Object> data = getFormHelper().hydrateData(organization.getData());

			if (data != null && data.containsKey(ORGANIZATION_ARCHIVE_MODE_KEY)) {
				eMailDataModel.addData(ORGANIZATION_ARCHIVE_MODE_KEY,
						data.get(ORGANIZATION_ARCHIVE_MODE_KEY).toString());
			}

		} catch (InvalidDataException e) {
			log.error("Failed to hydrate data", e);
		}
	}

}
