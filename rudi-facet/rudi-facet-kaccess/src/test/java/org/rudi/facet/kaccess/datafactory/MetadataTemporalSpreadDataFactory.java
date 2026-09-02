package org.rudi.facet.kaccess.datafactory;

import java.time.OffsetDateTime;

import org.rudi.facet.kaccess.bean.MetadataTemporalSpread;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer les objets MetadataTemporalSpread.
 * Représente la plage temporelle couverte par un dataset.
 */
@Component
@RequiredArgsConstructor
public class MetadataTemporalSpreadDataFactory {

	/**
	 * Crée une plage temporelle standard (2021 à 2022).
	 * Les dates sont en UTC (OffsetDateTime).
	 * // Aucun get possible
	 */
	public MetadataTemporalSpread createTemporalSpreadStandard() {
		MetadataTemporalSpread spread = new MetadataTemporalSpread();
		spread.setStartDate(OffsetDateTime.parse("2021-01-03T15:30:45Z"));
		spread.setEndDate(OffsetDateTime.parse("2022-04-22T15:26:00Z"));
		return spread;
	}

}

