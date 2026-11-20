/**
 * RUDI Portail
 */
package org.rudi.facet.bpmn.helper.workflow;

import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.ManualTask;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rudi.bpmn.core.bean.Action;
import org.rudi.bpmn.core.bean.Form;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.bpmn.datafactory.AuthenticatedUserDataFactory;
import org.rudi.facet.bpmn.datafactory.FormDataFactory;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.service.TaskConstants;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BpmnHelperUT {

	private static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";

	private final AuthenticatedUserDataFactory authenticatedUserDataFactory = new AuthenticatedUserDataFactory();
	private final FormDataFactory formDataFactory = new FormDataFactory();

	@Mock
	private ProcessEngine processEngine;

	@Mock
	private TaskService taskService;

	@Mock
	private RuntimeService runtimeService;

	@Mock
	private RepositoryService repositoryService;

	@Mock
	private UtilContextHelper utilContextHelper;

	@Mock
	private FormHelper formHelper;

	@Mock
	private ApplicationContext applicationContext;

	private BpmnHelper bpmnHelper;

	@BeforeEach
	void setUp() {
		bpmnHelper = new BpmnHelper(processEngine, utilContextHelper, formHelper, applicationContext);
		ReflectionTestUtils.setField(bpmnHelper, "adminRoleName", ADMIN_ROLE_NAME);
		ReflectionTestUtils.setField(bpmnHelper, "modules", List.of());
		lenient().when(processEngine.getTaskService()).thenReturn(taskService);
		lenient().when(processEngine.getRuntimeService()).thenReturn(runtimeService);
		lenient().when(processEngine.getRepositoryService()).thenReturn(repositoryService);
	}

	public static class CandidateComputationBean {

		private final List<String> candidates;

		public CandidateComputationBean(List<String> candidates) {
			this.candidates = candidates;
		}

		public List<String> computeCandidates(Object noop, Object execution) {
			return candidates;
		}
	}

	private Task buildTaskWithCandidateScriptlet(String taskId, String scriptlet) {
		Task task = mock(Task.class);
		lenient().when(task.getId()).thenReturn(taskId);
		lenient().when(task.getProcessDefinitionId()).thenReturn("procDefId");
		lenient().when(task.getProcessInstanceId()).thenReturn("pi-1");
		lenient().when(task.getTaskDefinitionKey()).thenReturn("userTaskKey");

		ProcessInstance processInstance = mock(ProcessInstance.class);
		lenient().when(processInstance.getId()).thenReturn("pi-1");
		lenient().when(processInstance.getProcessDefinitionKey()).thenReturn("procKey");
		lenient().when(processInstance.getProcessDefinitionName()).thenReturn("Process de test");
		lenient().when(processInstance.getProcessDefinitionVersion()).thenReturn(1);
		lenient().when(processInstance.getBusinessKey()).thenReturn("biz-1");

		ProcessInstanceQuery processInstanceQuery = mock(ProcessInstanceQuery.class, Mockito.RETURNS_SELF);
		lenient().when(processInstanceQuery.list()).thenReturn(List.of(processInstance));
		lenient().when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);

		UserTask userTask = new UserTask();
		userTask.setId("userTaskKey");
		userTask.setCandidateUsers(List.of(scriptlet));

		org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
		process.setId("procKey");
		process.addFlowElement(userTask);

		BpmnModel bpmnModel = new BpmnModel();
		bpmnModel.addProcess(process);

		lenient().when(repositoryService.getBpmnModel("procDefId")).thenReturn(bpmnModel);

		return task;
	}

	@Test
	@DisplayName("queryTaskById(String) : retourne la première tâche trouvée par la requête")
	void test_queryTaskById_tacheTrouvee() {
		Task task = mock(Task.class);
		TaskQuery taskQuery = mock(TaskQuery.class, Mockito.RETURNS_SELF);
		when(taskService.createTaskQuery()).thenReturn(taskQuery);
		when(taskQuery.list()).thenReturn(List.of(task));

		Task result = bpmnHelper.queryTaskById("task-1");

		assertThat(result).isEqualTo(task);
		verify(taskQuery).taskId("task-1");
	}

	@Test
	@DisplayName("queryTaskById(String, boolean) : un administrateur consultant en tant qu'admin n'est pas filtré")
	void test_queryTaskById_avecAsAdmin() {
		Task task = mock(Task.class);
		TaskQuery taskQuery = mock(TaskQuery.class, Mockito.RETURNS_SELF);
		when(taskService.createTaskQuery()).thenReturn(taskQuery);
		when(taskQuery.list()).thenReturn(List.of(task));

		AuthenticatedUser authenticatedUser = authenticatedUserDataFactory.createWithRoles("alice",
				List.of("ROLE_USER"));
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);
		when(utilContextHelper.hasAnyRoles(List.of(ADMIN_ROLE_NAME))).thenReturn(true);

		Task result = bpmnHelper.queryTaskById("task-1", true);

		assertThat(result).isEqualTo(task);
		verify(taskQuery).taskId("task-1");
		verify(taskQuery, never()).taskCandidateOrAssigned(anyString());
	}

	@Test
	@DisplayName("queryTaskByAssetId : filtre sur le type et l'id de l'asset puis retourne la tâche trouvée")
	void test_queryTaskByAssetId_tacheTrouvee() {
		Task task = mock(Task.class);
		TaskQuery taskQuery = mock(TaskQuery.class, Mockito.RETURNS_SELF);
		when(taskService.createTaskQuery()).thenReturn(taskQuery);
		when(taskQuery.list()).thenReturn(List.of(task));

		Task result = bpmnHelper.queryTaskByAssetId(Object.class, 42L);

		assertThat(result).isEqualTo(task);
		verify(taskQuery).processVariableValueEquals(TaskConstants.ME_TYPE, Object.class.getName());
	}

	@Test
	@DisplayName("saveTask : délègue la sauvegarde au TaskService d'Activiti")
	void test_saveTask_sauvegardeViaTaskService() {
		Task taskToSave = mock(Task.class);
		Task savedTask = mock(Task.class);
		when(taskService.saveTask(taskToSave)).thenReturn(savedTask);

		Task result = bpmnHelper.saveTask(taskToSave);

		assertThat(result).isEqualTo(savedTask);
	}

	@Test
	@DisplayName("recomputeAndAddCandidate : ajoute l'utilisateur comme candidat quand il est éligible et pas déjà affecté")
	void test_recomputeAndAddCandidate_ajouteCandidatEligible() {
		Task task = buildTaskWithCandidateScriptlet("task-1",
				"${candidateComputationBean.computeCandidates(execution, \"alice\")}");
		TaskQuery taskQuery = mock(TaskQuery.class, Mockito.RETURNS_SELF);
		when(taskService.createTaskQuery()).thenReturn(taskQuery);
		when(taskQuery.list()).thenReturn(List.of(task));
		when(applicationContext.getBean("candidateComputationBean"))
				.thenReturn(new CandidateComputationBean(List.of("alice")));
		when(taskService.getIdentityLinksForTask("task-1")).thenReturn(List.of());

		bpmnHelper.recomputeAndAddCandidate("alice", "procDefKey");

		verify(taskQuery).processDefinitionKey("procDefKey");
		verify(taskService).addCandidateUser("task-1", "alice");
	}

	@Test
	@DisplayName("recomputeAndRemoveCandidate : retire le candidat qui n'est plus éligible")
	void test_recomputeAndRemoveCandidate_supprimeCandidatNonEligible() {
		Task task = buildTaskWithCandidateScriptlet("task-1",
				"${candidateComputationBean.computeCandidates(execution, \"bob\")}");
		TaskQuery taskQuery = mock(TaskQuery.class, Mockito.RETURNS_SELF);
		when(taskService.createTaskQuery()).thenReturn(taskQuery);
		when(taskQuery.list()).thenReturn(List.of(task));
		when(applicationContext.getBean("candidateComputationBean"))
				.thenReturn(new CandidateComputationBean(List.of("bob")));
		IdentityLink identityLink = mock(IdentityLink.class);
		when(identityLink.getUserId()).thenReturn("alice");
		when(taskService.getIdentityLinksForTask("task-1")).thenReturn(List.of(identityLink));

		bpmnHelper.recomputeAndRemoveCandidate("alice", "procDefKey");

		verify(taskService).deleteCandidateUser("task-1", "alice");
		verify(runtimeService, never()).deleteProcessInstance(anyString(), anyString());
	}

	@Test
	@DisplayName("handleUserTasks : arrête la tâche qui n'a plus aucun candidat")
	void test_handleUserTasks_arreteTacheSansCandidat() {
		Task task = buildTaskWithCandidateScriptlet("task-1",
				"${candidateComputationBean.computeCandidates(execution, \"alice\")}");
		TaskQuery taskQuery = mock(TaskQuery.class, Mockito.RETURNS_SELF);
		when(taskService.createTaskQuery()).thenReturn(taskQuery);
		when(taskQuery.list()).thenReturn(List.of(task));
		when(applicationContext.getBean("candidateComputationBean"))
				.thenReturn(new CandidateComputationBean(List.of()));
		IdentityLink identityLink = mock(IdentityLink.class);
		when(identityLink.getUserId()).thenReturn("alice");
		when(taskService.getIdentityLinksForTask("task-1")).thenReturn(List.of(identityLink));

		bpmnHelper.handleUserTasks("alice", "procDefKey");

		verify(runtimeService).deleteProcessInstance("pi-1", "Process stopped by user");
	}

	@Test
	@DisplayName("applyACLCriteria : un utilisateur non admin est filtré sur ses rôles et son login")
	void test_applyACLCriteria_nonAdminAvecRoles() {
		TaskQuery taskQuery = mock(TaskQuery.class, Mockito.RETURNS_SELF);
		AuthenticatedUser authenticatedUser = authenticatedUserDataFactory.createWithRoles("alice",
				List.of("ROLE_USER"));
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);
		when(utilContextHelper.hasAnyRoles(List.of(ADMIN_ROLE_NAME))).thenReturn(false);

		bpmnHelper.applyACLCriteria(taskQuery, false, null);

		verify(taskQuery).or();
		verify(taskQuery).taskCandidateOrAssigned("alice");
		verify(taskQuery).taskCandidateGroupIn(List.of("ROLE_USER"));
		verify(taskQuery).endOr();
	}

	@Test
	@DisplayName("applySortCriteria : trie par priorité croissante puis par date de création décroissante")
	void test_applySortCriteria_appliqueTriPrioriteEtDate() {
		TaskQuery taskQuery = mock(TaskQuery.class, Mockito.RETURNS_SELF);

		bpmnHelper.applySortCriteria(taskQuery);

		verify(taskQuery).orderByTaskPriority();
		verify(taskQuery).asc();
		verify(taskQuery).orderByTaskCreateTime();
		verify(taskQuery).desc();
	}

	@Test
	@DisplayName("applyTaskIdCriteria : filtre la requête sur l'id de tâche")
	void test_applyTaskIdCriteria_appliqueFiltreTaskId() {
		TaskQuery taskQuery = mock(TaskQuery.class, Mockito.RETURNS_SELF);

		bpmnHelper.applyTaskIdCriteria(taskQuery, "task-42");

		verify(taskQuery).taskId("task-42");
	}

	@Test
	@DisplayName("lookupProcessInstance : retourne l'instance de process associée à la tâche")
	void test_lookupProcessInstance_instanceTrouvee() {
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn("pi-1");
		ProcessInstance processInstance = mock(ProcessInstance.class);
		ProcessInstanceQuery processInstanceQuery = mock(ProcessInstanceQuery.class, Mockito.RETURNS_SELF);
		when(processInstanceQuery.list()).thenReturn(List.of(processInstance));
		when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);

		ProcessInstance result = bpmnHelper.lookupProcessInstance(task);

		assertThat(result).isEqualTo(processInstance);
		verify(processInstanceQuery).processInstanceId("pi-1");
	}

	@Test
	@DisplayName("lookupProcessInstanceBusinessKey(Task) : retourne la businessKey de l'instance de process")
	void test_lookupProcessInstanceBusinessKey_parTask_retourneBusinessKey() {
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn("pi-1");
		ProcessInstance processInstance = mock(ProcessInstance.class);
		when(processInstance.getBusinessKey()).thenReturn("biz-1");
		ProcessInstanceQuery processInstanceQuery = mock(ProcessInstanceQuery.class, Mockito.RETURNS_SELF);
		when(processInstanceQuery.list()).thenReturn(List.of(processInstance));
		when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);

		String result = bpmnHelper.lookupProcessInstanceBusinessKey(task);

		assertThat(result).isEqualTo("biz-1");
	}

	@Test
	@DisplayName("lookupProcessInstanceAssetType : résout la classe de l'asset depuis la variable de process")
	void test_lookupProcessInstanceAssetType_typeTrouve() throws InvalidDataException, ClassNotFoundException {
		Task task = mock(Task.class);
		when(task.getExecutionId()).thenReturn("exec-1");
		VariableInstance variableInstance = mock(VariableInstance.class);
		when(variableInstance.getTextValue())
				.thenReturn("org.rudi.facet.bpmn.entity.workflow.AssetDescription1TestEntity");
		when(runtimeService.getVariableInstances("exec-1"))
				.thenReturn(Map.of(TaskConstants.ME_TYPE, variableInstance));

		Class<?> result = bpmnHelper.lookupProcessInstanceAssetType(task);

		assertThat(result).isEqualTo(
				Class.forName("org.rudi.facet.bpmn.entity.workflow.AssetDescription1TestEntity"));
	}

	@Test
	@DisplayName("lookupProcessInstanceBusinessKey(String, Integer) : retourne la clé pour la version demandée")
	void test_lookupProcessInstanceBusinessKey_parDefinition_retourneKey() {
		ProcessDefinitionQuery processDefinitionQuery = mock(ProcessDefinitionQuery.class, Mockito.RETURNS_SELF);
		ProcessDefinition processDefinition = mock(ProcessDefinition.class);
		when(processDefinition.getKey()).thenReturn("the-key");
		when(processDefinitionQuery.list()).thenReturn(List.of(processDefinition));
		when(repositoryService.createProcessDefinitionQuery()).thenReturn(processDefinitionQuery);

		String result = bpmnHelper.lookupProcessInstanceBusinessKey("procKey", 2);

		assertThat(result).isEqualTo("the-key");
		verify(processDefinitionQuery).processDefinitionVersion(2);
	}

	@Test
	@DisplayName("lookupUserTask : retrouve la UserTask BPMN correspondant à la tâche Activiti")
	void test_lookupUserTask_tacheUtilisateurTrouvee() {
		Task task = mock(Task.class);
		when(task.getProcessDefinitionId()).thenReturn("procDefId");
		when(task.getTaskDefinitionKey()).thenReturn("userTaskKey");
		when(task.getProcessInstanceId()).thenReturn("pi-1");

		ProcessInstance processInstance = mock(ProcessInstance.class);
		when(processInstance.getProcessDefinitionKey()).thenReturn("procKey");
		ProcessInstanceQuery processInstanceQuery = mock(ProcessInstanceQuery.class, Mockito.RETURNS_SELF);
		when(processInstanceQuery.list()).thenReturn(List.of(processInstance));
		when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);

		UserTask userTask = new UserTask();
		userTask.setId("userTaskKey");
		org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
		process.setId("procKey");
		process.addFlowElement(userTask);
		BpmnModel bpmnModel = new BpmnModel();
		bpmnModel.addProcess(process);
		when(repositoryService.getBpmnModel("procDefId")).thenReturn(bpmnModel);

		UserTask result = bpmnHelper.lookupUserTask(task);

		assertThat(result).isEqualTo(userTask);
	}

	@Test
	@DisplayName("computeTaskActions : calcule les actions depuis la gateway et depuis la tâche suivante puis renseigne leur formulaire")
	void test_computeTaskActions_calculeActionsAvecFormulaire() throws Exception {
		Task task = mock(Task.class);
		when(task.getProcessDefinitionId()).thenReturn("procDefId");
		when(task.getTaskDefinitionKey()).thenReturn("userTaskKey");
		when(task.getProcessInstanceId()).thenReturn("pi-1");

		ProcessInstance processInstance = mock(ProcessInstance.class);
		when(processInstance.getProcessDefinitionKey()).thenReturn("procKey");
		ProcessInstanceQuery processInstanceQuery = mock(ProcessInstanceQuery.class, Mockito.RETURNS_SELF);
		when(processInstanceQuery.list()).thenReturn(List.of(processInstance));
		when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);

		UserTask userTask = new UserTask();
		userTask.setId("userTaskKey");

		ExclusiveGateway gateway = new ExclusiveGateway();
		gateway.setId("gateway");
		SequenceFlow gatewayOutgoing = new SequenceFlow();
		gatewayOutgoing.setName("Approve");
		gatewayOutgoing.setDocumentation("Approuver");
		gateway.setOutgoingFlows(List.of(gatewayOutgoing));

		SequenceFlow toGateway = new SequenceFlow();
		toGateway.setTargetFlowElement(gateway);

		ManualTask nextTask = new ManualTask();
		nextTask.setId("nextTask");
		SequenceFlow toTask = new SequenceFlow();
		toTask.setName("Reject");
		toTask.setDocumentation("Rejeter");
		toTask.setTargetFlowElement(nextTask);

		userTask.setOutgoingFlows(List.of(toGateway, toTask));

		org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
		process.setId("procKey");
		process.addFlowElement(userTask);
		BpmnModel bpmnModel = new BpmnModel();
		bpmnModel.addProcess(process);
		when(repositoryService.getBpmnModel("procDefId")).thenReturn(bpmnModel);

		Form approveForm = formDataFactory.createEmpty();
		Form rejectForm = formDataFactory.createEmpty();
		when(formHelper.lookupForm(task, "Approve")).thenReturn(approveForm);
		when(formHelper.lookupForm(task, "Reject")).thenReturn(rejectForm);

		List<Action> result = bpmnHelper.computeTaskActions(task);

		assertThat(result).hasSize(2);
		assertThat(result).extracting(Action::getName).containsExactlyInAnyOrder("Approve", "Reject");
		assertThat(result).extracting(Action::getForm).containsExactlyInAnyOrder(approveForm, rejectForm);
	}

	@Test
	@DisplayName("extractActions : ajoute l'action par défaut quand la tâche définie n'est pas une UserTask")
	void test_extractActions_ajouteActionParDefaut() {
		ManualTask manualTask = new ManualTask();
		manualTask.setId("manualTaskKey");

		org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
		process.setId("procKey");
		process.addFlowElement(manualTask);
		BpmnModel bpmnModel = new BpmnModel();
		bpmnModel.addProcess(process);
		when(repositoryService.getBpmnModel("procDefId")).thenReturn(bpmnModel);

		List<Action> result = bpmnHelper.extractActions("procKey", "procDefId", "manualTaskKey");

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo(BpmnHelper.DEFAULT_ACTION);
		assertThat(result.get(0).getLabel()).isEqualTo("Envoyer");
	}

	@Test
	@DisplayName("addTaskCandidateUser : ajoute l'utilisateur candidat via le TaskService")
	void test_addTaskCandidateUser_ajouteCandidat() {
		bpmnHelper.addTaskCandidateUser("task-1", "alice");

		verify(taskService).addCandidateUser("task-1", "alice");
	}

	@Test
	@DisplayName("removeTaskCandidateUser : retire l'utilisateur candidat via le TaskService")
	void test_removeTaskCandidateUser_supprimeCandidat() {
		bpmnHelper.removeTaskCandidateUser("task-1", "alice");

		verify(taskService).deleteCandidateUser("task-1", "alice");
	}

	@Test
	@DisplayName("lookupSequenceFlow : retrouve le sequence flow correspondant à l'action sur la tâche suivante")
	void test_lookupSequenceFlow_viaTacheSuivante() {
		Task task = mock(Task.class);
		when(task.getProcessDefinitionId()).thenReturn("procDefId");
		when(task.getTaskDefinitionKey()).thenReturn("userTaskKey");
		when(task.getProcessInstanceId()).thenReturn("pi-1");

		ProcessInstance processInstance = mock(ProcessInstance.class);
		when(processInstance.getProcessDefinitionKey()).thenReturn("procKey");
		ProcessInstanceQuery processInstanceQuery = mock(ProcessInstanceQuery.class, Mockito.RETURNS_SELF);
		when(processInstanceQuery.list()).thenReturn(List.of(processInstance));
		when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);

		UserTask userTask = new UserTask();
		userTask.setId("userTaskKey");

		ExclusiveGateway gateway = new ExclusiveGateway();
		SequenceFlow gatewayOutgoing = new SequenceFlow();
		gatewayOutgoing.setName("other-action");
		gateway.setOutgoingFlows(List.of(gatewayOutgoing));
		SequenceFlow toGateway = new SequenceFlow();
		toGateway.setTargetFlowElement(gateway);

		ManualTask nextTask = new ManualTask();
		nextTask.setId("nextTask");
		SequenceFlow toTask = new SequenceFlow();
		toTask.setName("APPROVE");
		toTask.setTargetFlowElement(nextTask);

		userTask.setOutgoingFlows(List.of(toGateway, toTask));

		org.activiti.bpmn.model.Process process = new org.activiti.bpmn.model.Process();
		process.setId("procKey");
		process.addFlowElement(userTask);
		BpmnModel bpmnModel = new BpmnModel();
		bpmnModel.addProcess(process);
		when(repositoryService.getBpmnModel("procDefId")).thenReturn(bpmnModel);

		SequenceFlow result = bpmnHelper.lookupSequenceFlow(task, "approve");

		assertThat(result).isEqualTo(toTask);
	}

	@Test
	@DisplayName("recomputeCandidateUsers : exécute le scriptlet de la UserTask et retourne les candidats calculés")
	void test_recomputeCandidateUsers_executeScriptletEtRetourneCandidats() {
		Task task = buildTaskWithCandidateScriptlet("task-1",
				"${candidateComputationBean.computeCandidates(execution, \"alice\")}");
		when(applicationContext.getBean("candidateComputationBean"))
				.thenReturn(new CandidateComputationBean(List.of("alice")));

		List<String> result = bpmnHelper.recomputeCandidateUsers(task, true);

		assertThat(result).containsExactly("alice");
	}

	@Test
	@DisplayName("isCandidateUser : vrai quand l'utilisateur fait partie des identity links de la tâche")
	void test_isCandidateUser_utilisateurCandidat() {
		IdentityLink identityLink = mock(IdentityLink.class);
		when(identityLink.getUserId()).thenReturn("alice");
		when(taskService.getIdentityLinksForTask("task-1")).thenReturn(List.of(identityLink));

		boolean result = bpmnHelper.isCandidateUser("task-1", "alice");

		assertThat(result).isTrue();
	}

	@Test
	@DisplayName("stopTaskByTaskId : arrête l'instance de process associée à la tâche trouvée")
	void test_stopTaskByTaskId_arreteProcessInstance() {
		Task task = mock(Task.class);
		when(task.getProcessInstanceId()).thenReturn("pi-1");
		TaskQuery taskQuery = mock(TaskQuery.class, Mockito.RETURNS_SELF);
		when(taskService.createTaskQuery()).thenReturn(taskQuery);
		when(taskQuery.list()).thenReturn(List.of(task));

		ProcessInstance processInstance = mock(ProcessInstance.class);
		when(processInstance.getId()).thenReturn("pi-1");
		ProcessInstanceQuery processInstanceQuery = mock(ProcessInstanceQuery.class, Mockito.RETURNS_SELF);
		when(processInstanceQuery.list()).thenReturn(List.of(processInstance));
		when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);

		bpmnHelper.stopTaskByTaskId("task-1");

		verify(taskQuery).taskId("task-1");
		verify(runtimeService).deleteProcessInstance("pi-1", "Process stopped by user");
	}

	@Test
	@DisplayName("stopProcessInstance : supprime l'instance de process via le RuntimeService")
	void test_stopProcessInstance_supprimeInstance() {
		bpmnHelper.stopProcessInstance("pi-1");

		verify(runtimeService).deleteProcessInstance("pi-1", "Process stopped by user");
	}
}
