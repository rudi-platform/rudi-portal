/**
 * RUDI Portail
 */
package org.rudi.facet.bpmn.helper.workflow;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricDetailQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.history.HistoricVariableInstanceQuery;
import org.activiti.engine.history.NativeHistoricActivityInstanceQuery;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Action;
import org.rudi.bpmn.core.bean.HistoricInformation;
import org.rudi.bpmn.core.bean.ProcessHistoricInformation;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.mapper.workflow.HistoricInformationMapper;
import org.rudi.facet.bpmn.mapper.workflow.ProcessHistoricInformationMapper;
import org.rudi.facet.bpmn.service.TaskConstants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * @author FNI18300
 */
@Component
@RequiredArgsConstructor
public class HistoricHelper {

	public static final String USER_TASK_TYPE = "userTask";

	public static final String START_EVENT_TYPE = "startEvent";

	public static final String END_EVENT_TYPE = "endEvent";

	public static final String ACTION_VARIABLE_NAME = "action";

	public static final List<String> ACTIVITI_TYPES = List.of(USER_TASK_TYPE, START_EVENT_TYPE, END_EVENT_TYPE);

	private final ProcessEngine processEngine;

	private final HistoricInformationMapper historicInformationMapper;

	private final ProcessHistoricInformationMapper processHistoricInformationMapper;

	private final BpmnHelper bpmnHelper;

	private final ACLHelper aclHelper;

	public Page<HistoricInformation> collectHistoricByAssetUuid(UUID assetUuid, Pageable pageable) {
		return historicInformationMapper.entitiesToDto(collectHistoricActivitiByAssetUuid(assetUuid, pageable),
				pageable);
	}

	/**
	 * Retourne l'historique d'une tâche par son executionId en mode paginé
	 *
	 * @param executionId
	 * @param pageable
	 * @return la page demandée
	 */
	public Page<HistoricActivityInstance> collectHistoricActivitiByExecutionId(String executionId, Pageable pageable) {
		HistoryService historyService = processEngine.getHistoryService();
		HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();
		query.executionId(executionId).orderByHistoricActivityInstanceEndTime().asc();
		if (pageable == null || pageable.isUnpaged()) {
			return new PageImpl<>(query.list());
		} else {
			long count = query.count();
			return new PageImpl<>(query.listPage((int) pageable.getOffset(), pageable.getPageSize()), pageable, count);
		}
	}

	/**
	 * Retourne l'historique d'une tâche par son processInstanceId en mode paginé
	 *
	 * @param processInstanceId
	 * @param pageable
	 * @return la page demandée
	 */
	public Page<HistoricActivityInstance> collectHistoricActivitiByProcessInstanceId(String processInstanceId,
			Pageable pageable) {
		HistoryService historyService = processEngine.getHistoryService();
		HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();
		query.processInstanceId(processInstanceId).finished()
				// .activityType("userTask").activityType("startEvent").activityType("endEvent")
				.orderByHistoricActivityInstanceStartTime().asc();
		if (pageable == null || pageable.isUnpaged()) {
			return new PageImpl<>(query.list());
		} else {
			long count = query.count();
			return new PageImpl<>(query.listPage((int) pageable.getOffset(), pageable.getPageSize()), pageable, count);
		}
	}

	/**
	 * Retourne l'historique d'une tâche par son processInstanceId en mode paginé (natif)
	 *
	 * @param processInstanceId
	 * @param pageable
	 * @return
	 */
	public Page<HistoricActivityInstance> collectNativeHistoricActivitiByProcessInstanceId(String processInstanceId,
			Pageable pageable) {
		HistoryService historyService = processEngine.getHistoryService();
		NativeHistoricActivityInstanceQuery query = historyService.createNativeHistoricActivityInstanceQuery();
		query.parameter("processInstanceId", processInstanceId);
		query.parameter("activityType", ACTIVITI_TYPES);
		if (pageable == null || pageable.isUnpaged()) {
			return new PageImpl<>(query.list());
		} else {
			long count = query.count();
			return new PageImpl<>(query.listPage((int) pageable.getOffset(), pageable.getPageSize()), pageable, count);
		}
	}

