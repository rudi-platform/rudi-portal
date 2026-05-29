package org.rudi.microservice.kalim.service.integration.impl.validator;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.facet.kaccess.bean.Media;
import org.rudi.facet.kaccess.bean.MediaFile;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.constant.RudiMetadataField;
import org.rudi.microservice.kalim.service.IntegrationError;
import org.rudi.microservice.kalim.service.KalimSpringBootTest;
import org.rudi.microservice.kalim.service.datafactory.MimeTypeDatafactory;
import org.rudi.microservice.kalim.service.integration.impl.validator.metadata.MimeTypeValidator;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.rudi.microservice.kalim.storage.entity.mimetype.MimeTypeEntity;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@KalimSpringBootTest
class MimeTypeValidator2UT {

	@Autowired
	private MimeTypeValidator mimeTypeValidator;

	@Autowired
	private MimeTypeDatafactory mimeTypeDatafactory;

	@Test
	@DisplayName("Validation metadata: tous les mime types sont actifs => aucune erreur")
	void validateMetadata_shouldPassWhenAllMimeTypesAreActive() {
		final MimeTypeEntity activeMimeType1 = mimeTypeDatafactory.getOrCreateValidatorItActivePassMimeType1();
		final MimeTypeEntity activeMimeType2 = mimeTypeDatafactory.getOrCreateValidatorItActivePassMimeType2();

		final MediaFile activeMediaFile1 = new MediaFile();
		activeMediaFile1.setMediaType(Media.MediaTypeEnum.FILE);
		activeMediaFile1.setFileType(activeMimeType1.getCode());

		final MediaFile activeMediaFile2 = new MediaFile();
		activeMediaFile2.setMediaType(Media.MediaTypeEnum.FILE);
		activeMediaFile2.setFileType(activeMimeType2.getCode());

		final Metadata metadata = new Metadata();
		metadata.setAvailableFormats(List.of(activeMediaFile1, activeMediaFile2));

		final Set<IntegrationRequestErrorEntity> errors = mimeTypeValidator.validateMetadata(metadata);

		assertThat(errors).isEmpty();
	}

	@Test
	@DisplayName("Validation metadata: mime type actif + inactif en BDD => erreur sur l'inactif")
	void validateMetadata_shouldFailWhenOneMimeTypeIsInactive() {
		final MimeTypeEntity activeMimeType = mimeTypeDatafactory.getOrCreateValidatorItActiveMixedMimeType();
		final MimeTypeEntity inactiveMimeType = mimeTypeDatafactory.getOrCreateValidatorItInactiveMixedMimeType();

		final MediaFile activeMediaFile = new MediaFile();
		activeMediaFile.setMediaType(Media.MediaTypeEnum.FILE);
		activeMediaFile.setFileType(activeMimeType.getCode());

		final MediaFile inactiveMediaFile = new MediaFile();
		inactiveMediaFile.setMediaType(Media.MediaTypeEnum.FILE);
		inactiveMediaFile.setFileType(inactiveMimeType.getCode());

		final Metadata metadata = new Metadata();
		metadata.setAvailableFormats(List.of(activeMediaFile, inactiveMediaFile));

		final Set<IntegrationRequestErrorEntity> errors = mimeTypeValidator.validateMetadata(metadata);

		assertThat(errors).anyMatch(error -> error.getCode().equals(IntegrationError.ERR_307.getCode())
				&& error.getFieldName().equals(RudiMetadataField.FILE_TYPE.getLocalName())
				&& error.getMessage().contains(inactiveMimeType.getCode()));
	}

	@Test
	@DisplayName("Validation metadata: tous les mime types sont inactifs => erreurs sur tous les formats")
	void validateMetadata_shouldFailWhenAllMimeTypesAreInactive() {
		final MimeTypeEntity inactiveMimeType1 = mimeTypeDatafactory.getOrCreateValidatorItInactiveFailMimeType1();
		final MimeTypeEntity inactiveMimeType2 = mimeTypeDatafactory.getOrCreateValidatorItInactiveFailMimeType2();

		final MediaFile inactiveMediaFile1 = new MediaFile();
		inactiveMediaFile1.setMediaType(Media.MediaTypeEnum.FILE);
		inactiveMediaFile1.setFileType(inactiveMimeType1.getCode());

		final MediaFile inactiveMediaFile2 = new MediaFile();
		inactiveMediaFile2.setMediaType(Media.MediaTypeEnum.FILE);
		inactiveMediaFile2.setFileType(inactiveMimeType2.getCode());

		final Metadata metadata = new Metadata();
		metadata.setAvailableFormats(List.of(inactiveMediaFile1, inactiveMediaFile2));

		final Set<IntegrationRequestErrorEntity> errors = mimeTypeValidator.validateMetadata(metadata);

		assertThat(errors).hasSize(2);
		assertThat(errors).allMatch(error -> error.getCode().equals(IntegrationError.ERR_307.getCode())
				&& error.getFieldName().equals(RudiMetadataField.FILE_TYPE.getLocalName()));
		assertThat(errors).anyMatch(error -> error.getMessage().contains(inactiveMimeType1.getCode()));
		assertThat(errors).anyMatch(error -> error.getMessage().contains(inactiveMimeType2.getCode()));
	}
}

