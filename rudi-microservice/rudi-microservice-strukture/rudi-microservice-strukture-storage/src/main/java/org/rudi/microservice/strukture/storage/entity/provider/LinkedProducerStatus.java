package org.rudi.microservice.strukture.storage.entity.provider;

import org.rudi.common.storage.entity.PositionedStatus;
import org.rudi.common.storage.entity.StatusPosition;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LinkedProducerStatus implements PositionedStatus {

	DRAFT(StatusPosition.INITIAL),

	/**
	 * En cours
	 */
	IN_PROGRESS(StatusPosition.INTERMEDIATE),

	/**
	 * Abandonné
	 */
	CANCELLED(StatusPosition.FINAL),

	/**
	 * Validé : l'organisation créée
	 */
	VALIDATED(StatusPosition.FINAL),

	/**
	 * Lien rompu
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