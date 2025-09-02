package org.rudi.microservice.kalim.core.bean;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.rudi.facet.generator.model.GenerationFormat;
import org.rudi.facet.generator.text.model.AbstractTemplateDataModel;

import jakarta.validation.constraints.NotNull;

public class AbstractKalimTemplateDataModel extends AbstractTemplateDataModel {

	private Map<String, Object> additionalProperties;

	public AbstractKalimTemplateDataModel(Map<String, Object> additionalProperties, @NotNull Locale locale,
			@NotNull String model) {
		super(GenerationFormat.HTML, locale, model);
		this.additionalProperties = additionalProperties;
	}

	@Override
	protected void fillDataModel(Map<String, Object> data) {
		if (MapUtils.isNotEmpty(additionalProperties)) {
			data.putAll(additionalProperties);
		}
	}

}
