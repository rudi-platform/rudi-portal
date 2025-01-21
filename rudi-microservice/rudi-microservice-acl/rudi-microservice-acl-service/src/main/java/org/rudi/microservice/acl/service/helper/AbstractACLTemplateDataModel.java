/**
 * 
 */
package org.rudi.microservice.acl.service.helper;

import java.util.Locale;
import java.util.Map;

import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.MapUtils;
import org.rudi.facet.generator.model.GenerationFormat;
import org.rudi.facet.generator.text.model.AbstractTemplateDataModel;

/**
 * @author fni18300
 *
 */
public abstract class AbstractACLTemplateDataModel extends AbstractTemplateDataModel {

	private Map<String, Object> additionalProperties;

	public AbstractACLTemplateDataModel(Map<String, Object> additionalProperties, @NotNull Locale locale,
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
