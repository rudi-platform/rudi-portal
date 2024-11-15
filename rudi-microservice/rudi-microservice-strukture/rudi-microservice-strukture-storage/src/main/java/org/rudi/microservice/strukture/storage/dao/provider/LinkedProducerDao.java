package org.rudi.microservice.strukture.storage.dao.provider;

import java.util.UUID;

import org.rudi.facet.bpmn.dao.workflow.AssetDescriptionDao;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkedProducerDao extends AssetDescriptionDao<LinkedProducerEntity> {

	LinkedProducerEntity findByUuid(UUID uuid);

}
