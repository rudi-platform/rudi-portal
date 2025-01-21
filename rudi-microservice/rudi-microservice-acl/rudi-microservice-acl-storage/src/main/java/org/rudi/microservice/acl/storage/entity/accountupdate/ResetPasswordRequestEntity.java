package org.rudi.microservice.acl.storage.entity.accountupdate;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.rudi.common.storage.entity.AbstractLongIdEntity;
import org.rudi.microservice.acl.core.common.SchemaConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "reset_password_request", schema = SchemaConstants.DATA_SCHEMA)
@Getter
@Setter
@ToString
public class ResetPasswordRequestEntity extends AbstractLongIdEntity implements HasToken {

	private static final long serialVersionUID = 7354181789184122503L;

	@Column(name = "user_uuid", nullable = false)
	private UUID userUuid;

	@Column(name = "token", nullable = false)
	private UUID token;

	@Column(name = "creation_date", nullable = false)
	private LocalDateTime creationDate;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		final ResetPasswordRequestEntity that = (ResetPasswordRequestEntity) o;
		return Objects.equals(getUuid(), that.getUuid());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getUuid());
	}
}
