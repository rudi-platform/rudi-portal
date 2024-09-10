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

import io.swagger.models.Swagger;
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

	public Swagger getSwaggerContractFromValue(String contract) throws IOException {
		if (StringUtils.isEmpty(contract)) {
			return null;
		}
		URL url = Thread.currentThread().getContextClassLoader().getResource(contract);
		if (contract.endsWith(".json")) {
			return objectMapper.reader().forType(Swagger.class).readValue(url);
		} else {
			return objectMapperYaml.reader().forType(Swagger.class).readValue(url);
		}
	}

	public Swagger getSwaggerContractFromURL(String contractUrl) throws IOException {
		try {
			return objectMapper.reader().forType(Swagger.class).readValue(new URL(contractUrl));
		} catch (Exception e) {
			log.warn("Failed to read json contract from " + contractUrl + ".try to read yaml...", e);
			try {
				return objectMapperYaml.reader().forType(Swagger.class).readValue(new URL(contractUrl));
			} catch (Exception e2) {
				log.warn("Failed to read yaml contract from " + contractUrl + ".try to read yaml...", e);
				throw e2;
			}
		}
	}
}
