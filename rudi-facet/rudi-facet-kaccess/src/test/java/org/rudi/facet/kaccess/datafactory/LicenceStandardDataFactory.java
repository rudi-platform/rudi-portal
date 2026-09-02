package org.rudi.facet.kaccess.datafactory;

import org.rudi.facet.kaccess.bean.LicenceStandard;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer les objets LicenceStandard.
 * Une licence standard est une licence prédéfinie (Apache, MIT, CC0, etc.)
 */
@Component
@RequiredArgsConstructor
public class LicenceStandardDataFactory {

	/**
	 * Crée une licence standard de domaine public CC0.
	 * // Aucun get possible
	 */
	public LicenceStandard createLicencePublicDomainCc0() {
		return createLicenceStandard(LicenceStandard.LicenceLabelEnum.PUBLIC_DOMAIN_CC0);
	}

	/**
	 * Crée une licence standard Apache 2.0.
	 * // Aucun get possible
	 */
	public LicenceStandard createLicenceApache20() {
		return createLicenceStandard(LicenceStandard.LicenceLabelEnum.APACHE_2_0);
	}

	/**
	 * Crée une licence standard ODbL 1.0.
	 * // Aucun get possible
	 */
	public LicenceStandard createLicenceOdbl10() {
		return createLicenceStandard(LicenceStandard.LicenceLabelEnum.ODBL_1_0);
	}

	private LicenceStandard createLicenceStandard(LicenceStandard.LicenceLabelEnum label) {
		LicenceStandard licence = new LicenceStandard();
		licence.setLicenceType(LicenceStandard.LicenceTypeEnum.STANDARD);
		licence.setLicenceLabel(label);
		return licence;
	}

}
