package org.rudi.microservice.kalim.storage.dao.mimetype;

import org.rudi.common.storage.dao.StampedRepository;
import org.rudi.microservice.kalim.storage.entity.mimetype.MimeTypeEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface MimeTypeDao extends StampedRepository<MimeTypeEntity> {

}
