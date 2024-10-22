package org.rudi.microservice.strukture.storage.dao.provider;

import java.util.UUID;

import javax.annotation.Nonnull;

import org.rudi.common.storage.dao.StampedRepository;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

/**
 * Dao pour les Roles
 * 
 * @author FNI18300
 *
 */
@Repository
public interface ProviderDao extends StampedRepository<ProviderEntity> {

	@Nonnull
	ProviderEntity findByUUID(UUID uuid) throws EmptyResultDataAccessException;

	ProviderEntity findByCode(String code);
}
