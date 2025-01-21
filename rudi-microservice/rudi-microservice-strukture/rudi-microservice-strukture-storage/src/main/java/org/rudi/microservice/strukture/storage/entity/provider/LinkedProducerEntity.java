package org.rudi.microservice.strukture.storage.entity.provider;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.rudi.facet.bpmn.entity.workflow.AbstractAssetDescriptionEntity;
import org.rudi.microservice.strukture.core.common.SchemaConstants;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "linked_producer", schema = SchemaConstants.DATA_SCHEMA, uniqueConstraints = {
		@UniqueConstraint(columnNames = {
			LinkedProducerEntity.ORGANIZATION_FK,
			LinkedProducerEntity.PROVIDER_FK
		})
})
@Getter
@Setter
public class LinkedProducerEntity extends AbstractAssetDescriptionEntity {

	public static final String FIELD_ID = "id";
	public static final String FIELD_UUID = "uuid";
	public static final String FIELD_ORGANIZATION = "organization";

	public static final String PROVIDER_FK = "provider_fk";
	public static final String ORGANIZATION_FK = "organization_fk";

	@Column(name = "linked_producer_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private LinkedProducerStatus linkedProducerStatus = LinkedProducerStatus.DRAFT;

	@ManyToOne
	@JoinColumn(name = ORGANIZATION_FK)
	private OrganizationEntity organization;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		final LinkedProducerEntity that = (LinkedProducerEntity) o;
		return Objects.equals(organization, that.organization)
				&& Objects.equals(linkedProducerStatus, that.linkedProducerStatus);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), organization, linkedProducerStatus);
	}
}
