package org.rudi.microservice.projekt.service.datafactory;

import java.util.UUID;

import org.rudi.microservice.projekt.core.bean.ProjectKeySearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class ProjectKeySearchCriteriaDataFactory {

	public ProjectKeySearchCriteria createEmpty() {
		return new ProjectKeySearchCriteria();
	}

	public ProjectKeySearchCriteria createWithProjectUuid(UUID projectUuid) {
		return new ProjectKeySearchCriteria().projectUuid(projectUuid);
	}

}
