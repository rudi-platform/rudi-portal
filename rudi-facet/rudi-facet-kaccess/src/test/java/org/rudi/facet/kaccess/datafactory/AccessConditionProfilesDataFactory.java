package org.rudi.facet.kaccess.datafactory;

import java.util.List;

import org.rudi.facet.kaccess.bean.DictionaryEntry;
import org.rudi.facet.kaccess.bean.LicenceCustom;
import org.rudi.facet.kaccess.bean.MetadataAccessCondition;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Profils AccessCondition alignés avec les fixtures JSON de metadata/accessCondition.
 */
@Component
@RequiredArgsConstructor
public class AccessConditionProfilesDataFactory {

	private final MetadataAccessConditionConfidentialityDataFactory confidentialityDataFactory;
	private final DictionaryEntryDataFactory dictionaryEntryDataFactory;
	private final LicenceStandardDataFactory licenceStandardDataFactory;

	/**
	 * Crée le profil accessCondition/min-standard_licence.json.
	 */
	public MetadataAccessCondition createAccessConditionMinStandardLicence() {
		final MetadataAccessCondition accessCondition = new MetadataAccessCondition();
		accessCondition.setLicence(licenceStandardDataFactory.createLicencePublicDomainCc0());
		return accessCondition;
	}

	/**
	 * Crée le profil standard minimal avec licence Apache 2.0.
	 */
	public MetadataAccessCondition createAccessConditionMinStandardLicenceApache20() {
		final MetadataAccessCondition accessCondition = new MetadataAccessCondition();
		accessCondition.setLicence(licenceStandardDataFactory.createLicenceApache20());
		return accessCondition;
	}

	/**
	 * Crée le profil accessCondition/max-standard_licence.json.
	 */
	public MetadataAccessCondition createAccessConditionMaxStandardLicence() {
		final MetadataAccessCondition accessCondition = createAccessConditionMinStandardLicence();
		accessCondition.setConfidentiality(confidentialityDataFactory.createConfidentialityRestrictedWithGdpr());
		applyCommonTextConstraints(accessCondition);
		return accessCondition;
	}

	/**
	 * Crée le profil accessCondition/min-custom_licence.json.
	 */
	public MetadataAccessCondition createAccessConditionMinCustomLicence() {
		final MetadataAccessCondition accessCondition = new MetadataAccessCondition();
		accessCondition.setLicence(createCustomLicence("https://www.example.com/animals"));
		return accessCondition;
	}

	/**
	 * Crée le profil accessCondition/max-custom_licence.json.
	 */
	public MetadataAccessCondition createAccessConditionMaxCustomLicence() {
		final MetadataAccessCondition accessCondition = createAccessConditionMinCustomLicence();
		accessCondition.setConfidentiality(confidentialityDataFactory.createConfidentialityRestrictedWithGdpr());
		applyCommonTextConstraints(accessCondition);
		return accessCondition;
	}

	/**
	 * Crée le profil accessCondition/invalid_custom_licence_uri.json.
	 */
	public MetadataAccessCondition createAccessConditionInvalidCustomLicenceUri() {
		final MetadataAccessCondition accessCondition = new MetadataAccessCondition();
		accessCondition.setConfidentiality(confidentialityDataFactory.createConfidentialityRestrictedWithGdpr());
		accessCondition.setLicence(createCustomLicence("bump::\\_!$()"));
		return accessCondition;
	}

	private LicenceCustom createCustomLicence(String uri) {
		final LicenceCustom licence = new LicenceCustom();
		licence.setLicenceType(LicenceCustom.LicenceTypeEnum.CUSTOM);
		licence.setCustomLicenceUri(uri);
		licence.setCustomLicenceLabel(List.of(
				dictionaryEntryDataFactory.createDictionaryEntryFrench("Animaux"),
				dictionaryEntryDataFactory.createDictionaryEntryEnglish("Animals")
		));
		return licence;
	}

	private void applyCommonTextConstraints(MetadataAccessCondition accessCondition) {
		accessCondition.setUsageConstraint(singleFrenchEntry(
				"Usage libre sous réserve des mentions obligatoires sur tout document de diffusion"));
		accessCondition.setBibliographicalReference(singleFrenchEntry("abeilles"));
		accessCondition.setMandatoryMention(singleFrenchEntry("Source: Région Bretagne"));
		accessCondition.setAccessConstraint(singleFrenchEntry("abeilles"));
		accessCondition.setOtherConstraints(singleFrenchEntry("Pas de restriction d'accès public"));
	}

	private List<DictionaryEntry> singleFrenchEntry(String text) {
		return List.of(dictionaryEntryDataFactory.createDictionaryEntryFrench(text));
	}
}

