package org.rudi.facet.kaccess.datafactory;

import org.rudi.facet.kaccess.bean.MetadataAccessCondition;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer les objets MetadataAccessCondition.
 * Gère les conditions d'accès (licence, confidentialité, contraintes d'usage, etc.)
 */
@Component
@RequiredArgsConstructor
public class MetadataAccessConditionDataFactory {

	private final AccessConditionProfilesDataFactory accessConditionProfilesDataFactory;

	/**
	 * Crée des conditions d'accès standard avec licence CC0, accès restreint et sensibilité RGPD.
	 * // Aucun get possible
	 */
	public MetadataAccessCondition createAccessConditionStandard() {
		return accessConditionProfilesDataFactory.createAccessConditionMaxStandardLicence();
	}

	/**
	 * Crée des conditions d'accès minimales (licence + confidentialité seulement).
	 * // Aucun get possible
	 */
	public MetadataAccessCondition createAccessConditionMinimal() {
		return accessConditionProfilesDataFactory.createAccessConditionMinStandardLicence();
	}

	/**
	 * Crée des conditions d'accès minimales avec licence Apache 2.0.
	 */
	public MetadataAccessCondition createAccessConditionMinimalApache20() {
		return accessConditionProfilesDataFactory.createAccessConditionMinStandardLicenceApache20();
	}

}

