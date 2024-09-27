package org.rudi.microservice.strukture.service.config;

import static org.rudi.microservice.strukture.service.constant.BeanIds.STRUKTURE_CACHE_MANAGER;
import static org.rudi.microservice.strukture.service.constant.BeanIds.STRUKTURE_RESOURCES_CACHE;

import java.net.URL;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.rudi.common.core.DocumentContent;
import org.rudi.common.service.exception.AppServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SuppressWarnings({ "java:S2095", "java:S1075" })
@EnableCaching
public class StruktureResourceCacheConfig {
	private static final String CONFIG_FILE_PATH = "/cache/ehcache_strukture_config.xml";

	@Bean(name = STRUKTURE_CACHE_MANAGER)
	public org.ehcache.CacheManager ehCacheManager() throws AppServiceException {
		final URL myUrl = getClass().getResource(CONFIG_FILE_PATH);
		if (myUrl == null) {
			throw new AppServiceException(
					String.format("Impossible de trouver le fichier de configuration EhCache : %s", CONFIG_FILE_PATH));
		}
		XmlConfiguration xmlConfig = new XmlConfiguration(myUrl);
		org.ehcache.CacheManager cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
		cacheManager.init();
		return cacheManager;
	}

	@Bean(name = STRUKTURE_RESOURCES_CACHE)
	public Cache<String, DocumentContent> struktureResourceCache(
			@Qualifier(STRUKTURE_CACHE_MANAGER) CacheManager cacheManager) throws AppServiceException {
		Cache<String, DocumentContent> cache = cacheManager.getCache(STRUKTURE_RESOURCES_CACHE, String.class,
				DocumentContent.class);
		if (cache == null) {
			throw new AppServiceException("Erreur lors de la configuration du cache de resources de strukture");
		}

		return cache;
	}
}
