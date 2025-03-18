package org.rudi.microservice.strukture.facade.controller;

import java.util.UUID;

import org.rudi.common.core.DocumentContent;
import org.rudi.common.facade.helper.ControllerHelper;
import org.rudi.doks.core.bean.DocumentMetadata;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.service.TaskService;
import org.rudi.facet.doks.helper.DocumentContentHelper;
import org.rudi.facet.doks.helper.DocumentMetadataHelper;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.facade.controller.api.AttachmentsApi;
import org.rudi.microservice.strukture.service.helper.attachments.AttachmentsAuthorizationPolicy;
import org.rudi.microservice.strukture.service.helper.attachments.AttachmentsHelper;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AttachmentsController implements AttachmentsApi {


	private final ControllerHelper controllerHelper;
	private final DocumentContentHelper documentContentHelper;
	private final ACLHelper aclHelper;
	private final DocumentMetadataHelper documentMetadataHelper;
	private final AttachmentsAuthorizationPolicy attachmentsAuthorizationPolicy;
	private final TaskService<Organization> organizationTaskService;
	private final AttachmentsHelper attachmentsHelper;

	/**
	 * DELETE /attachments/{uuid}/content : Supprime une pièce-jointe.
	 * Supprime une pièce-jointe.
	 *
	 * @param uuid UUID de la pièce-jointe. (required)
	 * @return La pièce-jointe a été supprimée avec succès. (status code 204)
	 * or Bad Request Error (status code 400)
	 * or La ressource demandée est introuvable. (status code 404)
	 * or Internal server error (status code 500)
	 */
	@Override
	public ResponseEntity<Void> deleteAttachment(UUID uuid) throws Exception {
		documentContentHelper.deleteAttachment(uuid, attachmentsAuthorizationPolicy);
		return ResponseEntity.noContent().build();
	}

	/**
	 * GET /attachments/{uuid}/content : Télécharge le contenu d&#39;une pièce-jointe.
	 * Télécharge le contenu d&#39;une pièce-jointe.
	 *
	 * @param uuid UUID de la pièce-jointe. (required)
	 * @return Le contenu de la pièce-jointe. (status code 200)
	 * or Bad Request Error (status code 400)
	 * or La ressource demandée est introuvable. (status code 404)
	 * or Internal server error (status code 500)
	 */
	@Override
	public ResponseEntity<Resource> downloadAttachment(UUID uuid) throws Exception {
		final var documentContent = documentContentHelper.getDocumentContent(uuid, attachmentsAuthorizationPolicy);
		return controllerHelper.downloadableResponseEntity(documentContent);
	}

	/**
	 * GET /attachments/{uuid} : Recupère les informations principales d&#39;un fichier.
	 * Recupère les informations principales d&#39;un fichier. Il s&#39;agit du nom et de la taille notamment
	 *
	 * @param uuid UUID de la pièce-jointe. (required)
	 * @return Les informations basiques sur la pièce-jointe. (status code 200)
	 * or Bad Request Error (status code 400)
	 * or La ressource demandée est introuvable. (status code 404)
	 * or Internal server error (status code 500)
	 */
	@Override
	public ResponseEntity<DocumentMetadata> getAttachmentMetadata(UUID uuid) throws Exception {
		return ResponseEntity.ok(documentMetadataHelper.getDocumentMetadata(uuid));
	}

	/**
	 * POST /attachments : Ajout d&#39;une pièce-jointe.
	 * Upload le contenu d&#39;une pièce-jointe.
	 *
	 * @param file (optional)
	 * @return L&#39;UUID de la pièce-jointe créée. (status code 201)
	 * or Bad Request Error (status code 400)
	 * or Internal server error (status code 500)
	 */
	@Override
	public ResponseEntity<UUID> uploadAttachment(MultipartFile file) throws Exception {
		final UUID authenticatedUserUuid = aclHelper.getAuthenticatedUserUuid();

		// Check que le type fourni est bien un des types autorisés
		attachmentsHelper.checkMediaType(file, organizationTaskService.lookupDraftForm(null));

		DocumentContent documentContent = controllerHelper.documentContentFrom(file);
		UUID uuid = documentContentHelper.createDocumentContent(documentContent, false, authenticatedUserUuid);
		return controllerHelper.uploadResponseEntity(uuid);
	}




}
