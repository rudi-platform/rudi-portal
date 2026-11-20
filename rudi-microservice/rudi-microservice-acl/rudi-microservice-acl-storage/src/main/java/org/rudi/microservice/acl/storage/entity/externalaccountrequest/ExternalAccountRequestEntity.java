package org.rudi.microservice.acl.storage.entity.externalaccountrequest;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import org.rudi.facet.bpmn.entity.workflow.AbstractAssetDescriptionEntity;
import org.rudi.microservice.acl.core.common.SchemaConstants;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Demande de création de compte dans le système d'authentification externe (LDAP), soumise à validation d'un animateur
 */
@Entity
@Table(name = "external_account_request", schema = SchemaConstants.DATA_SCHEMA)
@Getter
@Setter
@ToString
public class ExternalAccountRequestEntity extends AbstractAssetDescriptionEntity {

	private static final long serialVersionUID = -6508639499690690561L;

	@Column(name = "lastname", length = 30)
	private String lastname;

	@Column(name = "firstname", length = 30)
	private String firstname;

	@Column(name = "email", length = 255, nullable = false)
	private String email;

	@Column(name = "organization_uuid")
	private UUID organizationUuid;

	@Column(name = "message", length = 1000)
	private String message;

	@Column(name = "identity_provider", length = 100)
	private String identityProvider;

	@Column(name = "external_account_request_status", length = 20, nullable = false)
	@Enumerated(EnumType.STRING)
	private ExternalAccountRequestStatus externalAccountRequestStatus = ExternalAccountRequestStatus.PENDING;

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ExternalAccountRequestEntity)) {
			return false;
		}
		return super.equals(obj);
	}

}
