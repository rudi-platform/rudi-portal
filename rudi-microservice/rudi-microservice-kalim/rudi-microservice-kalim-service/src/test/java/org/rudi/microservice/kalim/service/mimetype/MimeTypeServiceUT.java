package org.rudi.microservice.kalim.service.mimetype;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.microservice.kalim.core.bean.MimeType;
import org.rudi.microservice.kalim.core.bean.MimeTypeSearchCriteria;
import org.rudi.microservice.kalim.service.KalimSpringBootTest;
import org.rudi.microservice.kalim.service.datafactory.MimeTypeDatafactory;
import org.rudi.microservice.kalim.storage.entity.mimetype.MimeTypeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@KalimSpringBootTest
class MimeTypeServiceUT {

	@Autowired
	private MimeTypeService mimeTypeService;

	@Autowired
	private MimeTypeDatafactory mimeTypeDatafactory;

	@Test
	@DisplayName("Ajout d'un nouveau mimeType valide à la white list")
	void createMimeType() {
		final String rawCode = "application/x-ut-" + UUID.randomUUID() + "+crypt";

		final MimeType mimeTypeToCreate = new MimeType();
		mimeTypeToCreate.setUuid(UUID.randomUUID());
		mimeTypeToCreate.setCode(rawCode);
		mimeTypeToCreate.setLabel("UT MIME");
		mimeTypeToCreate.setOrder(10);
		mimeTypeToCreate.setOpeningDate(LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
		mimeTypeToCreate.setClosingDate(null);

		final MimeType createdMimeType = mimeTypeService.createAllowedMimeType(mimeTypeToCreate);

		assertThat(createdMimeType).isNotNull();
		assertThat(createdMimeType.getUuid()).isNotNull();
		assertThat(createdMimeType.getCode()).isEqualTo(rawCode.replace("+crypt", ""));
		assertThat(createdMimeType.getCode()).doesNotEndWith("+crypt");
		assertThat(createdMimeType.getClosingDate()).isNull();

		final MimeTypeSearchCriteria searchCriteria = MimeTypeSearchCriteria.builder()
				.code(createdMimeType.getCode())
				.active(true)
				.build();

		assertThat(mimeTypeService.searchAllowedMimeTypes(searchCriteria, Pageable.unpaged()).getTotalElements())
				.isEqualTo(1L);
	}

	@Test
	@DisplayName("Ajout d'un mimeType déjà actif : rejet attendu")
	void createMimeTypeWhenActiveCodeAlreadyExists_shouldFail() {
		final MimeTypeEntity existingActiveMimeType = mimeTypeDatafactory.getOrCreateActiveUniqueMimeType();

		final MimeType duplicateMimeType = new MimeType();
		duplicateMimeType.setUuid(UUID.randomUUID());
		duplicateMimeType.setCode(existingActiveMimeType.getCode());
		duplicateMimeType.setLabel("Duplicate active");
		duplicateMimeType.setOrder(11);
		duplicateMimeType.setOpeningDate(LocalDateTime.now(ZoneOffset.UTC));
		duplicateMimeType.setClosingDate(null);

		assertThatThrownBy(() -> mimeTypeService.createAllowedMimeType(duplicateMimeType))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining(existingActiveMimeType.getCode())
				.hasMessageContaining("déjà utilisé");
	}

	@Test
	@DisplayName("Ajout d'un mimeType existant mais inactif : création autorisée")
	void createMimeTypeWhenExistingCodeIsInactive_shouldSucceed() {
		final MimeTypeEntity existingInactiveMimeType = mimeTypeDatafactory.getOrCreateInactiveUniqueMimeType();

		final MimeType mimeTypeToCreate = new MimeType();
		mimeTypeToCreate.setUuid(UUID.randomUUID());
		mimeTypeToCreate.setCode(existingInactiveMimeType.getCode());
		mimeTypeToCreate.setLabel("Reactivated MIME");
		mimeTypeToCreate.setOrder(15);
		mimeTypeToCreate.setOpeningDate(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1));
		mimeTypeToCreate.setClosingDate(null);

		final MimeType createdMimeType = mimeTypeService.createAllowedMimeType(mimeTypeToCreate);

		assertThat(createdMimeType).isNotNull();
		assertThat(createdMimeType.getUuid()).isNotNull();
		assertThat(createdMimeType.getCode()).isEqualTo(existingInactiveMimeType.getCode());
		assertThat(createdMimeType.getClosingDate()).isNull();

		final MimeTypeSearchCriteria activeCriteria = MimeTypeSearchCriteria.builder()
				.code(existingInactiveMimeType.getCode())
				.active(true)
				.build();
		assertThat(mimeTypeService.searchAllowedMimeTypes(activeCriteria, Pageable.unpaged()).getTotalElements())
				.isEqualTo(1L);
	}

