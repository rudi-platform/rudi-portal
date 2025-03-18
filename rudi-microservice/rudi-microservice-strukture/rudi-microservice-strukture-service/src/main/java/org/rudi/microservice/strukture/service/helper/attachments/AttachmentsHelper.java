package org.rudi.microservice.strukture.service.helper.attachments;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
import org.rudi.microservice.strukture.core.bean.OrganizationLogoInformations;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import static org.rudi.microservice.strukture.service.workflow.StruktureWorkflowConstants.FIELD_NAME_IMAGE_ORGANIZATION;
import static org.rudi.microservice.strukture.service.workflow.StruktureWorkflowConstants.SECTION_NAME_IMAGE_ORGANIZATION;


@Component
@RequiredArgsConstructor
public class AttachmentsHelper {

	private final ObjectMapper objectMapper;
	private final MediaService mediaService;
	private final DocumentContentHelper documentContentHelper;
	private final AttachmentsAuthorizationPolicy attachmentsAuthorizationPolicy;


	public void checkMediaType(MultipartFile file, Form draftForm) throws FormDefinitionException, InvalidDataException {
		OrganizationLogoInformations informations = extractExtendedType(draftForm);
		List<String> allowedContentTypes = informations.getTypes().stream().map(t -> t.getMediaType()).toList();
		ContentTypeUtils.checkMediaType(file.getContentType(), allowedContentTypes);
	}

	/**
	 * Parse un extendedType en un objet : String maxSize; List<AllowedAttachementTypes> types;
	 *
	 * @param datas string à réhydrater en map
	 * @return objet issu de la réhydratation de la string
	 */
	private OrganizationLogoInformations hydrateExtendedType(String datas) throws InvalidDataException {
		OrganizationLogoInformations result = null;
		if (StringUtils.isNotEmpty(datas)) {
			ObjectReader objectReader = objectMapper.readerFor(OrganizationLogoInformations.class);
			try {
				return objectReader.readValue(datas);
			} catch (IOException e) {
				throw new InvalidDataException("Failed to hydrate:" + datas, e);
			}
		} else {
			result = new OrganizationLogoInformations();
		}
		return result;
	}

	/**
	 * @param form formulaire sur lequel s'appuyer pour la recherche
	 * @return l'objet contenant les informations sur l'attachement (fort, taille)
	 * @throws FormDefinitionException si le formulaire n'est pas cornforme à l'attendu
	 */
	private OrganizationLogoInformations extractExtendedType(Form form) throws FormDefinitionException, InvalidDataException {
		Optional<Section> section = form.getSections().stream().filter(s -> SECTION_NAME_IMAGE_ORGANIZATION.equals(s.getName())).findFirst();
		if (section.isPresent()) {
			Optional<Field> field = section.get().getFields().stream().filter(f -> f.getDefinition() != null && FIELD_NAME_IMAGE_ORGANIZATION.equals(f.getDefinition().getName())).findFirst();
			if (field.isPresent()) {
				String extendedType = field.get().getDefinition().getExtendedType();
				return hydrateExtendedType(extendedType);
			} else {
				throw new FormDefinitionException("Draft form must contain an image field");
			}
		} else {
			throw new FormDefinitionException("Draft form must have an image section");
		}
	}

	public void saveMediaInMediaService(UUID mediaUuid, UUID producerUuid) throws AppServiceException {
		DocumentContent media = null;
		try {
			media = documentContentHelper.getDocumentContent(mediaUuid, attachmentsAuthorizationPolicy);
		} catch (GeneralSecurityException | IOException | AppServiceNotFoundException | SQLException |
				 AppServiceForbiddenException | AppServiceUnauthorizedException e) {
			throw new AppServiceException(String.format("Erreur lors de la récupération du media %s du producteur d'id %s", mediaUuid, producerUuid), e);
		}

		if (media == null) {
			throw new AppServiceException("Le media de media n'existe pas");
		}

		saveMedia(media, producerUuid, KindOfData.LOGO);

		//Si le media est bien sauvegardé, on le supprime de doks
		documentContentHelper.deleteAttachment(mediaUuid, attachmentsAuthorizationPolicy);
	}


	public void saveMedia(DocumentContent documentContent, UUID producerUuid, KindOfData kindOfData) throws AppServiceException {
		try {
			File tempFile = File.createTempFile(UUID.randomUUID().toString(),
					"." + FilenameUtils.getExtension(documentContent.getFileName()));
			FileUtils.copyInputStreamToFile(documentContent.getFileStream(), tempFile);
			mediaService.setMediaFor(MediaOrigin.PRODUCER, producerUuid, kindOfData, tempFile);
		} catch (final DataverseAPIException |
					   IOException e) {
			throw new AppServiceException(String.format("Erreur lors de l'upload du %s du producteur d'id %s",
					kindOfData.getValue(), producerUuid), e);
		}
	}


}
