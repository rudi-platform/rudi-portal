package org.rudi.microservice.strukture.facade.controller;

import java.util.UUID;

import org.rudi.common.facade.helper.ControllerHelper;
import org.rudi.doks.core.bean.DocumentMetadata;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.service.TaskService;
import org.rudi.facet.doks.controller.AbstractAttachmentsController;
import org.rudi.facet.doks.helper.DocumentContentHelper;
import org.rudi.facet.doks.helper.DocumentMetadataHelper;
import org.rudi.facet.doks.policy.AuthorizationPolicy;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.facade.controller.api.AttachmentsApi;
import org.rudi.microservice.strukture.service.helper.attachments.AttachmentsHelper;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AttachmentsController extends AbstractAttachmentsController implements AttachmentsApi {



	private final TaskService<Organization> organizationTaskService;
	private final AttachmentsHelper attachmentsHelper;

	public AttachmentsController(ACLHelper aclHelper, ControllerHelper controllerHelper, DocumentContentHelper documentContentHelper, DocumentMetadataHelper documentMetadataHelper, AuthorizationPolicy attachmentsAuthorizationPolicy, TaskService<Organization> organizationTaskService, AttachmentsHelper attachmentsHelper) {
		super(aclHelper, controllerHelper, documentContentHelper, documentMetadataHelper, attachmentsAuthorizationPolicy);
		this.organizationTaskService = organizationTaskService;
		this.attachmentsHelper = attachmentsHelper;
	}

	@Override
	public ResponseEntity<UUID> uploadAttachment(MultipartFile file) throws Exception {
		// Check que le mediatype est bien autorisé pour ce champ
		attachmentsHelper.checkMediaType(file, organizationTaskService.lookupDraftForm(null));

		return super.upload(file);
	}

	@Override
	public ResponseEntity<Void> deleteAttachment(UUID uuid) throws Exception {
		return super.delete(uuid);
	}

	@Override
	public ResponseEntity<Resource> downloadAttachment(UUID uuid) throws Exception {
		return super.download(uuid);
	}

	@Override
	public ResponseEntity<DocumentMetadata> getAttachmentMetadata(UUID uuid) throws Exception {
		return super.getMetadata(uuid);
	}
}
