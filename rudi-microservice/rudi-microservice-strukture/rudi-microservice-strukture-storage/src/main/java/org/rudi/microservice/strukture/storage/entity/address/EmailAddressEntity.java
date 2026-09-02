package org.rudi.microservice.strukture.storage.entity.address;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.rudi.microservice.strukture.core.common.SchemaConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * EmailAddress Entity
 */

@Entity
@Table(name = "email_address", schema = SchemaConstants.DATA_SCHEMA)
@Getter
@Setter
@ToString
public class EmailAddressEntity extends AbstractAddressEntity {

	private static final long serialVersionUID = 9099267916742316242L;

	@Column(name = "email", length = 150, nullable = false)
	private String email;

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
		return obj instanceof EmailAddressEntity;
	}

}
