package org.rudi.facet.dataverse.helper;

import javax.net.ssl.SSLException;

import org.rudi.common.core.webclient.HttpClientHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author FNI18300
 */
@Component
@RequiredArgsConstructor
public class DataverseWebClientConfig {

	public static final String API_HEADER_KEY = "X-Dataverse-key";

	@Getter
	@Value("${dataverse.api.token}")
	private String apiToken;

	@Getter
	@Value("${dataverse.api.url}")
	private String serverUrl;

	private final HttpClientHelper httpClientHelper;

	final ExchangeStrategies strategies = ExchangeStrategies.builder()
			.codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build();

	@Bean(name = "rudi_dataverse_httpclient")
	public HttpClient httpClient() throws SSLException {
		return httpClientHelper.createReactorHttpClient(true, false, false);
	}

	@Bean(name = "rudi_dataverse_builder")
	public WebClient.Builder webClientBuilder(@Qualifier("rudi_dataverse_httpclient") HttpClient httpClient) {
		return WebClient.builder().defaultHeaders(header -> header.add(API_HEADER_KEY, apiToken))
				.clientConnector(new ReactorClientHttpConnector(httpClient));
	}

	@Bean(name = "rudi_dataverse_client")
	public WebClient webClient(@Qualifier("rudi_dataverse_builder") WebClient.Builder builder) {
		return builder.baseUrl(serverUrl).exchangeStrategies(strategies).build();
	}
}
