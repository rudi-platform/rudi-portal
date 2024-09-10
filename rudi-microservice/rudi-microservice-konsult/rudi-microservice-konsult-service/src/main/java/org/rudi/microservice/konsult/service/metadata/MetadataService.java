package org.rudi.microservice.konsult.service.metadata;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.DatasetSearchCriteria;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.MetadataFacets;
import org.rudi.facet.kaccess.bean.MetadataList;

public interface MetadataService {

	/**
	 * Recherche sur les métadonnées
	 *
	 * @param datasetSearchCriteria critères de recherches
	 * @return MetadataList
	 * @throws DataverseAPIException Erreur lors de la recherche dans le dataverse
	 */
	MetadataList searchMetadatas(DatasetSearchCriteria datasetSearchCriteria) throws DataverseAPIException;

	/**
	 * Recherche des informations sur les paramètres des métadonnées
	 *
	 * @param facets paramètres des métadonnnées pour lesquelles on récupère la liste des valeurs possibles
	 * @return MetadataFacets
	 * @throws DataverseAPIException Erreur lors de la recherche dans le dataverse
	 */
	MetadataFacets searchMetadatasFacets(List<String> facets) throws DataverseAPIException;

	/**
	 * Récupération des métadonnées d'un jeu de données
	 *
	 * @param globalId Identifiant du jeu de données
	 * @return Metadata
	 * @throws AppServiceException Erreur lors de la récupération des métadonnées
	 */
	@Nonnull
	Metadata getMetadataById(UUID globalId) throws AppServiceException;

	/**
	 * @return liste des JDD partageant le même thème (cf RUDI-292)
	 */
	List<Metadata> getMetadatasWithSameTheme(UUID globalId, Integer limit) throws AppServiceException;

	/**
	 * @param globalId du dataset
	 * @return nombre de dataset sur le même thème
	 */
	Integer getNumberOfDatasetsOnTheSameTheme(UUID globalId) throws AppServiceException;
}
