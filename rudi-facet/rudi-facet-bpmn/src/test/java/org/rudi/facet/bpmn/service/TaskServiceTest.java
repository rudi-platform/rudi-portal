/**
 * RUDI Portail
 */
package org.rudi.facet.bpmn.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rudi.bpmn.core.bean.Action;
import org.rudi.bpmn.core.bean.FormDefinition;
import org.rudi.bpmn.core.bean.FormSectionDefinition;
import org.rudi.bpmn.core.bean.ProcessDefinition;
import org.rudi.bpmn.core.bean.ProcessFormDefinition;
import org.rudi.bpmn.core.bean.SectionDefinition;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.common.core.DocumentContent;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.core.security.UserType;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.BpmnSpringBootTest;
import org.rudi.facet.bpmn.bean.Test1AssetDescription;
import org.rudi.facet.bpmn.bean.Test2AssetDescription;
import org.rudi.facet.bpmn.bean.form.FormDefinitionSearchCriteria;
import org.rudi.facet.bpmn.bean.form.ProcessFormDefinitionSearchCriteria;
import org.rudi.facet.bpmn.bean.form.SectionDefinitionSearchCriteria;
import org.rudi.facet.bpmn.bean.workflow.TestTaskSearchCriteria;
import org.rudi.facet.bpmn.dao.workflow.Test2AssetDescriptionDao;
import org.rudi.facet.bpmn.entity.workflow.Test2AssetDescriptionEntity;
import org.rudi.facet.bpmn.exception.BpmnInitializationException;
import org.rudi.facet.bpmn.exception.FormConvertException;
import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.workflow.Test2AssetDescriptionWorkflowHelper;
import org.rudi.facet.bpmn.mapper.workflow.Test2AssetDescriptionMapper;
import org.rudi.facet.bpmn.service.impl.Test1TaskServiceImpl;
import org.rudi.facet.bpmn.service.impl.Test2TaskServiceImpl;
import org.rudi.facet.generator.model.GenerationFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * @author FNI18300
 *
 */
@BpmnSpringBootTest
class TaskServiceTest {

	@Autowired
	private InitializationService initializationService;

	@MockBean
	private UtilContextHelper utilContextHelper;

	@MockBean
	private ACLHelper aclHelper;

	@MockBean
	protected JavaMailSender javaMailSender;

	@Autowired
	private Test2TaskServiceImpl test2TaskService;

	@Autowired
	private Test1TaskServiceImpl test1TaskService;

	@Autowired
	private TaskQueryService<TestTaskSearchCriteria> taskQueryService;

	@Autowired
	private FormService formService;

	@Autowired
	private Test2AssetDescriptionDao test2AssetDescriptionDao;

	@Autowired
	private Test2AssetDescriptionWorkflowHelper test2AssetDescriptionHelper;

	@Autowired
	private Test2AssetDescriptionMapper test2AssetDescriptionMapper;

	@Test
	void create_section() throws IOException {
		Page<SectionDefinition> sections = formService.searchSectionDefinitions(
				SectionDefinitionSearchCriteria.builder().name("T*").build(), PageRequest.of(0, 10));
		long sectionsCount = sections.getTotalElements();

		SectionDefinition sectionDefinition1 = buildSection("Test", "Test", "form/section-test.json");
		SectionDefinition sectionDefinition2 = formService.createSectionDefinition(sectionDefinition1);

		assertNotNull(sectionDefinition2);
		assertEquals(sectionDefinition1.getName(), sectionDefinition2.getName());
		assertEquals(sectionDefinition1.getLabel(), sectionDefinition2.getLabel());

		sectionDefinition2.setLabel("Test2");
		SectionDefinition sectionDefinition3 = formService.updateSectionDefinition(sectionDefinition2);
		assertNotNull(sectionDefinition3);
		assertEquals(sectionDefinition3.getName(), sectionDefinition2.getName());
		assertEquals(sectionDefinition3.getLabel(), sectionDefinition2.getLabel());

		sections = formService.searchSectionDefinitions(SectionDefinitionSearchCriteria.builder().name("T*").build(),
				PageRequest.of(0, 10));
		assertEquals(sectionsCount + 1, sections.getTotalElements());

		formService.deleteSectionDefinition(sectionDefinition3.getUuid());
	}

