/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.service.scheduler.account;

import org.rudi.microservice.acl.service.account.AccountService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler pour envoyer un avertissement aux utilisateurs concernant la suppression imminente de leur compte
 * en raison d'inactivité.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserAccountDeletionWarningScheduler {
	/**
	 * Default delay for scheduler (24 hours = 86400000 milliseconds)
	 */
	public static final long DEFAULT_DELAY = 86400000L;

	private final AccountService accountService;

	@Scheduled(fixedDelayString = "${rudi.acl.scheduler.account-deletion-warning.delay:" + DEFAULT_DELAY + "}")
	public void sendAccountDeletionWarning() {
		log.info("Start {}...", getClass().getSimpleName());
		accountService.sendAccountDeletionWarnings();
		log.info("{} done.", getClass().getSimpleName());
	}
}



