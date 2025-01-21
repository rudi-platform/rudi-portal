package org.rudi.facet.dataverse.helper;

import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;

@Component
public class RestTemplateHelper {

	@Getter
	@Value("${dataverse.api.token}")
	private String apiToken;

	@Getter
	@Value("${dataverse.api.url}")
	private String serverUrl;

	/**
	 * @return un RestTemplate correctement initialisé
	 * @throws DataverseAPIException Erreur lors de l'initialisation du restTemplate spring
	 */
	public RestTemplate buildRestTemplate() throws DataverseAPIException {
		RestTemplate restTemplate = null;
		CloseableHttpClient httpClient = null;
		try {
			final SSLContext sslContext = SSLContexts.custom().loadTrustMaterial((chain, authType) -> true).build();
			final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext, new NoopHostnameVerifier());
			final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
					.setTlsSocketStrategy(tlsStrategy).setDefaultTlsConfig(TlsConfig.custom().build()).build();

			httpClient = HttpClients.custom().setConnectionManager(cm).build();

			ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			restTemplate = new RestTemplate(requestFactory);
			restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
			restTemplate.setErrorHandler(new DataverseResponseHandler());
			return restTemplate;
		} catch (Exception e) {
			throw new DataverseAPIException("Failed to load ssl", e);
		}
	}
}
