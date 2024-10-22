package org.rudi.microservice.strukture.storage.entity.provider;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.rudi.facet.bpmn.entity.workflow.AbstractAssetDescriptionEntity;
import org.rudi.microservice.strukture.core.common.SchemaConstants;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "linked_producer", schema = SchemaConstants.DATA_SCHEMA)
@Getter
@Setter
public class LinkedProducerEntity extends AbstractAssetDescriptionEntity {

	public static final String PROVIDER_FK = "provider_fk";

	@Column(name = "linked_producer_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private LinkedProducerStatus linkedProducerStatus = LinkedProducerStatus.DRAFT;

	@ManyToOne
	@JoinColumn(name = "organization_fk")
	private OrganizationEntity organization;
}
