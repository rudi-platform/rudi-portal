/**
 * RUDI Portail
 */
package org.rudi.microservice.strukture.service.helper;

import java.io.IOException;

import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import org.ehcache.Cache;
import org.rudi.common.core.DocumentContent;
import org.rudi.common.core.resources.ResourcesHelper;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.strukture.service.constant.BeanIds;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * @author FNI18300
 */
@Component
public class StruktureResourceHelper extends ResourcesHelper {

	private static final String DEFAULT_LOGO_RESOURCE_ID = "defaultLogo";

	@Value("${rudi.producer.default.logo:medias/rudi_picto_profil_user.png}")
	private String defaultLogoResource;

	@Getter(AccessLevel.PUBLIC)
	@Value("${strukture.base-package:strukture}")
	private String basePackage;

	@Getter(AccessLevel.PUBLIC)
	@Value("${strukture.base-directory:}")
	private String baseDirectory;

	@Getter(AccessLevel.PROTECTED)
	private final Cache<String, DocumentContent> cache;

	StruktureResourceHelper(@Qualifier(BeanIds.STRUKTURE_RESOURCES_CACHE) Cache<String, DocumentContent> cache) {
		this.cache = cache;
	}

	@PostConstruct
	void initResourceMapping() {
		fillResourceMapping(defaultLogoResource, DEFAULT_LOGO_RESOURCE_ID);
	}

	@Nonnull
	public DocumentContent getDefaultLogo() throws AppServiceException {
		try {
			return loadResources(DEFAULT_LOGO_RESOURCE_ID);
		} catch (IOException e) {
			throw new AppServiceException("Impossible de trouver le logo par défaut d'un producer", e);
		}
	}

}
