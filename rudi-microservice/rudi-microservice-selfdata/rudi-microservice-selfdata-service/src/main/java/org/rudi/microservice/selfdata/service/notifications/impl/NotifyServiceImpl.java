package org.rudi.microservice.selfdata.service.notifications.impl;

import java.util.List;
import java.util.UUID;

import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.AssetDescriptionAnonymizeUserProcessor;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.microservice.selfdata.service.notifications.NotifyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class NotifyServiceImpl implements NotifyService {

	private final BpmnHelper bpmnHelper;
	private final List<AssetDescriptionAnonymizeUserProcessor> assetDescriptionAnonymizeUserProcessors;

	@Override
	@Transactional
	public void handleUserTasks(String login, UUID userUuid) throws FormDefinitionException, InvalidDataException {
		for(AssetDescriptionAnonymizeUserProcessor processor : assetDescriptionAnonymizeUserProcessors) {
			processor.process(login, userUuid);
		}
	}

}
