package org.rudi.microservice.konsult.service.robots.impl;

import java.io.IOException;

import org.rudi.common.core.DocumentContent;
import org.rudi.microservice.konsult.service.helper.robots.RobotsHelper;
import org.rudi.microservice.konsult.service.robots.RobotsService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RobotsServiceImpl implements RobotsService {

	private final RobotsHelper robotsHelper;

	@Override
	public DocumentContent getRobotsRessourceByName(String resourceName) throws IOException {
		return robotsHelper.loadResources(resourceName);
	}
}
