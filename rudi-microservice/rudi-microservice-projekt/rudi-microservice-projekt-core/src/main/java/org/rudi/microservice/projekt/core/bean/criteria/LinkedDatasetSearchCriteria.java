package org.rudi.microservice.projekt.core.bean.criteria;

import java.util.List;
import java.util.UUID;

import org.rudi.common.core.bean.criteria.SearchCriteria;
import org.rudi.microservice.projekt.core.bean.LinkedDatasetStatus;
import org.rudi.microservice.projekt.core.bean.RelationStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Critère de recherche interne (non exposé via l'API) d'un dataset lié.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class LinkedDatasetSearchCriteria implements SearchCriteria {
	private UUID datasetUuid;
	private List<UUID> projectOwnerUuids;
	private List<UUID> datasetOwnerUuids;
	private List<LinkedDatasetStatus> status;
	private Boolean endDateIsNotOver;
	private Boolean endDateIsNull;
	private Boolean endDateIsOver;
	private String datasetConfidentiality;
	private List<UUID> projectRelatedUuids;
	private List<RelationStatus> organizationProjectRelationStatuses;
	private String initiator;
}

