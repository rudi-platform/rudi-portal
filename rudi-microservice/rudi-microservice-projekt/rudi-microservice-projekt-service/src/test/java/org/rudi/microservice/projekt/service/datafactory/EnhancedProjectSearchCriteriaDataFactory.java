package org.rudi.microservice.projekt.service.datafactory;

import java.util.List;
import java.util.UUID;

import org.rudi.microservice.projekt.core.bean.criteria.EnhancedProjectSearchCriteria;
import org.rudi.microservice.projekt.core.bean.criteria.ProjectSearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class EnhancedProjectSearchCriteriaDataFactory {

	public EnhancedProjectSearchCriteria createFromProjectSearchCriteria(ProjectSearchCriteria projectSearchCriteria) {
		return new EnhancedProjectSearchCriteria(projectSearchCriteria);
	}

	public EnhancedProjectSearchCriteria createWithProjectUuids(List<UUID> projectUuids) {
		return EnhancedProjectSearchCriteria.builder().projectUuids(projectUuids).build();
	}

}
