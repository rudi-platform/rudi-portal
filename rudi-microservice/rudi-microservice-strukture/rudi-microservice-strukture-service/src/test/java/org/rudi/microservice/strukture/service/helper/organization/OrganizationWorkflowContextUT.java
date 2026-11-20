package org.rudi.microservice.strukture.service.helper.organization;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.bpmn.core.bean.Field;
import org.rudi.bpmn.core.bean.FieldDefinition;
import org.rudi.bpmn.core.bean.Form;
import org.rudi.bpmn.core.bean.Section;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.common.core.security.RoleCodes;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.bean.UserType;
import org.rudi.facet.acl.datafactory.UserDataFactory;
import org.rudi.facet.acl.helper.ACLHelper;
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
import org.rudi.facet.kmedia.bean.KindOfData;
import org.rudi.facet.kmedia.bean.MediaOrigin;
import org.rudi.facet.kmedia.service.MediaService;
import org.rudi.facet.projekt.helper.ProjektHelper;
import org.rudi.microservice.projekt.core.bean.ProjektArchiveMode;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.service.StruktureSpringBootTest;
import org.rudi.microservice.strukture.service.datafactory.dto.LinkedProducerDtoDataFactory;
import org.rudi.microservice.strukture.service.datafactory.dto.NodeProviderDtoDataFactory;
import org.rudi.microservice.strukture.service.datafactory.organization.OrganizationDataFactory;
import org.rudi.microservice.strukture.service.helper.LinkedProducerHelper;
import org.rudi.microservice.strukture.service.helper.NodeProviderUserHelper;
import org.rudi.microservice.strukture.service.helper.OwnerInfoHelper;
import org.rudi.microservice.strukture.service.helper.ProviderHelper;
import org.rudi.microservice.strukture.service.helper.ReportHelper;
import org.rudi.microservice.strukture.service.helper.attachments.AttachmentsHelper;
import org.rudi.microservice.strukture.service.helper.organization.processors.OrganizationTaskUpdateOrganizationProcessor;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationMemberEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationRole;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus;
import org.rudi.microservice.strukture.storage.entity.provider.NodeProviderEntity;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.rudi.facet.bpmn.helper.form.FormHelper.DRAFT_UPDATE_USER_TASK_ID;
import static org.rudi.facet.bpmn.helper.workflow.WorkflowConstants.ASSET_PICTURE_FIELD_NAME;
import static org.rudi.microservice.strukture.service.workflow.StruktureWorkflowConstants.ORGANIZATION_ARCHIVAGE_TYPE_DISENGAGED_VALUE;
import static org.rudi.microservice.strukture.service.workflow.StruktureWorkflowConstants.ORGANIZATION_ARCHIVE_MODE_KEY;
import static org.rudi.microservice.strukture.service.workflow.StruktureWorkflowConstants.ORGANIZATION_DRAFT_ARCHIVE_FORM_SECTION_NAME;
import static org.rudi.microservice.strukture.service.workflow.StruktureWorkflowConstants.ORGANIZATION_DRAFT_UPDATE_FORM_SECTION_NAME;

@StruktureSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Transactional
class OrganizationWorkflowContextUT {

	private final OrganizationWorkflowContext organizationWorkflowContext;
	private final OrganizationDao organizationDao;
	private final OrganizationDataFactory organizationDataFactory;
	private final UserDataFactory userDataFactory;
	private final NodeProviderDtoDataFactory nodeProviderDtoDataFactory;
	private final LinkedProducerDtoDataFactory linkedProducerDtoDataFactory;
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
	private OrganizationAssignmentHelper assignmentHelper;
	@MockitoBean
	private ACLHelper aclHelper;
	@MockitoBean
	private FormHelper formHelper;
	@MockitoBean
	private NodeProviderUserHelper nodeProviderUserHelper;
	@MockitoBean
	private ReportHelper reportHelper;
	@MockitoBean
	private ProviderHelper providerHelper;
	@MockitoBean
	private OwnerInfoHelper ownerInfoHelper;
	@MockitoBean
	private TaskService<LinkedProducer> linkedProducerTaskService;
	@MockitoBean
	private LinkedProducerHelper linkedProducerHelper;
	@MockitoBean
	private AttachmentsHelper attachmentsHelper;
	@MockitoBean
	private ProjektHelper projektHelper;
	@MockitoBean
	private OrganizationMembersHelper organizationMembersHelper;
	@MockitoBean
	private MediaService mediaService;
	@MockitoBean(name = "organizationTaskUpdateOrganizationNameProcessor")
	private OrganizationTaskUpdateOrganizationProcessor organizationTaskUpdateOrganizationProcessor;