	@Test
	@DisplayName("Modification d'un mimeType existant")
	void updateMimeType() {
		final MimeTypeEntity existingMimeType = mimeTypeDatafactory.getOrCreateActiveCSVMimeType();

		final LocalDateTime newOpeningDate = LocalDateTime.now(ZoneOffset.UTC).minusDays(3);
		final LocalDateTime newClosingDate = LocalDateTime.now(ZoneOffset.UTC).minusHours(1);

		final MimeType mimeTypeToUpdate = new MimeType();
		mimeTypeToUpdate.setUuid(existingMimeType.getUuid());
		mimeTypeToUpdate.setCode(existingMimeType.getCode());
		mimeTypeToUpdate.setLabel("CSV updated");
		mimeTypeToUpdate.setOrder(25);
		mimeTypeToUpdate.setOpeningDate(newOpeningDate);
		mimeTypeToUpdate.setClosingDate(newClosingDate);

		final MimeType updatedMimeType = mimeTypeService.updateAllowedMimeType(existingMimeType.getUuid(), mimeTypeToUpdate);

		assertThat(updatedMimeType).isNotNull();
		assertThat(updatedMimeType.getUuid()).isEqualTo(existingMimeType.getUuid());
		assertThat(updatedMimeType.getCode()).isEqualTo(existingMimeType.getCode());
		assertThat(updatedMimeType.getLabel()).isEqualTo("CSV updated");
		assertThat(updatedMimeType.getOrder()).isEqualTo(25);
		assertThat(updatedMimeType.getOpeningDate()).isEqualTo(newOpeningDate);
		assertThat(updatedMimeType.getClosingDate()).isEqualTo(newClosingDate);
	}

	@Test
	@DisplayName("Modification d'un mimeType avec changement de code : rejet attendu")
	void updateMimeTypeWhenChangingCode_shouldFail() {
		final MimeTypeEntity existingMimeType = mimeTypeDatafactory.getOrCreateActiveJSONMimeType();

		final MimeType mimeTypeToUpdate = new MimeType();
		mimeTypeToUpdate.setUuid(existingMimeType.getUuid());
		mimeTypeToUpdate.setCode("application/changed-code");
		mimeTypeToUpdate.setLabel(existingMimeType.getLabel());
		mimeTypeToUpdate.setOrder(existingMimeType.getOrder());
		mimeTypeToUpdate.setOpeningDate(existingMimeType.getOpeningDate());
		mimeTypeToUpdate.setClosingDate(existingMimeType.getClosingDate());

		assertThatThrownBy(() -> mimeTypeService.updateAllowedMimeType(existingMimeType.getUuid(), mimeTypeToUpdate))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("ne peut pas être modifié")
				.hasMessageContaining(existingMimeType.getCode());
	}

	@Test
	@DisplayName("Suppression d'un mimeType existant")
	void deleteAllowedMimeType_shouldDeleteExistingMimeType() {
		final MimeTypeEntity existingMimeType = mimeTypeDatafactory.getOrCreateActiveUniqueMimeType();

		final MimeTypeSearchCriteria beforeDeleteCriteria = MimeTypeSearchCriteria.builder()
				.code(existingMimeType.getCode())
				.active(true)
				.build();
		assertThat(mimeTypeService.searchAllowedMimeTypes(beforeDeleteCriteria, Pageable.unpaged()).getTotalElements())
				.isEqualTo(1L);

		mimeTypeService.deleteAllowedMimeType(existingMimeType.getUuid());

		final MimeTypeSearchCriteria afterDeleteCriteria = MimeTypeSearchCriteria.builder()
				.code(existingMimeType.getCode())
				.active(true)
				.build();
		assertThat(mimeTypeService.searchAllowedMimeTypes(afterDeleteCriteria, Pageable.unpaged()).getTotalElements())
				.isEqualTo(0L);
	}