	/**
	 * Retourne l'historique d'une tâche par son processInstanceId en mode paginé (HistoricTask)
	 *
	 * @param processInstanceId
	 * @param pageable
	 * @return
	 */
	public Page<HistoricTaskInstance> collectHistoricTaskByProcessInstanceId(String processInstanceId,
			Pageable pageable) {
		HistoryService historyService = processEngine.getHistoryService();
		HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();
		query.processInstanceId(processInstanceId).orderByHistoricTaskInstanceStartTime().asc();
		if (pageable == null || pageable.isUnpaged()) {
			return new PageImpl<>(query.list());
		} else {
			long count = query.count();
			return new PageImpl<>(query.listPage((int) pageable.getOffset(), pageable.getPageSize()), pageable, count);
		}
	}

	/**
	 * Retourne l'historique d'une tâche par son processInstanceId en mode paginé (HistoricDetail)
	 *
	 * @param activitiInstanceId
	 * @param pageable
	 * @return
	 */
	public Page<HistoricDetail> collectHistoricDetailByActivitiInstanceId(String activitiInstanceId,
			Pageable pageable) {
		HistoryService historyService = processEngine.getHistoryService();
		HistoricDetailQuery query = historyService.createHistoricDetailQuery();
		query.activityInstanceId(activitiInstanceId).orderByTime().asc();
		if (pageable == null || pageable.isUnpaged()) {
			return new PageImpl<>(query.list());
		} else {
			long count = query.count();
			return new PageImpl<>(query.listPage((int) pageable.getOffset(), pageable.getPageSize()), pageable, count);
		}
	}

	/**
	 * 
	 * @param activitiInstanceId
	 * @param pageable
	 * @return
	 */
	public Page<HistoricDetail> collectHistoricDetailByProcessInstanceId(String processInstanceId, Pageable pageable) {
		HistoryService historyService = processEngine.getHistoryService();
		HistoricDetailQuery query = historyService.createHistoricDetailQuery();
		query.processInstanceId(processInstanceId).orderByTime().asc();
		if (pageable == null || pageable.isUnpaged()) {
			return new PageImpl<>(query.list());
		} else {
			long count = query.count();
			return new PageImpl<>(query.listPage((int) pageable.getOffset(), pageable.getPageSize()), pageable, count);
		}
	}

