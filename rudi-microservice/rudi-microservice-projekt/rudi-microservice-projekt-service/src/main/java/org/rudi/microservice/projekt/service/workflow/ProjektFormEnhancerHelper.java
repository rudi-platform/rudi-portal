/**
 * RUDI Portail
 */
package org.rudi.microservice.projekt.service.workflow;

import java.util.List;

import org.rudi.bpmn.core.bean.FieldType;
import org.rudi.bpmn.core.bean.Form;
import org.rudi.bpmn.core.bean.Section;
import org.rudi.microservice.projekt.service.workflow.fieldcomputer.ListFormFieldProcessor;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * @author FNI18300
 *
 */
@Component
@RequiredArgsConstructor
public class ProjektFormEnhancerHelper {

	private final List<ListFormFieldProcessor> formFieldProcessors;

	public void enhance(Form form) {
		if (form != null) {
			form.getSections().forEach(this::enhance);
		}
	}

	public void enhance(Section section) {
		if (section != null) {
			section.getFields().stream().filter(f -> f.getDefinition().getType().equals(FieldType.LIST))
					.forEach(f -> formFieldProcessors.forEach(processor -> {
						if (processor.accept(f)) {
							processor.process(f);
						}
					}));
		}
	}
}
