package org.rudi.microservice.kalim.service.integration.impl.validator;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.facet.kaccess.bean.Media;
import org.rudi.facet.kaccess.bean.MediaFile;
import org.rudi.facet.kaccess.bean.MediaService;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.constant.RudiMetadataField;
import org.rudi.microservice.kalim.service.IntegrationError;
import org.rudi.microservice.kalim.service.helper.MimeTypeHelper;
import org.rudi.microservice.kalim.service.integration.impl.validator.metadata.MimeTypeValidator;
import org.rudi.microservice.kalim.storage.dao.mimetype.MimeTypeCustomDao;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;
import org.rudi.microservice.kalim.storage.entity.mimetype.MimeTypeEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MimeTypeValidatorUT {

	@Mock
	private MimeTypeCustomDao mimeTypeCustomDao;

	@Mock
	private MimeTypeHelper mimeTypeHelper;

	@Mock
	private UtilPageable utilPageable;

	@InjectMocks
	private MimeTypeValidator mimeTypeValidator;

	// --- Tests sur validate() ---

	@Test
	@DisplayName("Test de la validation avec une liste vide - aucune erreur attendue")
	void testValidateEmptyList() {
		Set<IntegrationRequestErrorEntity> errors = mimeTypeValidator.validate(List.of());
		assertThat(errors.size()).isEqualTo(0);
	}

	@Test
	@DisplayName("Test de la validation avec une liste null - aucune erreur attendue")
	void testValidateNullList() {
		Set<IntegrationRequestErrorEntity> errors = mimeTypeValidator.validate(null);
		assertThat(errors.size()).isEqualTo(0);
	}

	@Test
	@DisplayName("Test de la validation avec tous les mime types existants en BDD")
	void testValidateAllMimeTypesExist() {
		List<String> fileTypes = List.of("application/json", "text/csv");

		MimeTypeEntity entity1 = new MimeTypeEntity();
		entity1.setUuid(UUID.randomUUID());
		entity1.setCode("application/json");

		MimeTypeEntity entity2 = new MimeTypeEntity();
		entity2.setUuid(UUID.randomUUID());
		entity2.setCode("text/csv");

		when(utilPageable.getPageable(0, 2, null)).thenReturn(Pageable.unpaged());
		when(mimeTypeCustomDao.searchMimeTypes(any(), any())).thenReturn(new PageImpl<>(List.of(entity1, entity2)));

		Set<IntegrationRequestErrorEntity> errors = mimeTypeValidator.validate(fileTypes);
		assertThat(errors.size()).isEqualTo(0);
	}

	@Test
	@DisplayName("Test de la validation avec un mime type inconnu - une erreur ERR-307 attendue")
	void testValidateOneMimeTypeNotFound() {
		String unknownMimeType = "application/unknown";
		List<String> fileTypes = List.of("application/json", unknownMimeType);

		MimeTypeEntity entity1 = new MimeTypeEntity();
		entity1.setUuid(UUID.randomUUID());
		entity1.setCode("application/json");

		when(utilPageable.getPageable(0, 2, null)).thenReturn(Pageable.unpaged());
		when(mimeTypeCustomDao.searchMimeTypes(any(), any())).thenReturn(new PageImpl<>(List.of(entity1)));

		Set<IntegrationRequestErrorEntity> errors = mimeTypeValidator.validate(fileTypes);

		assertThat(errors.size()).isEqualTo(1);
		assertThat(errors).anyMatch(error -> error.getFieldName().equals(RudiMetadataField.FILE_TYPE.getLocalName())
				&& error.getCode().equals(IntegrationError.ERR_307.getCode())
				&& error.getMessage().equals(String.format(IntegrationError.ERR_307.getMessage(),
						unknownMimeType, RudiMetadataField.FILE_TYPE.getLocalName())));
	}

	@Test
	@DisplayName("Test de la validation avec tous les mime types inconnus - autant d'erreurs que de mime types")
	void testValidateAllMimeTypesNotFound() {
		List<String> fileTypes = List.of("application/unknown1", "application/unknown2");

		when(utilPageable.getPageable(0, 2, null)).thenReturn(Pageable.unpaged());
		when(mimeTypeCustomDao.searchMimeTypes(any(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

		Set<IntegrationRequestErrorEntity> errors = mimeTypeValidator.validate(fileTypes);
		assertThat(errors.size()).isEqualTo(2);
	}

	// --- Tests sur validateMetadata() (via getMetadataElementToValidate) ---

	@Test
	@DisplayName("Test de la validation d'une metadata null - aucune erreur attendue")
	void testValidateMetadataNull() {
		Set<IntegrationRequestErrorEntity> errors = mimeTypeValidator.validateMetadata(null);
		assertThat(errors.size()).isEqualTo(0);
	}

	@Test
	@DisplayName("Test de la validation d'une metadata sans availableFormats - aucune erreur attendue")
	void testValidateMetadataNoAvailableFormats() {
		Metadata metadata = new Metadata();
		metadata.setAvailableFormats(null);

		Set<IntegrationRequestErrorEntity> errors = mimeTypeValidator.validateMetadata(metadata);
		assertThat(errors.size()).isEqualTo(0);
	}

	@Test
	@DisplayName("Test de la validation d'une metadata avec un media SERVICE - pas de vérification mime type")
	void testValidateMetadataWithServiceMedia() {
		Metadata metadata = new Metadata();
		MediaService mediaService = new MediaService();
		mediaService.setMediaType(Media.MediaTypeEnum.SERVICE);
		metadata.setAvailableFormats(List.of(mediaService));

		Set<IntegrationRequestErrorEntity> errors = mimeTypeValidator.validateMetadata(metadata);
		assertThat(errors.size()).isEqualTo(0);
	}

	@Test
	@DisplayName("Test de la validation d'une metadata avec un media FILE dont le mime type existe en BDD")
	void testValidateMetadataWithFileMediaMimeTypeExists() {
		Metadata metadata = new Metadata();
		MediaFile mediaFile = new MediaFile();
		mediaFile.setMediaType(Media.MediaTypeEnum.FILE);
		mediaFile.setFileType("application/pdf+crypt");
		metadata.setAvailableFormats(List.of(mediaFile));

		when(mimeTypeHelper.getInitialMimeType("application/pdf+crypt")).thenReturn("application/pdf");

		MimeTypeEntity entity = new MimeTypeEntity();
		entity.setUuid(UUID.randomUUID());
		entity.setCode("application/pdf");

		when(utilPageable.getPageable(0, 1, null)).thenReturn(Pageable.unpaged());
		when(mimeTypeCustomDao.searchMimeTypes(any(), any())).thenReturn(new PageImpl<>(List.of(entity)));

		Set<IntegrationRequestErrorEntity> errors = mimeTypeValidator.validateMetadata(metadata);
		assertThat(errors.size()).isEqualTo(0);
	}

	@Test
	@DisplayName("Test de la validation d'une metadata avec un media FILE dont le mime type n'existe pas en BDD")
	void testValidateMetadataWithFileMediaMimeTypeNotFound() {
		Metadata metadata = new Metadata();
		MediaFile mediaFile = new MediaFile();
		mediaFile.setMediaType(Media.MediaTypeEnum.FILE);
		mediaFile.setFileType("application/unknown+crypt");
		metadata.setAvailableFormats(List.of(mediaFile));

		when(mimeTypeHelper.getInitialMimeType("application/unknown+crypt")).thenReturn("application/unknown");

		when(utilPageable.getPageable(0, 1, null)).thenReturn(Pageable.unpaged());
		when(mimeTypeCustomDao.searchMimeTypes(any(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

		Set<IntegrationRequestErrorEntity> errors = mimeTypeValidator.validateMetadata(metadata);
		assertThat(errors.size()).isEqualTo(1);
	}


}
