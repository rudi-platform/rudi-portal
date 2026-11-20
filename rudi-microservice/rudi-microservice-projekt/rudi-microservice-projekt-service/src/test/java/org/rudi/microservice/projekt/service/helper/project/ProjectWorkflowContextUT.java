package org.rudi.microservice.projekt.service.helper.project;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.bpmn.core.bean.Field;
import org.rudi.bpmn.core.bean.FieldDefinition;
import org.rudi.bpmn.core.bean.Form;
import org.rudi.bpmn.core.bean.Section;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.common.core.DocumentContent;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.acl.helper.RolesHelper;
import org.rudi.facet.bpmn.bean.workflow.EMailData;
import org.rudi.facet.bpmn.datafactory.EMailDataDataFactory;
import org.rudi.facet.bpmn.datafactory.FieldDataFactory;
import org.rudi.facet.bpmn.datafactory.FieldDefinitionDataFactory;
import org.rudi.facet.bpmn.datafactory.FormDataFactory;
import org.rudi.facet.bpmn.datafactory.SectionDataFactory;
import org.rudi.facet.bpmn.datafactory.TaskDataFactory;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.service.TaskConstants;
import org.rudi.facet.bpmn.service.TaskService;
import org.rudi.facet.email.EMailService;
import org.rudi.facet.email.exception.EMailException;
import org.rudi.facet.generator.text.TemplateGenerator;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.organization.bean.Organization;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.microservice.projekt.core.bean.LinkedDataset;
import org.rudi.microservice.projekt.core.bean.NewDatasetRequest;
import org.rudi.microservice.projekt.service.ProjectSpringBootTest;
import org.rudi.microservice.projekt.service.datafactory.LinkedDatasetDataFactory;
import org.rudi.microservice.projekt.service.datafactory.NewDatasetRequestDataFactory;
import org.rudi.microservice.projekt.service.datafactory.OrganizationDataFactory;
import org.rudi.microservice.projekt.service.datafactory.ProjectDataFactory;
import org.rudi.microservice.projekt.service.datafactory.UserDataFactory;
import org.rudi.microservice.projekt.service.datafactory.dto.LinkedDatasetDtoDataFactory;
import org.rudi.microservice.projekt.service.datafactory.dto.NewDatasetRequestDtoDataFactory;
import org.rudi.microservice.projekt.service.datafactory.kaccess.MetadataDataFactory;
import org.rudi.microservice.projekt.service.helper.attachment.AttachmentsHelper;
import org.rudi.microservice.projekt.service.helper.project.processor.ProjectTaskUpdateProjectProcessor;
import org.rudi.microservice.projekt.service.mapper.LinkedDatasetMapper;
import org.rudi.microservice.projekt.service.mapper.NewDatasetRequestMapper;
import org.rudi.microservice.projekt.service.project.ProjectHelper;
import org.rudi.microservice.projekt.storage.dao.project.ProjectDao;
import org.rudi.microservice.projekt.storage.entity.DatasetConfidentiality;
import org.rudi.microservice.projekt.storage.entity.OwnerType;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetStatus;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestEntity;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestStatus;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectStatus;
import org.rudi.microservice.projekt.storage.entity.relatedorganization.RelatedOrganizationEntity;
import org.rudi.microservice.projekt.storage.entity.relatedorganization.RelationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.rudi.common.core.security.RoleCodes.MODERATOR;
import static org.rudi.facet.bpmn.helper.workflow.WorkflowConstants.ASSET_PICTURE_FIELD_NAME;
import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.PROJECT_DRAFT_FORM_SECTION_NAME;

@ProjectSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Transactional
class ProjectWorkflowContextUT {

	private final ProjectWorkflowContext projectWorkflowContext;
	private final ProjectDao projectDao;
	private final ProjectDataFactory projectDataFactory;
	private final LinkedDatasetDataFactory linkedDatasetDataFactory;
	private final NewDatasetRequestDataFactory newDatasetRequestDataFactory;
	private final LinkedDatasetDtoDataFactory linkedDatasetDtoDataFactory;
	private final NewDatasetRequestDtoDataFactory newDatasetRequestDtoDataFactory;
	private final OrganizationDataFactory organizationDataFactory;
	private final UserDataFactory userDataFactory;
	private final MetadataDataFactory metadataDataFactory;
	private final EMailDataDataFactory emailDataDataFactory;
	private final TaskDataFactory taskDataFactory;
	private final FieldDataFactory fieldDataFactory;
	private final FieldDefinitionDataFactory fieldDefinitionDataFactory;
	private final SectionDataFactory sectionDataFactory;
	private final FormDataFactory formDataFactory;

