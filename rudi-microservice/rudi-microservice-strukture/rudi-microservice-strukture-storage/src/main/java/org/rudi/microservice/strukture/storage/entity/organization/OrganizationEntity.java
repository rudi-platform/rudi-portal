package org.rudi.microservice.strukture.storage.entity.organization;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.locationtech.jts.geom.Geometry;
import org.rudi.facet.bpmn.entity.workflow.AbstractAssetDescriptionEntity;
import org.rudi.microservice.strukture.core.common.SchemaConstants;

import lombok.Getter;
import lombok.Setter;

/**
 * <ul>
 *     <li>Sa photo peut être déposée dans le Dataverse identique à celui utilisé pour les images des Providers</li>
 * </ul>
 */
@Entity
@Table(name = "organization", schema = SchemaConstants.DATA_SCHEMA)
@Getter
@Setter
public class OrganizationEntity extends AbstractAssetDescriptionEntity {

	public static final String FIELD_UUID = "uuid";
	public static final String FIELD_MEMBERS = "members";
	public static final String FIELD_ORGANIZATION_STATUS = "organizationStatus";

	private static final long serialVersionUID = -8031214852147803138L;

	@NotNull
	private String name;

	@Column(name = "organization_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private OrganizationStatus organizationStatus = OrganizationStatus.DRAFT;

	@Column(name = "address")
	private String address;

	@Column(name = "position", columnDefinition = "Geometry")
	private Geometry position;

	@NotNull
	private LocalDateTime openingDate;

	private LocalDateTime closingDate;

	@Column(name = "url", length = 80)
	private String url;

	/**
	 * Membres
	 */
	@ElementCollection
	@CollectionTable(name = "organization_member", schema = SchemaConstants.DATA_SCHEMA, joinColumns = @JoinColumn(name = "organization_fk"))
	private Set<OrganizationMemberEntity> members = new HashSet<>();

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OrganizationEntity)) return false;
		if (!super.equals(o)) return false;
		final OrganizationEntity that = (OrganizationEntity) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public String toString() {
		return "OrganizationEntity{" +
				"name='" + name + '\'' +
				'}';
	}
}