	private ExecutionEntity mockExecutionEntity(UUID businessKey) {
		ExecutionEntity executionEntity = mock(ExecutionEntity.class);
		when(executionEntity.getProcessInstanceBusinessKey()).thenReturn(businessKey.toString());
		return executionEntity;
	}

	private User mockUser(String login, UserType type, boolean isProvider) {
		return userDataFactory.createUserWithRole(login, type, isProvider ? RoleCodes.PROVIDER : "AUTRE_ROLE");
	}

	@Test
	@DisplayName("Mise à jour du statut, du statut organisation et du statut fonctionnel d'une organisation existante")
	void test_updateStatus_5args_nominal() {
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		ExecutionEntity executionEntity = mockExecutionEntity(organization.getUuid());

		organizationWorkflowContext.updateStatus(null, executionEntity, "DELETED", "VALIDATED", "Validée");

		assertThat(organization.getStatus()).isEqualTo(Status.DELETED);
		assertThat(organization.getOrganizationStatus()).isEqualTo(OrganizationStatus.VALIDATED);
		assertThat(organization.getFunctionalStatus()).isEqualTo("Validée");
		verify(executionEntity).setVariable(TaskConstants.FUNCTIONAL_STATUS, "Validée");
	}

	@Test
	@DisplayName("Envoi d'un mail à l'initiateur personne physique d'une organisation")
	void test_sendEmailToOrganizationInitiator_nominal() throws EMailException {
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		organization.setInitiator("initiateur");
		organizationDao.save(organization);
		ExecutionEntity executionEntity = mockExecutionEntity(organization.getUuid());

		User initiator = mockUser("initiateur", UserType.PERSON, false);
		when(aclHelper.getUserByLogin("initiateur")).thenReturn(initiator);
		when(aclHelper.lookupEMailAddress(initiator)).thenReturn("initiateur@mail.com");

		organizationWorkflowContext.sendEmailToOrganizationInitiator(null, executionEntity,
				emailDataDataFactory.createEmpty(), true);

		verify(eMailService, times(1)).sendMail(any());
	}

	@Test
	@DisplayName("Attache l'initiateur robot provider d'une organisation à son node provider")
	void test_startAttachOrganizationToProvider_nominal() throws Exception {
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		organization.setInitiator("initiateur");
		organizationDao.save(organization);
		ExecutionEntity executionEntity = mockExecutionEntity(organization.getUuid());

		User initiator = mockUser("initiateur", UserType.ROBOT, true);
		when(aclHelper.getUserByLogin("initiateur")).thenReturn(initiator);

		NodeProvider nodeProvider = nodeProviderDtoDataFactory.create(UUID.randomUUID());
		when(nodeProviderUserHelper.getNodeProviderFromUser(initiator)).thenReturn(nodeProvider);

		ProviderEntity providerEntity = new ProviderEntity();
		when(providerHelper.getProviderFromNodeProvider(nodeProvider)).thenReturn(providerEntity);

		LinkedProducer linkedProducer = linkedProducerDtoDataFactory.create();
		when(linkedProducerHelper.createLinkedProducer(organization, providerEntity, nodeProvider))
				.thenReturn(linkedProducer);

		Task task = taskDataFactory.create();
		when(linkedProducerTaskService.createDraft(linkedProducer)).thenReturn(task);

		organizationWorkflowContext.startAttachOrganizationToProvider(null, executionEntity);

		assertThat(linkedProducer.getInitiator()).isEqualTo("initiateur");
		verify(linkedProducerTaskService).startTask(task);
	}

	@Test
	@DisplayName("Rattache l'initiateur personne physique comme membre administrateur de l'organisation")
	void test_attachInitiatorToOrganization_nominal() {
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		organization.setInitiator("initiateur");
		organizationDao.save(organization);
		ExecutionEntity executionEntity = mockExecutionEntity(organization.getUuid());

		User initiator = mockUser("initiateur", UserType.PERSON, false);
		when(aclHelper.getUserByLogin("initiateur")).thenReturn(initiator);

		organizationWorkflowContext.attachInitiatorToOrganization(null, executionEntity);

		assertThat(organization.getMembers()).hasSize(1)
				.allMatch(member -> member.getUserUuid().equals(initiator.getUuid())
						&& member.getRole() == OrganizationRole.ADMINISTRATOR);
	}

