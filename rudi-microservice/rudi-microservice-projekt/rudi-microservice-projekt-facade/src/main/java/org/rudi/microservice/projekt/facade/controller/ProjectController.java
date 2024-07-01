package org.rudi.microservice.projekt.facade.controller;

import java.util.List;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Form;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.common.core.DocumentContent;
import org.rudi.common.facade.helper.ControllerHelper;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.facet.apimaccess.exception.APIManagerException;
import org.rudi.facet.bpmn.exception.FormDefinitionException;
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
import org.rudi.microservice.projekt.core.bean.ProjectByOwner;
import org.rudi.microservice.projekt.core.bean.ProjectSearchCriteria;
import org.rudi.microservice.projekt.core.bean.ProjectStatus;
import org.rudi.microservice.projekt.facade.controller.api.ProjectsApi;
import org.rudi.microservice.projekt.service.project.LinkedDatasetService;
import org.rudi.microservice.projekt.service.project.NewDatasetRequestService;
import org.rudi.microservice.projekt.service.project.ProjectService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.val;
import static org.rudi.common.core.security.QuotedRoleCodes.ADMINISTRATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.MODERATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_PROJEKT;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_PROJEKT_ADMINISTRATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.PROJECT_MANAGER;
import static org.rudi.common.core.security.QuotedRoleCodes.PROVIDER;
import static org.rudi.common.core.security.QuotedRoleCodes.USER;

@RestController
@RequiredArgsConstructor
public class ProjectController implements ProjectsApi {

