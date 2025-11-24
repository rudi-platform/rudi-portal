package org.rudi.microservice.strukture.core.bean.criteria;

import java.util.UUID;

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
public class NodeOrganizationSearchCriteria extends OrganizationSearchCriteria {
	private UUID providerUUID;

}