	@MockitoBean
	private EMailService eMailService;
	@MockitoBean
	private TemplateGenerator templateGenerator;
	@MockitoBean
	private ProjectAssigmentHelper projectAssigmentHelper;
	@MockitoBean
	private ACLHelper aclHelper;
	@MockitoBean
	private FormHelper formHelper;
	@MockitoBean
	private TaskService<NewDatasetRequest> newDatasetRequestTaskService;
	@MockitoBean
	private NewDatasetRequestMapper newDatasetRequestMapper;
	@MockitoBean
	private TaskService<LinkedDataset> linkedDatasetTaskService;
	@MockitoBean
	private LinkedDatasetMapper linkedDatasetMapper;
	@MockitoBean
	private OrganizationHelper organizationHelper;
	@MockitoBean(name = "projectTaskUpdateProjectTitleProcessor")
	private ProjectTaskUpdateProjectProcessor projectTaskUpdateProjectProcessor;
	@MockitoBean
	private RolesHelper rolesHelper;
	@MockitoBean
	private ProjectHelper projectHelper;
	@MockitoBean
	private DatasetService datasetService;
	@MockitoBean
	private AttachmentsHelper attachmentsHelper;

	@BeforeEach
	void setUp() throws Exception {
		File templateFile = File.createTempFile("template", ".txt");
		templateFile.deleteOnExit();
		when(templateGenerator.generateDocument(any())).thenReturn(new DocumentContent("template.txt", "text/plain", templateFile));
	}

	private ExecutionEntity mockExecutionEntity(UUID businessKey) {
		ExecutionEntity executionEntity = mock(ExecutionEntity.class);
		when(executionEntity.getProcessInstanceBusinessKey()).thenReturn(businessKey.toString());
		return executionEntity;
	}

	@Test
	@DisplayName("Récupération du statut de réutilisation d'un projet existant")
	void test_getReutilisationStatus_nominal() {
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-reutilisationstatus",
				UUID.randomUUID(), OwnerType.USER);
		project.getReutilisationStatus().setLabel("En cours de réutilisation");
		projectDao.save(project);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		String result = projectWorkflowContext.getReutilisationStatus(null, executionEntity);

