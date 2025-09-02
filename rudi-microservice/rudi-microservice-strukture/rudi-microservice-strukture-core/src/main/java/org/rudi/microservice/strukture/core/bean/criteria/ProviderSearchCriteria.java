package org.rudi.microservice.strukture.core.bean.criteria;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author FNI18300
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ProviderSearchCriteria {

	private String code;

	private String label;

	private List<UUID> nodeProviderUuid;

	private Boolean active;

	private Boolean full;

	private LocalDateTime dateDebut;

	private LocalDateTime dateFin;

	private List<UUID> organisationUuid;

}
