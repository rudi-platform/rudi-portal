package org.rudi.microservice.projekt.storage.dao.territory;

import java.util.UUID;

import javax.annotation.Nonnull;

import org.rudi.common.storage.dao.StampedRepository;
import org.rudi.microservice.projekt.storage.entity.TerritorialScaleEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface TerritorialScaleDao extends StampedRepository<TerritorialScaleEntity> {

	/**
	 * @throws org.springframework.dao.EmptyResultDataAccessException si l'entité demandée n'a pas été trouvée
	 */
	@Nonnull
	TerritorialScaleEntity findByUUID(UUID uuid);
}
