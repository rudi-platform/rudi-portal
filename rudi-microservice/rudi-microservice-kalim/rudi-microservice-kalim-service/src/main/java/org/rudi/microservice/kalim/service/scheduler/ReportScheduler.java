/**
 * RUDI Portail
 */
package org.rudi.microservice.kalim.service.scheduler;

import java.util.Arrays;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.microservice.kalim.core.bean.IntegrationRequest;
import org.rudi.microservice.kalim.core.bean.IntegrationRequestSearchCriteria;
import org.rudi.microservice.kalim.core.bean.ProgressStatus;
import org.rudi.microservice.kalim.service.integration.IntegrationRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler pour la prise en compte des éléments traités et dont le rapport doit être envoyé
 * 
 * @author FNI18300
 *
 */
@Component
@Slf4j
public class ReportScheduler {

	@Autowired
	private IntegrationRequestService integrationRequestService;

	@Scheduled(fixedDelayString = "${rudi.kalim.scheduler.sendReport.delay}")
	public void handleIntegrationRequestReport() {
		log.info("Start ReportScheduler...");
		IntegrationRequestSearchCriteria searchCriteria = new IntegrationRequestSearchCriteria();
		searchCriteria.setProgressStatus(Arrays.asList(ProgressStatus.INTEGRATION_HANDLED));
		Page<IntegrationRequest> integrationRequests = integrationRequestService
				.searchIntegrationRequests(searchCriteria, Pageable.unpaged());

		if (CollectionUtils.isNotEmpty(integrationRequests.getContent())) {
			log.info("ReportScheduler nothing find {}", integrationRequests.getTotalElements());
			for (IntegrationRequest integrationRequest : integrationRequests) {
				try {
					integrationRequestService.handleIntegrationRequest(integrationRequest.getUuid());
				} catch (Exception e) {
					log.info("ReportScheduler skip {} - {}", integrationRequest.getUuid(), e.getMessage());
				}
			}
		} else {
			log.info("TreatmentScheduler nothing to do.");
		}

		log.info("ReportScheduler done.");
	}
}
