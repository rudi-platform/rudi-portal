package org.rudi.facet.doks.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.UUID;

import org.rudi.common.core.DocumentContent;
import org.rudi.common.facade.helper.ControllerHelper;
import org.rudi.common.service.exception.AppServiceForbiddenException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.doks.core.bean.DocumentMetadata;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.doks.exceptions.DocumentNotFoundException;
import org.rudi.facet.doks.helper.DocumentContentHelper;
import org.rudi.facet.doks.helper.DocumentMetadataHelper;
import org.rudi.facet.doks.policy.AuthorizationPolicy;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public abstract class AbstractAttachmentsController {
	private final ACLHelper aclHelper;
	private final ControllerHelper controllerHelper;
	private final DocumentContentHelper documentContentHelper;
	private final DocumentMetadataHelper documentMetadataHelper;
	private final AuthorizationPolicy attachmentsAuthorizationPolicy;

	public ResponseEntity<Void> delete(UUID uuid) throws AppServiceNotFoundException, AppServiceForbiddenException, AppServiceUnauthorizedException {
		documentContentHelper.deleteAttachment(uuid, attachmentsAuthorizationPolicy);
		return ResponseEntity.noContent().build();
	}

	public ResponseEntity<Resource> download(UUID uuid) throws AppServiceNotFoundException, AppServiceForbiddenException, SQLException, GeneralSecurityException, AppServiceUnauthorizedException, IOException {
		final var documentContent = documentContentHelper.getDocumentContent(uuid, attachmentsAuthorizationPolicy);
		return controllerHelper.downloadableResponseEntity(documentContent);
	}

	public ResponseEntity<DocumentMetadata> getMetadata(UUID uuid) throws DocumentNotFoundException {
		return ResponseEntity.ok(documentMetadataHelper.getDocumentMetadata(uuid));
	}


	public ResponseEntity<UUID> upload(MultipartFile file) throws Exception {
		final UUID authenticatedUserUuid = aclHelper.getAuthenticatedUserUuid();

		DocumentContent documentContent = controllerHelper.documentContentFrom(file);
		UUID documentContentUuid = documentContentHelper.createDocumentContent(documentContent, false, authenticatedUserUuid);

		return controllerHelper.uploadResponseEntity(documentContentUuid);
	}
}
