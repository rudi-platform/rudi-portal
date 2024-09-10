package org.rudi.microservice.konsult.facade.controller.media;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.rudi.facet.kaccess.bean.DatasetSearchCriteria;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.MetadataFacets;
import org.rudi.facet.kaccess.bean.MetadataList;
import org.rudi.microservice.konsult.facade.controller.api.DatasetsApi;
import org.rudi.microservice.konsult.service.metadata.MetadataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DatasetController implements DatasetsApi {

	private final MetadataService metadataService;

	@Override
	public ResponseEntity<MetadataList> searchMetadatas(String freeText, List<String> themes, List<String> keywords,
			List<String> producerNames, OffsetDateTime dateDebut, OffsetDateTime dateFin, Boolean restrictedAccess,
			Boolean gdprSensitive, List<UUID> globalId, List<UUID> producerUuids, Integer offset, Integer limit,
			String order) throws Exception {

		final DatasetSearchCriteria datasetSearchCriteria = new DatasetSearchCriteria().limit(limit).offset(offset)
				.freeText(freeText).keywords(keywords).themes(themes).producerNames(producerNames)
				.producerUuids(producerUuids).dateDebut(dateDebut).dateFin(dateFin).order(order)
				.restrictedAccess(restrictedAccess).gdprSensitive(gdprSensitive).globalIds(globalId);

		return ResponseEntity.ok(metadataService.searchMetadatas(datasetSearchCriteria));
	}

	@Override
	public ResponseEntity<MetadataFacets> searchMetadataFacets(List<String> facets) throws Exception {
		return ResponseEntity.ok(metadataService.searchMetadatasFacets(facets));
	}

	@Override
	public ResponseEntity<Metadata> getMetadataById(UUID globalId) throws Exception {
		return ResponseEntity.ok(metadataService.getMetadataById(globalId));
	}

	@Override
	public ResponseEntity<List<Metadata>> getMetadatasWithSameTheme(UUID globalId, Integer limit) throws Exception {
		return ResponseEntity.ok(metadataService.getMetadatasWithSameTheme(globalId, limit));
	}

	@Override
	public ResponseEntity<Integer> getNumberOfDatasetsOnTheSameTheme(UUID globalId) throws Exception {
		return ResponseEntity.ok(metadataService.getNumberOfDatasetsOnTheSameTheme(globalId));
	}
}
