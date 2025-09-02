package org.rudi.microservice.projekt.facade.controller;

import java.util.List;
import java.util.UUID;

import org.rudi.microservice.projekt.core.bean.ProjektArchiveMode;
import org.rudi.microservice.projekt.core.bean.criteria.ProjectSearchCriteria;
import org.rudi.microservice.projekt.facade.controller.api.ProjectOwnersApi;
import org.rudi.microservice.projekt.service.project.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_STRUKTURE;

@RestController
@RequiredArgsConstructor
public class ProjectOwnersController implements ProjectOwnersApi {

	private final ProjectService projectService;

	@Override
	@PreAuthorize("hasAnyRole(" + MODULE_STRUKTURE + ")")
	public ResponseEntity<Void> archiveOwnerProjects(UUID organizationUuid, ProjektArchiveMode action) throws Exception {
		projectService.archiveOwnerProjects(ProjectSearchCriteria.builder().ownerUuids(List.of(organizationUuid)).build(), action);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PreAuthorize("hasAnyRole(" + MODULE_STRUKTURE + ")")
	public ResponseEntity<Boolean> hasProjectOwnerRunningTask(UUID organizationUuid) throws Exception {
		return ResponseEntity.ok(projectService.hasProjectOwnerRunningTask(organizationUuid));
	}

}
