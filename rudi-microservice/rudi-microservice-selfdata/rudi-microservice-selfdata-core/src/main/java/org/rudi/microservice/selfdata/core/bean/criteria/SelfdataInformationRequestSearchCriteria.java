package org.rudi.microservice.selfdata.core.bean.criteria;

import java.util.List;
import java.util.UUID;

import org.rudi.microservice.selfdata.core.bean.SelfdataInformationRequestStatus;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Critères de recherche backend pour une recherche de demandes d'informations.
 * Utilisé en interne dans la couche storage, après conversion depuis le criteria d'API.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
@EqualsAndHashCode
public class SelfdataInformationRequestSearchCriteria {
	/**
	 * UUID du dataset
	 */
	private UUID datasetUuid;

	/**
	 * Statuts de la demande
	 */
	private List<SelfdataInformationRequestStatus> status;

	/**
	 * Le login d'un utilisateur ayant créé des demandes (utilisé pour filtrer par utilisateur connecté)
	 */
	private String login;

	/**
	 * L'initiator de la requête de Selfdata
	 */
	private String initiator;
}

