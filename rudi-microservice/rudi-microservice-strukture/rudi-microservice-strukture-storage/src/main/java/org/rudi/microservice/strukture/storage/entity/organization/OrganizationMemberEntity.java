package org.rudi.microservice.strukture.storage.entity.organization;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@ToString
public class OrganizationMemberEntity implements Serializable {

	public static final String FIELD_ID = "id";
	public static final String FIELD_USER_UUID = "userUuid";
	public static final String FIELD_ROLE = "role";

	private static final long serialVersionUID = 7622950489139578791L;

	@NotNull
	private UUID userUuid;
	@NotNull
	@Enumerated(EnumType.STRING)
	private OrganizationRole role;
	@NotNull
	private LocalDateTime addedDate;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getUserUuid() == null) ? 0 : getUserUuid().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof OrganizationMemberEntity)) {
			return false;
		}
		OrganizationMemberEntity other = (OrganizationMemberEntity) obj;
		if (getUserUuid() == null) {
			if (other.getUserUuid() != null) {
				return false;
			}
		} else if (!getUserUuid().equals(other.getUserUuid())) {
			return false;
		}
		return true;
	}
}
