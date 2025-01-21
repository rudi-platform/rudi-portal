package org.rudi.microservice.projekt.core.bean.criteria;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.rudi.common.core.bean.criteria.AbstractSearchCriteria;
import org.rudi.microservice.projekt.core.bean.ProjectStatus;
import org.rudi.microservice.projekt.core.bean.TargetAudience;

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
public class ProjectSearchCriteria extends AbstractSearchCriteria {
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
