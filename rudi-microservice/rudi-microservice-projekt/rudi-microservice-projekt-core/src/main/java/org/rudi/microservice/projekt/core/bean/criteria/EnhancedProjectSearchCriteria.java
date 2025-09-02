package org.rudi.microservice.projekt.core.bean.criteria;

import java.util.List;
import java.util.UUID;

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
public class EnhancedProjectSearchCriteria extends ProjectSearchCriteria {
	List<UUID> myOrganizationsUuids;
	List<UUID> myOrganizationsDatasetsUuids;

	public EnhancedProjectSearchCriteria(ProjectSearchCriteria projectSearchCriteria) {
		this.setThemes(projectSearchCriteria.getThemes());
		this.setKeywords(projectSearchCriteria.getKeywords());
		this.setTargetAudiences(projectSearchCriteria.getTargetAudiences());
		this.setDatasetUuids(projectSearchCriteria.getDatasetUuids());
		this.setLinkedDatasetUuids(projectSearchCriteria.getLinkedDatasetUuids());
		this.setOwnerUuids(projectSearchCriteria.getOwnerUuids());
		this.setProjectUuids(projectSearchCriteria.getProjectUuids());
		this.setProjectStatus(projectSearchCriteria.getProjectStatus());
		this.setIsPrivate(projectSearchCriteria.getIsPrivate());
	}

	public ProjectSearchCriteria getProjectSearchCriteria(){
		return ProjectSearchCriteria
				.builder()
				.themes(this.getThemes())
				.keywords(this.getKeywords())
				.targetAudiences(this.getTargetAudiences())
				.datasetUuids(this.getDatasetUuids())
				.linkedDatasetUuids(this.getLinkedDatasetUuids())
				.ownerUuids(this.getOwnerUuids())
				.projectUuids(this.getProjectUuids())
				.projectStatus(this.getProjectStatus())
				.isPrivate(this.getIsPrivate())
				.build();
	}
}