	@Test
	void create_form() throws IOException {
		SectionDefinition sectionDefinition2 = createSection("Test", "Test", "form/section-test.json");
		SectionDefinition sectionDefinition5 = createSection("Comment", "Comment", "form/section-comment.json");

		FormDefinition formDefinition1 = buildFormDefinition("Form1", sectionDefinition2);
		FormDefinition formDefinition2 = formService.createFormDefinition(formDefinition1);

		assertNotNull(formDefinition2);
		assertEquals(formDefinition1.getName(), formDefinition2.getName());
		assertEquals(formDefinition1.getFormSectionDefinitions().size(),
				formDefinition2.getFormSectionDefinitions().size());

		Page<FormDefinition> formDefinitions = formService.searchFormDefinitions(
				FormDefinitionSearchCriteria.builder().formName("F*").build(), PageRequest.of(0, 10));
		long formDefinitionCount = formDefinitions.getTotalElements();

		FormSectionDefinition formSectionDefinition2 = new FormSectionDefinition();
		formSectionDefinition2.setOrder(1);
		formSectionDefinition2.setReadOnly(false);
		formSectionDefinition2.setSectionDefinition(sectionDefinition5);
		formDefinition2.setFormSectionDefinitions(Arrays.asList(formSectionDefinition2));
		FormDefinition formDefinition3 = formService.updateFormDefinition(formDefinition2);

		assertNotNull(formDefinition3);
		assertEquals(formDefinition3.getName(), formDefinition3.getName());
		assertEquals(formDefinition3.getFormSectionDefinitions().size(),
				formDefinition3.getFormSectionDefinitions().size());

		formDefinitions = formService.searchFormDefinitions(
				FormDefinitionSearchCriteria.builder().formName("F*").build(), PageRequest.of(0, 10));
		assertEquals(formDefinitions.getTotalElements(), formDefinitionCount);

		formService.deleteFormDefinition(formDefinition3.getUuid());
	}

	@Test
	void create_process_form() throws IOException {
		SectionDefinition sectionDefinition1 = createSection("Comment", "Comment", "form/section-comment.json");
		FormDefinition formDefinition1 = createFormDefinition("Form1", sectionDefinition1);

		ProcessFormDefinition processFormDefinition1 = buildProcessFormDefinition("test", "UserTask_1",
				formDefinition1);
		ProcessFormDefinition processFormDefinition2 = formService.createProcessFormDefinition(processFormDefinition1);

		assertNotNull(processFormDefinition2);
		assertEquals(processFormDefinition1.getProcessDefinitionId(), processFormDefinition2.getProcessDefinitionId());
		assertEquals(processFormDefinition1.getUserTaskId(), processFormDefinition2.getUserTaskId());
		assertEquals(processFormDefinition1.getFormDefinition().getUuid(),
				processFormDefinition2.getFormDefinition().getUuid());

		Page<ProcessFormDefinition> processFormDefinitions = formService
				.searchProcessFormDefinitions(ProcessFormDefinitionSearchCriteria.builder().acceptFlexRevision(true)
						.processDefinitionId("test").userTaskId("UserTask_1").build(), PageRequest.of(0, 10));
		assertNotNull(processFormDefinitions);

		formService.deleteProcessFormDefinition(processFormDefinition2.getUuid());
	}

	@Test
	void load_workflow() throws BpmnInitializationException {
		Mockito.when(utilContextHelper.getAuthenticatedUser()).thenReturn(createAuthenticatedUser());
		Mockito.when(aclHelper.getUserByLogin(any())).thenReturn(createUser());

		// chargement de la carte de workflow
		List<ProcessDefinition> definitions1 = initializationService.searchProcessDefinitions();

		assertNotNull(definitions1);
		int size = definitions1.size();

		initializeWorkflow();

		List<ProcessDefinition> definitions2 = initializationService.searchProcessDefinitions();
		assertNotNull(definitions2);
		assertEquals(definitions2.size(), size + 1);
	}

