package org.rudi.microservice.projekt.service.helper.project.processor.impl;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.ACCESS_URL_FIELD_NAME;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.rudi.microservice.projekt.service.helper.project.processor.AbstractProjectTastUpdateProjectStringFieldProcessor;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProjectTaskUpdateProjectAccessUrlProcessor extends AbstractProjectTastUpdateProjectStringFieldProcessor {

	private static final List<String> ACCEPTED_SCHEMES = List.of("https", "http", "ftp");

	public ProjectTaskUpdateProjectAccessUrlProcessor() {
		super(ACCESS_URL_FIELD_NAME);
	}

	/**
	 * @param value
	 * @param projectEntity
	 */
	@Override
	protected void assignEntities(Object value, ProjectEntity projectEntity) {
		if (value == null) {
			projectEntity.setAccessUrl(null);
		} else if (validateUrl(value.toString())) {
			projectEntity.setAccessUrl(value.toString());
		}
	}

	private boolean validateUrl(String url) {
		if (StringUtils.isEmpty(url)) {
			return false;
		}

		try {
			URI uri = new URI(url);
			return ACCEPTED_SCHEMES.contains(uri.getScheme());
		} catch (URISyntaxException e) {
			log.error(String.format("URL non valide : %s. Schemas autorisés : %s", url,
					StringUtils.join(ACCEPTED_SCHEMES, ", ")));
			return false;
		}
	}
}
