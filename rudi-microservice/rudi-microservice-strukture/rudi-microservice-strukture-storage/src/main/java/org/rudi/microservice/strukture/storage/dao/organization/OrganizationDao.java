package org.rudi.microservice.strukture.storage.dao.organization;

import java.util.UUID;

import org.rudi.facet.bpmn.dao.workflow.AssetDescriptionDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationDao extends AssetDescriptionDao<OrganizationEntity> {

	OrganizationEntity findByUuid(UUID uuid);

}
