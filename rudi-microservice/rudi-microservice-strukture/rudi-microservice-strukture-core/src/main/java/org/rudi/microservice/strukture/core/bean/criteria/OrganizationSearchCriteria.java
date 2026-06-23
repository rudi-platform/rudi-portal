package org.rudi.microservice.strukture.core.bean.criteria;

import java.util.List;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.core.bean.criteria.SearchCriteria;
import org.rudi.microservice.strukture.core.bean.OrganizationStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class OrganizationSearchCriteria implements SearchCriteria {
	private List<UUID> uuids;
	private String name;
	private Boolean active;
	private UUID userUuid;
	private List<OrganizationStatus> organizationStatus;
	private Status status;
	private Boolean loadAllInformations;
	private List<UUID> excludeOrganizationUuids ;

	private Integer offset;
	private Integer limit;
	private String order;

	List<OrganizationStatus> adminMemberAllowedStatus;
}
