package org.rudi.common.storage.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.rudi.common.core.LongId;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * @author FNI18300
 */
@MappedSuperclass
@Setter
@Getter
@ToString
@NoArgsConstructor
@SuperBuilder
public abstract class AbstractLongIdEntity implements LongId, Serializable {

	private static final long serialVersionUID = 7110297013421909363L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "uuid", nullable = false, unique = true)
	private UUID uuid;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AbstractLongIdEntity)) {
			return false;
		}
		AbstractLongIdEntity other = (AbstractLongIdEntity) obj;
		return (getId() != null && getId().equals(other.getId()));
	}
}
