package org.rudi.microservice.acl.service.scheduler.account;

import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.microservice.acl.service.account.AccountService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserAccountDeletionScheduler {

	public static final long DEFAULT_DELAY = 86400000L;
	private final AccountService accountService;

	@Scheduled(fixedDelayString = "${rudi.acl.scheduler.account-deletion-warning.delay:" + DEFAULT_DELAY + "}")
	public void deleteUserAccount() {
		log.info("Start {}...", getClass().getSimpleName());
		log.info("Deleting inactive user account");
		accountService.deleteInactiveUsersAccount();
		log.info("Deleting inactive user account done.");
		log.info("{} done.", getClass().getSimpleName());
	}
}
