package org.rudi.microservice.konsult.service.metadata.impl;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.MissingParameterException;
import org.rudi.facet.dataverse.api.exceptions.DatasetNotFoundException;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.DatasetSearchCriteria;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.MetadataFacets;
import org.rudi.facet.kaccess.bean.MetadataList;
import org.rudi.facet.kaccess.bean.MetadataListFacets;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.microservice.konsult.core.harvest.DcatJsonLdContext;
import org.rudi.microservice.konsult.service.exception.DataverseExternalServiceException;
import org.rudi.microservice.konsult.service.exception.MetadataNotFoundException;
import org.rudi.microservice.konsult.service.helper.media.MediaUrlHelper;
import org.rudi.microservice.konsult.service.mapper.jsonld.DcatJsonLdMapper;
import org.rudi.microservice.konsult.service.metadata.MetadataService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetadataServiceImpl implements MetadataService {

	private static final Integer DEFAULT_START = 0;
	private static final Integer DEFAULT_RESULTS_NUMBER = 100;

	private final DatasetService datasetService;
	private final MetadataWithSameThemeFinder metadataWithSameThemeFinder;
	private final DcatJsonLdMapper dcatJsonLdMapper;
	private final MediaUrlHelper mediaUrlHelper;

	@Override
	public MetadataList searchMetadatas(DatasetSearchCriteria datasetSearchCriteria) throws DataverseAPIException {
		return searchMetadataWithFacets(datasetSearchCriteria, Collections.emptyList()).getMetadataList();
	}

	@Override
	public MetadataFacets searchMetadatasFacets(List<String> facets) throws DataverseAPIException {
		// récupération des métadonnées sans filtre, avec un seul élément
		// si on met limit = 0, le dataverse va mettre la valeur limit = 10 par défaut
		DatasetSearchCriteria datasetSearchCriteria = new DatasetSearchCriteria().limit(1).offset(0);
		return searchMetadataWithFacets(datasetSearchCriteria, facets).getFacets();
	}

	@Override
	@Nonnull
	public Metadata getMetadataById(UUID globalId) throws AppServiceException {
		if (globalId == null) {
			throw new MissingParameterException("L'identifiant du jeu de donnée est absent");
		}
		try {
			final Metadata metadata = datasetService.getDataset(globalId);
			mediaUrlHelper.rewriteMediaUrls(metadata);
			return metadata;
		} catch (DatasetNotFoundException e) {
			throw new MetadataNotFoundException(e);
		} catch (DataverseAPIException e) {
			throw new DataverseExternalServiceException(e);
		}
	}

	@Override
	public List<Metadata> getMetadatasWithSameTheme(UUID globalId, Integer limit) throws AppServiceException {
		final var metadata = getMetadataById(globalId);
		try {
			return metadataWithSameThemeFinder.find(metadata.getDataverseDoi(), limit);
		} catch (DataverseAPIException e) {
			throw new DataverseExternalServiceException(e);
		}
	}

	@Override
	public Integer getNumberOfDatasetsOnTheSameTheme(UUID globalId) throws AppServiceException {
		final var metadata = getMetadataById(globalId);
		try {
			return metadataWithSameThemeFinder.getNumberOfDatasetsOnTheSameTheme(metadata.getDataverseDoi());
		} catch (DataverseAPIException de) {
			throw new DataverseExternalServiceException(de);
		}

	}

	/**
	 * Moissonage des métadonnées
	 *
	 * @param datasetSearchCriteria critères de recherches
	 * @return une string au format jsonld
	 */
	@Override
	public String generateDcatJsonLd(DatasetSearchCriteria datasetSearchCriteria) throws DataverseAPIException, AppServiceException {
		MetadataList metadataList = searchMetadataWithFacets(datasetSearchCriteria, Collections.emptyList()).getMetadataList();

		long total = metadataList != null ? metadataList.getTotal() : 0L;

		DcatJsonLdContext context = DcatJsonLdContext.builder()
				.datasetSearchCriteria(datasetSearchCriteria)
				.totalDatasets(Long.valueOf(total))
				.build();

		return dcatJsonLdMapper.toJsonLd(metadataList, context).toString();
	}

	private MetadataListFacets searchMetadataWithFacets(DatasetSearchCriteria datasetSearchCriteria,
			List<String> facets) throws DataverseAPIException {
		if (datasetSearchCriteria.getOffset() == null) {
			datasetSearchCriteria.setOffset(DEFAULT_START);
		}
		if (datasetSearchCriteria.getLimit() == null) {
			datasetSearchCriteria.setLimit(DEFAULT_RESULTS_NUMBER);
		}
		return datasetService.searchDatasets(datasetSearchCriteria, facets);
	}


}
