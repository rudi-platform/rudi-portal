package org.rudi.microservice.kalim.storage.dao.mimetype;

import org.rudi.microservice.kalim.core.bean.MimeTypeSearchCriteria;
import org.rudi.microservice.kalim.storage.entity.mimetype.MimeTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface MimeTypeCustomDao {



	Page<MimeTypeEntity> searchMimeTypes(MimeTypeSearchCriteria criteria, Pageable pageable);
}
