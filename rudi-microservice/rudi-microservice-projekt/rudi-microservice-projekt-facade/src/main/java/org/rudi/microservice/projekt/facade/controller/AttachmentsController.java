package org.rudi.microservice.projekt.facade.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.UUID;

import org.rudi.common.facade.helper.ControllerHelper;
import org.rudi.common.service.exception.AppServiceForbiddenException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.doks.core.bean.DocumentMetadata;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.service.TaskService;
import org.rudi.facet.doks.controller.AbstractAttachmentsController;
import org.rudi.facet.doks.exceptions.DocumentNotFoundException;
import org.rudi.facet.doks.helper.DocumentContentHelper;
import org.rudi.facet.doks.helper.DocumentMetadataHelper;
import org.rudi.facet.doks.policy.AuthorizationPolicy;
import org.rudi.microservice.projekt.core.bean.Project;
import org.rudi.microservice.projekt.facade.controller.api.AttachmentsApi;
import org.rudi.microservice.projekt.service.helper.attachment.AttachmentsHelper;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
public class AttachmentsController extends AbstractAttachmentsController implements AttachmentsApi {

	private final AttachmentsHelper attachmentsHelper;
	private final TaskService<Project> projectTaskService;

	AttachmentsController(ACLHelper aclHelper, ControllerHelper controllerHelper, DocumentContentHelper documentContentHelper, DocumentMetadataHelper documentMetadataHelper, AuthorizationPolicy attachmentsAuthorizationPolicy, AttachmentsHelper attachmentsHelper, TaskService<Project> projectTaskService) {
		super(aclHelper, controllerHelper, documentContentHelper, documentMetadataHelper, attachmentsAuthorizationPolicy);
		this.attachmentsHelper = attachmentsHelper;
		this.projectTaskService = projectTaskService;
	}

	@Override
	public ResponseEntity<UUID> uploadAttachment(MultipartFile file) throws Exception {
		// Check que le mediatype est bien autorisé pour ce champ
		attachmentsHelper.checkMediaType(file, projectTaskService.lookupDraftForm(null));

		return super.upload(file);
	}

	@Override
	public ResponseEntity<Void> deleteAttachment(UUID uuid) throws AppServiceNotFoundException, AppServiceForbiddenException, AppServiceUnauthorizedException {
		return super.delete(uuid);
	}

	@Override
	public ResponseEntity<Resource> downloadAttachment(UUID uuid) throws AppServiceNotFoundException, AppServiceForbiddenException, SQLException, GeneralSecurityException, AppServiceUnauthorizedException, IOException {
		return super.download(uuid);
	}

	@Override
	public ResponseEntity<DocumentMetadata> getAttachmentMetadata(UUID uuid) throws DocumentNotFoundException {
		return super.getMetadata(uuid);
	}
}
