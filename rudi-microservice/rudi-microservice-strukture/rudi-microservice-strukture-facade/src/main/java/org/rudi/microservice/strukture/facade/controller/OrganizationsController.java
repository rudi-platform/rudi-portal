package org.rudi.microservice.strukture.facade.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Form;
import org.rudi.bpmn.core.bean.ProcessHistoricInformation;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.bpmn.exception.FormConvertException;
import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.service.TaskService;
import org.rudi.microservice.strukture.core.bean.NodeOrganization;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.core.bean.OrganizationFormType;
import org.rudi.microservice.strukture.core.bean.OrganizationMember;
import org.rudi.microservice.strukture.core.bean.OrganizationMemberType;
import org.rudi.microservice.strukture.core.bean.OrganizationSearchCriteria;
import org.rudi.microservice.strukture.core.bean.OrganizationStatus;
import org.rudi.microservice.strukture.core.bean.OwnerInfo;
import org.rudi.microservice.strukture.core.bean.PagedOrganizationList;
import org.rudi.microservice.strukture.core.bean.PagedOrganizationUserMembers;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationMembersSearchCriteria;
import org.rudi.microservice.strukture.facade.controller.api.OrganizationsApi;
import org.rudi.microservice.strukture.service.mapper.NodeOrganizationMapper;
import org.rudi.microservice.strukture.service.organization.OrganizationService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.val;
import static org.rudi.common.core.security.QuotedRoleCodes.ADMINISTRATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.MODERATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_KALIM;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_PROJEKT;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_STRUKTURE_ADMINISTRATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.PROVIDER;
import static org.rudi.common.core.security.QuotedRoleCodes.USER;

@RestController
@RequiredArgsConstructor
public class OrganizationsController implements OrganizationsApi {

