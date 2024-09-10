package org.rudi.microservice.projekt.facade.controller;

import java.util.UUID;

import org.rudi.common.facade.util.UtilPageable;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.projekt.core.bean.PagedSupportList;
import org.rudi.microservice.projekt.core.bean.Support;
import org.rudi.microservice.projekt.core.bean.criteria.SupportSearchCriteria;
import org.rudi.microservice.projekt.facade.controller.api.SupportsApi;
import org.rudi.microservice.projekt.service.support.SupportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.val;
import static org.rudi.common.core.security.QuotedRoleCodes.ADMINISTRATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_PROJEKT;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_PROJEKT_ADMINISTRATOR;

@RestController
@RequiredArgsConstructor
public class SupportController implements SupportsApi {

	private final SupportService supportService;
	private final UtilPageable utilPageable;

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ")")
	public ResponseEntity<Support> createSupport(Support support) throws AppServiceException {
		val createdSupport = supportService.createSupport(support);
		val location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{uuid}")
				.buildAndExpand(createdSupport.getUuid())
				.toUri();
		return ResponseEntity.created(location).body(createdSupport);
	}

	@Override
	public ResponseEntity<Support> getSupport(UUID uuid) {
		return ResponseEntity.ok(supportService.getSupport(uuid));
	}

	/**
	 * GET /supports : Liste tous les type d’accompagnements
	 * Liste tous les type d’accompagnements
	 *
	 * @param limit  Le nombre de résultats à retourner par page (optional)
	 * @param offset Index de début (positionne le curseur pour parcourir les résultats de la recherche) (optional)
	 * @param order  (optional)
	 * @param active (optional)
	 * @return OK (status code 200)
	 * or Internal server error (status code 500)
	 */
	@Override
	public ResponseEntity<PagedSupportList> searchSupports(Boolean active, Integer limit, Integer offset, String order ) throws Exception {
		val searchCriteria = SupportSearchCriteria.builder().active(active).build();
		val pageable = utilPageable.getPageable(offset, limit, order);
		val page = supportService.searchSupports(searchCriteria, pageable);
		return ResponseEntity.ok(new PagedSupportList()
				.total(page.getTotalElements())
				.elements(page.getContent()));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ")")
	public ResponseEntity<Void> updateSupport(UUID uuid, Support support) throws Exception {
		support.setUuid(uuid);
		supportService.updateSupport(support);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_PROJEKT_ADMINISTRATOR + ", " + MODULE_PROJEKT + ")")
	public ResponseEntity<Void> deleteSupport(UUID uuid) {
		supportService.deleteSupport(uuid);
		return ResponseEntity.noContent().build();
	}
}
