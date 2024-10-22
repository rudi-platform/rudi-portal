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
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.AbstractWorkflowContext;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.generator.text.impl.TemplateGeneratorImpl;
import org.rudi.microservice.strukture.core.bean.IntegrationStatus;
import org.rudi.microservice.strukture.core.bean.Method;
import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.core.bean.Report;
import org.rudi.microservice.strukture.core.bean.ReportError;
import org.rudi.microservice.strukture.service.helper.NodeProviderUserHelper;
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
public class LinkedProducerWorkflowContext extends AbstractWorkflowContext<LinkedProducerEntity, LinkedProducerDao, LinkedProducerAssignmentHelper> {

	private static final String COMMENT_KEY = "messageToAttachmentInitiator";

	@Value("${rudi.linked-producer.report.version:v1}")
	private String version;
	@Value("${rudi.linked-producer.report.attempts:3}")
	private int attempts;

	private final NodeProviderUserHelper nodeProviderUserHelper;
	private final ReportHelper reportHelper;

	protected LinkedProducerWorkflowContext(EMailService eMailService, TemplateGeneratorImpl templateGenerator, LinkedProducerDao assetDescriptionDao, LinkedProducerAssignmentHelper assignmentHelper, ACLHelper aclHelper, FormHelper formHelper, NodeProviderUserHelper nodeProviderUserHelper, ReportHelper reportHelper) {
		super(eMailService, templateGenerator, assetDescriptionDao, assignmentHelper, aclHelper, formHelper);
		this.nodeProviderUserHelper = nodeProviderUserHelper;
		this.reportHelper = reportHelper;
	}

	@Transactional(readOnly = false)
	@SuppressWarnings("unused") // Utilisé par linked-producer-process.bpmn20.xml
	public void updateStatus(ScriptContext scriptContext, ExecutionEntity executionEntity, String statusValue, String linkedProducerStatusValue, String functionalStatusValue) {
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		log.debug("WkC - Update {} to status {}, {}, {}", processInstanceBusinessKey, statusValue, linkedProducerStatusValue, functionalStatusValue);
		Status status = Status.valueOf(statusValue);
		LinkedProducerStatus linkedProducerStatus = LinkedProducerStatus.valueOf(linkedProducerStatusValue);

		if (!StringUtils.isEmpty(processInstanceBusinessKey) && status != null && functionalStatusValue != null) {
			UUID organizationUuid = UUID.fromString(processInstanceBusinessKey);
			LinkedProducerEntity assetDescriptionEntity = getAssetDescriptionDao().findByUuid(organizationUuid);
			if (assetDescriptionEntity != null) {
				assetDescriptionEntity.setStatus(status);
				assetDescriptionEntity.setLinkedProducerStatus(linkedProducerStatus);
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
	public void contactAttachmentInitator(ScriptContext scriptContext, ExecutionEntity executionEntity, boolean isValidated) {
		LinkedProducerEntity assetDescription = lookupAssetDescriptionEntity(executionEntity);
		User initiator = lookupUser(assetDescription.getInitiator());
		if (initiator != null) {
			if (initiator.getType().equals(UserType.ROBOT) && initiator.getRoles().stream().anyMatch(role -> role.getCode().equals(RoleCodes.PROVIDER))) {
				// On est dans le cas d'un provider : donc rapport
				NodeProvider nodeProvider = nodeProviderUserHelper.getNodeProviderFromUser(initiator);
				if (nodeProvider == null) {
					throw new InvalidParameterException("Node introuvable");
				}

				Report report = buildReport(assetDescription, isValidated);

				Thread thread = new Thread(new ReportSendExecutor(reportHelper, report, nodeProvider, attempts, assetDescription.getUuid()));
				thread.start();
			}
		}
	}

	private Report buildReport(LinkedProducerEntity assetDescription, boolean isValidated) {

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

		return new Report()
				.reportId(UUID.randomUUID())
				.submissionDate(assetDescription.getCreationDate())
				.treatmentDate(LocalDateTime.now())
				.method(Method.ATTACH)
				.version(version)
				.organizationId(assetDescription.getUuid())
				.organizationName(assetDescription.getOrganization().getName())
				.integrationStatus(status)
				.integrationErrors(getErrorsFromIntegrationError(integrationErrors))
				.comment(comment);


	}

	public List<ReportError> getErrorsFromIntegrationError(List<IntegrationError> integrationErrors) {
		ArrayList<ReportError> errors = new ArrayList<>();

		for (IntegrationError integrationError : integrationErrors) {
			errors.add(new ReportError().errorCode(integrationError.getCode()).errorMessage(integrationError.getMessage()));
		}

		return errors;
	}
}
