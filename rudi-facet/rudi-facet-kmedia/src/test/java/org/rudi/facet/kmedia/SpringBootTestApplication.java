package org.rudi.facet.kmedia;

import javax.net.ssl.SSLException;

import org.rudi.common.core.json.DefaultJackson2ObjectMapperBuilder;
import org.rudi.common.core.webclient.HttpClientHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import reactor.netty.http.client.HttpClient;

@SpringBootApplication(scanBasePackages = {
		"org.rudi.common.core",
		"org.rudi.facet.dataverse.api",
		"org.rudi.facet.dataverse.fields",
		"org.rudi.facet.dataverse.helper",
		"org.rudi.facet.kmedia.service",
		"org.rudi.facet.kmedia.helper"
})
public class SpringBootTestApplication {
	
	@Autowired
	private HttpClientHelper httpClientHelper;
	
	@Bean
	public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
		return new DefaultJackson2ObjectMapperBuilder();
	}
	
	@Bean(defaultCandidate = true)
	public HttpClient httpClient() throws SSLException {
		return httpClientHelper.createReactorHttpClient(true, false, false);
	}
}
