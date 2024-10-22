package org.rudi.microservice.strukture.core.bean.criteria;

import java.util.UUID;

import org.rudi.microservice.strukture.core.bean.OrganizationMemberType;
import org.rudi.microservice.strukture.core.bean.OrganizationRole;

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
public class OrganizationMembersSearchCriteria {

	private UUID organizationUuid;

	private String searchText;

	private OrganizationMemberType type; // ROBOT ou PERSON

	private OrganizationRole role; // ADMINISTRATOR ou EDITOR

	private UUID userUuid;

	private Integer limit;

}
