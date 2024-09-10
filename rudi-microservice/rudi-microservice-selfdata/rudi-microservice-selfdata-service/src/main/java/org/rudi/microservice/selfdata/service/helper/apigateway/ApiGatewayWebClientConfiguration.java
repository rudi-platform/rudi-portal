package org.rudi.microservice.selfdata.service.helper.apigateway;

import org.rudi.common.core.webclient.HttpClientHelper;
import org.rudi.facet.oauth2.config.WebClientConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ApiGatewayWebClientConfiguration extends WebClientConfig {

	public ApiGatewayWebClientConfiguration(HttpClientHelper httpClientHelper) {
		super(httpClientHelper);
	}

	@Bean(name = "rudi_selfdata")
	public WebClient apigatewayWebClient(@Qualifier("rudi_oauth2_builder") WebClient.Builder webClientBuilder,
			ApiGatewayWebClientProperties apigatewayProperties) {
		return webClientBuilder.baseUrl(apigatewayProperties.getServiceBaseUrl()).build();
	}
}
