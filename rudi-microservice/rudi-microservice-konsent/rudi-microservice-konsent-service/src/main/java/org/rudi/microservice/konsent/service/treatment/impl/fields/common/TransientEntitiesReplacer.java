package org.rudi.microservice.konsent.service.treatment.impl.fields.common;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.rudi.common.service.exception.AppServiceException;

import lombok.RequiredArgsConstructor;

/**
 * Doc Transient / Persistent : https://docs.jboss.org/hibernate/core/3.3/reference/fr-FR/html/objectstate.html
 *
 * @param <V>
 * @param <E>
 */
@RequiredArgsConstructor
public abstract class TransientEntitiesReplacer<V, E> {
	private final Function<V, E> entitiesGetter;
	private final BiConsumer<V, E> entitiesSetter;


	public void replaceTransientEntitiesWithPersistentEntities(@Nullable V fieldToProcess, @Nullable V fieldToProcessPreviousValueInDB) throws AppServiceException {
		if (fieldToProcess != null) {
			final var transientEntities = getTransientEntities(fieldToProcess);
			final var persistentEntities = getPersistentEntities(transientEntities);

			// Si on est en update (existingProject != null) alors c'est lui qu'on modifie sinon c'est l'autre (création)
			final var targetTreatmentVersion = Objects.requireNonNullElse(fieldToProcessPreviousValueInDB, fieldToProcess);

			setPersistentEntities(targetTreatmentVersion, persistentEntities);
		}
	}

	@Nullable
	private E getTransientEntities(@Nonnull V sourceTreatmentVersion) {
		return entitiesGetter.apply(sourceTreatmentVersion);
	}

	/**
	 * Retrouve les entités persistées à partir des entités transcientes, pour remplacement dans les entités les utilisant (pour les listes déroulantes
	 * par exemple)
	 * 
	 * @param transientEntities la ou les entités transientes
	 * @return la ou les entités persistes correspondantes
	 * @throws AppServiceException si les entités transcientes ne sont pas trouvées
	 */
	@Nullable
	protected abstract E getPersistentEntities(@Nullable E transientEntities) throws AppServiceException;

	private void setPersistentEntities(@Nonnull V targetTreatmentVersion, @Nullable E persistentEntities) {
		entitiesSetter.accept(targetTreatmentVersion, persistentEntities);
	}

}