		assertThat(result).isEqualTo("En cours de réutilisation");
	}

	@Test
	@DisplayName("Mise à jour du statut, du statut projet et du statut fonctionnel d'un projet existant")
	void test_updateStatus_nominal() {
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-updatestatus", UUID.randomUUID(),
				OwnerType.USER);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		projectWorkflowContext.updateStatus(null, executionEntity, "COMPLETED", "VALIDATED", "Validé");

		assertThat(project.getStatus()).isEqualTo(Status.COMPLETED);
		assertThat(project.getProjectStatus()).isEqualTo(ProjectStatus.VALIDATED);
		assertThat(project.getFunctionalStatus()).isEqualTo("Validé");
		verify(executionEntity).setVariable(TaskConstants.FUNCTIONAL_STATUS, "Validé");
	}

	@Test
	@DisplayName("Calcul des potentiels owners pour un projet porté par une organisation")
	void test_computePotentialProjectOwners_nominal() {
		UUID ownerUuid = UUID.randomUUID();
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-potentialowners", ownerUuid,
				OwnerType.ORGANIZATION);
		project.setInitiator("initiateur");
		projectDao.save(project);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		when(projectAssigmentHelper.computeOrganizationMembersLogins(ownerUuid))
				.thenReturn(List.of("membre1", "membre2"));

		List<String> result = projectWorkflowContext.computePotentialProjectOwners(null, executionEntity);

		assertThat(result).containsExactly("initiateur", "membre1", "membre2");
	}

	@Test
	@DisplayName("Envoi d'un mail aux membres de l'organisation porteuse du projet")
	void test_sendEmailToProjectOwner_nominal() throws EMailException {
		UUID ownerUuid = UUID.randomUUID();
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-sendmailowner", ownerUuid,
				OwnerType.ORGANIZATION);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		User member = userDataFactory.createSimpleUser(UUID.randomUUID(), "membre");
		List<User> members = List.of(member);
		when(projectAssigmentHelper.computeOrganizationMembers(ownerUuid)).thenReturn(members);
		when(aclHelper.lookupEmailAddresses(members)).thenReturn(List.of("membre@mail.fr"));

		projectWorkflowContext.sendEmailToProjectOwner(null, executionEntity, emailDataDataFactory.createEmpty());

		verify(eMailService, times(1)).sendMail(any());
	}

	@Test
	@DisplayName("Démarrage des sous-processus pour les JDD liés restreints et les demandes de nouveaux JDD")
	void test_startSubProcess_nominal() throws Exception {
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-startsubprocess", UUID.randomUUID(),
				OwnerType.USER);
		project.setInitiator("initiateur");

		LinkedDatasetEntity linkedDataset = linkedDatasetDataFactory.createNotPersisted(UUID.randomUUID(),
				UUID.randomUUID(), LinkedDatasetStatus.DRAFT, DatasetConfidentiality.RESTRICTED);
		linkedDataset.setInitiator("");
		project.getLinkedDatasets().add(linkedDataset);

		NewDatasetRequestEntity newDatasetRequest = newDatasetRequestDataFactory.createNotPersisted("Demande",
				NewDatasetRequestStatus.DRAFT);
		newDatasetRequest.setInitiator("");
		project.getDatasetRequests().add(newDatasetRequest);

		projectDao.save(project);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		LinkedDataset linkedDatasetDto = linkedDatasetDtoDataFactory.create();
		when(linkedDatasetMapper.entityToDto(any(LinkedDatasetEntity.class))).thenReturn(linkedDatasetDto);
		Task linkedDatasetTask = taskDataFactory.create();
		when(linkedDatasetTaskService.createDraft(linkedDatasetDto)).thenReturn(linkedDatasetTask);

		NewDatasetRequest newDatasetRequestDto = newDatasetRequestDtoDataFactory.create();
		when(newDatasetRequestMapper.entityToDto(any(NewDatasetRequestEntity.class))).thenReturn(newDatasetRequestDto);
		Task newDatasetRequestTask = taskDataFactory.create();
		when(newDatasetRequestTaskService.createDraft(newDatasetRequestDto)).thenReturn(newDatasetRequestTask);

		projectWorkflowContext.startSubProcess(executionEntity);

		ProjectEntity updatedProject = projectDao.findByUuid(project.getUuid());
		LinkedDatasetEntity updatedLinkedDataset = updatedProject.getLinkedDatasets().stream()
				.filter(candidate -> linkedDataset.getUuid().equals(candidate.getUuid())).findFirst().orElseThrow();
		NewDatasetRequestEntity updatedNewDatasetRequest = updatedProject.getDatasetRequests().stream()
				.filter(candidate -> newDatasetRequest.getUuid().equals(candidate.getUuid())).findFirst().orElseThrow();

		assertThat(updatedLinkedDataset.getInitiator()).isEqualTo("initiateur");
		assertThat(updatedNewDatasetRequest.getInitiator()).isEqualTo("initiateur");
		verify(linkedDatasetTaskService).startTask(linkedDatasetTask);
		verify(newDatasetRequestTaskService).startTask(newDatasetRequestTask);
	}

	@Test
	@DisplayName("Présence d'une organisation partenaire à l'index demandé")
	void test_hasRelatedOrganizationAtIndex_nominal() {
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-hasrelatedorgaatindex",
				UUID.randomUUID(), OwnerType.USER);
		RelatedOrganizationEntity relatedOrganization = projectDataFactory
				.createNotPersistedRelatedOrganizationEntity(UUID.randomUUID(), RelationStatus.ACCEPTED);
		project.setRelatedOrganizations(new LinkedHashSet<>(List.of(relatedOrganization)));
		projectDao.save(project);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		boolean result = projectWorkflowContext.hasRelatedOrganizationAtIndex(null, executionEntity, 0);

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("Calcul des potentiels owners d'une organisation partenaire à un index donné")
	void test_computePotentialProjectRelatedOrganizationOwner_nominal() {
		UUID organizationUuid = UUID.randomUUID();
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-potentialrelatedorgaowner",
				UUID.randomUUID(), OwnerType.USER);
		RelatedOrganizationEntity relatedOrganization = projectDataFactory
				.createNotPersistedRelatedOrganizationEntity(organizationUuid, RelationStatus.ACCEPTED);
		project.setRelatedOrganizations(new LinkedHashSet<>(List.of(relatedOrganization)));
		projectDao.save(project);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		when(projectAssigmentHelper.computeOrganizationMembersLogins(organizationUuid)).thenReturn(List.of("membre1"));

		List<String> result = projectWorkflowContext.computePotentialProjectRelatedOrganizationOwner(null,
				executionEntity, 0);

		assertThat(result).containsExactly("membre1");
	}

	@Test
	@DisplayName("Calcul du nom de l'organisation partenaire à un index donné")
	void test_computeRelatedOrganizationName_nominal() throws Exception {
		UUID organizationUuid = UUID.randomUUID();
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-relatedorganame", UUID.randomUUID(),
				OwnerType.USER);
		RelatedOrganizationEntity relatedOrganization = projectDataFactory
				.createNotPersistedRelatedOrganizationEntity(organizationUuid, RelationStatus.ACCEPTED);
		project.setRelatedOrganizations(new LinkedHashSet<>(List.of(relatedOrganization)));
		projectDao.save(project);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		Organization organization = organizationDataFactory.createWithName("Mon Organisation");
		when(organizationHelper.getOrganization(organizationUuid)).thenReturn(organization);

		String result = projectWorkflowContext.computeRelatedOrganizationName(null, executionEntity, 0);

		assertThat(result).isEqualTo("Mon Organisation");
	}

	@Test
	@DisplayName("Calcul de l'uuid de l'organisation partenaire à un index donné")
	void test_computeRelatedOrganizationUuid_nominal() throws Exception {
		UUID organizationUuid = UUID.randomUUID();
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-relatedorgauuid", UUID.randomUUID(),
				OwnerType.USER);
		RelatedOrganizationEntity relatedOrganization = projectDataFactory
				.createNotPersistedRelatedOrganizationEntity(organizationUuid, RelationStatus.ACCEPTED);
		project.setRelatedOrganizations(new LinkedHashSet<>(List.of(relatedOrganization)));
		projectDao.save(project);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		UUID organizationBusinessUuid = UUID.randomUUID();
		Organization organization = organizationDataFactory.createWithUuid(organizationBusinessUuid);
		when(organizationHelper.getOrganization(organizationUuid)).thenReturn(organization);

		UUID result = projectWorkflowContext.computeRelatedOrganizationUuid(null, executionEntity, 0);

		assertThat(result).isEqualTo(organizationBusinessUuid);
	}

	@Test
	@DisplayName("Envoi d'un mail aux membres d'une organisation partenaire à un index donné")
	void test_sendEmailToRelatedOrganizations_nominal() throws EMailException {
		UUID organizationUuid = UUID.randomUUID();
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-sendmailrelatedorga",
				UUID.randomUUID(), OwnerType.USER);
		RelatedOrganizationEntity relatedOrganization = projectDataFactory
				.createNotPersistedRelatedOrganizationEntity(organizationUuid, RelationStatus.ACCEPTED);
		project.setRelatedOrganizations(new LinkedHashSet<>(List.of(relatedOrganization)));
		projectDao.save(project);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		User member = userDataFactory.createSimpleUser(UUID.randomUUID(), "membre");
		List<User> members = List.of(member);
		when(projectAssigmentHelper.computeOrganizationMembers(organizationUuid)).thenReturn(members);
		when(aclHelper.lookupEmailAddresses(members)).thenReturn(List.of("membre@mail.fr"));

		projectWorkflowContext.sendEmailToRelatedOrganizations(null, executionEntity, emailDataDataFactory.createEmpty(), 0);

		verify(eMailService, times(1)).sendMail(any());
	}

	@Test
	@DisplayName("Mise à jour du statut d'une organisation partenaire acceptée")
	void test_updateRelatedOrganisation_nominal() {
		UUID organizationUuid = UUID.randomUUID();
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-updaterelatedorga", UUID.randomUUID(),
				OwnerType.USER);
		RelatedOrganizationEntity relatedOrganization = projectDataFactory
				.createNotPersistedRelatedOrganizationEntity(organizationUuid, RelationStatus.PENDING);
		project.setRelatedOrganizations(new LinkedHashSet<>(List.of(relatedOrganization)));
		projectDao.saveAndFlush(project);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		projectWorkflowContext.updateRelatedOrganisation(null, executionEntity, 0, true);

		ProjectEntity updatedProject = projectDao.findByUuid(project.getUuid());
		RelatedOrganizationEntity updatedRelatedOrganization = updatedProject.getRelatedOrganizations().stream()
				.filter(candidate -> organizationUuid.equals(candidate.getOrganizationUuid())).findFirst()
				.orElseThrow();
		assertThat(updatedRelatedOrganization.getRelationStatus()).isEqualTo(RelationStatus.ACCEPTED);
	}

	@Test
	@DisplayName("Publication des modifications d'un projet avec mise à jour de l'image associée")
	void test_publishProjectModification_nominal() throws Exception {
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-publishmodification",
				UUID.randomUUID(), OwnerType.USER);
		project.setData("{}");
		projectDao.save(project);
		UUID uuid = project.getUuid();
		ExecutionEntity executionEntity = mockExecutionEntity(uuid);

		UUID mediaUuid = UUID.randomUUID();
		Map<String, Object> data = new HashMap<>();
		data.put(ASSET_PICTURE_FIELD_NAME, mediaUuid.toString());
		when(formHelper.hydrateData(project.getData())).thenReturn(data);

		FieldDefinition fieldDefinition = fieldDefinitionDataFactory.create("test-publish-field");
		Field field = fieldDataFactory.create(fieldDefinition);

		Section section = sectionDataFactory.create(PROJECT_DRAFT_FORM_SECTION_NAME, List.of(field));

		Form draftForm = formDataFactory.create(List.of(section));

		when(formHelper.lookupDraftForm(uuid.toString())).thenReturn(draftForm);
		when(projectTaskUpdateProjectProcessor.accept(field)).thenReturn(true);

		projectWorkflowContext.publishProjectModification(null, executionEntity);

		verify(projectTaskUpdateProjectProcessor).process(field, project);
		verify(attachmentsHelper).saveMediaInMediaService(mediaUuid, uuid);
	}

	@Test
	@DisplayName("Calcul du nom du owner organisation d'un projet")
	void test_computeProjectOwnerName_nominal() throws Exception {
		UUID ownerUuid = UUID.randomUUID();
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-ownername", ownerUuid,
				OwnerType.ORGANIZATION);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		Organization organization = organizationDataFactory.createWithName("Mon Organisation");
		when(organizationHelper.getOrganization(ownerUuid)).thenReturn(organization);

		String result = projectWorkflowContext.computeProjectOwnerName(null, executionEntity);

		assertThat(result).isEqualTo("Mon Organisation");
	}

	@Test
	@DisplayName("Réinitialisation des données du formulaire de brouillon d'un projet")
	void test_resetDraftForm_nominal() throws Exception {
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-resetdraftform", UUID.randomUUID(),
				OwnerType.USER);
		project.setProcessDefinitionKey("project-process");
		project.setData("{\"title\":\"t\"}");
		projectDao.save(project);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		Map<String, Object> data = new HashMap<>();
		data.put("title", "t");
		when(formHelper.hydrateData(project.getData())).thenReturn(data);

		FieldDefinition fieldDefinition = fieldDefinitionDataFactory.create("title");
		Field field = fieldDataFactory.create(fieldDefinition);

		Section section = sectionDataFactory.create(PROJECT_DRAFT_FORM_SECTION_NAME, List.of(field));

		Form form = formDataFactory.create(List.of(section));

		when(formHelper.lookupForm(eq("project-process"), eq(FormHelper.DRAFT_USER_TASK_ID), isNull()))
				.thenReturn(form);
		when(formHelper.deshydrateData(any())).thenReturn("{}");

		projectWorkflowContext.resetDraftForm(null, executionEntity);

		assertThat(project.getData()).isEqualTo("{}");
	}

	@Test
	@DisplayName("Archivage d'un projet refusé par le modérateur")
	void test_archiveRefusedProject_nominal() {
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-archiverefused", UUID.randomUUID(),
				OwnerType.USER);
		project.setProjectStatus(ProjectStatus.REJECTED);
		projectDao.save(project);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		projectWorkflowContext.archiveRefusedProject(null, executionEntity);

		verify(projectHelper).archiveProject(project, ProjectStatus.DISENGAGED, LinkedDatasetStatus.DISENGAGED);
	}

	@Test
	@DisplayName("Archivage d'un projet avec envoi des mails au producteur restreint, au modérateur et au owner")
	void test_archiveProject_nominal() throws Exception {
		UUID ownerUuid = UUID.randomUUID();
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-archiveproject", ownerUuid,
				OwnerType.ORGANIZATION);

		UUID datasetUuid = UUID.randomUUID();
		LinkedDatasetEntity linkedDataset = linkedDatasetDataFactory.createNotPersisted(datasetUuid,
				UUID.randomUUID(), LinkedDatasetStatus.DRAFT, DatasetConfidentiality.RESTRICTED);
		project.getLinkedDatasets().add(linkedDataset);
		projectDao.save(project);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		Organization organization = organizationDataFactory.createWithName("Mon Organisation");
		when(organizationHelper.getOrganization(ownerUuid)).thenReturn(organization);

		UUID producerOrganizationUuid = UUID.randomUUID();
		Metadata dataset = metadataDataFactory.createWithRestrictedProducer(producerOrganizationUuid);

		when(datasetService.getDataset(datasetUuid)).thenReturn(dataset);

		User producerMember = userDataFactory.createSimpleUser(UUID.randomUUID(), "producteur");
		List<User> producerMembers = List.of(producerMember);
		when(projectAssigmentHelper.computeOrganizationMembers(producerOrganizationUuid)).thenReturn(producerMembers);
		when(aclHelper.lookupEmailAddresses(producerMembers)).thenReturn(List.of("producteur@mail.fr"));

		User moderator = userDataFactory.createSimpleUser(UUID.randomUUID(), "moderateur");
		when(aclHelper.searchUsers(MODERATOR)).thenReturn(List.of(moderator));
		when(aclHelper.lookupEMailAddress(moderator)).thenReturn("moderateur@mail.fr");

		User ownerMember = userDataFactory.createSimpleUser(UUID.randomUUID(), "owner");
		List<User> ownerMembers = List.of(ownerMember);
		when(projectAssigmentHelper.computeOrganizationMembers(ownerUuid)).thenReturn(ownerMembers);
		when(aclHelper.lookupEmailAddresses(ownerMembers)).thenReturn(List.of("owner@mail.fr"));

		EMailData producerEmailData = emailDataDataFactory.createEmpty();
		EMailData moderatorEmailData = emailDataDataFactory.createEmpty();
		EMailData projectOwnerEmailData = emailDataDataFactory.createEmpty();

		projectWorkflowContext.archiveProject(null, executionEntity, producerEmailData, moderatorEmailData,
				projectOwnerEmailData);

		verify(projectHelper).archiveProject(project, ProjectStatus.DISENGAGED, LinkedDatasetStatus.DISENGAGED);
		verify(eMailService, times(3)).sendMail(any());
	}

	@Test
	@DisplayName("Suppression du keystore d'un projet")
	void test_deleteProjectKeyStore_nominal() {
		ProjectEntity project = projectDataFactory.getOrCreateSimpleProject("pwc-deletekeystore", UUID.randomUUID(),
				OwnerType.USER);
		ExecutionEntity executionEntity = mockExecutionEntity(project.getUuid());

		projectWorkflowContext.deleteProjectKeyStore(executionEntity);

		verify(projectHelper).deleteProjectKeyStore(project);
	}
}
