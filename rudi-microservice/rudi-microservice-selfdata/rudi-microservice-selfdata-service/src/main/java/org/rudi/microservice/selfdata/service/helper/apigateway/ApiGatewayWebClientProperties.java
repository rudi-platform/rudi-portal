package org.rudi.microservice.selfdata.service.helper.apigateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "rudi.apigateway")
@Getter
@Setter
@SuppressWarnings("java:S1075") // Les URL par défaut des paramètres sont obligatoirement en dur dans le code
public class ApiGatewayWebClientProperties {
	/**
	 * URL de base pour appeler le micro-service Strukture via Load Balancer
	 */
	private String serviceBaseUrl = "lb://RUDI-APIGATEWAY/apigateway/";

	private String datasetApiPath = "/datasets/{globalId}/{mediaId}/{contact}";

}
