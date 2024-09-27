package org.rudi.microservice.konsult.facade.controller;

import org.rudi.common.core.DocumentContent;
import org.rudi.common.facade.helper.ControllerHelper;
import org.rudi.microservice.konsult.facade.controller.api.RobotsApi;
import org.rudi.microservice.konsult.service.robots.RobotsService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class RobotsController implements RobotsApi {
	private final ControllerHelper controllerHelper;
	private final RobotsService robotsService;

	@Override
	public ResponseEntity<Resource> getRobotsRessourceByName(String resourceName) throws Exception {
		DocumentContent documentContent = robotsService.getRobotsRessourceByName(resourceName);
		if (documentContent != null) {
			return controllerHelper.downloadableResponseEntity(documentContent);
		}
		return ResponseEntity.notFound().build();
	}
}