	@Test
	@DisplayName("Sauvegarde du média d'une organisation à partir des données du formulaire")
	void test_saveMedia_nominal() throws Exception {
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		organization.setData("data");
		organizationDao.save(organization);
		UUID uuid = organization.getUuid();
		ExecutionEntity executionEntity = mockExecutionEntity(uuid);

		UUID mediaUuid = UUID.randomUUID();
		Map<String, Object> data = new HashMap<>();
		data.put(ASSET_PICTURE_FIELD_NAME, mediaUuid.toString());
		when(formHelper.hydrateData("data")).thenReturn(data);

		organizationWorkflowContext.saveMedia(null, executionEntity);

		verify(attachmentsHelper).saveMediaInMediaService(mediaUuid, uuid);
	}

	@Test
	@DisplayName("Archivage d'une organisation avec détachement de ses providers")
	void test_archiveOrganization_nominal() throws Exception {
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		organization.setData("data");
		organizationDao.save(organization);
		UUID uuid = organization.getUuid();
		ExecutionEntity executionEntity = mockExecutionEntity(uuid);

		Map<String, Object> data = new HashMap<>();
		data.put(ORGANIZATION_ARCHIVE_MODE_KEY, ORGANIZATION_ARCHIVAGE_TYPE_DISENGAGED_VALUE);
		when(formHelper.hydrateData("data")).thenReturn(data);

		organizationWorkflowContext.archiveOrganization(null, executionEntity);

		assertThat(organization.getStatus()).isEqualTo(Status.DELETED);
		assertThat(organization.getOrganizationStatus()).isEqualTo(OrganizationStatus.DISENGAGED);
		assertThat(organization.getClosingDate()).isNotNull();
		verify(projektHelper).archiveOwnerProjects(uuid, ProjektArchiveMode.DISENGAGED);
		verify(linkedProducerHelper).detachOrganizationFromItsProviders(uuid);
		verify(mediaService).deleteMediaFor(MediaOrigin.PRODUCER, uuid, KindOfData.LOGO);
	}

	@Test
	@DisplayName("Envoi d'un rapport à l'ensemble des providers liés à l'organisation")
	void test_sendReportToLinkedProducers_nominal() {
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		organization.setUpdatedDate(LocalDateTime.now());
		organizationDao.save(organization);
		UUID uuid = organization.getUuid();
		ExecutionEntity executionEntity = mockExecutionEntity(uuid);

		when(providerHelper.getOrganizationsNodeProviders(uuid)).thenReturn(List.of());

		organizationWorkflowContext.sendReportToLinkedProducers(null, executionEntity, true);

		verify(providerHelper).getOrganizationsNodeProviders(uuid);
	}

	@Test
	@DisplayName("Envoi d'un mail d'archivage aux membres de l'organisation en excluant l'initiateur")
	void test_sendArchivageEmailToOrganizationMembers_nominal() throws EMailException {
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		organization.setInitiator("initiateur");
		OrganizationMemberEntity member = new OrganizationMemberEntity();
		member.setUserUuid(UUID.randomUUID());
		member.setRole(OrganizationRole.ADMINISTRATOR);
		member.setAddedDate(LocalDateTime.now());
		organization.setMembers(new LinkedHashSet<>(Set.of(member)));
		organizationDao.save(organization);
		ExecutionEntity executionEntity = mockExecutionEntity(organization.getUuid());

		User initiator = mockUser("initiateur", UserType.PERSON, false);
		when(aclHelper.getUserByLogin("initiateur")).thenReturn(initiator);
		when(aclHelper.lookupEMailAddress(initiator)).thenReturn("initiateur@mail.com");

		User memberUser = userDataFactory.createSimpleUser(member.getUserUuid(), "membre");
		when(organizationMembersHelper.searchCorrespondingUsers(eq(List.of(member)), any()))
				.thenReturn(List.of(initiator, memberUser));
		when(aclHelper.lookupEMailAddress(memberUser)).thenReturn("membre@mail.com");

		organizationWorkflowContext.sendArchivageEmailToOrganizationMembers(null, executionEntity,
				emailDataDataFactory.createEmpty());

		verify(eMailService, times(1)).sendMail(any());
	}