	@Test
	@DisplayName("Suppression d'un mimeType inexistant : rejet attendu")
	void deleteAllowedMimeType_whenUuidDoesNotExist_shouldFail() {
		assertThatThrownBy(() -> mimeTypeService.deleteAllowedMimeType(UUID.randomUUID()))
				.isInstanceOf(EmptyResultDataAccessException.class);
	}

	@Test
	@DisplayName("Modification d'un mimeType inexistant : rejet attendu")
	void updateAllowedMimeType_whenUuidDoesNotExist_shouldFail() {
		final MimeType mimeTypeToUpdate = new MimeType();
		mimeTypeToUpdate.setUuid(UUID.randomUUID());
		mimeTypeToUpdate.setCode("application/x-missing-" + UUID.randomUUID());
		mimeTypeToUpdate.setLabel("Missing MIME");
		mimeTypeToUpdate.setOrder(10);
		mimeTypeToUpdate.setOpeningDate(LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
		mimeTypeToUpdate.setClosingDate(null);

		assertThatThrownBy(() -> mimeTypeService.updateAllowedMimeType(UUID.randomUUID(), mimeTypeToUpdate))
				.isInstanceOf(EmptyResultDataAccessException.class);
	}

	@Test
	@DisplayName("Recherche des mimeTypes : filtre actif + code")
	void searchAllowedMimeTypes_shouldFilterActiveByCode() {
		final MimeTypeEntity activeMimeType = mimeTypeDatafactory.getOrCreateActiveUniqueMimeType();
		final MimeTypeEntity inactiveMimeType = mimeTypeDatafactory.getOrCreateInactiveUniqueMimeType();

		final MimeTypeSearchCriteria activeCriteria = MimeTypeSearchCriteria.builder()
				.code(activeMimeType.getCode())
				.active(true)
				.build();
		assertThat(mimeTypeService.searchAllowedMimeTypes(activeCriteria, Pageable.unpaged()).getTotalElements())
				.isEqualTo(1L);

		final MimeTypeSearchCriteria inactiveCodeWithActiveFilterCriteria = MimeTypeSearchCriteria.builder()
				.code(inactiveMimeType.getCode())
				.active(true)
				.build();
		assertThat(mimeTypeService.searchAllowedMimeTypes(inactiveCodeWithActiveFilterCriteria, Pageable.unpaged()).getTotalElements())
				.isEqualTo(0L);
	}

	@Test
	@DisplayName("Recherche des mimeTypes : même code avec actif + inactif")
	void searchAllowedMimeTypes_shouldHandleActiveAndInactiveForSameCode() {
		final MimeTypeEntity existingInactiveMimeType = mimeTypeDatafactory.getOrCreateInactiveUniqueMimeType();

		final MimeType mimeTypeToCreate = new MimeType();
		mimeTypeToCreate.setUuid(UUID.randomUUID());
		mimeTypeToCreate.setCode(existingInactiveMimeType.getCode());
		mimeTypeToCreate.setLabel("Active for same code");
		mimeTypeToCreate.setOrder(20);
		mimeTypeToCreate.setOpeningDate(LocalDateTime.now(ZoneOffset.UTC).minusMinutes(1));
		mimeTypeToCreate.setClosingDate(null);
		mimeTypeService.createAllowedMimeType(mimeTypeToCreate);

		final MimeTypeSearchCriteria allByCodeCriteria = MimeTypeSearchCriteria.builder()
				.code(existingInactiveMimeType.getCode())
				.build();
		assertThat(mimeTypeService.searchAllowedMimeTypes(allByCodeCriteria, Pageable.unpaged()).getTotalElements())
				.isEqualTo(2L);

		final MimeTypeSearchCriteria activeByCodeCriteria = MimeTypeSearchCriteria.builder()
				.code(existingInactiveMimeType.getCode())
				.active(true)
				.build();
		assertThat(mimeTypeService.searchAllowedMimeTypes(activeByCodeCriteria, Pageable.unpaged()).getTotalElements())
				.isEqualTo(1L);
	}

	@Test
	@DisplayName("Recherche des mimeTypes : code inconnu")
	void searchAllowedMimeTypes_shouldReturnEmptyForUnknownCode() {
		final MimeTypeSearchCriteria criteria = MimeTypeSearchCriteria.builder()
				.code("application/x-unknown-" + UUID.randomUUID())
				.active(true)
				.build();

		assertThat(mimeTypeService.searchAllowedMimeTypes(criteria, Pageable.unpaged()).getTotalElements())
				.isEqualTo(0L);
	}

	@Test
	@DisplayName("Recherche des mimeTypes : isolation par code")
	void searchAllowedMimeTypes_shouldFilterByExactCode() {
		final MimeTypeEntity firstActiveMimeType = mimeTypeDatafactory.getOrCreateActiveUniqueMimeType();
		final MimeTypeEntity secondActiveMimeType = mimeTypeDatafactory.getOrCreateActiveUniqueMimeType();

		final MimeTypeSearchCriteria firstCodeCriteria = MimeTypeSearchCriteria.builder()
				.code(firstActiveMimeType.getCode())
				.active(true)
				.build();

		final var result = mimeTypeService.searchAllowedMimeTypes(firstCodeCriteria, Pageable.unpaged());
		assertThat(result.getTotalElements()).isEqualTo(1L);
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getCode()).isEqualTo(firstActiveMimeType.getCode());
		assertThat(result.getContent().get(0).getCode()).isNotEqualTo(secondActiveMimeType.getCode());
	}

