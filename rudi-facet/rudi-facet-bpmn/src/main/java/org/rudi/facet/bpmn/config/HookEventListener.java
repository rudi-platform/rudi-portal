/**
 * 
 */
package org.rudi.facet.bpmn.config;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiVariableEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 *
 */
@Slf4j
public class HookEventListener implements ActivitiEventListener {

	@Override
	public void onEvent(ActivitiEvent event) {
		switch (event.getType()) {

		case ENGINE_CLOSED:
			log.info("Activiti - Engine closed.");
			break;

		case ENGINE_CREATED:
			log.info("Activiti - Engine created...");
			break;

		case JOB_EXECUTION_SUCCESS:
			log.info("Activiti - A job well done! {}", event);
			break;

		case JOB_EXECUTION_FAILURE:
			log.info("Activiti - A job has failed... {}", event);
			break;

		case ENTITY_CREATED: {
			ActivitiEntityEvent ec = (ActivitiEntityEvent) event;
			Object o = ec.getEntity();
			log.info("Activiti - Entity created: {}/{}=>{}", event.getType(), o, event);
		}
			break;

		case ENTITY_INITIALIZED: {
			ActivitiEntityEvent ei = (ActivitiEntityEvent) event;
			Object o = ei.getEntity();
			log.info("Activiti - Entity initialized: {}/{}=>{}", event.getType(), o, event);
		}
			break;

		case ENTITY_DELETED: {
			ActivitiEntityEvent ed = (ActivitiEntityEvent) event;
			Object o = ed.getEntity();
			log.info("Activiti - Entity deleted: {}/{}=>{}", event.getType(), o, event);
		}
			break;

		case VARIABLE_CREATED: {
			ActivitiVariableEvent vc = (ActivitiVariableEvent) event;
			log.info("Activiti - Variable created: {}/{}=>{}", event.getType(), vc.getVariableName(), event);
		}
			break;

		case VARIABLE_UPDATED: {
			ActivitiVariableEvent vu = (ActivitiVariableEvent) event;
			log.info("Activiti - Variable updated: {}/{}=>{}", event.getType(), vu.getVariableName(), event);
		}
			break;

		case VARIABLE_DELETED: {
			ActivitiVariableEvent vd = (ActivitiVariableEvent) event;
			log.info("Activiti - Variable deleted: {}/{}=>{}", event.getType(), vd.getVariableName(), event);
		}
			break;

		case TASK_ASSIGNED: {
			ActivitiEntityEvent ea = (ActivitiEntityEvent) event;
			Object o = ea.getEntity();
			log.info("Activiti - Entity initialized: {}/{}=>{}", event.getType(), o, event);
		}
			break;

		default:
			log.info("Activiti - Event received: {}=>{}", event.getType(), event);
		}
	}

	@Override
	public boolean isFailOnException() {
		return false;
	}

}
