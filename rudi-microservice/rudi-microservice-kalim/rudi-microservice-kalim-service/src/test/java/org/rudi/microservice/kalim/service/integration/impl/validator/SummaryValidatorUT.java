package org.rudi.microservice.kalim.service.integration.impl.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rudi.facet.kaccess.bean.DictionaryEntry;
import org.rudi.facet.kaccess.bean.Language;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.constant.RudiMetadataField;
import org.rudi.microservice.kalim.service.IntegrationError;
import org.rudi.microservice.kalim.service.integration.impl.validator.metadata.SummaryValidator;
import org.rudi.microservice.kalim.storage.entity.integration.IntegrationRequestErrorEntity;

@ExtendWith(MockitoExtension.class)
class SummaryValidatorUT {

	@InjectMocks
	private SummaryValidator summaryValidator;

	@Test
	@DisplayName("Test de la validation summary avec une liste null")
	void testValidateSummaryWithNullValue() {
		Metadata metadata = new Metadata().summary(null);
		Set<IntegrationRequestErrorEntity> integrationRequestErrorEntities = summaryValidator
				.validateMetadata(metadata);

		assertThat(integrationRequestErrorEntities.size()).isEqualTo(1);

		assertThat(integrationRequestErrorEntities)
				.anyMatch(integrationRequestErrorEntity -> integrationRequestErrorEntity.getFieldName()
						.equals(RudiMetadataField.SUMMARY.getLocalName())
						&& integrationRequestErrorEntity.getMessage().equals(String.format(
								IntegrationError.ERR_202.getMessage(), RudiMetadataField.SUMMARY.getLocalName())));
	}

	@Test
	@DisplayName("Test de la validation titre du jdd avec une liste vide")
	void testValidateSummaryWithEmptyList() {
		Metadata metadata = new Metadata().summary(List.of());
		Set<IntegrationRequestErrorEntity> integrationRequestErrorEntities = summaryValidator
				.validateMetadata(metadata);

		assertThat(integrationRequestErrorEntities.size()).isEqualTo(1);

		assertThat(integrationRequestErrorEntities)
				.anyMatch(integrationRequestErrorEntity -> integrationRequestErrorEntity.getFieldName()
						.equals(RudiMetadataField.SUMMARY.getLocalName())
						&& integrationRequestErrorEntity.getMessage().equals(String.format(
								IntegrationError.ERR_202.getMessage(), RudiMetadataField.SUMMARY.getLocalName())));
	}

	@ParameterizedTest
	//@formatter:off
	@ValueSource(strings = {
		"", 
		"<b></b>",
		"<a href=\\\"http://www.example.com\\\"></a>",
	})
	@DisplayName("Test de la validation titre du jdd avec une valeur vide")
	void testValidateSummaryWithEmptyValue(String inputText) {
		DictionaryEntry entry = new DictionaryEntry();

		entry.text(inputText);
		entry.lang(Language.FR);

		List<DictionaryEntry> entries = List.of(entry);

		Metadata metadata = new Metadata().summary(entries);
		Set<IntegrationRequestErrorEntity> integrationRequestErrorEntities = summaryValidator
				.validateMetadata(metadata);

		assertThat(integrationRequestErrorEntities.size()).isEqualTo(1);

		assertThat(integrationRequestErrorEntities)
				.anyMatch(integrationRequestErrorEntity -> integrationRequestErrorEntity.getFieldName()
						.equals(RudiMetadataField.SUMMARY.getLocalName())
						&& integrationRequestErrorEntity.getMessage().equals(String.format(
								IntegrationError.ERR_202.getMessage(), RudiMetadataField.SUMMARY.getLocalName())));
	}

	@ParameterizedTest
	//@formatter:off
	@ValueSource(strings = { 
		"Test validate", 
		"<b>Test validate</b>",
		"<a href=\"http://www.example.com\">Test validate</a>", 
	})
	//@formatter:on
	@DisplayName("Test de la validation summary avec une valeur valide")
	void testValidateSummaryWithNoErrors(String inputText) {
		DictionaryEntry entry = new DictionaryEntry();

		entry.text(inputText);
		entry.lang(Language.FR);

		List<DictionaryEntry> entries = List.of(entry);

		Metadata metadata = new Metadata().summary(entries);

		Set<IntegrationRequestErrorEntity> integrationRequestErrorEntities = summaryValidator
				.validateMetadata(metadata);

		assertThat(integrationRequestErrorEntities).isEmpty();
	}

	@Test
	@DisplayName("Test de la validation summary multilingue avec une valeur de dictionnaire vide")
	void testValidateSummaryMultilingualWithOneEmptyValue() {
		DictionaryEntry entryFr = new DictionaryEntry();

		entryFr.text("<b></b>");
		entryFr.lang(Language.FR);

		DictionaryEntry entryEn = new DictionaryEntry();

		entryEn.text("<b>valid value</b>");
		entryEn.lang(Language.EN);

		List<DictionaryEntry> entries = List.of(entryFr, entryEn);

		Metadata metadata = new Metadata().summary(entries);

		Set<IntegrationRequestErrorEntity> integrationRequestErrorEntities = summaryValidator
				.validateMetadata(metadata);

		assertThat(integrationRequestErrorEntities.size()).isEqualTo(1);

		assertThat(integrationRequestErrorEntities)
				.anyMatch(integrationRequestErrorEntity -> integrationRequestErrorEntity.getFieldName()
						.equals(RudiMetadataField.SUMMARY.getLocalName())
						&& integrationRequestErrorEntity.getMessage().equals(String.format(
								IntegrationError.ERR_202.getMessage(), RudiMetadataField.SUMMARY.getLocalName())));
	}

	@Test
	@DisplayName("Test de la validation summary multilingue avec 2 valeurs valides")
	void testValidateSummaryMultilingualWithTwoValidValues() {
		DictionaryEntry entryFr = new DictionaryEntry();

		entryFr.text("<b>valeur valide</b>");
		entryFr.lang(Language.FR);

		DictionaryEntry entryEn = new DictionaryEntry();

		entryEn.text("<b>valid value</b>");
		entryEn.lang(Language.EN);

		List<DictionaryEntry> entries = List.of(entryFr, entryEn);

		Metadata metadata = new Metadata().summary(entries);

		Set<IntegrationRequestErrorEntity> integrationRequestErrorEntities = summaryValidator
				.validateMetadata(metadata);

		assertThat(integrationRequestErrorEntities).isEmpty();
	}
}
