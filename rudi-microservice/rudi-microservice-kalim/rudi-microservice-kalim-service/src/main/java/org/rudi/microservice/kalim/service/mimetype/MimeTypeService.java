package org.rudi.microservice.kalim.service.mimetype;

import java.util.UUID;

import org.rudi.microservice.kalim.core.bean.MimeType;
import org.rudi.microservice.kalim.core.bean.MimeTypeSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MimeTypeService {

	MimeType createAllowedMimeType(MimeType mimeType);

	void deleteAllowedMimeType(UUID uuid);

	Page<MimeType> searchAllowedMimeTypes(MimeTypeSearchCriteria criteria, Pageable pageable);

	MimeType updateAllowedMimeType(UUID uuid, MimeType mimeType);
}
