package org.rudi.microservice.projekt.facade.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.rudi.bpmn.core.bean.Form;
import org.rudi.bpmn.core.bean.ProcessHistoricInformation;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.common.core.DocumentContent;
import org.rudi.common.facade.helper.ControllerHelper;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.facet.acl.bean.ProjectKey;
import org.rudi.facet.acl.bean.ProjectKeyPageResult;
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
import org.rudi.microservice.projekt.core.bean.ProjectFormType;
import org.rudi.microservice.projekt.core.bean.ProjectKeyCredential;
import org.rudi.microservice.projekt.core.bean.ProjectKeySearchCriteria;
import org.rudi.microservice.projekt.core.bean.ProjectStatus;
import org.rudi.microservice.projekt.core.bean.TargetAudience;
import org.rudi.microservice.projekt.core.bean.criteria.ProjectSearchCriteria;
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
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_KALIM;
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
	public ResponseEntity<Project> getProject(UUID uuid) throws AppServiceException {
		return ResponseEntity.ok(projectService.getProject(uuid));
	}

	/**
	 * GET /projects : Recherche de projets Recherche de projets
	 *
	 * @param datasetUuids       UUIDs des jeux de données liés aux projets recherchés (optional)
	 * @param linkedDatasetUuids UUIDs des LinkedDatasetEntity (des demandes d&#39;accès) liées aux projets recherchés (optional)
	 * @param ownerUuids         UUIDs des utilisateurs ou des organisations ayant déclaré la réutilisation ou soumis le projet (optional)
	 * @param projectUuids       UUIDs des projets (optional)
	 * @param status             (optional)
	 * @param themes             (optional)
	 * @param keywords           (optional)
	 * @param targetAudiennces   (optional)
	 * @param offset             Index de début (positionne le curseur pour parcourir les résultats de la recherche) (optional)
	 * @param limit              Le nombre de résultats à retourner par page (optional)
	 * @param order              (optional)
	 * @return OK (status code 200) or Service Unavailable (status code 500)
	 */
	@Override
	public ResponseEntity<PagedProjectList> searchProjects(@Valid List<UUID> datasetUuids,
			@Valid List<UUID> linkedDatasetUuids, @Valid List<UUID> ownerUuids, @Valid List<UUID> projectUuids,
			@Valid List<ProjectStatus> status, @Valid List<String> themes, @Valid List<String> keywords,
			@Valid List<@Valid TargetAudience> targetAudiennces, @Valid Integer offset, @Valid Integer limit,
			@Valid String order) throws Exception {
		val searchCriteria = ProjectSearchCriteria.builder().datasetUuids(datasetUuids)
				.linkedDatasetUuids(linkedDatasetUuids).ownerUuids(ownerUuids).projectUuids(projectUuids).projectStatus(status)
				.themes(themes).keywords(keywords).targetAudiences(targetAudiennces).build();

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
			throws DataverseAPIException, AppServiceException {
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
			+ PROJECT_MANAGER + ", " + USER + ", " + MODULE_KALIM + ", " + MODULE_PROJEKT_ADMINISTRATOR + ")")
	public ResponseEntity<Void> unlinkProjectToDataset(UUID projectUuid, UUID linkedDatasetUuid, Boolean force) throws Exception {
		linkedDatasetService.unlinkProjectToDataset(projectUuid, linkedDatasetUuid, force);
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
			throws AppServiceException {
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
	public ResponseEntity<Form> lookupProjectDraftForm(ProjectFormType formType) throws Exception {
		return ResponseEntity.ok(projectTaskService.lookupDraftForm(formType != null ? formType.name() : null));
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

		ProjectSearchCriteria searchCriteria = ProjectSearchCriteria.builder().build();

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

	/**
	 * GET /projects/count-per-owner : Retourne le nombre de projects par owner Recherche de statut de réutilisation
	 *
	 * @param datasetUuids       UUIDs des jeux de données liés aux projets recherchés (optional)
	 * @param linkedDatasetUuids UUIDs des LinkedDatasetEntity (des demandes d&#39;accès) liées aux projets recherchés (optional)
	 * @param ownerUuids         UUIDs des utilisateurs ou des organisations ayant déclaré la réutilisation ou soumis le projet (optional)
	 * @param projectUuids       UUIDs des projets (optional)
	 * @param status             (optional)
	 * @param themes             (optional)
	 * @param keywords           (optional)
	 * @param targetAudiennces   (optional)
	 * @param offset             Index de début (positionne le curseur pour parcourir les résultats de la recherche) (optional)
	 * @param limit              Le nombre de résultats à retourner par page (optional)
	 * @param order              (optional)
	 * @return OK (status code 200) or Internal server error (status code 500)
	 */
	@Override
	public ResponseEntity<List<ProjectByOwner>> getNumberOfProjectsPerOwners(@Valid List<UUID> datasetUuids,
			@Valid List<UUID> linkedDatasetUuids, @Valid List<UUID> ownerUuids, @Valid List<UUID> projectUuids,
			@Valid List<ProjectStatus> status, @Valid List<String> themes, @Valid List<String> keywords,
			@Valid List<@Valid TargetAudience> targetAudiennces, @Valid Integer offset, @Valid Integer limit,
			@Valid String order) throws Exception {
		val criteria = ProjectSearchCriteria.builder().datasetUuids(datasetUuids).linkedDatasetUuids(linkedDatasetUuids)
				.ownerUuids(ownerUuids).projectUuids(projectUuids).projectStatus(status).themes(themes).keywords(keywords)
				.targetAudiences(targetAudiennces).build();

		return ResponseEntity.ok(projectService.getNumberOfProjectsPerOwners(criteria));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + USER + ")")
	public ResponseEntity<ProjectKey> createProjectKey(UUID projectUuid, ProjectKeyCredential projectKeyCredential)
			throws Exception {
		return ResponseEntity.ok(projectService.createProjectKey(projectUuid, projectKeyCredential));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + USER + ")")
	public ResponseEntity<Void> deleteProjectKey(UUID projectUuid, UUID projectKeyUuid) throws Exception {
		projectService.deleteProjectKey(projectUuid, projectKeyUuid);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + USER + ")")
	public ResponseEntity<ProjectKeyPageResult> searchProjectKeys(UUID projectUuid) throws Exception {
		ProjectKeySearchCriteria searchCriteria = new ProjectKeySearchCriteria().projectUuid(projectUuid);

		List<ProjectKey> projectKeys = projectService.searchProjectKeys(searchCriteria);
		ProjectKeyPageResult result = new ProjectKeyPageResult();
		result.setTotal((long) projectKeys.size());
		result.setElements(projectKeys);
		return ResponseEntity.ok(result);
	}

	@Override
	public ResponseEntity<ProcessHistoricInformation> getProjectTaskHistoryByTaskId(String taskId, Boolean asAdmin)
			throws Exception {
		return ResponseEntity.ok(projectTaskService.getTaskHistoryByTaskId(taskId, asAdmin));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_KALIM + ")")
	public ResponseEntity<Void> deleteProjectTask(String taskId) throws Exception {
		projectTaskService.stopTaskByTaskId(taskId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

}