	private final OrganizationService organizationService;
	private final UtilPageable utilPageable;
	private final TaskService<Organization> organizationTaskService;
	private final NodeOrganizationMapper nodeOrganizationMapper;

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODERATOR + ")")
	public ResponseEntity<OwnerInfo> getOrganizationOwnerInfo(UUID uuid) throws Exception {
		return ResponseEntity.ok(organizationService.getOrganizationOwnerInfo(uuid));
	}

	@Override
	public ResponseEntity<Organization> getOrganization(UUID uuid) throws AppServiceNotFoundException, AppServiceUnauthorizedException {
		return ResponseEntity.ok(organizationService.getOrganization(uuid));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_STRUKTURE_ADMINISTRATOR + ", " + USER + ")")
	public ResponseEntity<User> getOrganizationUserFromOrganizationUuid(UUID organizationUuid) throws Exception {
		return ResponseEntity.ok(organizationService.getOrganizationUserFromOrganizationUuid(organizationUuid));
	}

	@Override
	public ResponseEntity<PagedOrganizationList> searchOrganizations(UUID uuid, String name, Boolean active,
			UUID userUuid, OrganizationStatus organizationStatus, Integer offset, Integer limit, String order) {
		val searchCriteria = new OrganizationSearchCriteria().uuid(uuid).name(name).active(active).userUuid(userUuid)
				.organizationStatus(organizationStatus);
		val pageable = utilPageable.getPageable(offset, limit, order);
		val page = organizationService.searchOrganizations(searchCriteria, pageable);
		return ResponseEntity
				.ok(new PagedOrganizationList().total(page.getTotalElements()).elements(page.getContent()));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_STRUKTURE_ADMINISTRATOR + ")")
	public ResponseEntity<Void> updateOrganization(Organization organization) throws AppServiceException {
		organizationService.updateOrganization(organization);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_STRUKTURE_ADMINISTRATOR + ", " + MODULE_KALIM + ","
			+ USER + ")")
	public ResponseEntity<OrganizationMember> addOrganizationMember(UUID organizationUuid,
			OrganizationMember organizationMember) throws Exception {
		return ResponseEntity.ok(organizationService.addOrganizationMember(organizationUuid, organizationMember));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODERATOR + ", " + MODULE_STRUKTURE_ADMINISTRATOR + ", "
			+ MODULE_PROJEKT + ", " + USER + ", " + MODULE_KALIM  + ")")
	public ResponseEntity<List<OrganizationMember>> getOrganizationMembers(UUID organizationUuid)
			throws AppServiceException {
		return ResponseEntity.ok(organizationService.getOrganizationMembers(organizationUuid));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_STRUKTURE_ADMINISTRATOR + ", " + USER + ")")
	public ResponseEntity<Void> removeOrganizationMember(UUID organizationUuid, UUID userUuid) throws Exception {
		organizationService.removeOrganizationMembers(organizationUuid, userUuid);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PreAuthorize("hasAnyRole(" + USER + ", " + ADMINISTRATOR + ", " + MODULE_STRUKTURE_ADMINISTRATOR + ")")
	public ResponseEntity<PagedOrganizationUserMembers> searchOrganizationUserMembers(UUID uuid, String searchText,
			OrganizationMemberType memberType, Integer offset, Integer limit, String order) throws Exception {
		Pageable pageable = utilPageable.getPageable(offset, limit, order);
		OrganizationMembersSearchCriteria criteria = OrganizationMembersSearchCriteria.builder().organizationUuid(uuid).searchText(searchText)
				.type(memberType)
				// Rajout de la limit pour que les deux listes à mapper soient de la même taille
				.limit(limit).build();
		val page = organizationService.searchOrganizationMembers(criteria, pageable);
		return ResponseEntity
				.ok(new PagedOrganizationUserMembers().total(page.getTotalElements()).elements(page.getContent()));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + USER + ", " + MODULE_STRUKTURE_ADMINISTRATOR + ")")
	public ResponseEntity<Boolean> isAuthenticatedUserOrganizationAdministrator(UUID organizationUuid)
			throws Exception {
		return ResponseEntity.ok(organizationService.isAuthenticatedOrganizationAdministrator(organizationUuid));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + USER + ")")
	public ResponseEntity<OrganizationMember> updateOrganizationMember(UUID organizationUuid, UUID userUuid,
			OrganizationMember organizationMember) throws Exception {
		return ResponseEntity
				.ok(organizationService.updateOrganizationMember(organizationUuid, userUuid, organizationMember));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODERATOR + ", " + PROVIDER + ", " + USER + ")")
	public ResponseEntity<Task> claimOrganizationTask(String taskId) throws Exception {
		return ResponseEntity.ok(organizationTaskService.claimTask(taskId));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODERATOR + ", " + PROVIDER + ", " + USER + ")")
	public ResponseEntity<Task> createOrganizationDraft(Organization organization) throws Exception {
		return ResponseEntity.ok(organizationTaskService.createDraft(organization));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODERATOR + ", " + PROVIDER + ", " + USER + ")")
	public ResponseEntity<Void> doItOrganization(String taskId, String actionName) throws Exception {
		organizationTaskService.doIt(taskId, actionName);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODERATOR + ", " + PROVIDER + ", " + USER + ")")
	public ResponseEntity<ProcessHistoricInformation> getOrganizationTaskHistoryByTaskId(String taskId,
			@Valid Boolean asAdmin) throws Exception {
		return ResponseEntity.ok(organizationTaskService.getTaskHistoryByTaskId(taskId, asAdmin));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODERATOR + ", " + PROVIDER + ", " + USER + ")")
	public ResponseEntity<Form> lookupOrganizationDraftForm(OrganizationFormType formType) throws Exception {
		return ResponseEntity.ok(organizationTaskService.lookupDraftForm(formType != null ? formType.name() : null));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODERATOR + ", " + PROVIDER + ", " + USER + ")")
	public ResponseEntity<Task> startOrganizationTask(Task task) throws Exception {
		return ResponseEntity.ok(organizationTaskService.startTask(task));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODERATOR + ", " + PROVIDER + ", " + USER + ")")
	public ResponseEntity<Task> unclaimOrganizationTask(String taskId) throws Exception {
		organizationTaskService.unclaimTask(taskId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODERATOR + ", " + PROVIDER + ", " + USER + ")")
	public ResponseEntity<Task> updateOrganizationTask(Task task) throws Exception {
		return ResponseEntity.ok(organizationTaskService.updateTask(task));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODERATOR + ", " + USER + ")")
	public ResponseEntity<Organization> createOrganization(Organization organization)
			throws AppServiceBadRequestException {
		// Crée une organisation en BDD
		Organization createdOrganization = organizationService.createOrganization(organization);
		val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
				.buildAndExpand(createdOrganization.getUuid()).toUri();
		// Retourne l'organisation créée
		return ResponseEntity.created(location).body(createdOrganization);
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + PROVIDER + ")")
	public ResponseEntity<UUID> requestOrganizationCreation(NodeOrganization nodeOrganization)
			throws AppServiceBadRequestException, FormDefinitionException, FormConvertException, InvalidDataException {
		Organization organization = nodeOrganizationMapper.nodeDtoToDTO(nodeOrganization);
		// Le champ description est obligatoire dans Rudi :
		if (StringUtils.isEmpty(organization.getDescription())) {
			organization.setDescription(String.format("Organsiation %s", organization.getName()));
		}
		Organization createdOrganization = organizationService.createOrganization(organization);

		Task task = organizationTaskService.createDraft(createdOrganization);

		organizationTaskService.startTask(task);

		val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{uuid}")
				.buildAndExpand(createdOrganization.getUuid()).toUri();

		return ResponseEntity.created(location).body(createdOrganization.getUuid());
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + PROVIDER + ")")
	public ResponseEntity<Void> requestOrganizationDelete(UUID uuid) throws Exception {
		organizationService.deleteOrganization(uuid);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + PROVIDER + ")")
	public ResponseEntity<UUID> requestOrganizationUpdate(NodeOrganization nodeOrganization) throws Exception {
		Organization organization = nodeOrganizationMapper.nodeDtoToDTO(nodeOrganization);
		organizationService.updateOrganization(organization);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + PROVIDER + ")")
	public ResponseEntity<NodeOrganization> getNodeOrganization(UUID uuid) throws Exception {
		return ResponseEntity.ok(nodeOrganizationMapper.dtoToNodeDto(organizationService.getOrganization(uuid)));
	}
}
