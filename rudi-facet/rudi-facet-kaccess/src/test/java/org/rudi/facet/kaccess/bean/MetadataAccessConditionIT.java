package org.rudi.facet.kaccess.bean;

import java.util.Arrays;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.common.core.validator.URI;
import org.rudi.facet.kaccess.datafactory.AccessConditionProfilesDataFactory;
import org.rudi.facet.kaccess.datafactory.DictionaryEntryDataFactory;
import org.rudi.facet.kaccess.datafactory.LicenceStandardDataFactory;
import org.rudi.facet.kaccess.datafactory.MetadataAccessConditionConfidentialityDataFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataAccessConditionIT {

	private final AccessConditionProfilesDataFactory accessConditionProfilesDataFactory =
			new AccessConditionProfilesDataFactory(
					new MetadataAccessConditionConfidentialityDataFactory(),
					new DictionaryEntryDataFactory(),
					new LicenceStandardDataFactory());
	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void licence_standard_licence() {
		final MetadataAccessCondition metadataAccessCondition = accessConditionProfilesDataFactory
				.createAccessConditionMinStandardLicence();

		assertThat(metadataAccessCondition).hasFieldOrPropertyWithValue("licence.licenceLabel",
				LicenceStandard.LicenceLabelEnum.PUBLIC_DOMAIN_CC0);
	}

	@Test
	void licence_custom_licence() {
		final MetadataAccessCondition metadataAccessCondition = accessConditionProfilesDataFactory
				.createAccessConditionMinCustomLicence();

		assertThat(metadataAccessCondition)
				.hasFieldOrPropertyWithValue("licence.customLicenceUri", "https://www.example.com/animals")
				.hasFieldOrPropertyWithValue("licence.customLicenceLabel",
						Arrays.asList(new DictionaryEntry().lang(Language.FR_FR).text("Animaux"),
								new DictionaryEntry().lang(Language.EN_US).text("Animals")));
	}

	@Test
	void access_condition_complet_standard_licence() {
		final MetadataAccessCondition metadataAccessCondition = accessConditionProfilesDataFactory
				.createAccessConditionMaxStandardLicence();

		assertThat(metadataAccessCondition).hasFieldOrPropertyWithValue("licence.licenceLabel",
				LicenceStandard.LicenceLabelEnum.PUBLIC_DOMAIN_CC0);
	}

	@Test
	void access_condition_complet_custom_licence() {
		final MetadataAccessCondition metadataAccessCondition = accessConditionProfilesDataFactory
				.createAccessConditionMaxCustomLicence();

		assertThat(metadataAccessCondition)
				.hasFieldOrPropertyWithValue("licence.customLicenceUri", "https://www.example.com/animals")
				.hasFieldOrPropertyWithValue("licence.customLicenceLabel",
						Arrays.asList(new DictionaryEntry().lang(Language.FR_FR).text("Animaux"),
								new DictionaryEntry().lang(Language.EN_US).text("Animals")));
	}


	@Test
	@Disabled("Je ne comprends pas comment ce test a pu fonctionner : le générateur ne met pas de validation sur ce champ même s'il est de format URI")
	@DisplayName("Test de la propriété customLicenceUri avec une url invalide")
	void access_condition_custom_licence_uri_validation() {
		final MetadataAccessCondition metadataAccessCondition = accessConditionProfilesDataFactory
				.createAccessConditionInvalidCustomLicenceUri();
		Set<ConstraintViolation<MetadataAccessCondition>> violations = validator.validate(metadataAccessCondition);
		assertTrue(violations.stream().anyMatch(violation -> violation.getPropertyPath() instanceof PathImpl
				&& ((PathImpl) (violation.getPropertyPath())).getLeafNode().asString().equals("customLicenceUri") // vérification que l'erreur concerne bien l'attribut customLicenceUri
				&& violation.getConstraintDescriptor().getAnnotation().annotationType().isAssignableFrom(URI.class))); // vérification que l'erreur est du à la validation @URL
	}

}
