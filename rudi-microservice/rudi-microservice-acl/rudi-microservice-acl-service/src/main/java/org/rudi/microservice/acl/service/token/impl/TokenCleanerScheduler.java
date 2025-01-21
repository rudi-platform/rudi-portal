/**
 * RUDI
 */
package org.rudi.microservice.acl.service.token.impl;

import org.rudi.microservice.acl.service.token.TokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 *
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TokenCleanerScheduler {

	/**
	 * Default delay for scheduler
	 */
	public static final long DEFAULT_DELAY = 60000L;

	private final TokenService tokenService;

	@Scheduled(fixedDelayString = "${security.token.cleaner.delay:" + DEFAULT_DELAY + "}")
	public void scheduleTokenCleaner() {
		log.info("Start {}...", getClass().getSimpleName());
		tokenService.cleanup();
		log.info("Start {} done.", getClass().getSimpleName());
	}

}
