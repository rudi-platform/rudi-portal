package org.rudi.microservice.projekt.storage.entity.linkeddataset;

import org.rudi.common.storage.entity.PositionedStatus;
import org.rudi.common.storage.entity.StatusPosition;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LinkedDatasetStatus implements PositionedStatus {
	/**
	 * En cours
	 */
	DRAFT(StatusPosition.INITIAL),

	/**
	 * Validé => le projet est une réutilisation
	 */
	IN_PROGRESS(StatusPosition.INTERMEDIATE),

	/**
	 * Validé
	 */
	VALIDATED(StatusPosition.FINAL),

	/**
	 * Abandonné
	 */
	CANCELLED(StatusPosition.FINAL),

	/**
	 * Archivé visible dans un projet archivé
	 */
	ARCHIVED(StatusPosition.FINAL),

	/**
	 * Archivé invisible
	 */
	DISENGAGED(StatusPosition.FINAL);

	private final StatusPosition position;

	@Override
	public boolean isInitial() {
		return position == StatusPosition.INITIAL;
	}

	@Override
	public boolean isFinal() {
		return position == StatusPosition.FINAL;
	}
}
