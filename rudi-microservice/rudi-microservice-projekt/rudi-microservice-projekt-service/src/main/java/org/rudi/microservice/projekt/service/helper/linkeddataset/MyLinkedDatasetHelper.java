package org.rudi.microservice.projekt.service.helper.linkeddataset;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.projekt.core.bean.LinkedDataset;
import org.rudi.microservice.projekt.core.bean.LinkedDatasetSearchCriteria;
import org.rudi.microservice.projekt.service.project.LinkedDatasetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MyLinkedDatasetHelper {
	private final LinkedDatasetService linkedDatasetService;

	public List<UUID> searchMyOrganizationsLinkedDatasets(LinkedDatasetSearchCriteria searchCriteria) throws AppServiceException {
		Pageable pageable = Pageable.unpaged();
		Page<LinkedDataset> pagedLinkedDatasetList = linkedDatasetService.searchMyOrganizationsLinkedDatasets(searchCriteria, pageable);
		return pagedLinkedDatasetList.getContent().stream().map(LinkedDataset::getDatasetUuid).collect(Collectors.toList());
	}

}
