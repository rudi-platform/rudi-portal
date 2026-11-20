package org.rudi.facet.bpmn.datafactory;

import org.rudi.facet.bpmn.bean.workflow.EMailData;
import org.springframework.stereotype.Component;

@Component
public class EMailDataDataFactory {

	public EMailData create(String subject, String body) {
		return new EMailData(subject, body);
	}

	public EMailData createEmpty() {
		return create(null, null);
	}

}
