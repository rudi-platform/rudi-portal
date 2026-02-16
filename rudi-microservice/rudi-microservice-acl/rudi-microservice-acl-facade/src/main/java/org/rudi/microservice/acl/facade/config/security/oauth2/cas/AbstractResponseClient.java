package org.rudi.microservice.acl.facade.config.security.oauth2.cas;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractResponseClient {

	private boolean sslVerifier = true;

	protected AbstractResponseClient(boolean sslVerifier) {
		super();
		this.sslVerifier = sslVerifier;
	}

	protected RestTemplate buildRestTemplate()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		RestTemplate restTemplate = new RestTemplate(
				Arrays.asList(new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
		restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

		if (!sslVerifier) {
			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

			HostnameVerifier allHostsValid = (hostname, session) -> !sslVerifier;

			SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(acceptingTrustStrategy).build();

			DefaultClientTlsStrategy sslConnectionSocketFactory = new DefaultClientTlsStrategy(sslContext,
					allHostsValid);

			PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
					.setTlsSocketStrategy(sslConnectionSocketFactory).build();

			CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();

			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setHttpClient(httpClient);

			restTemplate.setRequestFactory(requestFactory);
		}
		return restTemplate;
	}
}
