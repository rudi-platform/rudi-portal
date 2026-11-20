package org.rudi.microservice.acl.service;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe application pour les tests unitaires de la couche service
 */
@SpringBootApplication(scanBasePackages = { "org.rudi.common.core", "org.rudi.common.service",
		"org.rudi.common.storage", "org.rudi.facet.email", "org.rudi.facet.generator",
		"org.rudi.facet.oauth2", "org.rudi.microservice.acl.service", "org.rudi.microservice.acl.storage",
		"org.rudi.facet.projekt", "org.rudi.facet.selfdata", "org.rudi.facet.strukture", "org.rudi.facet.projekt",
		"org.rudi.facet.selfdata", "org.rudi.facet.strukture", "org.rudi.facet.organization", "org.rudi.facet.acl", "org.rudi.facet.bpmn" })
public class SpringBootTestApplication {
}
