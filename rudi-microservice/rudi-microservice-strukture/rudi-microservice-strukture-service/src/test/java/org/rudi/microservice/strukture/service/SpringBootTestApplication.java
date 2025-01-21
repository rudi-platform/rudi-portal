package org.rudi.microservice.strukture.service;

import org.rudi.common.core.json.DefaultJackson2ObjectMapperBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Classe application pour les tests unitaires de la couche service
 */
@SpringBootApplication(scanBasePackages = {
		"org.rudi.common.facade",
		"org.rudi.common.service",
		"org.rudi.common.storage",
		"org.rudi.common.core",
		"org.rudi.facet.acl",
		"org.rudi.facet.dataverse",
		"org.rudi.facet.kmedia",
		"org.rudi.facet.kaccess",
		"org.rudi.facet.email",
		"org.rudi.facet.projekt.helper",
		"org.rudi.facet.generator",
		"org.rudi.facet.bpmn.bean",
		"org.rudi.facet.bpmn.config",
		"org.rudi.facet.bpmn.dao",
		"org.rudi.facet.bpmn.entity",
		"org.rudi.facet.bpmn.exception",
		"org.rudi.facet.bpmn.helper",
		"org.rudi.facet.bpmn.mapper",
		"org.rudi.facet.bpmn.service",
		"org.rudi.microservice.strukture.core",
		"org.rudi.microservice.strukture.service",
		"org.rudi.microservice.strukture.storage"
		})
public class SpringBootTestApplication {

	@Bean
	public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
		return new DefaultJackson2ObjectMapperBuilder();
	}

}
