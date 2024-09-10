package org.rudi.microservice.projekt.storage.dao.confidentiality;

import java.util.UUID;

import javax.annotation.Nonnull;

import org.rudi.common.storage.dao.StampedRepository;
import org.rudi.microservice.projekt.storage.entity.ConfidentialityEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfidentialityDao extends StampedRepository<ConfidentialityEntity> {

	/**
	 * @throws org.springframework.dao.EmptyResultDataAccessException si l'entité demandée n'a pas été trouvée
	 */
	@Nonnull
	ConfidentialityEntity findByUUID(UUID uuid);

	ConfidentialityEntity findByCode(String code);
}