	@Test
	@DisplayName("Envoi d'un mail d'archivage aux providers liés à l'organisation")
	void test_sendArchivageEmailToLinkedProviders_nominal() throws EMailException {
		UUID nodeProviderUuid = UUID.randomUUID();
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		organization.setInitiator(nodeProviderUuid.toString());
		organizationDao.save(organization);
		UUID uuid = organization.getUuid();
		ExecutionEntity executionEntity = mockExecutionEntity(uuid);

		User initiator = mockUser(nodeProviderUuid.toString(), UserType.PERSON, false);
		when(aclHelper.getUserByLogin(nodeProviderUuid.toString())).thenReturn(initiator);

		NodeProviderEntity nodeProviderEntity = new NodeProviderEntity();
		nodeProviderEntity.setUuid(nodeProviderUuid);
		ProviderEntity providerEntity = new ProviderEntity();
		providerEntity.setNodeProviders(Set.of(nodeProviderEntity));
		when(providerHelper.searchAllOrganizationsProviders(uuid, true)).thenReturn(List.of(providerEntity));
		when(providerHelper.getContactEmail(providerEntity)).thenReturn("provider@mail.com");

		organizationWorkflowContext.sendArchivageEmailToLinkedProviders(null, executionEntity,
				emailDataDataFactory.createEmpty());

		verify(eMailService, times(1)).sendMail(any());
	}

	@Test
	@DisplayName("Envoi d'un mail à l'ensemble des membres de l'organisation")
	void test_sendEMailToAllOrganizationMembers_nominal() throws EMailException {
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		OrganizationMemberEntity member = new OrganizationMemberEntity();
		member.setUserUuid(UUID.randomUUID());
		member.setRole(OrganizationRole.ADMINISTRATOR);
		member.setAddedDate(LocalDateTime.now());
		organization.setMembers(new LinkedHashSet<>(Set.of(member)));
		organizationDao.save(organization);
		ExecutionEntity executionEntity = mockExecutionEntity(organization.getUuid());

		User memberUser = userDataFactory.createSimpleUser(member.getUserUuid(), "membre");
		when(organizationMembersHelper.searchCorrespondingUsers(eq(List.of(member)), any()))
				.thenReturn(List.of(memberUser));
		when(aclHelper.lookupEMailAddress(memberUser)).thenReturn("membre@mail.com");

		organizationWorkflowContext.sendEMailToAllOrganizationMembers(null, executionEntity,
				emailDataDataFactory.createEmpty());

		verify(eMailService, times(1)).sendMail(any());
	}

	@Test
	@DisplayName("Réinitialisation des données du formulaire draft d'archivage")
	void test_resetArchiveDraftForm_nominal() throws Exception {
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		organization.setProcessDefinitionKey("organization-process");
		organization.setData("data");
		organizationDao.save(organization);
		ExecutionEntity executionEntity = mockExecutionEntity(organization.getUuid());

		Map<String, Object> data = new HashMap<>();
		data.put("champ", "valeur");
		when(formHelper.hydrateData("data")).thenReturn(data);

		FieldDefinition fieldDefinition = fieldDefinitionDataFactory.create("champ");
		Field field = fieldDataFactory.create(fieldDefinition);
		Section section = sectionDataFactory.create(ORGANIZATION_DRAFT_ARCHIVE_FORM_SECTION_NAME, List.of(field));
		Form form = formDataFactory.create(List.of(section));
		when(formHelper.lookupForm("organization-process", FormHelper.DRAFT_ARCHIVE_USER_TASK_ID, null))
				.thenReturn(form);
		when(formHelper.deshydrateData(any())).thenReturn("newData");

		organizationWorkflowContext.resetArchiveDraftForm(null, executionEntity);

		assertThat(organization.getData()).isEqualTo("newData");
	}

