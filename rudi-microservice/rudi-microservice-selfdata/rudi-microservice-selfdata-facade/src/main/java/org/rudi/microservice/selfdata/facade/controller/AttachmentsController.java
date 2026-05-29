package org.rudi.microservice.selfdata.facade.controller;

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
import org.rudi.facet.doks.controller.AbstractAttachmentsController;
import org.rudi.facet.doks.exceptions.DocumentNotFoundException;
import org.rudi.facet.doks.helper.DocumentContentHelper;
import org.rudi.facet.doks.helper.DocumentMetadataHelper;
import org.rudi.microservice.selfdata.facade.controller.api.AttachmentsApi;
import org.rudi.microservice.selfdata.service.helper.selfdatainformationrequest.SelfdataInformationRequestHelper;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import static org.rudi.common.core.security.QuotedRoleCodes.ADMINISTRATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.MODERATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.USER;

@Controller
public class AttachmentsController extends AbstractAttachmentsController implements AttachmentsApi {
	private final SelfdataInformationRequestHelper selfdataInformationRequestHelper;

	public AttachmentsController(ACLHelper aclHelper, ControllerHelper controllerHelper, DocumentContentHelper documentContentHelper, DocumentMetadataHelper documentMetadataHelper, AttachmentAuthorizationPolicy attachmentsAuthorizationPolicy, SelfdataInformationRequestHelper selfdataInformationRequestHelper) {
		super(aclHelper, controllerHelper, documentContentHelper, documentMetadataHelper, attachmentsAuthorizationPolicy);
		this.selfdataInformationRequestHelper = selfdataInformationRequestHelper;
	}


	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODERATOR + "," + USER + ")")
	public ResponseEntity<UUID> uploadAttachment(MultipartFile file) throws Exception {
		// Check que le médiatype est bien autorisé sur ce champ
		selfdataInformationRequestHelper.checkMediaType(file.getContentType());

		return super.upload(file);
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODERATOR + "," + USER + ")")
	public ResponseEntity<Resource> downloadAttachment(UUID attachmentUuid) throws AppServiceNotFoundException, AppServiceForbiddenException, SQLException, GeneralSecurityException, AppServiceUnauthorizedException, IOException {
		return super.download(attachmentUuid);
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODERATOR + "," + USER + ")")
	public ResponseEntity<Void> deleteAttachment(UUID attachmentUuid) throws AppServiceNotFoundException, AppServiceForbiddenException, AppServiceUnauthorizedException {
		return super.delete(attachmentUuid);
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + MODERATOR + "," + USER + ")")
	public ResponseEntity<DocumentMetadata> getAttachmentMetadata(UUID uuid) throws DocumentNotFoundException {
		return super.getMetadata(uuid);
	}
}
