package org.rudi.facet.bpmn.datafactory;

import java.util.List;

import org.rudi.bpmn.core.bean.Form;
import org.rudi.bpmn.core.bean.Section;
import org.springframework.stereotype.Component;

@Component
public class FormDataFactory {

	public Form createEmpty() {
		return new Form();
	}

	public Form create(List<Section> sections) {
		return new Form().sections(sections);
	}

}
