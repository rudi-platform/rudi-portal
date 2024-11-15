package org.rudi.microservice.strukture.storage.dao.provider;

import org.rudi.microservice.strukture.core.bean.criteria.LinkedProducerSearchCriteria;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LinkedProducerCustomDao {
	Page<LinkedProducerEntity> searchLinkedProducers(LinkedProducerSearchCriteria criteria, Pageable pageable);
}
