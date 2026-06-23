package org.rudi.microservice.projekt.service.project.impl.fields;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.Nullable;
import org.rudi.facet.bpmn.service.impl.AbstractAssetDescriptionAssetListener;
import org.rudi.microservice.projekt.storage.entity.OwnerType;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.stereotype.Component;

@Component
public class RelatedOrganizationsProcessor extends AbstractAssetDescriptionAssetListener<ProjectEntity>
		implements CreateProjectFieldProcessor {
	/**
	 * @param project to be created/updated
	 * @param existingProject nullable existing project in, database
	 */
	@Override
	public void process(@Nullable ProjectEntity project, @Nullable ProjectEntity existingProject) {
		if (project == null || CollectionUtils.isEmpty(project.getRelatedOrganizations())) {
			return;
		}

		if (OwnerType.ORGANIZATION.equals(project.getOwnerType())
				&& project.getRelatedOrganizations().stream().anyMatch(
						ro -> Objects.equals(ro.getOrganizationUuid(), project.getOwnerUuid()))) {
			throw new IllegalArgumentException("RelatedOrganization cannot be the owner of the project");
		}

		project.getRelatedOrganizations().forEach(ro -> ro.setUuid(UUID.randomUUID()));
	}
}

