package org.rudi.microservice.strukture.storage.dao.provider;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.rudi.microservice.strukture.storage.entity.provider.NodeProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Dao pour les NodeProvider
 *
 * @author NTR18299
 */
@Repository
public interface NodeProviderDao extends JpaRepository<NodeProviderEntity, Long> {


    @Nullable
    NodeProviderEntity findByUuid(UUID uuid);

    List<NodeProviderEntity> findAll();

    void deleteByUuid(UUID uuid);

}
