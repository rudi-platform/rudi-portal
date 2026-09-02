package org.rudi.microservice.strukture.storage.entity.organization;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.locationtech.jts.geom.Geometry;
import org.rudi.facet.bpmn.entity.workflow.AbstractAssetDescriptionEntity;
import org.rudi.microservice.strukture.core.common.SchemaConstants;
import org.rudi.microservice.strukture.storage.entity.address.AbstractAddressEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * <ul>
 * <li>Sa photo peut être déposée dans le Dataverse identique à celui utilisé pour les images des Providers</li>
 * </ul>
 */
@Entity
@Table(name = "organization", schema = SchemaConstants.DATA_SCHEMA)
@Getter
@Setter
public class OrganizationEntity extends AbstractAssetDescriptionEntity {

	public static final String FIELD_NAME = "name";
	public static final String FIELD_MEMBERS = "members";
	public static final String FIELD_ORGANIZATION_STATUS = "organizationStatus";
	public static final String FIELD_STATUS = "status";
	public static final String FIELD_ADDRESS = "address";
	public static final String FIELD_POSITION = "position";
	public static final String FIELD_ADDRESSES = "addresses";
	public static final String FIELD_DESCRIPTION = "description";
	public static final String FIELD_CREATION_DATE = "creationDate";
	public static final String FIELD_UPDATED_DATE = "updatedDate";
	public static final String FIELD_DATA = "data";

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

	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "organization_address", schema = SchemaConstants.DATA_SCHEMA, joinColumns = @JoinColumn(name = "organization_fk"), inverseJoinColumns = @JoinColumn(name = "address_fk"))
	private Set<AbstractAddressEntity> addresses = new HashSet<>();

	/**
	 * Membres
	 */
	@ElementCollection
	@OnDelete(action = OnDeleteAction.CASCADE)
	@CollectionTable(name = "organization_member", schema = SchemaConstants.DATA_SCHEMA, joinColumns = @JoinColumn(name = "organization_fk"))
	private Set<OrganizationMemberEntity> members = new HashSet<>();

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), name);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof OrganizationEntity))
			return false;
		if (!super.equals(o))
			return false;
		final OrganizationEntity that = (OrganizationEntity) o;
		return Objects.equals(name, that.name);
	}

	@Override
	public String toString() {
		return "OrganizationEntity{" + "name='" + name + '\'' + '}';
	}
}
