package org.rudi.microservice.projekt.service;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe application pour les tests unitaires de la couche service
 */
@SpringBootApplication(scanBasePackages = {
		"org.rudi.common.core",
		"org.rudi.common.service",
		"org.rudi.common.storage", 
		"org.rudi.common.facade",
		"org.rudi.facet.acl", 
		"org.rudi.facet.dataverse",
		"org.rudi.facet.kmedia", 
		"org.rudi.facet.strukture",
		"org.rudi.facet.organization", 		
		"org.rudi.facet.bpmn.bean",
		"org.rudi.facet.bpmn.config",
		"org.rudi.facet.bpmn.dao",
		"org.rudi.facet.bpmn.entity",
		"org.rudi.facet.bpmn.exception",
		"org.rudi.facet.bpmn.helper",
		"org.rudi.facet.bpmn.mapper",
		"org.rudi.facet.bpmn.service",
		"org.rudi.facet.email", 
		"org.rudi.facet.generator",
		"org.rudi.facet.kaccess",
		"org.rudi.microservice.projekt.core",
		"org.rudi.microservice.projekt.service",
		"org.rudi.microservice.projekt.storage"
})
public class SpringBootTestApplication {
}
