package org.rudi.microservice.strukture.storage.entity.address;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.rudi.microservice.strukture.core.common.SchemaConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * WebsiteAddress Entity
 */

@Entity
@Table(name = "web_site_address", schema = SchemaConstants.DATA_SCHEMA)
@Getter
@Setter
@ToString
public class WebsiteAddressEntity extends AbstractAddressEntity {

	public static final String FIELD_URL = "url";

	private static final long serialVersionUID = 6532289280799661093L;

	@Column(name = "url", length = 1024, nullable = false)
	private String url;

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
		return obj instanceof WebsiteAddressEntity;
	}

}
