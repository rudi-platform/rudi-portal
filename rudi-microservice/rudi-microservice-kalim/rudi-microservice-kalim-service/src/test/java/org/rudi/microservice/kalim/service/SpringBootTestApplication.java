package org.rudi.microservice.kalim.service;

import org.rudi.common.core.json.DefaultJackson2ObjectMapperBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Classe de configuration globale de l'application.
 */
@SpringBootApplication(scanBasePackages = {
		"org.rudi.common.core",
		"org.rudi.common.facade",
		"org.rudi.common.service",
		"org.rudi.common.storage",
		"org.rudi.facet.acl",
		"org.rudi.facet.apigateway",
		"org.rudi.facet.dataverse",
		"org.rudi.facet.email",
		"org.rudi.facet.generator",
		"org.rudi.facet.generator.text",
		"org.rudi.facet.kaccess",
		"org.rudi.facet.kos",
		"org.rudi.facet.organization",
		"org.rudi.facet.projekt",
		"org.rudi.facet.providers",
		"org.rudi.facet.strukture",
		"org.rudi.microservice.kalim.service",
		"org.rudi.microservice.kalim.storage",
})
public class SpringBootTestApplication {

	@Bean
	public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
		return new DefaultJackson2ObjectMapperBuilder();
	}

}
