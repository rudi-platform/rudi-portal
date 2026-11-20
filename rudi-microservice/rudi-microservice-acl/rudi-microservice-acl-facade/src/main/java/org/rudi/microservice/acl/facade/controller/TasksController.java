package org.rudi.microservice.acl.facade.controller;

import java.util.List;

import org.rudi.bpmn.core.bean.ProcessHistoricInformation;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.facet.bpmn.bean.workflow.HistoricSearchCriteria;
import org.rudi.facet.bpmn.bean.workflow.TaskSearchCriteria;
import org.rudi.facet.bpmn.service.TaskQueryService;
import org.rudi.microservice.acl.facade.controller.api.TasksApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controller générique de consultation des tâches (recherche, chargement par id, historique)
 */
@RestController
@RequiredArgsConstructor
public class TasksController implements TasksApi {

	private final TaskQueryService<TaskSearchCriteria, HistoricSearchCriteria> taskQueryService;

	@Override
	public ResponseEntity<Task> getTask(String taskId) throws Exception {
		return ResponseEntity.ok(taskQueryService.getTask(taskId));
	}

	@Override
	public ResponseEntity<List<Task>> searchTasks(String description, List<String> processDefinitionKeys,
			List<Status> status, List<String> fonctionalStatus, Boolean asAdmin, String userLogin) throws Exception {
		TaskSearchCriteria criteria = TaskSearchCriteria.builder().description(description)
				.processDefinitionKeys(processDefinitionKeys).status(status).functionalStatus(fonctionalStatus)
				.asAdmin(Boolean.TRUE.equals(asAdmin)).userLogin(userLogin).build();
		Page<Task> tasks = taskQueryService.searchTasks(criteria, Pageable.unpaged());
		return ResponseEntity.ok(tasks.getContent());
	}

	@Override
	public ResponseEntity<List<ProcessHistoricInformation>> getMyHistoricInformations() throws Exception {
		return ResponseEntity.ok(taskQueryService.getMyHistoricInformations());
	}

}


