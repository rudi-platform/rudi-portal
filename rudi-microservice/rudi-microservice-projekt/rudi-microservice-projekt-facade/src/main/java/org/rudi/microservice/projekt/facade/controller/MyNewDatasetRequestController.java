package org.rudi.microservice.projekt.facade.controller;

import java.util.List;

import org.rudi.common.facade.util.UtilPageable;
import org.rudi.microservice.projekt.core.bean.NewDatasetRequestStatus;
import org.rudi.microservice.projekt.core.bean.PagedNewDatasetRequestList;
import org.rudi.microservice.projekt.core.bean.criteria.NewDatasetRequestSearchCriteria;
import org.rudi.microservice.projekt.facade.controller.api.MyNewDatasetRequestsApi;
import org.rudi.microservice.projekt.service.project.NewDatasetRequestService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MyNewDatasetRequestController implements MyNewDatasetRequestsApi {

	private final NewDatasetRequestService newDatasetRequestService;
	private final UtilPageable utilPageable;

	@Override
	public ResponseEntity<PagedNewDatasetRequestList> searchMyNewDatasetRequests(
			List<NewDatasetRequestStatus> status,
			Integer offset,
			Integer limit,
			String order) throws Exception {

		// Créer le criteria avec les paramètres reçus
		NewDatasetRequestSearchCriteria criteria = new NewDatasetRequestSearchCriteria();
		criteria.setStatus(status);
		criteria.setOffset(offset);
		criteria.setLimit(limit);
		criteria.setOrder(order);

		Pageable pageable = utilPageable.getPageable(offset, limit, order);
		return ResponseEntity.ok(newDatasetRequestService.searchMyNewDatasetRequests(criteria, pageable));
	}
}


