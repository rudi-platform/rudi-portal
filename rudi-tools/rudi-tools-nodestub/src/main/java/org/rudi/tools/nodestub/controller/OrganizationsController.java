package org.rudi.tools.nodestub.controller;

import java.util.UUID;

import org.rudi.microservice.strukture.core.bean.Report;
import org.rudi.tools.nodestub.controller.api.TestApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class OrganizationsController implements TestApi {
	/**
	 * PUT /test/api/v1/organizations/{organizationUuid}/report : Réception du rapport d&#39;intégration de la création d&#39;une organization sur un nom d&#39;url /nodestub/test
	 *
	 * @param organizationUuid (required)
	 * @param report             (optional)
	 * @return ok (status code 204)
	 */
	@Override
	public ResponseEntity<Void> recieveReport(UUID organizationUuid, Report report) throws Exception {
		if (report == null) {
			log.error("ATTENTION - Le rapport ne doit pas être vide");
		}
		else {
			log.info("Le rapport n'est pas vide : {}", report);
		}
		return ResponseEntity.noContent().build();
	}
}
