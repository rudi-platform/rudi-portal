package org.rudi.microservice.strukture.core.bean.criteria;


import java.util.UUID;

import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.core.bean.criteria.SearchCriteria;
import org.rudi.microservice.strukture.core.bean.OrganizationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class OrganizationSearchCriteria implements SearchCriteria {
	private UUID uuid;
	private String name;
	private Boolean active;
	private UUID userUuid;
	private OrganizationStatus organizationStatus;
	private Status status;
	private Boolean loadAllInformations;

	private Integer offset;
	private Integer limit;
	private String order;
}
