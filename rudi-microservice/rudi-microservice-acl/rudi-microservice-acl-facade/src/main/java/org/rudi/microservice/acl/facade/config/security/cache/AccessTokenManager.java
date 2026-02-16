/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.cache;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.Status;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.xml.XmlConfiguration;
import org.rudi.common.facade.config.filter.Tokens;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author FNI18300
 *
 */
@Component
@Slf4j
public class AccessTokenManager {

	public static final String ACCESS_TOKEN_MANAGER_NAME = "accessTokenCacheManager";

	public static final String ACCESS_TOKEN_CACHE_NAME = "accessTokenCache";

	public static final String ACCESS_TOKEN_CACHE = "accessTokens";

	private static final String CONFIG_FILE_PATH = "acl-ehcache.xml";

	private CacheManager cacheManager;

	@Bean(name = ACCESS_TOKEN_MANAGER_NAME)
	public CacheManager cacheManager() {
		final URL myUrl = Thread.currentThread().getContextClassLoader().getResource(CONFIG_FILE_PATH);
		if (myUrl == null) {
			throw new IllegalArgumentException(
					String.format("Impossible de trouver le fichier de configuration EhCache : %s", CONFIG_FILE_PATH));
		}
		XmlConfiguration xmlConfig = new XmlConfiguration(myUrl, Thread.currentThread().getContextClassLoader());
		cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
		cacheManager.init();

		return cacheManager;

	}

	protected Cache<String, Tokens> cache() {
		try {
			Cache<String, Tokens> cache = cacheManager.getCache(ACCESS_TOKEN_CACHE, String.class, Tokens.class);
			if (cache == null) {
				throw new IllegalArgumentException("Erreur lors de la configuration du cache tokens");
			}
			return cache;
		} catch (Exception e) {
			// petite tambouille pour supporter les redémarrage springboot en mode
			// developpement
			cacheManager.removeCache(ACCESS_TOKEN_CACHE);
			ResourcePoolsBuilder resourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder().heap(50,
					EntryUnit.ENTRIES);
			CacheConfigurationBuilder<String, Tokens> builder = CacheConfigurationBuilder
					.newCacheConfigurationBuilder(String.class, Tokens.class, resourcePoolsBuilder)
					.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(15)));
			return cacheManager.createCache(ACCESS_TOKEN_CACHE, builder);
		}

	}

	@PreDestroy
	public void preDestroyCacheManager() {
		if (cacheManager != null && cacheManager.getStatus() != Status.UNINITIALIZED) {
			try {
				cacheManager.close();
			} catch (Exception e) {
				log.warn("Failed to destroy cache", e);
			}
			cacheManager = null;
		}
	}

	/**
	 * 
	 * @param accessToken
	 * @return les tokens associés à une clef
	 */
	public Tokens lookupTokens(String accessToken) {
		Tokens result = cache().get(accessToken);
		cache().remove(accessToken);
		return result;
	}

	/**
	 * Stoken un tokens
	 * 
	 * @param tokens
	 * @return la clef
	 */
	public String storeTokens(Tokens tokens) {
		String result = UUID.randomUUID().toString();
		cache().put(result, tokens);
		return result;
	}

}