	private final ControllerHelper controllerHelper;
	private final ProjectService projectService;
	private final LinkedDatasetService linkedDatasetService;
	private final NewDatasetRequestService newDatasetRequestService;
	private final UtilPageable utilPageable;
	private final TaskService<Project> projectTaskService;

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ", "
			+ PROJECT_MANAGER + ", " + USER + ")")
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
	public ResponseEntity<PagedProjectList> searchProjects(List<UUID> datasetUuids, List<UUID> linkedDatasetUuids,
			List<UUID> ownerUuids, List<ProjectStatus> status, Integer offset, Integer limit, String order)
			throws Exception {
		val searchCriteria = new ProjectSearchCriteria().datasetUuids(datasetUuids)
				.linkedDatasetUuids(linkedDatasetUuids).ownerUuids(ownerUuids).status(status);
		val pageable = utilPageable.getPageable(offset, limit, order);
		val page = projectService.searchProjects(searchCriteria, pageable);
		return ResponseEntity.ok(new PagedProjectList().total(page.getTotalElements()).elements(page.getContent()));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ", "
			+ PROJECT_MANAGER + ", " + USER + ", " + MODERATOR + ")")
	public ResponseEntity<Void> updateProject(UUID uuid, Project project) throws AppServiceException {
		project.setUuid(uuid);
		projectService.updateProject(project);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ", "
			+ PROJECT_MANAGER + ", " + USER + ", " + MODERATOR + ")")
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
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ", "
			+ PROJECT_MANAGER + ", " + USER + ")")
	public ResponseEntity<Void> uploadProjectMediaByType(UUID projectUuid, KindOfData kindOfData, MultipartFile file)
			throws Exception {
		DocumentContent documentContent = controllerHelper.documentContentFrom(file);
		projectService.uploadMedia(projectUuid, kindOfData, documentContent);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ", "
			+ PROJECT_MANAGER + ", " + USER + ")")
	public ResponseEntity<Void> deleteProjectMediaByType(UUID projectUuid, KindOfData kindOfData) throws Exception {
		projectService.deleteMedia(projectUuid, kindOfData);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<List<LinkedDataset>> getLinkedDatasets(UUID projectUuid, List<LinkedDatasetStatus> status)
			throws AppServiceNotFoundException {
		return ResponseEntity.ok(linkedDatasetService.getLinkedDatasets(projectUuid, status));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ", "
			+ PROJECT_MANAGER + ", " + USER + ")")
	public ResponseEntity<LinkedDataset> linkProjectToDataset(UUID projectUuid, LinkedDataset linkedDataset)
			throws AppServiceNotFoundException, DataverseAPIException, AppServiceException, APIManagerException {
		return ResponseEntity.ok(linkedDatasetService.linkProjectToDataset(projectUuid, linkedDataset));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ", "
			+ PROJECT_MANAGER + ", " + USER + ")")
	public ResponseEntity<LinkedDataset> updateLinkedDataset(UUID projectUuid, LinkedDataset linkedDataset)
			throws Exception {
		return ResponseEntity.ok(linkedDatasetService.updateLinkedDataset(projectUuid, linkedDataset));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ", "
			+ PROJECT_MANAGER + ", " + USER + ")")
	public ResponseEntity<Void> unlinkProjectToDataset(UUID projectUuid, UUID linkedDatasetUUID)
			throws AppServiceException, APIManagerException {
		linkedDatasetService.unlinkProjectToDataset(projectUuid, linkedDatasetUUID);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<LinkedDataset> getLinkedDataset(UUID projectUuid, UUID linkedDatasetUUID)
			throws AppServiceNotFoundException {
		return ResponseEntity.ok(linkedDatasetService.getLinkedDataset(projectUuid, linkedDatasetUUID));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ", "
			+ PROJECT_MANAGER + ", " + USER + ")")
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
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ", "
			+ PROJECT_MANAGER + ", " + USER + ")")
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
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ", "
			+ PROJECT_MANAGER + ", " + USER + ")")
	public ResponseEntity<Void> deleteNewDatasetRequest(UUID projectUuid, UUID requestUuid) throws Exception {
		projectService.deleteNewDatasetRequest(projectUuid, requestUuid);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	public ResponseEntity<Task> claimProjectTask(String taskId) throws Exception {
		return ResponseEntity.ok(projectTaskService.claimTask(taskId));
	}

	@Override
	public ResponseEntity<Task> createProjectDraft(Project project) throws Exception {
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
		projectTaskService.unclaimTask(taskId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Override
	public ResponseEntity<Task> updateProjectTask(Task task) throws Exception {
		return ResponseEntity.ok(projectTaskService.updateTask(task));
	}

	@Override
	public ResponseEntity<Indicators> computeIndicators(UUID projectUuid, UUID excludedProducerUuid) throws Exception {
		ComputeIndicatorsSearchCriteria searchCriteria = new ComputeIndicatorsSearchCriteria();
		searchCriteria.setProjectUuid(projectUuid);
		searchCriteria.setExcludedProducerUuid(excludedProducerUuid);
		Indicators indicators = projectService.computeIndicators(searchCriteria);
		return ResponseEntity.ok(indicators);
	}

	@Override
	public ResponseEntity<Integer> getNumberOfRequests(UUID projectUuid) throws Exception {
		return ResponseEntity.ok(projectService.getNumberOfRequests(projectUuid));
	}

	@Override
	public ResponseEntity<PagedProjectList> getMyProjects(Integer offset, Integer limit, String order)
			throws Exception {

		ProjectSearchCriteria searchCriteria = new ProjectSearchCriteria();
		searchCriteria.setOffset(offset);
		searchCriteria.setLimit(limit);

		Pageable pageable = utilPageable.getPageable(offset, limit, order);
		val page = projectService.getMyProjects(searchCriteria, pageable);
		return ResponseEntity.ok(new PagedProjectList().total(page.getTotalElements()).elements(page.getContent()));
	}

	@Override
	public ResponseEntity<Boolean> isAuthenticatedUserProjectOwner(UUID projectUuid) throws Exception {
		return ResponseEntity.ok(projectService.isAuthenticatedUserProjectOwner(projectUuid));
	}

	/**
	 * Retourne le formulaire de consultation des informations de la décision concernant la demande d'accès au JDD
	 * 
	 * @param projectUuid       l'uuid du projet
	 * @param linkedDatasetUUID le lien du JDD
	 * @return le formulaire avec les informations à afficher
	 * @throws AppServiceException     si les informations ne sont pas trouvées ou n'existent pas
	 * @throws FormDefinitionException si le formulaire défini n'est pas valide
	 */
	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ", "
			+ PROJECT_MANAGER + ", " + USER + "," + PROVIDER + ")")
	public ResponseEntity<Form> getDecisionInformationsForLinkedDataset(UUID projectUuid, UUID linkedDatasetUUID)
			throws Exception {
		Form form = linkedDatasetService.getDecisionInformations(projectUuid, linkedDatasetUUID);
		if (form != null) {
			return ResponseEntity.ok(form);
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	/**
	 * Retourne le formulaire de consultation des informations de la décision concernant la demande de nouveau JDD
	 * 
	 * @param projectUuid           l'uuid du projet
	 * @param newDatasetRequestUUID le lien de la demande
	 * @return le formulaire avec les informations à afficher
	 * @throws AppServiceException     si les informations ne sont pas trouvées ou n'existent pas
	 * @throws FormDefinitionException si le formulaire défini n'est pas valide
	 */
	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ", "
			+ PROJECT_MANAGER + ", " + USER + ", " + MODERATOR + ")")
	public ResponseEntity<Form> getDecisionInformationsForNewRequest(UUID projectUuid, UUID newDatasetRequestUUID)
			throws Exception {
		Form form = newDatasetRequestService.getDecisionInformations(projectUuid, newDatasetRequestUUID);
		if (form != null) {
			return ResponseEntity.ok(form);
		} else {
			return ResponseEntity.noContent().build();
		}
	}

	@Override
	public ResponseEntity<List<ProjectByOwner>> getNumberOfProjectsPerOwners(ProjectSearchCriteria criteria)
			throws Exception {
		return ResponseEntity.ok(projectService.getNumberOfProjectsPerOwners(criteria));
	}
}
