package org.rudi.microservice.projekt.storage.dao.reutilisationstatus;

import org.rudi.microservice.projekt.core.bean.criteria.ReutilisationStatusSearchCriteria;
import org.rudi.microservice.projekt.storage.entity.ReutilisationStatusEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReutilisationStatusCustomDao {
	Page<ReutilisationStatusEntity> searchReutilisationStatus(ReutilisationStatusSearchCriteria criteria, Pageable pageable);
}