	@Test
	@DisplayName("Réinitialisation des données du formulaire draft de mise à jour")
	void test_resetUpdateDraftForm_nominal() throws Exception {
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		organization.setProcessDefinitionKey("organization-process");
		organization.setData("data");
		organizationDao.save(organization);
		ExecutionEntity executionEntity = mockExecutionEntity(organization.getUuid());

		Map<String, Object> data = new HashMap<>();
		data.put("champ", "valeur");
		when(formHelper.hydrateData("data")).thenReturn(data);

		FieldDefinition fieldDefinition = fieldDefinitionDataFactory.create("champ");
		Field field = fieldDataFactory.create(fieldDefinition);
		Section section = sectionDataFactory.create(ORGANIZATION_DRAFT_UPDATE_FORM_SECTION_NAME, List.of(field));
		Form form = formDataFactory.create(List.of(section));
		when(formHelper.lookupForm("organization-process", DRAFT_UPDATE_USER_TASK_ID, null)).thenReturn(form);
		when(formHelper.deshydrateData(any())).thenReturn("newData");

		organizationWorkflowContext.resetUpdateDraftForm(null, executionEntity);

		assertThat(organization.getData()).isEqualTo("newData");
	}

	@Test
	@DisplayName("Mise à jour d'une organisation inexistante : aucune action effectuée")
	void test_updateOrganization_nominal() {
		UUID uuid = UUID.randomUUID();
		ExecutionEntity executionEntity = mockExecutionEntity(uuid);

		organizationWorkflowContext.updateOrganization(null, executionEntity);

		assertThat(organizationDao.findByUuid(uuid)).isNull();
	}

	@Test
	@DisplayName("Publication de la modification d'une organisation avec mise à jour de son média")
	void test_publishOrganizationModification_nominal() throws Exception {
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		organization.setProcessDefinitionKey("organization-process");
		organization.setData("data");
		organizationDao.save(organization);
		UUID uuid = organization.getUuid();
		ExecutionEntity executionEntity = mockExecutionEntity(uuid);

		UUID mediaUuid = UUID.randomUUID();
		Map<String, Object> data = new HashMap<>();
		data.put(ASSET_PICTURE_FIELD_NAME, mediaUuid.toString());
		when(formHelper.hydrateData("data")).thenReturn(data);

		FieldDefinition fieldDefinition = fieldDefinitionDataFactory.create("champ");
		Field field = fieldDataFactory.create(fieldDefinition);
		Section section = sectionDataFactory.create(ORGANIZATION_DRAFT_UPDATE_FORM_SECTION_NAME, List.of(field));
		Form draftForm = formDataFactory.create(List.of(section));
		when(formHelper.lookupDraftForm(uuid.toString(), DRAFT_UPDATE_USER_TASK_ID)).thenReturn(draftForm);

		when(organizationTaskUpdateOrganizationProcessor.accept(field)).thenReturn(true);

		organizationWorkflowContext.publishOrganizationModification(null, executionEntity);

		assertThat(organization.getUpdatedDate()).isNotNull();
		verify(organizationTaskUpdateOrganizationProcessor).process(field, organization);
		verify(attachmentsHelper).saveMediaInMediaService(mediaUuid, uuid);
	}

	@Test
	@DisplayName("Envoi d'un mail à l'initiateur utilisateur dans le cadre de l'archivage")
	void test_sendEMailToInitiatorUserOrProvider_nominal() throws EMailException {
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		organization.setInitiator("initiateur");
		organizationDao.save(organization);
		ExecutionEntity executionEntity = mockExecutionEntity(organization.getUuid());

		User initiator = mockUser("initiateur", UserType.PERSON, false);
		when(aclHelper.getUserByLogin("initiateur")).thenReturn(initiator);
		when(aclHelper.lookupEMailAddress(initiator)).thenReturn("initiateur@mail.com");

		organizationWorkflowContext.sendEMailToInitiatorUserOrProvider(null, executionEntity,
				emailDataDataFactory.createEmpty());

		verify(eMailService, times(1)).sendMail(any());
	}

	@Test
	@DisplayName("Mise à jour du statut et du statut fonctionnel d'une organisation existante")
	void test_updateStatus_4args_nominal() {
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(UUID.randomUUID());
		ExecutionEntity executionEntity = mockExecutionEntity(organization.getUuid());

		organizationWorkflowContext.updateStatus(null, executionEntity, "COMPLETED", "Terminée");

		assertThat(organization.getStatus()).isEqualTo(Status.COMPLETED);
		assertThat(organization.getFunctionalStatus()).isEqualTo("Terminée");
		verify(executionEntity).setVariable(TaskConstants.FUNCTIONAL_STATUS, "Terminée");
	}
}
