/**
 * RUDI Portail
 */
package org.rudi.microservice.konsult.service.helper.robots;

import static org.rudi.microservice.konsult.service.constant.BeanIds.ROBOTS_RESOURCES_CACHE;

import org.ehcache.Cache;
import org.rudi.common.core.DocumentContent;
import org.rudi.common.core.resources.ResourcesHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * @author FNI18300
 */
@Component
public class RobotsHelper extends ResourcesHelper {

	private static final String ROBOTS_RESOURCE_KEY = "robots";
	private static final String ROBOTS_RESOURCE_FILENAME = "robots.txt";

	@Getter(AccessLevel.PUBLIC)
	@Value("${robots.base-package:robots}")
	private String basePackage;

	@Getter(AccessLevel.PUBLIC)
	@Value("${robots.base-directory:}")
	private String baseDirectory;

	@Getter(AccessLevel.PROTECTED)
	private final Cache<String, DocumentContent> cache;

	RobotsHelper(@Qualifier(ROBOTS_RESOURCES_CACHE) Cache<String, DocumentContent> cache) {
		this.cache = cache;
		fillResourceMapping(ROBOTS_RESOURCE_FILENAME, ROBOTS_RESOURCE_KEY);
	}

}
