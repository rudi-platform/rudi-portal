/**
 * RUDI
 */
package org.rudi.microservice.acl.storage.entity.token;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import org.rudi.microservice.acl.core.bean.TokenType;
import org.rudi.microservice.acl.core.common.SchemaConstants;
import org.rudi.microservice.acl.storage.entity.user.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author FNI18300
 *
 */
@Entity
@Table(name = "token", schema = SchemaConstants.DATA_SCHEMA)
@Getter
@Setter
@ToString
public class TokenEntity implements Serializable {

	private static final long serialVersionUID = -1061569294666044902L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "registered_client_id", length = 255)
	private String registeredClientId;

	@Column(name = "authorization_grant_type", length = 255)
	private String authorizationGrantType;

	@Column(name = "attributes", length = 255)
	private String attributes;

	@Column(name = "state", length = 255)
	private String state;

	@Column(name = "type", length = 20, nullable = false)
	@Enumerated(EnumType.STRING)
	private TokenType type;

	@Column(name = "value_", length = 2048)
	private String value;

	@Column(name = "issued_at")
	private LocalDateTime issuedAt;

	@Column(name = "expires_at")
	private LocalDateTime expriresAt;

	@Column(name = "metadata", length = 255)
	private String metadata;

	@ManyToOne
	@JoinColumn(name = "user_fk")
	private UserEntity user;

	@Override
	public int hashCode() {
		return Objects.hash(id, type, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TokenEntity other = (TokenEntity) obj;
		return Objects.equals(id, other.id) && type == other.type && Objects.equals(value, other.value);
	}

}
