package org.rudi.microservice.projekt.storage.dao.targetaudience;


import org.rudi.microservice.projekt.core.bean.criteria.TargetAudienceSearchCriteria;
import org.rudi.microservice.projekt.storage.entity.TargetAudienceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TargetAudienceCustomDao {
	Page<TargetAudienceEntity> searchTargetAudiences(TargetAudienceSearchCriteria searchCriteria, Pageable pageable);
}
