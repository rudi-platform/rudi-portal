/**
 * RUDI Portail
 */
package org.rudi.microservice.projekt.facade.controller;

import static org.rudi.common.core.security.QuotedRoleCodes.ADMINISTRATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_KALIM;

import java.util.UUID;

import org.rudi.bpmn.core.bean.Form;
import org.rudi.bpmn.core.bean.ProcessHistoricInformation;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.facet.bpmn.service.TaskService;
import org.rudi.microservice.projekt.core.bean.NewDatasetRequest;
import org.rudi.microservice.projekt.core.bean.Project;
import org.rudi.microservice.projekt.facade.controller.api.NewDatasetRequestApi;
import org.rudi.microservice.projekt.service.project.NewDatasetRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * @author FNI18300
 */
@RestController
@RequiredArgsConstructor
public class NewDatasetRequestController implements NewDatasetRequestApi {

	private final TaskService<NewDatasetRequest> newDatasetRequestTaskService;

	private final NewDatasetRequestService newDatasetRequestService;

	@Override
	public ResponseEntity<Task> claimNewDatasetRequestTask(String taskId) throws Exception {
		return ResponseEntity.ok(newDatasetRequestTaskService.claimTask(taskId));
	}

	@Override
	public ResponseEntity<Task> createNewDatasetRequestDraft(NewDatasetRequest project) throws Exception {
		return ResponseEntity.ok(newDatasetRequestTaskService.createDraft(project));
	}

	@Override
	public ResponseEntity<Void> doItNewDatasetRequest(String taskId, String actionName) throws Exception {
		newDatasetRequestTaskService.doIt(taskId, actionName);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Override
	public ResponseEntity<Form> lookupNewDatasetRequestDraftForm() throws Exception {
		return ResponseEntity.ok(newDatasetRequestTaskService.lookupDraftForm(null));
	}

	@Override
	public ResponseEntity<Task> startNewDatasetRequestTask(Task task) throws Exception {
		return ResponseEntity.ok(newDatasetRequestTaskService.startTask(task));
	}

	@Override
	public ResponseEntity<Task> unclaimNewDatasetRequestTask(String taskId) throws Exception {
		newDatasetRequestTaskService.unclaimTask(taskId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Override
	public ResponseEntity<Task> updateNewDatasetRequestTask(Task task) throws Exception {
		return ResponseEntity.ok(newDatasetRequestTaskService.updateTask(task));
	}

	@Override
	public ResponseEntity<Project> findProjectByNewDatasetRequest(UUID newDatasetRequestUuid) throws Exception {
		return ResponseEntity.ok(newDatasetRequestService.findProjectByNewDatasetRequest(newDatasetRequestUuid));
	}

	@Override
	public ResponseEntity<ProcessHistoricInformation> getNewDatasetRequestTaskHistoryByTaskId(String taskId,
			@Valid Boolean asAdmin) throws Exception {
		return ResponseEntity.ok(newDatasetRequestTaskService.getTaskHistoryByTaskId(taskId, asAdmin));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_KALIM + ")")
	public ResponseEntity<Void> deleteNewDatasetRequestTask(String taskId) throws Exception {
		newDatasetRequestTaskService.stopTaskByTaskId(taskId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
