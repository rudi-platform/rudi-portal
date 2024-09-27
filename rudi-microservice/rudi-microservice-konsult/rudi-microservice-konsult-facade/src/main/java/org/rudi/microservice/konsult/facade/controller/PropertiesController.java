package org.rudi.microservice.konsult.facade.controller;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.rudi.common.core.DocumentContent;
import org.rudi.common.facade.helper.ControllerHelper;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.helper.ResourceHelper;
import org.rudi.microservice.konsult.core.bean.FrontOfficeProperties;
import org.rudi.microservice.konsult.facade.controller.api.PropertiesApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RestController
@RequiredArgsConstructor
public class PropertiesController implements PropertiesApi {
	private static final String FRONT_OFFICE_JSON = "konsult-front-office.json";

	private final ResourceHelper resourceHelper;
	private final ObjectMapper objectMapper;
	private final ControllerHelper controllerHelper;

	@Value("${front.teamName:Rudi}")
	private String teamName;

	@Value("${front.projectName:Rudi}")
	private String projectName;

	@Override
	public ResponseEntity<FrontOfficeProperties> getFrontOfficeProperties() throws Exception {
		return getProperties(FRONT_OFFICE_JSON);
	}

	@Override
	public ResponseEntity<Resource> downloadScript(String scriptName) throws Exception {
		// filtrer les scripts à charger configurés dans FRONT_OFFICE_JSON et qui commencent par /konsult/v1/properties/scripts/
		// si accepté, return getResource(scriptName)

		FrontOfficeProperties frontOfficeProperties = getFrontOfficeProperties().getBody();
		if (frontOfficeProperties != null && frontOfficeProperties.getScripts().stream()
				.filter(a -> StringUtils.equals(a, "/konsult/v1/properties/scripts/" + scriptName)).count() > 0) {
			DocumentContent documentContent = resourceHelper.convertToDocumentContent(getResource(scriptName));
			return controllerHelper.downloadableResponseEntity(documentContent);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@Nonnull
	private ResponseEntity<FrontOfficeProperties> getProperties(String filename) throws AppServiceException {
		try {
			val resource = getResource(filename);
			val properties = objectMapper.readValue(resource.getInputStream(), FrontOfficeProperties.class);
			properties.getFront().setTeamName(teamName);
			properties.getFront().setProjectName(projectName);
			return ResponseEntity.ok(properties);
		} catch (IOException e) {
			throw new AppServiceException("Error loading " + filename, e);
		}
	}

	@Nonnull
	private Resource getResource(String filename) {
		return resourceHelper.getResourceFromAdditionalLocationOrFromClasspath(filename);
	}

}
