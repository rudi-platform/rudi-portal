package org.rudi.microservice.projekt.storage.entity.relatedorganization;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.rudi.common.storage.entity.AbstractLongIdEntity;
import org.rudi.microservice.projekt.core.common.SchemaConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "related_organization", schema = SchemaConstants.DATA_SCHEMA)
@Getter
@Setter
@ToString()
public class RelatedOrganizationEntity extends AbstractLongIdEntity {

	private static final long serialVersionUID = -6508639499690690580L;
	public static final String FIELD_ORGANIZATION_UUID = "organizationUuid";
	public static final String FIELD_RELATION_STATUS = "relationStatus";
	public static final String FIELD_ID = "id";
	public static final String PROJECT_FK = "project_fk";

	@Column(name = "organization_uuid")
	private UUID organizationUuid;

	@Column(name = "relation_status", length = 50)
	@Enumerated(EnumType.STRING)
	private RelationStatus relationStatus = RelationStatus.PENDING;

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
}
