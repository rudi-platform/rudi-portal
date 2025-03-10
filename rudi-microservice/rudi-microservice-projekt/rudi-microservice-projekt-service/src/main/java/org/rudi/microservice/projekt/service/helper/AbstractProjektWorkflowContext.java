/**
 * RUDI Portail
 */
package org.rudi.microservice.projekt.service.helper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.script.ScriptContext;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.acl.helper.RolesHelper;
import org.rudi.facet.bpmn.bean.workflow.EMailData;
import org.rudi.facet.bpmn.dao.workflow.AssetDescriptionDao;
import org.rudi.facet.bpmn.entity.workflow.AssetDescriptionEntity;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.AbstractWorkflowContext;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.generator.text.TemplateGenerator;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 *
 */
@Slf4j
public abstract class AbstractProjektWorkflowContext<E extends AssetDescriptionEntity, D extends AssetDescriptionDao<E>, A extends AbstractProjektAssigmentHelper<E>>
		extends AbstractWorkflowContext<E, D, A> {

	@Getter(value = AccessLevel.PROTECTED)
	private final RolesHelper rolesHelper;

	protected AbstractProjektWorkflowContext(EMailService eMailService, TemplateGenerator templateGenerator,
			D assetDescriptionDao, A assignmentHelper, ACLHelper aclHelper, FormHelper formHelper,
			RolesHelper rolesHelper) {
		super(eMailService, templateGenerator, assetDescriptionDao, assignmentHelper, aclHelper, formHelper);
		this.rolesHelper = rolesHelper;
	}

	public E injectData(ExecutionEntity executionEntity, String key, Object value) throws AppServiceException {
		E assetDescriptionEntity = lookupAssetDescriptionEntity(executionEntity);
		try {
			log.debug("Try to populate asset with ({}{})", key, value);
			if (assetDescriptionEntity != null) {
				Map<String, Object> map = getFormHelper().hydrateData(assetDescriptionEntity.getData());
				map.put(key, value);
				assetDescriptionEntity.setData(getFormHelper().deshydrateData(map));
			}
		} catch (InvalidDataException e) {
			throw new AppServiceException("Une erreur est survenue lors de l'injection de données dans l'asset", e);
		}
		return assetDescriptionEntity;
	}

	public LocalDateTime getCurrentLocalDateTime() {
		return LocalDateTime.now();
	}

	/**
	 * Envoi de courriel
	 * 
	 * @param scriptContext   le context du script
	 * @param executionEntity le context d'execution
	 * @param eMailData
	 */
	@Override
	public void sendEMailToInitiator(ScriptContext scriptContext, ExecutionEntity executionEntity,
			EMailData eMailData) {
		log.debug("Send email to initiator...");
		try {
			AssetDescriptionEntity assetDescription = lookupAssetDescriptionEntity(executionEntity);

			if (assetDescription != null && eMailData != null) {
				List<String> assigneesEmails = new ArrayList<>();
				User initiator = lookupUser(assetDescription.getInitiator());
				if (initiator != null) {
					// Utilisateur de type USER
					assigneesEmails = lookupEMailAddresses(List.of(initiator));
					assigneesEmails
							.add(Objects.requireNonNull(getAclHelper().getUserByUUID(initiator.getUuid())).getLogin());
				} else {
					// Utilisateur non trouvé comme USER, recherche comme ORGANIZATION
					List<User> users = getAssignmentHelper()
							.computeOrganizationMembers(UUID.fromString(assetDescription.getInitiator()));
					CollectionUtils.addAll(assigneesEmails, getAclHelper().lookupEmailAddresses(users));
				}

				if (CollectionUtils.isNotEmpty(assigneesEmails)) {
					// suppression des duplications
					assigneesEmails = assigneesEmails.stream().distinct().collect(Collectors.toList());
					sendEMail(executionEntity, assetDescription, eMailData, assigneesEmails, null);
				} else {
					log.error("Unable to find email for asset {} and initiator {}", assetDescription.getUuid(),
							assetDescription.getInitiator());
				}
			}
			else {
				log.error("Unable to send email : asset description {} or emailData {} is invalid", assetDescription, eMailData);
			}
		} catch (Exception e) {
			log.warn(WK_C_FAILED_TO_SEND_MAIL_FOR + executionEntity.getProcessDefinitionKey(), e);
		}
	}
}
