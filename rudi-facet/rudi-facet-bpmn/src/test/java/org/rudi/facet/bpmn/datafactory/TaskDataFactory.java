package org.rudi.facet.bpmn.datafactory;

import org.rudi.bpmn.core.bean.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskDataFactory {

	public Task create() {
		return new Task();
	}

}
