package org.rudi.microservice.kalim.core.bean;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class DeleteDatasetTemplateDataModel extends AbstractKalimTemplateDataModel {
	private final String producerName;
	private final String datasetTitle;
	private final String projectTitle;

	/**
	 * @param additionalProperties
	 * @param locale
	 * @param model
	 */
	public DeleteDatasetTemplateDataModel(Map<String, Object> additionalProperties, Locale locale, String model, String producerName, String datasetTitle, String projectTitle) {
		super(additionalProperties, locale, model);
		this.producerName = producerName;
		this.datasetTitle = datasetTitle;
		this.projectTitle = projectTitle;
	}

	/**
	 * @param data
	 */
	@Override
	protected void fillDataModel(Map<String, Object> data) {
		super.fillDataModel(data);
		data.put("producerName", producerName);
		data.put("datasetTitle", datasetTitle);
		data.put("projectTitle", projectTitle);
	}
}
