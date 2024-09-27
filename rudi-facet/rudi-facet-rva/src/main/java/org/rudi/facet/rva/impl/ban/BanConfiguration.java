package org.rudi.facet.rva.impl.ban;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BanConfiguration {

	@Bean
	public WebClient banWebClient(BanProperties banProperties) {
		return WebClient.builder().baseUrl(banProperties.getUrl()).build();
	}
}
