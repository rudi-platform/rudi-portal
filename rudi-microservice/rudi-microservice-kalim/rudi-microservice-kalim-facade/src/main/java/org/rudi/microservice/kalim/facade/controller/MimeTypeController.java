package org.rudi.microservice.kalim.facade.controller;

import java.util.UUID;

import org.rudi.common.facade.util.UtilPageable;
import org.rudi.microservice.kalim.core.bean.MimeType;
import org.rudi.microservice.kalim.core.bean.MimeTypeSearchCriteria;
import org.rudi.microservice.kalim.core.bean.PagedMimeTypes;
import org.rudi.microservice.kalim.facade.controller.api.MediaApi;
import org.rudi.microservice.kalim.service.mimetype.MimeTypeService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import static org.rudi.common.core.security.QuotedRoleCodes.ADMINISTRATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_KALIM_ADMINISTRATOR;

@RestController
@RequiredArgsConstructor
public class MimeTypeController implements MediaApi {

	private final MimeTypeService mimeTypeService;
	private final UtilPageable utilPageable;

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_KALIM_ADMINISTRATOR + ")")
	public ResponseEntity<MimeType> createAllowedMimeType(MimeType mimeType) {
		return ResponseEntity.ok(mimeTypeService.createAllowedMimeType(mimeType));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_KALIM_ADMINISTRATOR + ")")
	public ResponseEntity<Void> deleteAllowedMimeType(UUID uuid) {
		try {
			mimeTypeService.deleteAllowedMimeType(uuid);
			return ResponseEntity.noContent().build();
		} catch (EmptyResultDataAccessException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Override
	// Path public
	public ResponseEntity<PagedMimeTypes> searchAllowedMimeTypes(Boolean active, Integer offset, Integer limit,
			String order) {
		MimeTypeSearchCriteria criteria = MimeTypeSearchCriteria.builder().active(active).build();

		Page<MimeType> page = mimeTypeService.searchAllowedMimeTypes(criteria,
				utilPageable.getPageable(offset, limit, order));
		return ResponseEntity.ok(new PagedMimeTypes().elements(page.getContent()).total(page.getTotalElements()));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_KALIM_ADMINISTRATOR + ")")
	public ResponseEntity<MimeType> updateAllowedMimeType(UUID uuid, MimeType mimeType) {
		if (!mimeType.getUuid().equals(uuid)) {
			throw new IllegalArgumentException("UUIDs do not match");
		}

		return ResponseEntity.ok(mimeTypeService.updateAllowedMimeType(uuid, mimeType));
	}
}
