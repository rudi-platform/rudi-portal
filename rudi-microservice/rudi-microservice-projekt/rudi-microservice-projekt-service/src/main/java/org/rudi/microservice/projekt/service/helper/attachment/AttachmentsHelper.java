package org.rudi.microservice.projekt.service.helper.attachment;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Field;
import org.rudi.bpmn.core.bean.Form;
import org.rudi.bpmn.core.bean.Section;
import org.rudi.common.core.DocumentContent;
import org.rudi.common.core.util.ContentTypeUtils;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.AppServiceForbiddenException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.doks.helper.DocumentContentHelper;
import org.rudi.facet.kmedia.bean.KindOfData;
import org.rudi.facet.kmedia.bean.MediaOrigin;
import org.rudi.facet.kmedia.service.MediaService;
import org.rudi.microservice.projekt.core.bean.AllowedAttachementType;
import org.rudi.microservice.projekt.core.bean.ProjectPictureInformations;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.PROJECT_PICTURE_FIELD_NAME;
import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.PROJECT_PICTURE_SECTION_NAME;

@Component
@RequiredArgsConstructor
public class AttachmentsHelper {
	private final ObjectMapper objectMapper;
	private final MediaService mediaService;
	private final DocumentContentHelper documentContentHelper;
	private final AttachmentsAuthorizationPolicy attachmentsAuthorizationPolicy;


	/**
	 * Check the media type of the file
	 * @param file the file to check
	 * @param draftForm the form to check the media type against
	 * @throws FormDefinitionException if the form definition is invalid
	 * @throws InvalidDataException if the media type is invalid
	 */
	public void checkMediaType(MultipartFile file, Form draftForm) throws FormDefinitionException, InvalidDataException {
		ProjectPictureInformations informations = extractExtendedType(draftForm);
		List<String> allowedContentTypes = informations.getTypes().stream().map(AllowedAttachementType::getMediaType).toList();
		ContentTypeUtils.checkMediaType(file.getContentType(), allowedContentTypes);
	}

	/**
	 * Extract the extended type from the form definition
	 * @param form the form to extract the extended type from
	 * @return the extended type
	 * @throws FormDefinitionException if the form definition is invalid
	 * @throws InvalidDataException if the extended type is invalid
	 */
	private ProjectPictureInformations extractExtendedType(Form form) throws FormDefinitionException, InvalidDataException {
		Optional<Section> imageSection = form.getSections().stream().filter(s -> s.getName().equals(PROJECT_PICTURE_SECTION_NAME)).findFirst();
		if (imageSection.isEmpty()) {
			throw new FormDefinitionException("Draft form must contain an image section");
		}

		Optional<Field> imageField = imageSection.get().getFields().stream().filter(f -> f.getDefinition() != null && f.getDefinition().getName().equals(PROJECT_PICTURE_FIELD_NAME)).findFirst();
		if (imageField.isEmpty()) {
			throw new FormDefinitionException("Draft form must contain an image field");
		}

		String extendedType = imageField.get().getDefinition().getExtendedType();
		return hydrateExtentedType(extendedType);
	}

	/**
	 * Hydrate the extended type from the form definition
	 * @param datas the extended type as string
	 * @return the hydrated extended type
	 * @throws InvalidDataException if the extended type is invalid
	 */
	private ProjectPictureInformations hydrateExtentedType(String datas) throws InvalidDataException {
		if (StringUtils.isNotEmpty(datas)) {
			ObjectReader objectReader = objectMapper.readerFor(ProjectPictureInformations.class);
			try {
				return objectReader.readValue(datas);
			} catch (IOException e) {
				throw new InvalidDataException("Failed to hydrate:" + datas, e);
			}
		}
		return new ProjectPictureInformations();
	}

	/**
	 * Check if the authenticated user can delete the document
	 *
	 * @param documentUuid the uuid of the document to check
	 * @throws AppServiceNotFoundException     if the document is not found
	 * @throws AppServiceForbiddenException    if the user is not allowed to delete the document
	 * @throws AppServiceUnauthorizedException if the user is not authenticated
	 */
	public void checkIfAuthenticatedUserCanDeleteDocument(UUID documentUuid) throws AppServiceNotFoundException, AppServiceForbiddenException, AppServiceUnauthorizedException {
		documentContentHelper.checkIfAuthenticatedUserCanDeleteDocument(documentUuid, attachmentsAuthorizationPolicy);
	}

	public void saveMediaInMediaService(UUID mediaUuid, UUID projectUuid) throws AppServiceException {
		DocumentContent media = null;
		try {
			media = documentContentHelper.getDocumentContent(mediaUuid, attachmentsAuthorizationPolicy);
		} catch (GeneralSecurityException | IOException | AppServiceNotFoundException | SQLException |
		         AppServiceForbiddenException | AppServiceUnauthorizedException e) {
			throw new AppServiceException(String.format("Erreur lors de la récupération du media %s du projet d'id %s", mediaUuid, projectUuid), e);
		}

		if (media == null) {
			throw new AppServiceException("Le media de media n'existe pas");
		}

		saveMedia(media, projectUuid, KindOfData.LOGO);

		//Si le media est bien sauvegardé, on le supprime de doks
		documentContentHelper.deleteAttachment(mediaUuid, attachmentsAuthorizationPolicy);
	}

	public void saveMedia(DocumentContent documentContent, UUID projectUuid, KindOfData kindOfData) throws AppServiceException {
		try {
			File tempFile = File.createTempFile(UUID.randomUUID().toString(), "." + FilenameUtils.getExtension(documentContent.getFileName()));
			FileUtils.copyInputStreamToFile(documentContent.getFileStream(), tempFile);
			mediaService.setMediaFor(MediaOrigin.PROJECT, projectUuid, kindOfData, tempFile);
		} catch (IOException | DataverseAPIException e) {
			throw new AppServiceException(String.format("Erreur lors de l'upload du %s du projet d'id %s", kindOfData.getValue(), projectUuid), e);
		}
	}
}
