package org.rudi.microservice.projekt.facade.controller;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.rudi.bpmn.core.bean.Form;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.common.core.DocumentContent;
import org.rudi.common.facade.helper.ControllerHelper;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.facet.bpmn.service.TaskService;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kmedia.bean.KindOfData;
import org.rudi.microservice.projekt.core.bean.ComputeIndicatorsSearchCriteria;
import org.rudi.microservice.projekt.core.bean.Indicators;
import org.rudi.microservice.projekt.core.bean.LinkedDataset;
import org.rudi.microservice.projekt.core.bean.LinkedDatasetStatus;
import org.rudi.microservice.projekt.core.bean.NewDatasetRequest;
import org.rudi.microservice.projekt.core.bean.PagedProjectList;
import org.rudi.microservice.projekt.core.bean.Project;
import org.rudi.microservice.projekt.core.bean.ProjectSearchCriteria;
import org.rudi.microservice.projekt.core.bean.ProjectStatus;
import org.rudi.microservice.projekt.facade.controller.api.ProjectsApi;
import org.rudi.microservice.projekt.service.project.LinkedDatasetService;
import org.rudi.microservice.projekt.service.project.ProjectService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProjectController implements ProjectsApi {

	private final ControllerHelper controllerHelper;
	private final ProjectService projectService;
	private final LinkedDatasetService linkedDatasetService;
	private final UtilPageable utilPageable;
	private final TaskService<Project> projectTaskService;

	@Override
	@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODULE_PROJEKT_ADMINISTRATOR', 'MODULE_PROJEKT', 'PROJECT_MANAGER', 'USER')")
	public ResponseEntity<Project> createProject(Project project) throws Exception {
		val createdProject = projectService.createProject(project);
		val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
				.buildAndExpand(createdProject.getUuid()).toUri();
		return ResponseEntity.created(location).body(createdProject);
	}

	@Override
	public ResponseEntity<Project> getProject(UUID uuid) throws AppServiceNotFoundException {
		return ResponseEntity.ok(projectService.getProject(uuid));
	}

	@Override
	public ResponseEntity<PagedProjectList> searchProjects(@Valid List<UUID> datasetUuids,
			@Valid List<UUID> linkedDatasetUuids, @Valid List<UUID> ownerUuids, @Valid List<ProjectStatus> status,
			@Valid Integer offset, @Valid Integer limit, @Valid String order) throws Exception {
		val searchCriteria = new ProjectSearchCriteria()
				.datasetUuids(datasetUuids)
				.linkedDatasetUuids(linkedDatasetUuids)
				.ownerUuids(ownerUuids)
				.status(status);
		val pageable = utilPageable.getPageable(offset, limit, order);
		val page = projectService.searchProjects(searchCriteria, pageable);
		return ResponseEntity.ok(new PagedProjectList().total(page.getTotalElements()).elements(page.getContent()));
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODULE_PROJEKT_ADMINISTRATOR', 'MODULE_PROJEKT', 'PROJECT_MANAGER', 'USER')")
	public ResponseEntity<Void> updateProject(UUID uuid, Project project) throws AppServiceException {
		project.setUuid(uuid);
		projectService.updateProject(project);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODULE_PROJEKT_ADMINISTRATOR', 'MODULE_PROJEKT', 'PROJECT_MANAGER', 'USER')")
	public ResponseEntity<Void> deleteProject(UUID uuid) throws AppServiceException {
		projectService.deleteProject(uuid);
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<Resource> downloadProjectMediaByType(UUID uuid, KindOfData kindOfData) throws Exception {
		final DocumentContent documentContent = projectService.getMediaContent(uuid, kindOfData);
		return controllerHelper.downloadableResponseEntity(documentContent);
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODULE_PROJEKT_ADMINISTRATOR', 'MODULE_PROJEKT', 'PROJECT_MANAGER', 'USER')")
	public ResponseEntity<Void> uploadProjectMediaByType(UUID projectUuid, KindOfData kindOfData, Resource body)
			throws Exception {
		projectService.uploadMedia(projectUuid, kindOfData, body);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODULE_PROJEKT_ADMINISTRATOR', 'MODULE_PROJEKT', 'PROJECT_MANAGER', 'USER')")
	public ResponseEntity<Void> deleteProjectMediaByType(UUID projectUuid, KindOfData kindOfData) throws Exception {
		projectService.deleteMedia(projectUuid, kindOfData);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<List<LinkedDataset>> getLinkedDatasets(UUID projectUuid, LinkedDatasetStatus status)
			throws AppServiceNotFoundException {
		return ResponseEntity.ok(linkedDatasetService.getLinkedDatasets(projectUuid, status));
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODULE_PROJEKT_ADMINISTRATOR', 'MODULE_PROJEKT', 'PROJECT_MANAGER', 'USER')")
	public ResponseEntity<LinkedDataset> linkProjectToDataset(UUID projectUuid, LinkedDataset linkedDataset)
			throws AppServiceNotFoundException, DataverseAPIException, AppServiceException {
		return ResponseEntity.ok(linkedDatasetService.linkProjectToDataset(projectUuid, linkedDataset));
	}

	@Override
	public ResponseEntity<LinkedDataset> updateLinkedDataset(UUID projectUuid, LinkedDataset linkedDataset)
			throws Exception {
		return ResponseEntity.ok(linkedDatasetService.updateLinkedDataset(projectUuid, linkedDataset));
	}

	@Override
	public ResponseEntity<Void> unlinkProjectToDataset(UUID projectUuid, UUID linkedDatasetUUID)
			throws AppServiceNotFoundException, AppServiceException {
		linkedDatasetService.unlinkProjectToDataset(projectUuid, linkedDatasetUUID);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<LinkedDataset> getLinkedDataset(UUID projectUuid, UUID linkedDatasetUUID)
			throws AppServiceNotFoundException {
		return ResponseEntity.ok(linkedDatasetService.getLinkedDataset(projectUuid, linkedDatasetUUID));
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODULE_PROJEKT_ADMINISTRATOR', 'MODULE_PROJEKT', 'PROJECT_MANAGER', 'USER')")
	public ResponseEntity<NewDatasetRequest> createNewDatasetRequest(UUID projectUuid, NewDatasetRequest datasetRequest)
			throws AppServiceNotFoundException, AppServiceException {
		val createdRequest = projectService.createNewDatasetRequest(projectUuid, datasetRequest);
		val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
				.buildAndExpand(createdRequest.getUuid()).toUri();
		return ResponseEntity.created(location).body(createdRequest);
	}

	@Override
	public ResponseEntity<List<NewDatasetRequest>> getNewDatasetRequests(UUID projectUuid) throws Exception {
		return ResponseEntity.ok(projectService.getNewDatasetRequests(projectUuid));
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODULE_PROJEKT_ADMINISTRATOR', 'MODULE_PROJEKT', 'PROJECT_MANAGER', 'USER')")
	public ResponseEntity<Void> updateNewDatasetRequest(UUID projectUuid, NewDatasetRequest newDatasetRequest)
			throws Exception {
		projectService.updateNewDatasetRequest(projectUuid, newDatasetRequest);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<NewDatasetRequest> getNewDatasetRequestByUuid(UUID projectUuid, UUID requestUuid)
			throws Exception {
		return ResponseEntity.ok(projectService.getNewDatasetRequestByUuid(projectUuid, requestUuid));
	}

	@Override
	@PreAuthorize("hasAnyRole('ADMINISTRATOR', 'MODULE_PROJEKT_ADMINISTRATOR', 'MODULE_PROJEKT', 'PROJECT_MANAGER', 'USER')")
	public ResponseEntity<Void> deleteNewDatasetRequest(UUID projectUuid, UUID requestUuid) throws Exception {
		projectService.deleteNewDatasetRequest(projectUuid, requestUuid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Task> claimProjectTask(String taskId) throws Exception {
		return ResponseEntity.ok(projectTaskService.claimTask(taskId));
	}

	@Override
	public ResponseEntity<Task> createProjectDraft(@Valid Project project) throws Exception {
		return ResponseEntity.ok(projectTaskService.createDraft(project));
	}

	@Override
	public ResponseEntity<Void> doItProject(String taskId, String actionName) throws Exception {
		projectTaskService.doIt(taskId, actionName);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Override
	public ResponseEntity<Form> lookupProjectDraftForm() throws Exception {
		return ResponseEntity.ok(projectTaskService.lookupDraftForm());
	}

	@Override
	public ResponseEntity<Task> startProjectTask(Task task) throws Exception {
		return ResponseEntity.ok(projectTaskService.startTask(task));
	}

	@Override
	public ResponseEntity<Task> unclaimProjectTask(String taskId) throws Exception {
		return ResponseEntity.ok(projectTaskService.claimTask(taskId));
	}

	@Override
	public ResponseEntity<Task> updateProjectTask(Task task) throws Exception {
		return ResponseEntity.ok(projectTaskService.updateTask(task));
	}

	public ResponseEntity<Indicators> computeIndicators(@Parameter(description = "UUID du projet",required=true) @PathVariable("project-uuid") UUID projectUuid
			,@Parameter(description = "UUID du producteur de données dont on ne veut pas les infos sur les demandes d'accès") @Valid @RequestParam(value = "excluded-producer-uuid", required = false) UUID excludedProducerUuid
	) throws Exception {
		ComputeIndicatorsSearchCriteria searchCriteria = new ComputeIndicatorsSearchCriteria();
		searchCriteria.setProjectUuid(projectUuid);
		searchCriteria.setExcludedProducerUuid(excludedProducerUuid);
		Indicators indicators = projectService.computeIndicators(searchCriteria);
		return ResponseEntity.ok(indicators);
	}
}