	public HistoricDetailVariableInstanceUpdateEntity lookupHistoricDetail(List<HistoricDetail> historicVariables,
			String name) {
		HistoricDetailVariableInstanceUpdateEntity result = null;
		if (CollectionUtils.isNotEmpty(historicVariables)) {
			for (HistoricDetail historicVariable : historicVariables) {
				if (historicVariable instanceof HistoricDetailVariableInstanceUpdateEntity item
						&& item.getName().equalsIgnoreCase(name)) {
					result = item;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Retourne l'historique d'une tâche par son processInstanceId en mode paginé (HistoricProcessInstance)
	 *
	 * @param processInstanceId
	 * @param pageable
	 * @return
	 */
	public Page<HistoricProcessInstance> collectHistoricProcessByProcessInstanceId(String processInstanceId,
			Pageable pageable) {
		HistoryService historyService = processEngine.getHistoryService();
		HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
		query.processInstanceId(processInstanceId).orderByProcessInstanceStartTime().asc();
		if (pageable == null || pageable.isUnpaged()) {
			return new PageImpl<>(query.list());
		} else {
			long count = query.count();
			return new PageImpl<>(query.listPage((int) pageable.getOffset(), pageable.getPageSize()), pageable, count);
		}
	}

	/**
	 * Retourne l'historique d'une tâche par son activityId en mode paginé (HistoricVariableInstance)
	 *
	 * @param processInstanceId
	 * @param pageable
	 * @return
	 */
	public Page<HistoricVariableInstance> collectHistoricVariableById(String id, Pageable pageable) {
		HistoryService historyService = processEngine.getHistoryService();
		HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
		query.id(id);
		if (pageable == null || pageable.isUnpaged()) {
			return new PageImpl<>(query.list());
		} else {
			long count = query.count();
			return new PageImpl<>(query.listPage((int) pageable.getOffset(), pageable.getPageSize()), pageable, count);
		}
	}

	/**
	 * Retourne l'historique d'une tâche par l'uuid de son asset en mode paginé
	 *
	 * @param assetUuid
	 * @param pageable
	 * @return la page demandée
	 */
	public Page<HistoricActivityInstance> collectHistoricActivitiByAssetUuid(UUID assetUuid, Pageable pageable) {
		HistoryService historyService = processEngine.getHistoryService();
		// récupération de l'historique des variables
		HistoricVariableInstanceQuery variableQuery = historyService.createHistoricVariableInstanceQuery();
		variableQuery.variableValueEquals(TaskConstants.ME_UUID, assetUuid);
		List<HistoricVariableInstance> variableResults = variableQuery.list();
		if (CollectionUtils.isNotEmpty(variableResults)) {
			// conversion de l'id
			String executionId = convertExecutionId(variableResults.get(0));
			if (executionId != null) {
				return collectHistoricActivitiByExecutionId(executionId, pageable);
			}
		}
		return Page.empty(pageable);
	}

	private String convertExecutionId(HistoricVariableInstance variable) {
		if (variable instanceof HistoricVariableInstanceEntity historicVariableInstanceEntity) {
			String variableExecutionId = historicVariableInstanceEntity.getExecutionId();
			Page<HistoricActivityInstance> taskResults = collectHistoricActivitiByProcessInstanceId(variableExecutionId,
					Pageable.unpaged());
			if (!taskResults.isEmpty()) {
				HistoricActivityInstance taskInstance = taskResults.getContent().get(0);
				return taskInstance.getExecutionId();
			}
		}
		return null;
	}

	public HistoricTaskInstance getAssetLastFinishedTask(UUID assetUuid) {
		List<HistoricTaskInstance> list = processEngine.getHistoryService().createHistoricTaskInstanceQuery()
				.processInstanceBusinessKey(assetUuid.toString()).orderByHistoricTaskInstanceEndTime().desc().list()
				.stream().filter(t -> t.getEndTime() != null)
				// Le .finsihed ne semble pas fonctionner ou en tout cas
				// pas sur ce champ, obliger de le faire à la main
				.toList();
		if (CollectionUtils.isNotEmpty(list)) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public List<HistoricActivityInstance> filterHistoricActivityInstances(List<HistoricActivityInstance> content) {
		return filterHistoricActivityInstances(content, ACTIVITI_TYPES);
	}

	public List<HistoricActivityInstance> filterHistoricActivityInstances(List<HistoricActivityInstance> content,
			List<String> activitiTypes) {
		if (content != null) {
			return content.stream().filter(item -> activitiTypes.contains(item.getActivityType())).toList();
		} else {
			return List.of();
		}
	}

	public List<HistoricInformation> convertHistoricActivityInstance(
			List<HistoricActivityInstance> historicActivityInstances) {
		return historicInformationMapper.entitiesToDto(historicActivityInstances);
	}

	public List<ProcessHistoricInformation> convertHistoricProcessInstance(
			List<HistoricProcessInstance> historicProcessInstances) {
		return processHistoricInformationMapper.entitiesToDto(historicProcessInstances);
	}

	/**
	 * Retourne l'historique d'une tâche par son executionId en mode paginé
	 *
	 * @param processDefinitionKeys
	 * @param assignees
	 * @param finished
	 * @param startCompletionDate
	 * @param endCompletionDate
	 * @return
	 */
	public List<HistoricTaskInstance> collectHistoricActiviti(List<String> processDefinitionKeys,
			String processInstanceId, List<String> assignees, boolean finished, LocalDateTime startCompletionDate,
			LocalDateTime endCompletionDate) {
		HistoryService historyService = processEngine.getHistoryService();
		HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();
		if (CollectionUtils.isNotEmpty(processDefinitionKeys)) {
			query.processDefinitionKeyIn(processDefinitionKeys);
		}
		if (CollectionUtils.isNotEmpty(assignees)) {
			query.taskAssigneeIds(assignees);
		}
		if (StringUtils.isNotBlank(processInstanceId)) {
			query.processInstanceId(processInstanceId);
		}
		if (finished) {
			query.finished();
		}
		if (startCompletionDate != null) {
			Date d = Date.from(startCompletionDate.toInstant(ZoneOffset.UTC));
			query.taskCompletedAfter(d);
		}
		if (endCompletionDate != null) {
			Date d = Date.from(endCompletionDate.toInstant(ZoneOffset.UTC));
			query.taskCompletedBefore(d);
		}
		query.orderByProcessDefinitionId().asc();
		return query.list();
	}

	/**
	 * Retourne l'historique d'une tâche par son executionId en mode paginé
	 *
	 * @param processDefinitionKeys
	 * @param assignees
	 * @param finished
	 * @param startCompletionDate
	 * @param endCompletionDate
	 * @return
	 */
	public List<HistoricProcessInstance> collectHistoricProcess(Collection<String> processInstanceIds) {
		HistoryService historyService = processEngine.getHistoryService();
		HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
		if (CollectionUtils.isNotEmpty(processInstanceIds)) {
			Set<String> processInstanceIdSet = new HashSet<>();
			processInstanceIdSet.addAll(processInstanceIds);
			query.processInstanceIds(processInstanceIdSet);
		}
		query.orderByProcessInstanceId().asc();
		return query.list();
	}

	public void convertActions(String processDefinitionKey, List<HistoricActivityInstance> historicActivitiInstances,
			List<HistoricInformation> result) {
		if (CollectionUtils.isNotEmpty(historicActivitiInstances) && CollectionUtils.isNotEmpty(result)
				&& result.size() == historicActivitiInstances.size()) {
			for (int i = 0; i < result.size(); i++) {
				HistoricActivityInstance historicActivityInstance = historicActivitiInstances.get(i);
				HistoricInformation historicInformation = result.get(i);
				historicInformation.setAction(convertAction(processDefinitionKey, historicActivityInstance));
			}
		}
	}

	protected String convertAction(String processDefinitionKey, HistoricActivityInstance historicActivityInstance) {
		String result = null;
		Page<HistoricDetail> historicVariables = collectHistoricDetailByActivitiInstanceId(
				historicActivityInstance.getId(), Pageable.unpaged());
		for (HistoricDetail historicVariable : historicVariables) {
			if (historicVariable instanceof HistoricDetailVariableInstanceUpdateEntity item
					&& item.getName().equalsIgnoreCase(ACTION_VARIABLE_NAME)) {
				result = translateAction(processDefinitionKey, historicActivityInstance, item);
				break;
			}
		}
		return result;
	}

	protected String translateAction(String processDefinitionKey, HistoricActivityInstance historicActivityInstance,
			HistoricDetailVariableInstanceUpdateEntity item) {
		String value = null;
		List<Action> actions = bpmnHelper.extractActions(processDefinitionKey,
				historicActivityInstance.getProcessDefinitionId(), historicActivityInstance.getActivityId());
		if (CollectionUtils.isNotEmpty(actions)) {
			value = actions.stream().filter(action -> action.getName().equalsIgnoreCase(item.getTextValue()))
					.findFirst().map(Action::getLabel).orElse(null);
		}
		if (value == null) {
			value = item.getValue().toString();
		}
		return value;
	}

	public Map<String, String> convertAssignees(List<HistoricInformation> result, String initiator) {
		Map<String, String> userNames = new HashMap<>();
		if (CollectionUtils.isNotEmpty(result)) {
			for (HistoricInformation historicInformation : result) {
				if (HistoricHelper.START_EVENT_TYPE.equalsIgnoreCase(historicInformation.getActivityType())) {
					historicInformation.setAssignee(convertAssignee(userNames, initiator));
				} else if (StringUtils.isNotEmpty(historicInformation.getAssignee())) {
					historicInformation.setAssignee(convertAssignee(userNames, historicInformation));
				}
			}
		}
		return userNames;
	}

	public String convertAssignee(Map<String, String> userNames, String assignee) {
		if (!userNames.containsKey(assignee)) {
			try {
				User user = aclHelper.getUserByLogin(assignee);
				if (user != null && (StringUtils.isNotEmpty(user.getFirstname())
						|| StringUtils.isNotEmpty(user.getLastname()))) {
					userNames.put(assignee, convertAssignee(user));
				} else {
					userNames.put(assignee, assignee);
				}
			} catch (Exception e) {
				userNames.put(assignee, assignee);
			}
		}
		return userNames.get(assignee);
	}

	protected String convertAssignee(Map<String, String> userNames, HistoricInformation historicInformation) {
		return convertAssignee(userNames, historicInformation.getAssignee());
	}

	protected String convertAssignee(User user) {
		StringBuilder userName = new StringBuilder();
		if (StringUtils.isNotEmpty(user.getFirstname())) {
			userName.append(user.getFirstname());
		}
		if (StringUtils.isNotEmpty(user.getLastname())) {
			if (userName.length() > 0) {
				userName.append(' ');
			}
			userName.append(user.getLastname());
		}
		userName.append(" (").append(user.getLogin()).append(')');
		return userName.toString();
	}

	public void enhancedProcessHistoricInformation(ProcessHistoricInformation processHistoricInformation) {
		String processInstanceId = processHistoricInformation.getId();
		Page<HistoricDetail> historicDetails = collectHistoricDetailByProcessInstanceId(processInstanceId,
				Pageable.unpaged());

		// initialisation de la description et du statut fonctionnel
		HistoricDetailVariableInstanceUpdateEntity detail = lookupHistoricDetail(historicDetails.getContent(),
				TaskConstants.DESCRIPTION);
		if (detail != null) {
			processHistoricInformation.setDescription(detail.getTextValue());
		}
		// initialisation du statut fonctionnel
		detail = lookupHistoricDetail(historicDetails.getContent(), TaskConstants.FUNCTIONAL_STATUS);
		if (detail != null) {
			processHistoricInformation.setFunctionnalStatus(detail.getTextValue());
		}
		// initialisation de l'initiateur
		detail = lookupHistoricDetail(historicDetails.getContent(), TaskConstants.INITIATOR);
		if (detail != null) {
			processHistoricInformation.setStartUser(detail.getTextValue());
		}
		Page<HistoricActivityInstance> historicActivityInstances = collectHistoricActivitiByProcessInstanceId(
				processInstanceId, Pageable.unpaged());
		List<HistoricActivityInstance> filteredHistoricActivityInstances = filterHistoricActivityInstances(
				historicActivityInstances.getContent());
		processHistoricInformation
				.setHistoricInformations(convertHistoricActivityInstance(filteredHistoricActivityInstances));

		convertActions(processHistoricInformation.getProcessDefinitionKey(), filteredHistoricActivityInstances,
				processHistoricInformation.getHistoricInformations());
		Map<String, String> userNames = convertAssignees(processHistoricInformation.getHistoricInformations(),
				processHistoricInformation.getStartUser());
		processHistoricInformation.setStartUser(convertAssignee(userNames, processHistoricInformation.getStartUser()));
	}
}
