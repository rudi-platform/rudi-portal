package org.rudi.microservice.strukture.storage.dao.organization;

import org.rudi.microservice.strukture.core.bean.criteria.NodeOrganizationSearchCriteria;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationSearchCriteria;
import org.rudi.microservice.strukture.storage.bean.NodeOrganizationProjectionBean;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Permet d'obtenir une liste de provider paginée et triée
 */
public interface OrganizationCustomDao {
	Page<OrganizationEntity> searchOrganizations(OrganizationSearchCriteria searchCriteria, Pageable pageable);

	Page<NodeOrganizationProjectionBean> searchNodeOrganizations(NodeOrganizationSearchCriteria searchCriteria,
			Pageable pageable);
}
