package org.rudi.microservice.kalim.service.integration.impl.validator.metadata;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.facet.kaccess.bean.Media;
import org.rudi.facet.kaccess.bean.MediaFile;
import org.rudi.facet.kaccess.constant.RudiMetadataField;
import org.rudi.microservice.kalim.core.bean.MimeTypeSearchCriteria;
import org.rudi.microservice.kalim.service.IntegrationError;
import org.rudi.microservice.kalim.service.helper.MimeTypeHelper;
import org.rudi.microservice.kalim.storage.dao.mimetype.MimeTypeCustomDao;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.rudi.microservice.kalim.storage.entity.mimetype.MimeTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class MimeTypeValidator extends AbstractMetadataValidator<List<String>> {


	private final MimeTypeCustomDao mimeTypeCustomDao;
	private final MimeTypeHelper mimeTypeHelper;
	private final UtilPageable utilPageable;

	public MimeTypeValidator(MimeTypeCustomDao mimeTypeCustomDao, MimeTypeHelper mimeTypeHelper, UtilPageable utilPageable) {
		this.mimeTypeCustomDao = mimeTypeCustomDao;
		this.mimeTypeHelper = mimeTypeHelper;
		this.utilPageable = utilPageable;
	}


	@Override
	protected List<String> getMetadataElementToValidate(org.rudi.facet.kaccess.bean.Metadata metadata) {
		if (metadata == null || CollectionUtils.isEmpty(metadata.getAvailableFormats())) {
			return List.of();
		}
		return metadata.getAvailableFormats().stream()
				.filter(media -> Media.MediaTypeEnum.FILE.equals(media.getMediaType()))
				.map(media -> mimeTypeHelper.getInitialMimeType(((MediaFile) media).getFileType()))
				.toList();
	}


	@Override
	public Set<IntegrationRequestErrorEntity> validate(List<String> fileTypes) {
		Set<IntegrationRequestErrorEntity> integrationRequestErrors = new HashSet<>();

		// Si la liste est vide, on ne retourne aucune erreur.
		// Le champ est requis si un media de type file est présent
		// Mais les medias ne sont pas obligatoires, donc on ne peut pas dire que c'est une erreur si la liste est vide.
		if(CollectionUtils.isEmpty(fileTypes)) {
			return integrationRequestErrors;
		}

		// Récupération des mimetypes correspondant en BDD
		MimeTypeSearchCriteria criteria = MimeTypeSearchCriteria.builder().codes(fileTypes).active(true).build();
		// on ne peut pas avoir plus de mimeTypes que de fileTypes, donc on met la taille de fileTypes en pageSize
		Pageable pageable = utilPageable.getPageable(0, fileTypes.size(), null);

		Page<MimeTypeEntity> mimeTypes = mimeTypeCustomDao.searchMimeTypes(criteria, pageable);


		for(String fileType : fileTypes) {
			boolean fileTypeExists = mimeTypes.getContent().stream()
					.anyMatch(mimeTypeEntity -> mimeTypeEntity.getCode().equals(fileType));

			if (!fileTypeExists) {
				// Si le mimeType n'existe pas, alors on ajoute une erreur à integrationRequestErrors
				String errorMessage = String.format(IntegrationError.ERR_307.getMessage(), fileType, RudiMetadataField.FILE_TYPE.getLocalName());
				IntegrationRequestErrorEntity integrationRequestError = new IntegrationRequestErrorEntity(
						java.util.UUID.randomUUID(), IntegrationError.ERR_307.getCode(), errorMessage, RudiMetadataField.FILE_TYPE.getLocalName(), java.time.LocalDateTime.now());
				integrationRequestErrors.add(integrationRequestError);
			}
		}

		return integrationRequestErrors;
	}
}
