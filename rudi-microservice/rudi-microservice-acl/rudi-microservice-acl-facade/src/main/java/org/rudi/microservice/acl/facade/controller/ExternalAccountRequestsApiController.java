package org.rudi.microservice.acl.facade.controller;

import java.util.UUID;

import org.rudi.bpmn.core.bean.Form;
import org.rudi.bpmn.core.bean.ProcessHistoricInformation;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.facet.bpmn.service.TaskService;
import org.rudi.microservice.acl.core.bean.ExternalAccountRequest;
import org.rudi.microservice.acl.facade.controller.api.ExternalAccountRequestsApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controller exposant le workflow de demande de création de compte dans le système d'authentification externe (LDAP)
 */
@RestController
@RequiredArgsConstructor
public class ExternalAccountRequestsApiController implements ExternalAccountRequestsApi {

	private final TaskService<ExternalAccountRequest> externalAccountRequestTaskService;

	/**
	 * GET /external-account-requests/tasks/draft-form : chargement du formulaire draft
	 */
	@Override
	public ResponseEntity<Form> getExternalAccountRequestDraftForm() throws Exception {
		return ResponseEntity.ok(externalAccountRequestTaskService.lookupDraftForm(null));
	}

	/**
	 * POST /external-account-requests : soumission publique d'une demande de création de compte (création du draft + démarrage immédiat du
	 * workflow)
	 */
	@Override
	public ResponseEntity<UUID> requestExternalAccountCreation(ExternalAccountRequest externalAccountRequest)
			throws Exception {
		Task draft = externalAccountRequestTaskService.createDraft(externalAccountRequest);
		Task startedTask = externalAccountRequestTaskService.startTask(draft);
		return ResponseEntity.ok(startedTask.getAsset().getUuid());
	}

	@Override
	public ResponseEntity<Task> createExternalAccountRequestDraft(ExternalAccountRequest externalAccountRequest)
			throws Exception {
		return ResponseEntity.ok(externalAccountRequestTaskService.createDraft(externalAccountRequest));
	}

	@Override
	public ResponseEntity<Task> updateExternalAccountRequestTask(Task task) throws Exception {
		return ResponseEntity.ok(externalAccountRequestTaskService.updateTask(task));
	}

	@Override
	public ResponseEntity<Task> startExternalAccountRequestTask(Task task) throws Exception {
		return ResponseEntity.ok(externalAccountRequestTaskService.startTask(task));
	}

	@Override
	public ResponseEntity<Task> claimExternalAccountRequestTask(String taskId) throws Exception {
		return ResponseEntity.ok(externalAccountRequestTaskService.claimTask(taskId));
	}

	@Override
	public ResponseEntity<Task> unclaimExternalAccountRequestTask(String taskId) throws Exception {
		externalAccountRequestTaskService.unclaimTask(taskId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Override
	public ResponseEntity<Void> doItExternalAccountRequest(String taskId, String actionName) throws Exception {
		externalAccountRequestTaskService.doIt(taskId, actionName);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Override
	public ResponseEntity<ProcessHistoricInformation> getExternalAccountRequestTaskHistoryByTaskId(String taskId,
			Boolean asAdmin) throws Exception {
		return ResponseEntity.ok(externalAccountRequestTaskService.getTaskHistoryByTaskId(taskId, asAdmin));
	}

}

