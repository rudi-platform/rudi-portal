package org.rudi.microservice.strukture.facade.controller;

import java.util.List;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Form;
import org.rudi.bpmn.core.bean.HistoricInformation;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.facet.bpmn.service.TaskService;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.core.bean.OwnerInfo;
import org.rudi.microservice.strukture.facade.controller.api.LinkedProducersApi;
import org.rudi.microservice.strukture.service.helper.provider.LinkedProducerAssignmentHelper;
import org.rudi.microservice.strukture.service.provider.LinkedProducerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import static org.rudi.common.core.security.QuotedRoleCodes.ADMINISTRATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.MODERATOR;
import static org.rudi.common.core.security.QuotedRoleCodes.PROVIDER;

@RestController
@RequiredArgsConstructor
public class LinkedProducerController implements LinkedProducersApi {
	private final TaskService<LinkedProducer> linkedProducerTaskService;
	private final LinkedProducerService linkedProducerService;
	private final LinkedProducerAssignmentHelper linkedProducerAssignmentHelper;

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + ", " + MODERATOR + ")")
	public ResponseEntity<OwnerInfo> getLinkedProducerOwnerInfo(UUID uuid) throws Exception {
		return ResponseEntity.ok(linkedProducerService.getLinkedProducerOwnerInfo(uuid));
	}

	@Override
	public ResponseEntity<LinkedProducer> getLinkedProducer(UUID linkedProducerUuid) throws Exception {
		return ResponseEntity.ok(linkedProducerService.getLinkedProducer(linkedProducerUuid));
	}


	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + PROVIDER + ")")
	public ResponseEntity<UUID> attachProducer(UUID organizationUuid) throws Exception {
		LinkedProducer linkedProducer = linkedProducerService.createLinkedProducer(organizationUuid);

		Task task = linkedProducerTaskService.createDraft(linkedProducer);

		linkedProducerTaskService.startTask(task);

		return ResponseEntity.ok(linkedProducer.getUuid());
	}

	@Override
	public ResponseEntity<Task> claimLinkedProducerTask(String taskId) throws Exception {
		return ResponseEntity.ok(linkedProducerTaskService.claimTask(taskId));
	}

	@Override
	public ResponseEntity<Task> createLinkedProducerDraft(LinkedProducer linkedProducer) throws Exception {
		return ResponseEntity.ok(linkedProducerTaskService.createDraft(linkedProducer));
	}

	@Override
	public ResponseEntity<Void> doItLinkedProducer(String taskId, String actionName) throws Exception {
		linkedProducerTaskService.doIt(taskId, actionName);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Override
	public ResponseEntity<List<HistoricInformation>> getLinkedProducerTaskHistoryByTaskId(String taskId, Boolean asAdmin) throws Exception {
		return ResponseEntity.ok(linkedProducerTaskService.getTaskHistoryByTaskId(taskId, asAdmin));
	}

	@Override
	public ResponseEntity<Form> lookupLinkedProducerDraftForm() throws Exception {
		return ResponseEntity.ok(linkedProducerTaskService.lookupDraftForm(null));
	}

	@Override
	public ResponseEntity<Task> startLinkedProducerTask(Task task) throws Exception {
		return ResponseEntity.ok(linkedProducerTaskService.startTask(task));
	}

	@Override
	public ResponseEntity<Task> unclaimLinkedProducerTask(String taskId) throws Exception {
		linkedProducerTaskService.unclaimTask(taskId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Override
	public ResponseEntity<Task> updateLinkedProducerTask(Task task) throws Exception {
		return ResponseEntity.ok(linkedProducerTaskService.updateTask(task));
	}

	@Override
	@PreAuthorize("hasAnyRole(" + ADMINISTRATOR + "," + PROVIDER + ")")
	public ResponseEntity<UUID> detachProducer(UUID organizationUuid) throws Exception {
		LinkedProducer linkedProducer = linkedProducerService.getMyLinkedProducerFromOrganizationUuid(organizationUuid);

		Task task = linkedProducerTaskService.createDraft(linkedProducer);

		linkedProducerTaskService.startTask(task);

		return ResponseEntity.ok(linkedProducer.getUuid());
	}

	@Override
	public ResponseEntity<Boolean> isAttachedToProducer(UUID uuid) throws Exception {
		return ResponseEntity.ok(linkedProducerService.isOrganizationAttachedToMyProvider(uuid));
	}
}
