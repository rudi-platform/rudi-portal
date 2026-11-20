package org.rudi.microservice.projekt.service.notifications.impl;

import java.util.List;
import java.util.UUID;

import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.AssetDescriptionAnonymizeUserProcessor;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.microservice.projekt.service.notifications.NotifyService;
import org.rudi.microservice.projekt.service.workflow.LinkedDatasetTaskServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotifyServiceImpl implements NotifyService {

	private final BpmnHelper bpmnHelper;
	private final ACLHelper aclHelper;
	private final List<AssetDescriptionAnonymizeUserProcessor> assetDescriptionAnonymizeUserProcessors;

	@Override
	public void handleAddOrganizationMember(UUID organizationUuid, UUID userUUid) {
		String login = aclHelper.getUserByUUID(userUUid).getLogin();
		bpmnHelper.recomputeAndAddCandidate(login, LinkedDatasetTaskServiceImpl.PROCESS_DEFINITION_ID);
	}

	@Override
	public void handleRemoveOrganizationMember(UUID organizationUuid, UUID userUUid) {
		String login = aclHelper.getUserByUUID(userUUid).getLogin();
		bpmnHelper.recomputeAndRemoveCandidate(login, LinkedDatasetTaskServiceImpl.PROCESS_DEFINITION_ID);
	}

	@Override
	@Transactional
	public void handleUserTasks(String login, UUID userUuid) throws FormDefinitionException, InvalidDataException {
		for(AssetDescriptionAnonymizeUserProcessor processor : assetDescriptionAnonymizeUserProcessors) {
			processor.process(login, userUuid);
		}
	}
}
