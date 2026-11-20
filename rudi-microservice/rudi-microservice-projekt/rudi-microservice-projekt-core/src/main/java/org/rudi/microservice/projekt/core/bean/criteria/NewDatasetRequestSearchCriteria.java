package org.rudi.microservice.projekt.core.bean.criteria;

import java.util.List;
import java.util.UUID;

import org.rudi.common.core.bean.criteria.SearchCriteria;
import org.rudi.microservice.projekt.core.bean.NewDatasetRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Critère de recherche pour les demandes de nouveau JDD.
 * Contient les champs du contrat API (status, offset, limit, order)
 * ainsi que les champs enrichis par le service (projectOwnerUuids).
 * Les champs initiator et titleDescription peuvent être utilisés en interne par le DAO
 * mais ne sont pas exposés en frontend.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class NewDatasetRequestSearchCriteria implements SearchCriteria {
	// Champs du contrat API (reçus du controller)
	private List<NewDatasetRequestStatus> status;
	private Integer offset;
	private Integer limit;
	private String order;

	// Champs enrichis par le service (optionnels, calculés lors de la recherche)
	private List<UUID> projectOwnerUuids;

	// Champs utilisés en interne par le DAO (optionnels, non exposés en API)
	private String titleDescription;
	private String initiator;
}




