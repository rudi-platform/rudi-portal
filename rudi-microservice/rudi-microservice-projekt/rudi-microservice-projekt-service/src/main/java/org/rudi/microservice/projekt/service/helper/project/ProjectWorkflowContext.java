/**
 * RUDI Portail
 */
package org.rudi.microservice.projekt.service.helper.project;

import static org.rudi.common.core.security.RoleCodes.MODERATOR;
import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.DRAFT_FORM_SECTION_NAME;
import static org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestStatus.ARCHIVED;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.script.ScriptContext;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Field;
import org.rudi.bpmn.core.bean.Form;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.facet.acl.bean.ProjectKeystore;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.acl.helper.ProjectKeystoreSearchCriteria;
import org.rudi.facet.acl.helper.RolesHelper;
import org.rudi.facet.bpmn.bean.workflow.EMailData;
import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.service.TaskService;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.email.exception.EMailException;
import org.rudi.facet.generator.exception.GenerationException;
import org.rudi.facet.generator.exception.GenerationModelNotFoundException;
import org.rudi.facet.generator.text.TemplateGenerator;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.organization.bean.Organization;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.microservice.projekt.core.bean.LinkedDataset;
import org.rudi.microservice.projekt.core.bean.NewDatasetRequest;
import org.rudi.microservice.projekt.service.helper.AbstractProjektWorkflowContext;
import org.rudi.microservice.projekt.service.helper.project.processor.ProjectTaskUpdateProjectProcessor;
import org.rudi.microservice.projekt.service.mapper.LinkedDatasetMapper;
import org.rudi.microservice.projekt.service.mapper.NewDatasetRequestMapper;
import org.rudi.microservice.projekt.storage.dao.project.ProjectDao;
import org.rudi.microservice.projekt.storage.entity.DatasetConfidentiality;
import org.rudi.microservice.projekt.storage.entity.OwnerType;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetStatus;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 */
@Component(value = "projectWorkflowContext")
@Transactional(readOnly = true)
@Slf4j
public class ProjectWorkflowContext
		extends AbstractProjektWorkflowContext<ProjectEntity, ProjectDao, ProjectAssigmentHelper> {

	private final TaskService<NewDatasetRequest> newDatasetRequestTaskService;

	private final NewDatasetRequestMapper newDatasetRequestMapper;

	private final TaskService<LinkedDataset> linkedDatasetTaskService;

	private final LinkedDatasetMapper linkedDatasetMapper;

	private final ACLHelper aclHelper;

	private final OrganizationHelper organizationHelper;

	private final List<ProjectTaskUpdateProjectProcessor> projectTaskUpdateProjectProcessors;

	private final DatasetService datasetService;

	private final UtilPageable utilPageable;

	private static final String WKC_UNKNOWN_SKIPPED = "WkC - Unkown {} skipped.";

	private static final String ARCHIVED_FUNCTIONAL_STATUS = "Archivé";

	public ProjectWorkflowContext(EMailService eMailService, TemplateGenerator templateGenerator,
			ProjectDao assetDescriptionDao, ProjectAssigmentHelper assignmentHelper, ACLHelper aclHelper,
			FormHelper formHelper, TaskService<NewDatasetRequest> newDatasetRequestTaskService,
			NewDatasetRequestMapper newDatasetRequestMapper, TaskService<LinkedDataset> linkedDatasetTaskService,
			LinkedDatasetMapper linkedDatasetMapper, ACLHelper aclHelper1, OrganizationHelper organizationHelper,
			List<ProjectTaskUpdateProjectProcessor> projectTaskUpdateProjectProcessors, RolesHelper rolesHelper,
			DatasetService datasetService, UtilPageable utilPageable) {
		super(eMailService, templateGenerator, assetDescriptionDao, assignmentHelper, aclHelper, formHelper,
				rolesHelper);
		this.utilPageable = utilPageable;
		this.newDatasetRequestTaskService = newDatasetRequestTaskService;
		this.newDatasetRequestMapper = newDatasetRequestMapper;
		this.linkedDatasetTaskService = linkedDatasetTaskService;
		this.linkedDatasetMapper = linkedDatasetMapper;
		this.aclHelper = aclHelper1;
		this.organizationHelper = organizationHelper;
		this.projectTaskUpdateProjectProcessors = projectTaskUpdateProjectProcessors;
		this.datasetService = datasetService;
	}

	@Transactional(readOnly = true)
	public String getReutilisationStatus(ScriptContext scriptContext, ExecutionEntity executionEntity) {
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		String reutilisationStatusLabel = StringUtils.EMPTY;
		log.debug("WkC - Get status of reuse {}", processInstanceBusinessKey);
		if (processInstanceBusinessKey != null) {
			UUID uuid = UUID.fromString(processInstanceBusinessKey);
			ProjectEntity assetDescription = getAssetDescriptionDao().findByUuid(uuid);
			if (assetDescription != null) {
				reutilisationStatusLabel = assetDescription.getReutilisationStatus().getLabel();
				log.debug("WkC - Status of reuse {} is {}.", processInstanceBusinessKey, reutilisationStatusLabel);
			} else {
				log.debug(WKC_UNKNOWN_SKIPPED, processInstanceBusinessKey);
			}
		} else {
			log.debug("WkC - Get reuse status of {} skipped.", processInstanceBusinessKey);
		}
		return reutilisationStatusLabel;
	}

	@Transactional(readOnly = false)
	@SuppressWarnings("unused") // Utilisé par project-process.bpmn20.xml
	public void updateStatus(ScriptContext scriptContext, ExecutionEntity executionEntity, String statusValue,
			String projectStatusValue, String functionalStatusValue) {
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		log.debug("WkC - Update {} to status {}, {}, {}", processInstanceBusinessKey, statusValue, projectStatusValue,
				functionalStatusValue);
		Status status = Status.valueOf(statusValue);
		ProjectStatus projectStatus = StringUtils.isEmpty(projectStatusValue) ? null
				: ProjectStatus.valueOf(projectStatusValue);
		if (processInstanceBusinessKey != null && status != null && functionalStatusValue != null) {
			UUID uuid = UUID.fromString(processInstanceBusinessKey);
			ProjectEntity assetDescription = getAssetDescriptionDao().findByUuid(uuid);
			if (assetDescription != null) {
				assetDescription.setStatus(status);
				assetDescription
						.setProjectStatus(projectStatus == null ? assetDescription.getProjectStatus() : projectStatus);
				assetDescription.setFunctionalStatus(functionalStatusValue);
				assetDescription.setUpdatedDate(LocalDateTime.now());
				getAssetDescriptionDao().save(assetDescription);
				log.debug("WkC - Update {} to status {} done.", processInstanceBusinessKey, statusValue);
			} else {
				log.debug(WKC_UNKNOWN_SKIPPED, processInstanceBusinessKey);
			}
		} else {
			log.debug("WkC - Update {} to status {} skipped.", processInstanceBusinessKey, statusValue);
		}
	}

	/**
	 * Calcul des potentiels owners pour le gestionnaire de projet et le modérateur
	 *
	 * @param scriptContext   contexte
	 * @param executionEntity entity
	 * @return List<String> liste des logins des owners potentiels
	 */
	@SuppressWarnings("unused") // Utilisé par project-process.bpmn20.xml
	public List<String> computePotentialProjectOwners(ScriptContext scriptContext, ExecutionEntity executionEntity) {
		List<String> result = new ArrayList<>();
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		if (processInstanceBusinessKey != null) {
			UUID uuid = UUID.fromString(processInstanceBusinessKey);
			ProjectEntity assetDescription = getAssetDescriptionDao().findByUuid(uuid);
			if (assetDescription != null) {

				// Ajout du demandeur
				result.add(assetDescription.getInitiator());

				if (assetDescription.getOwnerType() == OwnerType.ORGANIZATION) {
					// Ajout de tous les membres de l'organisation
					CollectionUtils.addAll(result,
							getAssignmentHelper().computeOrganizationMembersLogins(assetDescription.getOwnerUuid()));
				}

				if (log.isInfoEnabled()) {
					log.info("Assignees: {}", StringUtils.join(result, ", "));
				}
			}
		}
		return result;
	}

	/**
	 * Envoit un mail au porteur d'un projet pour les notifier de la réponse du MODERATOR - Owner de type organisation : envoie un mail à tous les membres
	 * de l'organisation
	 *
	 * @param context         le context
	 * @param executionEntity entité de l'éxécution, ici projet
	 * @param eMailData       email à envoyer
	 */
	@SuppressWarnings("unused") // Utilisé par project-process.bpmn20.xml
	public void sendEmailToProjectOwner(ScriptContext context, ExecutionEntity executionEntity, EMailData eMailData) {
		List<String> assigneesEmails = new ArrayList<>();
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		try {
			if (processInstanceBusinessKey != null) {

				UUID uuid = UUID.fromString(processInstanceBusinessKey);
				ProjectEntity assetDescription = getAssetDescriptionDao().findByUuid(uuid);

				// Récupération des mails de contact que le project owner soit une organisation ou un user
				assigneesEmails = computeProjectOwnerMail(assetDescription);

				if (eMailData != null && CollectionUtils.isNotEmpty(assigneesEmails)) {
					sendEMail(executionEntity, assetDescription, eMailData, assigneesEmails, null);
				}
			}
		} catch (Exception e) {
			log.warn("Failed to send email to owner", e);
		}
	}

	/**
	 * Envoit un mail au producer du dataset dans le cas de l'archivage
	 *
	 * @param executionEntity  entité de l'éxécution, ici projet
	 * @param assetDescription l'asset description
	 * @param dataset          les Matadatas du dataset
	 * @param eMailData        email à envoyer
	 */
	private void sendEmailToProducer(ExecutionEntity executionEntity, ProjectEntity assetDescription, Metadata dataset,
			EMailData eMailData) {
		List<String> assigneesEmails = new ArrayList<>();
		try {
			List<User> users = getAssignmentHelper()
					.computeOrganizationMembers(dataset.getProducer().getOrganizationId());
			CollectionUtils.addAll(assigneesEmails, aclHelper.lookupEmailAddresses(users));

			if (eMailData != null && CollectionUtils.isNotEmpty(assigneesEmails)) {
				sendEMail(executionEntity, assetDescription, eMailData, assigneesEmails, null);
			}

		} catch (Exception e) {
			log.warn("Failed to send email to producer", e);
		}

	}

	@SuppressWarnings("unused") // Utilisé par project-process.bpmn20.xml
	public void startSubProcess(ExecutionEntity executionEntity) {
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		if (processInstanceBusinessKey != null) {
			UUID uuid = UUID.fromString(processInstanceBusinessKey);
			ProjectEntity assetDescription = getAssetDescriptionDao().findByUuid(uuid);
			startSubProcessNewDatasetRequests(assetDescription);
			startSubProcessLinkedDatasets(assetDescription);
		}
	}

	@SuppressWarnings("unused") // Utilisé par project-process.bpmn20.xml
	public void publishProjectModification(ScriptContext context, ExecutionEntity executionEntity) {
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		if (processInstanceBusinessKey != null) {
			UUID uuid = UUID.fromString(processInstanceBusinessKey);
			ProjectEntity assetDescriptionEntity = getAssetDescriptionDao().findByUuid(uuid);
			if (assetDescriptionEntity != null) {
				try {
					Map<String, Object> data = getFormHelper().hydrateData(assetDescriptionEntity.getData());
					Form draftForm = getFormHelper().lookupDraftForm(processInstanceBusinessKey);

					// on remplit le formulaire avec les data pour pouvoir le parcourir lors de l'update.
					getFormHelper().fillForm(draftForm, data);
					draftForm.getSections().stream()
							.filter(section -> section.getName().equals(DRAFT_FORM_SECTION_NAME)).findFirst()
							.ifPresentOrElse(s -> updateProject(s.getFields(), assetDescriptionEntity), () -> {
								throw new NoSuchElementException(String.format(
										"Update project : No section found draft form: %s", DRAFT_FORM_SECTION_NAME));
							});

				} catch (InvalidDataException e) {
					log.error("Failed to hydrate data for {}", assetDescriptionEntity.getInitiator());
				} catch (FormDefinitionException e) {
					log.error("Failed to look up for draft form for {}", assetDescriptionEntity.getInitiator());
				}
			}
		}
	}

	private void updateProject(List<Field> fields, ProjectEntity projectEntity) {
		if (fields != null) {
			fields.forEach(f -> projectTaskUpdateProjectProcessors.forEach(processor -> {
				if (processor.accept(f)) {
					processor.process(f, projectEntity);
				}
			}));
		}

	}

	private void startSubProcessLinkedDatasets(ProjectEntity project) {
		if (CollectionUtils.isNotEmpty(project.getLinkedDatasets())) {
			for (LinkedDatasetEntity linkedDataset : project.getLinkedDatasets()) {
				if (linkedDataset.getDatasetConfidentiality() == DatasetConfidentiality.RESTRICTED) {
					try {
						Task t = linkedDatasetTaskService.createDraft(linkedDatasetMapper.entityToDto(linkedDataset));
						linkedDatasetTaskService.startTask(t);
					} catch (Exception e) {
						log.error("Failed to start workflow for linkedDataset:" + linkedDataset, e);
					}
				}
			}
		}
	}

	private void startSubProcessNewDatasetRequests(ProjectEntity project) {
		if (CollectionUtils.isNotEmpty(project.getDatasetRequests())) {
			for (NewDatasetRequestEntity newDatasetRequest : project.getDatasetRequests()) {
				try {
					Task t = newDatasetRequestTaskService
							.createDraft(newDatasetRequestMapper.entityToDto(newDatasetRequest));
					newDatasetRequestTaskService.startTask(t);
				} catch (Exception e) {
					log.error("Failed to start workflow for newDatasetRequest:" + newDatasetRequest, e);
				}
			}
		}
	}

	/**
	 * @param context         context
	 * @param executionEntity entity - ici project
	 * @return le nom du project owner ou de l'organisation owner du projet
	 */
	@SuppressWarnings("unused") // Utilisé par project-process.bpmn20.xml
	public String computeProjectOwnerName(ScriptContext context, ExecutionEntity executionEntity) {
		String result = null;
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		if (processInstanceBusinessKey != null) {
			UUID uuid = UUID.fromString(processInstanceBusinessKey);
			ProjectEntity assetDescription = getAssetDescriptionDao().findByUuid(uuid);
			if (assetDescription != null) {
				if (assetDescription.getOwnerType().equals(OwnerType.ORGANIZATION)) {
					result = computeProjectOwnerNameOrganization(assetDescription);
				} else {
					result = computeProjectOwnerNameUser(assetDescription);
				}
			}

		}
		return result;
	}

	private String computeProjectOwnerNameOrganization(ProjectEntity assetDescription) {
		String result = null;
		try {
			Organization organization = organizationHelper.getOrganization(assetDescription.getOwnerUuid());
			result = organization != null ? organization.getName() : null;
		} catch (Exception e) {
			log.error("Erreur lors de la récupéation de l'organisation owner du projet {} : {}",
					assetDescription.getTitle(), e);
		}

		return result;
	}

	private String computeProjectOwnerNameUser(ProjectEntity assetDescription) {
		String result = null;
		User owner = aclHelper.getUserByUUID(assetDescription.getOwnerUuid());
		if (owner != null) {
			result = String.format("%s %s", owner.getFirstname(), owner.getLastname());
		} else {
			log.error("L'UUID renseignée n'est pas rattachée à un utilisateur RUDI.");
		}
		return result;
	}

	private List<String> computeProjectOwnerMail(ProjectEntity assetDescription) {
		List<String> assigneesEmails = new ArrayList<>();

		if (assetDescription != null) {
			if (assetDescription.getOwnerType().equals(OwnerType.ORGANIZATION)) {
				List<User> users = getAssignmentHelper().computeOrganizationMembers(assetDescription.getOwnerUuid());

				CollectionUtils.addAll(assigneesEmails, aclHelper.lookupEmailAddresses(users));
			} else {
				assigneesEmails.add(
						Objects.requireNonNull(aclHelper.getUserByUUID(assetDescription.getOwnerUuid())).getLogin());
			}
		}

		return assigneesEmails;
	}

	@SuppressWarnings("unused") // Utilisé par project-process.bpmn20.xml
	public void resetDraftForm(ScriptContext context, ExecutionEntity executionEntity) {
		resetFormData(context, executionEntity, FormHelper.DRAFT_USER_TASK_ID, null, DRAFT_FORM_SECTION_NAME);
	}

	@Transactional(readOnly = false)
	@SuppressWarnings("unused") // Utilisé par project-process.bpmn20.xml
	public void archiveProject(ScriptContext context, ExecutionEntity executionEntity, EMailData producerEmailData,
			EMailData moderatorEmailData, EMailData projectOwnerEmailData)
			throws EMailException, GenerationException, IOException, GenerationModelNotFoundException {
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		String ownerName = computeProjectOwnerName(context, executionEntity);
		if (processInstanceBusinessKey != null) {
			UUID uuid = UUID.fromString(processInstanceBusinessKey);
			ProjectEntity assetDescription = getAssetDescriptionDao().findByUuid(uuid);

			if (assetDescription != null) {
				assetDescription.setProjectStatus(ProjectStatus.DISENGAGED);
				assetDescription.setStatus(Status.DELETED);
				assetDescription.setFunctionalStatus("Archivée");
				assetDescription.setUpdatedDate(LocalDateTime.now());

				// Gestion des jeux de données
				handleArchivedLinkedDatasets(assetDescription, executionEntity, producerEmailData);

				// Gestion des demande de nouvelles données
				handleArchivedNewDatasetRequest(assetDescription, executionEntity);

				// Envoie du mail de notification de l'archivage au moderator
				getAssetDescriptionDao().save(assetDescription);
				sendEMailToRole(null, executionEntity, moderatorEmailData, MODERATOR);

				// Envoie du mail de notification de l'archivage au project owner
				// Récupération des mails de contact que le project owner soit une organisation ou un user
				List<String> assigneesEmails = computeProjectOwnerMail(assetDescription);

				if (CollectionUtils.isNotEmpty(assigneesEmails)) {
					sendEMail(executionEntity, assetDescription, projectOwnerEmailData, assigneesEmails, null);
				}

			} else {
				log.debug(WKC_UNKNOWN_SKIPPED, processInstanceBusinessKey);
			}
		} else {
			log.debug("WkC - Unlink {} to project skipped.", processInstanceBusinessKey);
		}
	}

	@SuppressWarnings("unused") // Utilisé par project-process.bpmn20.xml
	public void deleteProjectKeyStore(ExecutionEntity executionEntity) {
		String processInstanceBusinessKey = executionEntity.getProcessInstanceBusinessKey();
		if (processInstanceBusinessKey != null) {
			UUID uuid = UUID.fromString(processInstanceBusinessKey);
			ProjectEntity assetDescription = getAssetDescriptionDao().findByUuid(uuid);
			if (assetDescription != null) {
				ProjectKeystoreSearchCriteria searchCriteria = new ProjectKeystoreSearchCriteria();
				List<UUID> uuidList = new ArrayList<>();
				uuidList.add(assetDescription.getUuid());
				searchCriteria.setProjectUuids(uuidList);
				Pageable page = utilPageable.getPageable(0, 1, null);
				Optional<ProjectKeystore> projectKeystore = getAclHelper().searchProjectKeystores(searchCriteria, page)
						.get().findFirst();

				if (projectKeystore.isPresent()) {
					aclHelper.deleteProjectKeyStore(projectKeystore.get().getUuid());
				} else {
					log.info("WkC - There is no projectKeystore associated to the project {}.",
							processInstanceBusinessKey);
				}
			} else {
				log.debug(WKC_UNKNOWN_SKIPPED, processInstanceBusinessKey);
			}
		} else {
			log.debug("WkC - Unlink {} to project skipped.", processInstanceBusinessKey);
		}
	}

	/**
	 * Prise en compte des requests archivés
	 * 
	 * @param assetDescription l'asset
	 * @param executionEntity  l'exécution en cours
	 */
	protected void handleArchivedNewDatasetRequest(ProjectEntity assetDescription, ExecutionEntity executionEntity) {
		for (NewDatasetRequestEntity newDatasetRequestEntity : assetDescription.getDatasetRequests()) {
			newDatasetRequestEntity.setNewDatasetRequestStatus(ARCHIVED);
			newDatasetRequestEntity.setStatus(Status.DELETED);
			newDatasetRequestEntity.setFunctionalStatus(ARCHIVED_FUNCTIONAL_STATUS);
			newDatasetRequestEntity.setUpdatedDate(LocalDateTime.now());
		}
	}

	/**
	 * Prise en compte des requests archivés
	 * 
	 * @param assetDescription l'asset
	 * @param executionEntity  l'exécution en cours
	 * @param emailData        données des courriels
	 */
	protected void handleArchivedLinkedDatasets(ProjectEntity assetDescription, ExecutionEntity executionEntity,
			EMailData emailData) {
		for (LinkedDatasetEntity linkedDataset : assetDescription.getLinkedDatasets()) {
			try {
				linkedDataset.setLinkedDatasetStatus(LinkedDatasetStatus.ARCHIVED);
				linkedDataset.setStatus(Status.DELETED);
				linkedDataset.setFunctionalStatus(ARCHIVED_FUNCTIONAL_STATUS);
				linkedDataset.setUpdatedDate(LocalDateTime.now());
				Metadata dataset = datasetService.getDataset(linkedDataset.getDatasetUuid());
				if (dataset.getAccessCondition().getConfidentiality().getRestrictedAccess()) {

					sendEmailToProducer(executionEntity, assetDescription, dataset, emailData);
				}
			} catch (Exception e) {
				log.debug(WKC_UNKNOWN_SKIPPED, executionEntity.getProcessInstanceBusinessKey());
			}
		}
	}

}