	private void initializeWorkflow() throws BpmnInitializationException {
		URL url = Thread.currentThread().getContextClassLoader().getResource("bpmn/test.bpmn20.xml");
		// URL url = Thread.currentThread().getContextClassLoader().getResource("bpmn/handle-group.bpmn20.xml");
		DocumentContent bpmn = new DocumentContent("test.bpmn20.xml", GenerationFormat.XML.getMimeType(),
				new File(url.getFile()));
		initializationService.updateProcessDefinition("test", bpmn);
	}

	@Test
	void start_update_workflow() throws BpmnInitializationException, InvalidDataException, FormConvertException,
			FormDefinitionException, IOException {
		Mockito.when(utilContextHelper.getAuthenticatedUser()).thenReturn(createAuthenticatedUser());
		Mockito.when(aclHelper.getUserByLogin(any())).thenReturn(createUser());

		// création des formulaires
		SectionDefinition sectionDefinition1 = createSection("Comment", "Comment", "form/section-comment.json");
		FormDefinition formDefinition1 = createFormDefinition("Form1", sectionDefinition1);
		ProcessFormDefinition processFormDefinition1 = buildProcessFormDefinition("test", "UserTask_1",
				formDefinition1);
		formService.createProcessFormDefinition(processFormDefinition1);

		SectionDefinition sectionDefinition2 = createSection("Test", "Test", "form/section-test.json");
		FormDefinition formDefinition2 = createFormDefinition("Form2", sectionDefinition2);
		ProcessFormDefinition processFormDefinition2 = buildProcessFormDefinition("test", "draft", formDefinition2);
		formService.createProcessFormDefinition(processFormDefinition2);

		// chargement de la carte de workflow
		List<ProcessDefinition> definitions1 = initializationService.searchProcessDefinitions();

		assertNotNull(definitions1);
		int size = definitions1.size();

		initializeWorkflow();

		List<ProcessDefinition> definitions2 = initializationService.searchProcessDefinitions();
		assertNotNull(definitions2);
		assertEquals(definitions2.size(), size + 1);

		// création d'un tâche
		Test1AssetDescription draft = new Test1AssetDescription();
		draft.setDescription("Test workflow");
		draft.setA("toto");
		draft.setProcessDefinitionKey("test");
		draft.setFunctionalStatus("mon statut fonctionnel");
		Task t1 = test1TaskService.createDraft(draft);
		assertNotNull(t1);
		assertNotNull(t1.getAsset());

		// modification
		t1.getAsset().setDescription("titi");
		Task t1bis = test1TaskService.updateTask(t1);
		assertNotNull(t1bis);
		assertNotNull(t1bis.getAsset());
		assertEquals("titi", t1bis.getAsset().getDescription());

		List<Task> ts = taskQueryService.searchTasks(TestTaskSearchCriteria.builder().a("toto").build());
		int tsCount = ts.size();

		// doCallRealMethod().when(javaMailSender).createMimeMessage();
		doNothing().when(javaMailSender).send((MimeMessage) any());

		// execution démarrage
		Task t2 = test1TaskService.startTask(t1);
		assertNotNull(t2);

		ts = taskQueryService.searchTasks(TestTaskSearchCriteria.builder().a("toto").build());
		assertNotNull(ts);
		assertEquals(ts.size(), tsCount + 1);

		Task t3 = ts.stream().sorted(Comparator.comparing(Task::getCreationDate).reversed()).findFirst().orElse(null);

		test1TaskService.claimTask(t3.getId());

		t3.getAsset().getForm().getSections().get(0).getFields().get(0).setValues(Arrays.asList("commentaire"));
		Task t4 = test1TaskService.updateTask(t3);

		// test création entity sans le draft
		Test2AssetDescription t21bis = createTest2AssetDescription();

		Task t22 = test2TaskService.createDraft(t21bis);
		assertNotNull(t22);
		assertNotNull(t22.getAsset());
		Task t23 = test2TaskService.startTask(t22);
		assertNotNull(t23);

		ts = taskQueryService.searchTasks(TestTaskSearchCriteria.builder().a("toto").build());
		assertNotNull(ts);
		assertEquals(ts.size(), tsCount + 2);

		ts = taskQueryService.searchTasks(
				TestTaskSearchCriteria.builder().status(Arrays.asList(Status.DRAFT, Status.PENDING)).build());
		assertNotNull(ts);
		assertEquals(ts.size(), tsCount + 2);

		ts = taskQueryService.searchTasks(TestTaskSearchCriteria.builder().description("%es%").build());
		assertNotNull(ts);
		assertEquals(ts.size(), tsCount + 1);

		Action a = t4.getActions().get(0);
		test1TaskService.doIt(t4.getId(), a.getName());

		ts = taskQueryService.searchTasks(
				TestTaskSearchCriteria.builder().status(Arrays.asList(Status.DRAFT, Status.PENDING)).build());
		assertNotNull(ts);
		assertEquals(ts.size(), tsCount + 1);

	}

