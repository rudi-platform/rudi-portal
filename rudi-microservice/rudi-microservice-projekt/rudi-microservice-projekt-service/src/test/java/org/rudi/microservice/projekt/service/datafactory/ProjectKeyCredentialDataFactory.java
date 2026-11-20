package org.rudi.microservice.projekt.service.datafactory;

import java.time.LocalDateTime;

import org.rudi.facet.acl.bean.ProjectKey;
import org.rudi.microservice.projekt.core.bean.ProjectKeyCredential;
import org.springframework.stereotype.Component;

@Component
public class ProjectKeyCredentialDataFactory {

	public ProjectKeyCredential createProjectKeyCredential(String name) {
		ProjectKey projectKey = new ProjectKey();
		projectKey.setName(name);
		projectKey.setExpirationDate(LocalDateTime.now());
		return new ProjectKeyCredential().projectKey(projectKey).password("pwd" + name);
	}

}
