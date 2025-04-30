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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DatasetController implements DatasetsApi {

	@Value("${harvest.dcat.default.page-size:10}")
	private int harvestDefaultPageSize;
	@Value("${harvest.dcat.max.page-size:50}")
	private int harvestMaxPageSize;
	@Value("${harvest.dcat.default.order:-dataset_dates.created}")
	private String harvestDefaultOrder;
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


	@Override
	public ResponseEntity<String> generateDcatJsonLd(String freeText, List<String> themes, List<String> keywords, List<String> producerNames, List<UUID> producerUuids, OffsetDateTime dateDebut, OffsetDateTime dateFin, Boolean restrictedAccess, Boolean gdprSensitive, Integer pageSize, Integer page) throws Exception {
		int limit = pageSize == null ? harvestDefaultPageSize : pageSize;

		if(limit > harvestMaxPageSize) {
			limit = harvestMaxPageSize;
		}

		int offset = page == null ? 0 : (page - 1) * limit;
		String order = harvestDefaultOrder;

		boolean restricted = Boolean.TRUE.equals(restrictedAccess);
		boolean gdpr = Boolean.TRUE.equals(gdprSensitive);

		final DatasetSearchCriteria datasetSearchCriteria = new DatasetSearchCriteria().limit(limit).offset(offset)
				.freeText(freeText).keywords(keywords).themes(themes).producerNames(producerNames)
				.producerUuids(producerUuids).dateDebut(dateDebut).dateFin(dateFin).order(order)
				.restrictedAccess(restricted).gdprSensitive(gdpr);

		return ResponseEntity.ok(metadataService.generateDcatJsonLd(datasetSearchCriteria));
	}
}
