/**
 * 
 */
package org.rudi.facet.bpmn.service.impl;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.rudi.facet.generator.model.GenerationFormat;
import org.rudi.facet.generator.text.model.AbstractTemplateDataModel;

/**
 * @author fni18300
 *
 */
public class TemplateSectionDataModel extends AbstractTemplateDataModel {

	private Map<String, Object> parameters;

	public TemplateSectionDataModel(String content, Map<String, Object> parameters) {
		super(GenerationFormat.TEXT, Locale.FRENCH, "stl:section" + UUID.randomUUID() + ":" + content);
		this.parameters = parameters;
	}

	@Override
	protected void fillDataModel(Map<String, Object> data) {
		data.putAll(parameters);
	}

	@Override
	protected String generateFileName() {
		return getFormat().generateFileName("section");
	}

}