	private Test2AssetDescription createTest2AssetDescription() throws FormConvertException, InvalidDataException {
		Test2AssetDescription draft2 = new Test2AssetDescription();
		draft2.setDescription("Test2 workflow");
		draft2.setA("toto");
		Test2AssetDescriptionEntity t21 = test2AssetDescriptionHelper.createAssetEntity(draft2);
		t21.setProcessDefinitionKey("test");
		t21.setStatus(Status.DRAFT);
		t21.setFunctionalStatus("Draft");
		t21.setInitiator("xxx");
		t21.setCreationDate(LocalDateTime.now());
		t21.setUpdatedDate(t21.getCreationDate());
		test2AssetDescriptionDao.save(t21);
		Test2AssetDescription t21bis = test2AssetDescriptionMapper.entityToDto(t21);
		return t21bis;
	}

	private User createUser() {
		User u = new User();
		u.setLogin("test@test.com");
		u.setType(org.rudi.facet.acl.bean.UserType.PERSON);
		u.setFirstname("test");
		u.setLastname("test");
		return u;
	}

	private AuthenticatedUser createAuthenticatedUser() {
		AuthenticatedUser authenticatedUser = new AuthenticatedUser("test@test.com", UserType.PERSON);
		authenticatedUser.setRoles(Arrays.asList("ADMINISTRATOR"));
		return authenticatedUser;
	}

	protected SectionDefinition buildSection(String name, String label, String resource) throws IOException {
		SectionDefinition sectionDefinition1 = new SectionDefinition();
		sectionDefinition1.setName(name);
		sectionDefinition1.setLabel(label);
		URL sectionTestURL = Thread.currentThread().getContextClassLoader().getResource(resource);
		sectionDefinition1
				.setDefinition(FileUtils.readFileToString(new File(sectionTestURL.getFile()), StandardCharsets.UTF_8));
		return sectionDefinition1;
	}

	protected SectionDefinition createSection(String name, String label, String resource) throws IOException {
		return formService.createSectionDefinition(buildSection(name, label, resource));
	}

	protected FormDefinition buildFormDefinition(String name, SectionDefinition sectionDefinition1) {
		FormDefinition formDefinition1 = new FormDefinition();
		formDefinition1.setName("Form1");
		FormSectionDefinition formSectionDefinition1 = new FormSectionDefinition();
		formSectionDefinition1.setOrder(1);
		formSectionDefinition1.setReadOnly(false);
		formSectionDefinition1.setSectionDefinition(sectionDefinition1);
		formDefinition1.addFormSectionDefinitionsItem(formSectionDefinition1);
		return formDefinition1;
	}

	protected FormDefinition createFormDefinition(String name, SectionDefinition sectionDefinition1) {
		return formService.createFormDefinition(buildFormDefinition(name, sectionDefinition1));
	}

	protected ProcessFormDefinition buildProcessFormDefinition(String processDefinitionId, String userTaskId,
			FormDefinition formDefinition1) {
		ProcessFormDefinition processFormDefinition1 = new ProcessFormDefinition();
		processFormDefinition1.setProcessDefinitionId(processDefinitionId);
		processFormDefinition1.setFormDefinition(formDefinition1);
		processFormDefinition1.setUserTaskId(userTaskId);
		return processFormDefinition1;
	}
}