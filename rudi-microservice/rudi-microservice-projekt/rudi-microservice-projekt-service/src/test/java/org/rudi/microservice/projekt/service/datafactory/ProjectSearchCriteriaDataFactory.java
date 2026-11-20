package org.rudi.microservice.projekt.service.datafactory;

import java.util.List;
import java.util.UUID;

import org.rudi.microservice.projekt.core.bean.ProjectStatus;
import org.rudi.microservice.projekt.core.bean.RelationStatus;
import org.rudi.microservice.projekt.core.bean.criteria.ProjectSearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class ProjectSearchCriteriaDataFactory {

	public ProjectSearchCriteria createEmpty() {
		return new ProjectSearchCriteria();
	}

	public ProjectSearchCriteria createWithThemes(List<String> themes) {
		return ProjectSearchCriteria.builder().themes(themes).build();
	}

	public ProjectSearchCriteria createWithKeywords(List<String> keywords) {
		return ProjectSearchCriteria.builder().keywords(keywords).build();
	}

	public ProjectSearchCriteria createWithTitleOrDescription(String titleOrDescription) {
		return ProjectSearchCriteria.builder().titleOrDescription(titleOrDescription).build();
	}

	public ProjectSearchCriteria createWithOwnerUuids(List<UUID> ownerUuids) {
		return ProjectSearchCriteria.builder().ownerUuids(ownerUuids).build();
	}

	public ProjectSearchCriteria createWithProjectStatusAndOwnerUuids(List<ProjectStatus> projectStatus,
			List<UUID> ownerUuids) {
		ProjectSearchCriteria criteria = new ProjectSearchCriteria();
		criteria.setProjectStatus(projectStatus);
		criteria.setOwnerUuids(ownerUuids);
		return criteria;
	}

	public ProjectSearchCriteria createWithProjectStatusRelationStatusAndRelatedOrganizationUuids(
			List<ProjectStatus> projectStatus, List<RelationStatus> relationStatus,
			List<UUID> relatedOrganizationUuids) {
		ProjectSearchCriteria criteria = new ProjectSearchCriteria();
		criteria.setProjectStatus(projectStatus);
		criteria.setRelationStatus(relationStatus);
		criteria.setRelatedOrganizationUuids(relatedOrganizationUuids);
		return criteria;
	}

}
