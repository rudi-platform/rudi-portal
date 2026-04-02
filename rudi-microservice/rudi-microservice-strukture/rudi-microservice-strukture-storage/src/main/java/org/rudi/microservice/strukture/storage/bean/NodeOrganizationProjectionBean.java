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

	private static final int PRODUCER_ID_INDEX = 0;
	private static final int LINKED_PRODUCER_STATUS_INDEX = 1;
	private static final int LINKED_STATUS_INDEX = 2;
	private static final int PARTS_COUNT = 3;

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

	private Status linkedStatus;

	// Id du producteur / peut être null
	private UUID producerId;

	private String data;

	@SuppressWarnings("java:S107") // Constructeur appelé par JPA avec tous les paramètres
	public NodeOrganizationProjectionBean(UUID organizationUuid, String organizationName,
			OrganizationStatus organizationStatus, Status status, LocalDateTime organizationOpeningDate,
			LocalDateTime organizationClosingDate, String organizationDescription, String organizationUrl,
			String organizationAddress, LocalDateTime creationDate, LocalDateTime updatedDate, String data,
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

		this.data = data;

		if (StringUtils.isNotBlank(providerUuidAndLinkedProvidersStatus)) {

			String[] parts = StringUtils.split(providerUuidAndLinkedProvidersStatus,
					RepositoryConstants.QUERY_ITEM_SEPARATOR);

			if (parts.length == PARTS_COUNT) {
				this.producerId = UUID.fromString(parts[PRODUCER_ID_INDEX]);
				this.linkedProducerStatus = LinkedProducerStatus.valueOf(parts[LINKED_PRODUCER_STATUS_INDEX]);
				this.linkedStatus = Status.valueOf(parts[LINKED_STATUS_INDEX]);
			}
		}

	}

}
