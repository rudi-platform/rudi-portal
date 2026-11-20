package org.rudi.microservice.acl.service.helper.externalaccountrequest;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.script.ScriptContext;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.core.security.AccountState;
import org.rudi.common.core.security.UserType;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.bean.workflow.EMailData;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.AbstractWorkflowContext;
import org.rudi.facet.bpmn.service.TaskConstants;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.generator.text.TemplateGenerator;
import org.rudi.microservice.acl.core.bean.RoleSearchCriteria;
import org.rudi.microservice.acl.service.helper.PasswordHelper;
import org.rudi.microservice.acl.storage.dao.externalaccountrequest.ExternalAccountRequestDao;
import org.rudi.microservice.acl.storage.dao.role.RoleCustomDao;
import org.rudi.microservice.acl.storage.dao.user.UserDao;
import org.rudi.microservice.acl.storage.entity.externalaccountrequest.ExternalAccountRequestEntity;
import org.rudi.microservice.acl.storage.entity.externalaccountrequest.ExternalAccountRequestStatus;
import org.rudi.microservice.acl.storage.entity.role.RoleEntity;
import org.rudi.microservice.acl.storage.entity.user.UserEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

/**
 * Contexte d'exécution du workflow de demande de création de compte dans le système d'authentification externe. Les méthodes de cette classe sont
 * appelées directement depuis les scriptTask du fichier external-account-request-process.bpmn20.xml
 */
