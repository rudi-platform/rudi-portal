package org.rudi.common.facade.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RudiApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

	/**
	 * Methode appelée à l'initilisation de l'application SpringBoot
	 *
	 * @param event
	 */
	@Override
	public void onApplicationEvent(final ApplicationReadyEvent event) {
		if (log.isDebugEnabled()) {
			log.debug("Args: {} {}", event.getArgs(), event.getSpringApplication().getWebApplicationType());
		}
	}

}