	@Test
	@DisplayName("Recherche des mimeTypes : pagination sur actifs")
	void searchAllowedMimeTypes_shouldSupportPagination() {
		mimeTypeDatafactory.getOrCreateActiveUniqueMimeType();
		mimeTypeDatafactory.getOrCreateActiveUniqueMimeType();

		final MimeTypeSearchCriteria criteria = MimeTypeSearchCriteria.builder()
				.active(true)
				.build();

		final var page0 = mimeTypeService.searchAllowedMimeTypes(criteria, PageRequest.of(0, 1));
		final var page1 = mimeTypeService.searchAllowedMimeTypes(criteria, PageRequest.of(1, 1));

		assertThat(page0.getContent()).hasSize(1);
		assertThat(page1.getContent()).hasSize(1);
		assertThat(page0.getTotalElements()).isGreaterThanOrEqualTo(2L);
		assertThat(page1.getTotalElements()).isGreaterThanOrEqualTo(2L);
	}

	@Test
	@DisplayName("Ajout d'un mimeType déjà existant après normalisation +crypt : rejet attendu")
	void createMimeTypeWhenCodeExistsAfterCryptNormalization_shouldFail() {
		final MimeTypeEntity existingActiveMimeType = mimeTypeDatafactory.getOrCreateActiveUniqueMimeType();

		final MimeType duplicateMimeType = new MimeType();
		duplicateMimeType.setUuid(UUID.randomUUID());
		duplicateMimeType.setCode(existingActiveMimeType.getCode() + "+crypt");
		duplicateMimeType.setLabel("Duplicate crypt");
		duplicateMimeType.setOrder(12);
		duplicateMimeType.setOpeningDate(LocalDateTime.now(ZoneOffset.UTC));
		duplicateMimeType.setClosingDate(null);

		assertThatThrownBy(() -> mimeTypeService.createAllowedMimeType(duplicateMimeType))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("Ajout d'un mimeType avec code vide : rejet attendu")
	void createMimeTypeWithBlankCode_shouldFail() {
		mimeTypeDatafactory.getOrCreateActiveUniqueMimeType();

		final MimeType mimeTypeToCreate = new MimeType();
		mimeTypeToCreate.setUuid(UUID.randomUUID());
		mimeTypeToCreate.setCode("");
		mimeTypeToCreate.setLabel("Blank code");
		mimeTypeToCreate.setOrder(10);
		mimeTypeToCreate.setOpeningDate(LocalDateTime.now(ZoneOffset.UTC));
		mimeTypeToCreate.setClosingDate(null);

		assertThatThrownBy(() -> mimeTypeService.createAllowedMimeType(mimeTypeToCreate))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
