package org.rudi.microservice.projekt.service.project.impl.fields;

import javax.annotation.Nullable;

import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.util.SanitizerUtils;
import org.rudi.facet.bpmn.service.impl.AbstractAssetDescriptionAssetListener;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
class DescriptionProcessor extends AbstractAssetDescriptionAssetListener<ProjectEntity>
		implements CreateProjectFieldProcessor, UpdateProjectFieldProcessor {

	@Autowired
	private SanitizerUtils sanitizerUtils;

	@Override
	public void process(@Nullable ProjectEntity project, ProjectEntity existingProject) throws AppServiceException {
		if (project == null) {
			return;
		}

		final String description = project.getDescription();
		project.setDescription(sanitizerUtils.cleanupHtml(description));
	}

	@Override
	public void beforeCreate(ProjectEntity projectEntity) {
		if (projectEntity == null) {
			return;
		}

		final String description = projectEntity.getDescription();
		projectEntity.setDescription(sanitizerUtils.cleanupHtml(description));

	}

}
