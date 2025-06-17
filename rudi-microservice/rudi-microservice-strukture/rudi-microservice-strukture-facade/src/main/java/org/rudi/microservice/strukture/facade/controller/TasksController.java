package org.rudi.microservice.strukture.facade.controller;

import java.util.List;
import java.util.UUID;

import org.rudi.bpmn.core.bean.ProcessHistoricInformation;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.facet.bpmn.bean.workflow.HistoricSearchCriteria;
import org.rudi.facet.bpmn.service.TaskQueryService;
import org.rudi.microservice.strukture.core.bean.OrganizationStatus;
import org.rudi.microservice.strukture.core.bean.workflow.StruktureTaskSearchCriteria;
import org.rudi.microservice.strukture.facade.controller.api.TasksApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TasksController implements TasksApi {

	private final TaskQueryService<StruktureTaskSearchCriteria, HistoricSearchCriteria> taskQueryService;

	/**
	 * GET /tasks/{task-id} : Chargement d&#39;une tâche Chargement d&#39;une tâche
	 *
	 * @param taskId Id de la tâche (required)
	 * @return OK (status code 200) or Bad Request Error (status code 400) or Unauthorized (status code 401) or Forbidden (status code 403) or Not
	 *         Acceptable (status code 406) or Request Timeout (status code 408) or Too Many Requests (status code 429) or Internal server error (status
	 *         code 500) or Service Unavailable (status code 503)
	 */
	@Override
	public ResponseEntity<Task> getTask(String taskId) throws Exception {
		return ResponseEntity.ok(taskQueryService.getTask(taskId));
	}

	@Override
	public ResponseEntity<List<Task>> searchTasks(String name, String description, List<String> processDefinitionKeys,
			List<Status> status, List<String> fonctionalStatus, OrganizationStatus organizationStatus, Boolean asAdmin,
			UUID organizationUuid) throws Exception {

		StruktureTaskSearchCriteria criteria = StruktureTaskSearchCriteria.builder().name(name).description(description)
				.processDefinitionKeys(processDefinitionKeys).status(status).functionalStatus(fonctionalStatus)
				.organizationStatus(organizationStatus).asAdmin(asAdmin).organizationUuid(organizationUuid).build();
		Page<Task> tasks = taskQueryService.searchTasks(criteria, Pageable.unpaged());
		return ResponseEntity.ok(tasks.getContent());
	}

	@Override
	public ResponseEntity<List<ProcessHistoricInformation>> getMyHistoricInformations() throws Exception {
		return ResponseEntity.ok(taskQueryService.getMyHistoricInformations());
	}

}
