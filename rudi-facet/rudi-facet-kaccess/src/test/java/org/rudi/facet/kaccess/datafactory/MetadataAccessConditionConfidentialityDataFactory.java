package org.rudi.facet.kaccess.datafactory;

import org.rudi.facet.kaccess.bean.MetadataAccessConditionConfidentiality;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer les objets MetadataAccessConditionConfidentiality.
 * Gère les règles de confidentialité d'un dataset (accès restreint, sensibilité RGPD).
 */
@Component
@RequiredArgsConstructor
public class MetadataAccessConditionConfidentialityDataFactory {

	/**
	 * Crée des conditions de confidentialité avec accès restreint et sensibilité RGPD.
	 * // Aucun get possible
	 */
	public MetadataAccessConditionConfidentiality createConfidentialityRestrictedWithGdpr() {
		return createConfidentiality(true, true);
	}

	/**
	 * Crée des conditions de confidentialité sans restriction et sans sensibilité RGPD (données publiques).
	 * // Aucun get possible
	 */
	public MetadataAccessConditionConfidentiality createConfidentialityPublic() {
		return createConfidentiality(false, false);
	}

	private MetadataAccessConditionConfidentiality createConfidentiality(boolean restricted, boolean gdprSensitive) {
		MetadataAccessConditionConfidentiality confidentiality = new MetadataAccessConditionConfidentiality();
		confidentiality.setRestrictedAccess(restricted);
		confidentiality.setGdprSensitive(gdprSensitive);
		return confidentiality;
	}

	/**
	 * Crée des conditions de confidentialité minimales.
	 * // Aucun get possible
	 */
	public MetadataAccessConditionConfidentiality createConfidentialityMinimal() {
		return createConfidentialityPublic();
	}

}
