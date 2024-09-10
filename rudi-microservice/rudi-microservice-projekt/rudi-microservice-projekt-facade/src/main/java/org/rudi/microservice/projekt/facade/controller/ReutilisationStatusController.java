package org.rudi.microservice.projekt.facade.controller;

import java.util.UUID;

import org.rudi.common.facade.util.UtilPageable;
import org.rudi.microservice.projekt.core.bean.PagedReutilisationStatusList;
import org.rudi.microservice.projekt.core.bean.ReutilisationStatus;
import org.rudi.microservice.projekt.core.bean.criteria.ReutilisationStatusSearchCriteria;
import org.rudi.microservice.projekt.facade.controller.api.ReutilisationStatusApi;
import org.rudi.microservice.projekt.service.reutilisationstatus.ReutilisationStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.val;
import static org.rudi.common.core.security.QuotedRoleCodes.ADMINISTRATOR;

@RestController
@RequiredArgsConstructor
public class ReutilisationStatusController implements ReutilisationStatusApi {
	private final ReutilisationStatusService reutilisationStatusService;
	private final UtilPageable utilPageable;

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ")")
	public ResponseEntity<ReutilisationStatus> createReutilisationStatus(ReutilisationStatus reutilisationStatus) {
		return ResponseEntity.ok(reutilisationStatusService.createReutilisationStatus(reutilisationStatus));
	}

	@Override
	public ResponseEntity<ReutilisationStatus> getReutilisationStatus(UUID uuid) {
		return ResponseEntity.ok(reutilisationStatusService.getReutilisationStatus(uuid));
	}

	/**
	 * GET /reutilisation-status : Recherche de statut de réutilisation
	 * Recherche de statut de réutilisation
	 *
	 * @param limit  Le nombre de résultats à retourner par page (optional)
	 * @param offset Index de début (positionne le curseur pour parcourir les résultats de la recherche) (optional)
	 * @param order  (optional)
	 * @param active (optional)
	 * @return OK (status code 200)
	 * or Internal server error (status code 500)
	 */
	@Override
	public ResponseEntity<PagedReutilisationStatusList> searchReutilisationStatus(Boolean active, Integer limit, Integer offset, String order) throws Exception {
		val pageable = utilPageable.getPageable(offset, limit, order);
		ReutilisationStatusSearchCriteria criteria = ReutilisationStatusSearchCriteria.builder().active(active).build();
		val pages =  reutilisationStatusService.searchReutilisationStatus(criteria, pageable);
		return ResponseEntity.ok(new PagedReutilisationStatusList()
				.total(pages.getTotalElements())
				.elements(pages.getContent()));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ")")
	public ResponseEntity<ReutilisationStatus> updateReutilisationStatus(UUID uuid, ReutilisationStatus reutilisationStatus) {
		return ResponseEntity.ok(reutilisationStatusService.updateReutilisationStatus(uuid, reutilisationStatus));
	}
}
