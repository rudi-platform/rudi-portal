/**
 * RUDI Portail
 */
package org.rudi.facet.bpmn.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.CaseUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Getter;

/**
 * @author FNI18300
 *
 */
@ConfigurationProperties("rudi.bpmn.form")
@Component
public class FormTemplateConfiguration {

	@Getter
	private Map<String, String> properties = new HashMap<>();

	@Getter
	private Map<String, Object> namedProperties = new HashMap<>();

	@PostConstruct
	protected void initializeNamedProperties() {
		for (Map.Entry<String, String> item : properties.entrySet()) {
			namedProperties.put(normalizeName(item.getKey()), item.getValue());
		}
	}

	private String normalizeName(String key) {
		return CaseUtils.toCamelCase(key, false, '.', '-', '_');
	}
}
