package org.rudi.microservice.selfdata.facade.controller;

import java.util.UUID;

import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.microservice.selfdata.facade.controller.api.NotifyApi;
import org.rudi.microservice.selfdata.service.notifications.NotifyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import static org.rudi.common.core.security.QuotedRoleCodes.ADMINISTRATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.MODULE_ACL;

@RestController
@RequiredArgsConstructor
public class NotifyController implements NotifyApi {

	private final NotifyService notifyService;

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODULE_ACL + ")")
	public ResponseEntity<Void> handleUserDeactivation(String login, UUID userUuid) throws FormDefinitionException, InvalidDataException {
		notifyService.handleUserTasks(login, userUuid);
		return ResponseEntity.noContent().build();
	}
}
