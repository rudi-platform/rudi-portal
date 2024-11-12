package org.rudi.microservice.projekt.service.project;

import java.util.List;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Form;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationException;
import org.rudi.microservice.projekt.core.bean.LinkedDataset;
import org.rudi.microservice.projekt.core.bean.LinkedDatasetSearchCriteria;
import org.rudi.microservice.projekt.core.bean.LinkedDatasetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LinkedDatasetService {
	/**
	 * @param projectUuid UUID du projet
	 * @param status      Statut des jdd demandés
	 * @return Tous les jeux de données réutilisés par le projet
	 */
	List<LinkedDataset> getLinkedDatasets(UUID projectUuid, List<LinkedDatasetStatus> status)
			throws AppServiceNotFoundException;

	/**
	 * @param projectUuid       UUID du projet
	 * @param linkedDatasetUuid UUID du linkedDataset
	 * @return Tous les jeux de données réutilisés par le projet
	 */
	LinkedDataset getLinkedDataset(UUID projectUuid, UUID linkedDatasetUuid) throws AppServiceNotFoundException;

	/**
	 * Ajoute un lien entre un projet et un jeu de données
	 *
	 * @param projectUuid   UUID du projet
	 * @param linkedDataset LinkedDataset fourni par le JSON
	 */
	LinkedDataset linkProjectToDataset(UUID projectUuid, LinkedDataset linkedDataset)
			throws DataverseAPIException, AppServiceException;

	/**
	 * MAJ un linked dataset
	 *
	 * @param projectUuid   UUID du projet
	 * @param linkedDataset LinkedDataset fourni par le JSON à garder
	 */
	LinkedDataset updateLinkedDataset(UUID projectUuid, LinkedDataset linkedDataset) throws AppServiceException;

	/**
	 * Supprime un lien entre un projet et un jeu de données
	 *
	 * @param projectUuid       UUID du projet
	 * @param linkedDatasetUuid UUID du jeu de données
	 */
	void unlinkProjectToDataset(UUID projectUuid, UUID linkedDatasetUuid) throws AppServiceException;

	Page<LinkedDataset> searchMyLinkedDatasets(LinkedDatasetSearchCriteria criteria, Pageable pageable)
			throws AppServiceException;

	Page<LinkedDataset> searchMyOrganizationsLinkedDatasets(LinkedDatasetSearchCriteria criteria, Pageable pageable)
			throws AppServiceException;
	/**
	 * Retourne le formulaire de consultation des informations de la décision concernant la demande d'accès au JDD
	 * 
	 * @param linkedDatasetUuid le lien du JDD
	 * @return le formulaire avec les informations à afficher
	 * @throws AppServiceException     si les informations ne sont pas trouvées ou n'existent pas
	 * @throws FormDefinitionException si le formulaire défini n'est pas valide
	 */
	Form getDecisionInformations(UUID projectUuid, UUID linkedDatasetUuid)
			throws AppServiceException, FormDefinitionException;

	/**
	 * Permet de vérifier si l'utilisateur connecté a accès au JDD, ciblé par son uuid.
	 *
	 * @param datasetUuid globalId du dataset
	 * @return true s'il a accès, false sinon.
	 */
	boolean isMyAccessGratedToDataset(UUID datasetUuid)
			throws GetOrganizationException, AppServiceUnauthorizedException;

	/**
	 * Permet de vérifier si l'un des uuid a accès au JDD ciblé par son uuid
	 * 
	 * @param userList    liste d'UUID d'utilisateur (uuid utilisateur + ses organisations par exemple)
	 * @param datasetUuid globalId du dataset
	 * @return true si l'un des uuid a accès, false sinon.
	 */
	boolean isAccessGrantedToDatasetForAUser(List<UUID> userList, UUID datasetUuid)
			throws GetOrganizationException, AppServiceUnauthorizedException;
}
