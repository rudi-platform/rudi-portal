/**
 * RUDI Portail
 */
package org.rudi.common.service.swagger;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SwaggerHelper {

	private final ObjectMapper objectMapper;

	private ObjectMapper objectMapperYaml = new ObjectMapper(new YAMLFactory());

	public OpenAPI getSwaggerContractFromValue(String contractPath) throws IOException {
		if (StringUtils.isEmpty(contractPath)) {
			return null;
		}
		URL url = Thread.currentThread().getContextClassLoader().getResource(contractPath);
		if (contractPath.endsWith(".json")) {
			return objectMapper.reader().forType(OpenAPI.class).readValue(url);
		} else {
			return objectMapperYaml.reader().forType(OpenAPI.class).readValue(url);
		}
	}

	public OpenAPI getSwaggerContractFromURL(String contractUrl) throws IOException {
		try {
			return objectMapper.reader().forType(OpenAPI.class).readValue(new URL(contractUrl));
		} catch (Exception e) {
			log.warn("Failed to read json contract from " + contractUrl + ".try to read yaml...", e);
			try {
				return objectMapperYaml.reader().forType(OpenAPI.class).readValue(new URL(contractUrl));
			} catch (Exception e2) {
				log.warn("Failed to read yaml contract from " + contractUrl + ".try to read yaml...", e);
				throw e2;
			}
		}
	}
}