@Component(value = "externalAccountRequestWorkflowContext")
@Transactional(readOnly = true)
@Slf4j
public class ExternalAccountRequestWorkflowContext extends
		AbstractWorkflowContext<ExternalAccountRequestEntity, ExternalAccountRequestDao, ExternalAccountRequestAssignmentHelper> {

	@Value("${application.role.user.code:USER}")
	private String userRoleCode;

	private final UserDao userDao;
	private final RoleCustomDao roleCustomDao;
	private final PasswordHelper passwordHelper;
	private final ObjectProvider<PasswordReinitialisationUriProvider> passwordReinitialisationUriProvider;

	public ExternalAccountRequestWorkflowContext(EMailService eMailService, TemplateGenerator templateGenerator,
			ExternalAccountRequestDao assetDescriptionDao, ExternalAccountRequestAssignmentHelper assignmentHelper,
			ACLHelper aclHelper, FormHelper formHelper, UserDao userDao, RoleCustomDao roleCustomDao, PasswordHelper passwordHelper,
			ObjectProvider<PasswordReinitialisationUriProvider> passwordReinitialisationUriProvider) {
		super(eMailService, templateGenerator, assetDescriptionDao, assignmentHelper, aclHelper, formHelper);
		this.userDao = userDao;
		this.roleCustomDao = roleCustomDao;
		this.passwordHelper = passwordHelper;
		this.passwordReinitialisationUriProvider = passwordReinitialisationUriProvider;
	}

	@Transactional(readOnly = false)
	@SuppressWarnings("unused") // Utilisé par external-account-request-process.bpmn20.xml
	public void updateStatus(ScriptContext context, ExecutionEntity executionEntity, String statusValue,
			String externalAccountRequestStatusValue, String functionalStatusValue) {
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		log.debug("WkC - Update {} to status {}, {}, {}", processInstanceBusinessKey, statusValue,
				externalAccountRequestStatusValue, functionalStatusValue);
		Status status = Status.valueOf(statusValue);
		ExternalAccountRequestStatus externalAccountRequestStatus = StringUtils
				.isEmpty(externalAccountRequestStatusValue) ? null
						: ExternalAccountRequestStatus.valueOf(externalAccountRequestStatusValue);

		if (StringUtils.isNotEmpty(processInstanceBusinessKey) && functionalStatusValue != null) {
			UUID uuid = UUID.fromString(processInstanceBusinessKey);
			ExternalAccountRequestEntity assetDescriptionEntity = getAssetDescriptionDao().findByUuid(uuid);
			if (assetDescriptionEntity != null) {
				assetDescriptionEntity.setStatus(status);
				assetDescriptionEntity.setExternalAccountRequestStatus(
						externalAccountRequestStatus == null ? assetDescriptionEntity.getExternalAccountRequestStatus()
								: externalAccountRequestStatus);
				assetDescriptionEntity.setFunctionalStatus(functionalStatusValue);
				assetDescriptionEntity.setUpdatedDate(LocalDateTime.now());
				getAssetDescriptionDao().save(assetDescriptionEntity);
				executionEntity.setVariable(TaskConstants.FUNCTIONAL_STATUS, functionalStatusValue);
				log.debug("WkC - Update {} to status {} done.", processInstanceBusinessKey, statusValue);
			} else {
				log.debug("WkC - Unkown {} skipped.", processInstanceBusinessKey);
			}
		} else {
			log.debug("WkC - Update {} to status {} skipped.", processInstanceBusinessKey, statusValue);
		}
	}

	/**
	 * Création effective du compte utilisateur Rudi suite à la validation de la demande par un animateur.
	 * Le mot de passe est géré par un système d'authentification externe. (Un lien est fournit dans l'email d'acceptation pour y accéder)
	 */
	@Transactional(readOnly = false)
	@SuppressWarnings("unused") // Utilisé par external-account-request-process.bpmn20.xml
	public void createUserAccount(ScriptContext context, ExecutionEntity executionEntity) {
		ExternalAccountRequestEntity assetDescription = lookupAssetDescriptionEntity(executionEntity);
		if (assetDescription == null) {
			return;
		}

		UserEntity existingUser = userDao.findByLogin(assetDescription.getEmail());
		if (existingUser != null) {
			log.warn("WkC - User already exists for login {}, skip creation", assetDescription.getEmail());
			return;
		}

		RoleSearchCriteria roleSearchCriteria = new RoleSearchCriteria();
		roleSearchCriteria.setCode(userRoleCode);
		List<RoleEntity> roles = roleCustomDao.searchRoles(roleSearchCriteria);

		UserEntity userToCreate = new UserEntity();
		userToCreate.setUuid(UUID.randomUUID());
		userToCreate.setLogin(assetDescription.getEmail());
		userToCreate.setFirstname(assetDescription.getFirstname());
		userToCreate.setLastname(assetDescription.getLastname());
		userToCreate.setType(UserType.PERSON);
		userToCreate.setAccountState(AccountState.ACTIVE);
		// Mot-de-passe random : le compte s'authentifie via un système d'authentification externe
		userToCreate.setPassword(passwordHelper.encodePassword(UUID.randomUUID().toString()));
		userToCreate.setRoles(new HashSet<>(roles));
		userToCreate.setHasSubscribeToNotifications(true);

		userDao.save(userToCreate);
	}

	/**
	 * Envoi d'un courriel directement au demandeur (qui n'est pas forcément encore connu de l'ACL)
	 */
	@SuppressWarnings("unused") // Utilisé par external-account-request-process.bpmn20.xml
	public void sendEmailToRequester(ScriptContext context, ExecutionEntity executionEntity, EMailData eMailData) {
		ExternalAccountRequestEntity assetDescription = lookupAssetDescriptionEntity(executionEntity);
		if (assetDescription == null || eMailData == null || StringUtils.isEmpty(assetDescription.getEmail())) {
			return;
		}
		try {
			PasswordReinitialisationUriProvider provider = passwordReinitialisationUriProvider.getIfAvailable();
			if (provider != null) {
				final String pwdReinitialisationUri = provider.getPasswordReinitialisationUri(assetDescription.getIdentityProvider());
				eMailData.addData("pwdReinitialisationUri", pwdReinitialisationUri);
			}
			sendEMail(executionEntity, assetDescription, eMailData, List.of(assetDescription.getEmail()));
		} catch (Exception e) {
			log.warn("WkC - Failed to send mail to requester for " + executionEntity.getProcessDefinitionKey(), e);
		}
	}

}

