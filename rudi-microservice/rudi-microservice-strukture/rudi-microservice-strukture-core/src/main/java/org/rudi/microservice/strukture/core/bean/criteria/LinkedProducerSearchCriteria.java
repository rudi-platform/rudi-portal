package org.rudi.microservice.strukture.core.bean.criteria;

import java.util.UUID;

import org.rudi.microservice.strukture.core.bean.SearchCriteria;

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
public class LinkedProducerSearchCriteria extends SearchCriteria {
	private UUID uuid;

	private UUID organizationUuid;

	private UUID providerUuid;
}
