package org.rudi.microservice.strukture.storage.entity.address;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.rudi.microservice.strukture.core.common.SchemaConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * TelephoneAddress Entity
 */

@Entity
@Table(name = "telephone_address", schema = SchemaConstants.DATA_SCHEMA)
@Getter
@Setter
@ToString
public class TelephoneAddressEntity extends AbstractAddressEntity {

	private static final long serialVersionUID = 6990556660409228821L;

	@Column(name = "phone_number", length = 20, nullable = false)
	private String phoneNumber;

	@Override
	public int hashCode() {
		final int prime = 31;
		return prime * super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		return obj instanceof TelephoneAddressEntity;
	}

}
