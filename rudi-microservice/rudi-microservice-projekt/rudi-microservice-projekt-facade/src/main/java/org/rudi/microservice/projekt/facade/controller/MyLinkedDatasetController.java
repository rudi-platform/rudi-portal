package org.rudi.microservice.projekt.facade.controller;

import java.util.List;

import org.rudi.common.facade.util.UtilPageable;
import org.rudi.microservice.projekt.core.bean.LinkedDataset;
import org.rudi.microservice.projekt.core.bean.LinkedDatasetStatus;
import org.rudi.microservice.projekt.core.bean.PagedLinkedDatasetList;
import org.rudi.microservice.projekt.core.bean.criteria.LinkedDatasetSearchCriteria;
import org.rudi.microservice.projekt.facade.controller.api.MyLinkedDatasetsApi;
import org.rudi.microservice.projekt.service.project.LinkedDatasetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MyLinkedDatasetController implements MyLinkedDatasetsApi {

	private final LinkedDatasetService linkedDatasetService;
	private final UtilPageable utilPageable;

	@Override
	public ResponseEntity<PagedLinkedDatasetList> searchMyLinkedDatasets(String datasetConfidentiality,
			List<LinkedDatasetStatus> status, Integer offset, Integer limit, String order) throws Exception {
		LinkedDatasetSearchCriteria criteria = LinkedDatasetSearchCriteria.builder()
				.datasetConfidentiality(datasetConfidentiality).status(status).build();

		Pageable pageable = utilPageable.getPageable(offset, limit, order);
		Page<LinkedDataset> page = linkedDatasetService.searchMyLinkedDatasets(criteria, pageable);

		return ResponseEntity.ok(new PagedLinkedDatasetList().elements(page.getContent()).total(page.getTotalElements()));
	}
}
