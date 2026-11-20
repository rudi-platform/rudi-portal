package org.rudi.microservice.selfdata.core.bean.criteria;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Critères de recherche pour les datasets selfdata.
 * Utilisé en interne côté service/storage.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class SelfdataDatasetSearchCriteria {

	/**
	 * Index de début
	 */
	private Integer offset;

	/**
	 * Limite de résultats par page
	 */
	private Integer limit;

	/**
	 * Critère de tri
	 */
	private String order;
}

