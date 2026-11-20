package org.rudi.facet.acl.datafactory;

import java.util.List;
import java.util.UUID;

import org.rudi.facet.acl.helper.ProjectKeystoreSearchCriteria;
import org.springframework.stereotype.Component;

@Component
public class ProjectKeystoreSearchCriteriaDataFactory {

	public ProjectKeystoreSearchCriteria createForClientId(String clientId) {
		return ProjectKeystoreSearchCriteria.builder().clientId(clientId).build();
	}

	public ProjectKeystoreSearchCriteria createForProjectUuids(List<UUID> projectUuids) {
		return ProjectKeystoreSearchCriteria.builder().projectUuids(projectUuids).build();
	}

}
