package org.rudi.microservice.projekt.core.bean.criteria;

import java.util.List;
import java.util.UUID;

import org.rudi.common.core.bean.criteria.SearchCriteria;
import org.rudi.microservice.projekt.core.bean.ProjectStatus;
import org.rudi.microservice.projekt.core.bean.TargetAudience;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString
public class ProjectSearchCriteria implements SearchCriteria {
	private List<String> themes;
	private List<String> keywords;
	private List<@Valid TargetAudience> targetAudiences;
	private List<UUID> datasetUuids;
	private List<UUID> linkedDatasetUuids;
	private List<UUID> ownerUuids;
	private List<UUID> projectUuids;
	private List<ProjectStatus> status;
	private Boolean isPrivate;
}
