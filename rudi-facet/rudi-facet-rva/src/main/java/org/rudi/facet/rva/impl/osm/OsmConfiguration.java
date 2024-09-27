package org.rudi.facet.rva.impl.osm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OsmConfiguration {

	@Bean
	public WebClient osmWebClient(OsmProperties banProperties) {
		return WebClient.builder().baseUrl(banProperties.getUrl()).build();
	}
}
