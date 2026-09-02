package org.rudi.facet.kaccess.datafactory;

import java.time.OffsetDateTime;

import org.rudi.facet.kaccess.bean.ReferenceDates;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer les objets ReferenceDates.
 * Représente les dates importantes d'un dataset (création, validation, publication, mise à jour, suppression).
 */
@Component
@RequiredArgsConstructor
public class ReferenceDatesDataFactory {

	/**
	 * Crée des dates de référence standard (dataset créé en 2021).
	 * Les dates sont en UTC (OffsetDateTime).
	 * // Aucun get possible
	 */
	public ReferenceDates createReferenceDatesStandard() {
		return createReferenceDates(
				OffsetDateTime.parse("2021-02-01T00:00:00Z"),
				OffsetDateTime.parse("2021-02-02T00:00:00Z"),
				OffsetDateTime.parse("2021-02-03T00:00:00Z"),
				OffsetDateTime.parse("2021-03-12T07:00:00Z"),
				OffsetDateTime.parse("2021-04-29T00:00:00Z"));
	}

	/**
	 * Crée des dates de référence minimales (créé et mis à jour seulement).
	 * Les dates sont en UTC (OffsetDateTime).
	 * // Aucun get possible
	 */
	public ReferenceDates createReferenceDatesMinimal() {
		return createReferenceDates(
				OffsetDateTime.parse("2021-04-01T12:10:48.970Z"),
				null,
				null,
				OffsetDateTime.parse("2021-04-01T12:10:48.970Z"),
				null);
	}

	private ReferenceDates createReferenceDates(OffsetDateTime created, OffsetDateTime validated,
			OffsetDateTime published, OffsetDateTime updated, OffsetDateTime deleted) {
		ReferenceDates dates = new ReferenceDates();
		dates.setCreated(created);
		dates.setValidated(validated);
		dates.setPublished(published);
		dates.setUpdated(updated);
		dates.setDeleted(deleted);
		return dates;
	}

}

