package org.rudi.microservice.strukture.core.bean.criteria;

import java.util.Objects;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o))
			return false;
		LinkedProducerSearchCriteria searchCriteria = (LinkedProducerSearchCriteria) o;
		return Objects.equals(this.uuid, searchCriteria.uuid)
				&& Objects.equals(this.organizationUuid, searchCriteria.organizationUuid)
				&& Objects.equals(this.providerUuid, searchCriteria.providerUuid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), uuid, organizationUuid, providerUuid);
	}
}
