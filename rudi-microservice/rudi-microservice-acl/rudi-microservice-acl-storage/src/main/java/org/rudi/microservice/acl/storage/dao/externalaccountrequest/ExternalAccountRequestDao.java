package org.rudi.microservice.acl.storage.dao.externalaccountrequest;

import java.util.UUID;

import org.rudi.facet.bpmn.dao.workflow.AssetDescriptionDao;
import org.rudi.microservice.acl.storage.entity.externalaccountrequest.ExternalAccountRequestEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface ExternalAccountRequestDao extends AssetDescriptionDao<ExternalAccountRequestEntity> {

	ExternalAccountRequestEntity findByUuid(UUID uuid);

}

