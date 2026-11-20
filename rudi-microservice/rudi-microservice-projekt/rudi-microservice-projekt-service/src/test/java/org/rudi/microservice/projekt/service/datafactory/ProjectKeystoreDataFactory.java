package org.rudi.microservice.projekt.service.datafactory;

import java.util.UUID;

import org.rudi.facet.acl.bean.ProjectKeystore;
import org.springframework.stereotype.Component;

@Component
public class ProjectKeystoreDataFactory {

	public ProjectKeystore createProjectKeystore(UUID projectUuid, UUID uuid) {
		return new ProjectKeystore().projectUuid(projectUuid).uuid(uuid);
	}

}
