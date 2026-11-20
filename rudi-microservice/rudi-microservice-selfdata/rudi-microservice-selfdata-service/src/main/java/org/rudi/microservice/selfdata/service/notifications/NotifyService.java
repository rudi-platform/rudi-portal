package org.rudi.microservice.selfdata.service.notifications;

import java.util.UUID;

import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;

public interface NotifyService {

	void handleUserTasks(String login, UUID userUuid) throws FormDefinitionException, InvalidDataException;
}
