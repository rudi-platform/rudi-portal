package org.rudi.microservice.strukture.storage.entity.address;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.rudi.microservice.strukture.core.common.SchemaConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * PostalAddress Entity
 */

@Entity
@Table(name = "postal_address", schema = SchemaConstants.DATA_SCHEMA)
@Getter
@Setter
@ToString
public class PostalAddressEntity extends AbstractAddressEntity {

	private static final long serialVersionUID = 5928492308899757432L;

	@Column(name = "recipientIdentification")
	private String recipientIdentification;

	@Column(name = "additionalIdentification")
	private String additionalIdentification;

	@Column(name = "streetNumber")
	private String streetNumber;

	@Column(name = "distributionService")
	private String distributionService;

	@Column(name = "locality")
	private String locality;

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * super.hashCode();
	}

	@Override
	@SuppressWarnings("java:S3776") // méthode générée
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		return obj instanceof PostalAddressEntity;
	}

}
