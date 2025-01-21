package org.rudi.microservice.selfdata.service;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe application pour les tests unitaires de la couche service
 */
@SpringBootApplication(scanBasePackages = { 
		"org.rudi.common.service", 
		"org.rudi.common.storage",
		"org.rudi.common.core", 
		"org.rudi.facet.dataverse", 
		"org.rudi.facet.kaccess", 
		"org.rudi.facet.email",
		"org.rudi.facet.generator",
		"org.rudi.facet.organization", 
		"org.rudi.facet.rva", 
		"org.rudi.facet.strukture",
		"org.rudi.facet.acl.helper", 
		"org.rudi.facet.bpmn.bean",
		"org.rudi.facet.bpmn.config",
		"org.rudi.facet.bpmn.dao",
		"org.rudi.facet.bpmn.entity",
		"org.rudi.facet.bpmn.exception",
		"org.rudi.facet.bpmn.helper",
		"org.rudi.facet.bpmn.mapper",
		"org.rudi.facet.bpmn.service", 
		"org.rudi.facet.providers",
		"org.rudi.facet.doks", 
		"org.rudi.facet.crypto", 
		"org.rudi.facet.apigateway",
		"org.rudi.microservice.selfdata.core",
		"org.rudi.microservice.selfdata.service", 
		"org.rudi.microservice.selfdata.storage", 
		})
public class SpringBootTestApplication {
}
