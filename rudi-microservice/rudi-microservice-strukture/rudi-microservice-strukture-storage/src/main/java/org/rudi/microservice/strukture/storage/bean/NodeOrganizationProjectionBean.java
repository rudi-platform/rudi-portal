package org.rudi.microservice.strukture.storage.bean;

import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.storage.dao.RepositoryConstants;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerStatus;
import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeOrganizationProjectionBean {

	// Infos de l'organisation
	private UUID organizationId;

	private String organizationName;

	private OrganizationStatus organizationStatus;

	private Status status;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime organizationOpeningDate;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime organizationClosingDate;

	private String organizationSummary;

	private String organizationUrl;

	private String organizationAddress;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime created;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private LocalDateTime modified;

	// Statut du lien avec le producteur
	private LinkedProducerStatus linkedProducerStatus;

	// Id du producteur / peut être null
	private UUID producerId;

	@SuppressWarnings("java:S107") // Constructeur appelé par JPA avec tous les paramètres
	public NodeOrganizationProjectionBean(UUID organizationUuid, String organizationName,
			OrganizationStatus organizationStatus, Status status, LocalDateTime organizationOpeningDate,
			LocalDateTime organizationClosingDate, String organizationDescription, String organizationUrl,
			String organizationAddress, LocalDateTime creationDate, LocalDateTime updatedDate,
			String providerUuidAndLinkedProvidersStatus) {

		this.organizationId = organizationUuid;
		this.organizationName = organizationName;

		this.organizationStatus = organizationStatus;
		this.status = status;

		this.organizationOpeningDate = organizationOpeningDate;
		this.organizationClosingDate = organizationClosingDate;
		this.organizationSummary = organizationDescription;
		this.organizationUrl = organizationUrl;
		this.organizationAddress = organizationAddress;

		this.created = creationDate;
		this.modified = updatedDate;

		if (StringUtils.isNotBlank(providerUuidAndLinkedProvidersStatus)) {

			String[] parts = StringUtils.split(providerUuidAndLinkedProvidersStatus,
					RepositoryConstants.QUERY_ITEM_SEPARATOR);

			if (parts.length == 2) {
				this.producerId = UUID.fromString(parts[0]);
				this.linkedProducerStatus = LinkedProducerStatus.valueOf(parts[1]);
			}
		}

	}

}
