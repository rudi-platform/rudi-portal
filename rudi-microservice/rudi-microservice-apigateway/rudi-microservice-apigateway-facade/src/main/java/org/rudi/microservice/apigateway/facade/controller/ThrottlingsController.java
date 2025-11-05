/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.controller;

import java.util.UUID;

import org.rudi.common.facade.util.UtilPageable;
import org.rudi.microservice.apigateway.core.bean.PagedThrottlingList;
import org.rudi.microservice.apigateway.core.bean.Throttling;
import org.rudi.microservice.apigateway.core.bean.ThrottlingSearchCriteria;
import org.rudi.microservice.apigateway.facade.controller.api.ThrottlingsApi;
import org.rudi.microservice.apigateway.service.throttling.ThrottlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_APIGATEWAY_ADMINISTRATOR;

/**
 * @author FNI18300
 *
 */
@RestController
public class ThrottlingsController implements ThrottlingsApi {

	@Autowired
	private ThrottlingService throttlingService;

	@Autowired
	private UtilPageable utilPageable;

	@Override
	@PreAuthorize("hasAnyRole(" + MODULE_APIGATEWAY_ADMINISTRATOR + ")")
	public ResponseEntity<Throttling> createThrottling(Throttling throttling) throws Exception {
		return ResponseEntity.ok(throttlingService.createThrottling(throttling));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + MODULE_APIGATEWAY_ADMINISTRATOR + ")")
	public ResponseEntity<Void> deleteThrottling(UUID throttlingUuid) throws Exception {
		throttlingService.deleteThrottling(throttlingUuid);
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<Throttling> getThorttling(UUID throttlingUuid) throws Exception {
		return ResponseEntity.ok(throttlingService.getThrottling(throttlingUuid));
	}

	/**
	 * GET /throttlings : Search Thorttlings
	 * Return a list of Thorttlings
	 *
	 * @param active (optional)
	 * @param code   (optional)
	 * @param limit  Le nombre de résultats à retourner par page (optional)
	 * @param offset Index de début (positionne le curseur pour parcourir les résultats de la recherche) (optional)
	 * @param order  (optional)
	 * @return OK (status code 200)
	 * or Internal server error (status code 500)
	 */
	@Override
	public ResponseEntity<PagedThrottlingList> searchThorttlings(Boolean active, String code, Integer limit, 
			Integer offset, String order) throws Exception {
		PagedThrottlingList result = new PagedThrottlingList();
		ThrottlingSearchCriteria searchCriteria = new ThrottlingSearchCriteria().active(active).code(code);

		Page<Throttling> apis = throttlingService.searchThrottlings(searchCriteria,
				utilPageable.getPageable(offset, limit, order));
		result.setElements(apis.getContent());
		result.setTotal(apis.getTotalElements());
		return ResponseEntity.ok(result);
	}

	@Override
	@PreAuthorize("hasAnyRole(" + MODULE_APIGATEWAY_ADMINISTRATOR + ")")
	public ResponseEntity<Throttling> updateThrottling(Throttling throttling) throws Exception {
		return ResponseEntity.ok(throttlingService.updateThrottling(throttling));
	}

}
