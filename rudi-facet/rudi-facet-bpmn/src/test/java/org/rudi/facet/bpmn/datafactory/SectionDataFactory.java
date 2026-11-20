package org.rudi.facet.bpmn.datafactory;

import java.util.List;

import org.rudi.bpmn.core.bean.Field;
import org.rudi.bpmn.core.bean.Section;
import org.springframework.stereotype.Component;

@Component
public class SectionDataFactory {

	public Section create(String name, List<Field> fields) {
		return new Section().name(name).fields(fields);
	}

}
