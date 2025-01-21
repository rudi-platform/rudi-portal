/**
 * RUDI Portail
 */
package org.rudi.microservice.apigateway.facade.config.security;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author FNI18300
 *
 */
@Component
public class RestTemplateConfiguration {

	@Value("${trust.trust-all-certs:false}")
	private boolean trustAllCerts;

	@Value("${trust.store:}")
	private Resource trustStore;

	@Value("${trust.store.password:}")
	private String trustStorePassword;

	@Bean
	public RestTemplate internalRestTemplate() throws KeyManagementException, NoSuchAlgorithmException,
			KeyStoreException, CertificateException, IOException {
		RestTemplate result = null;
		CloseableHttpClient httpClient = null;
		if (trustAllCerts) {
			final SSLContext sslContext = SSLContexts.custom().loadTrustMaterial((chain, authType) -> true).build();
			final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext, new NoopHostnameVerifier());
			final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
					.setTlsSocketStrategy(tlsStrategy).setDefaultTlsConfig(TlsConfig.custom().build()).build();

			httpClient = HttpClients.custom().setConnectionManager(cm).build();
		} else if (trustStore != null) {
			SSLContext sslContext = new SSLContextBuilder()
					.loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray()).build();
			final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext);
			final HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
					.setTlsSocketStrategy(tlsStrategy).setDefaultTlsConfig(TlsConfig.custom()
							// .setSupportedProtocols(TLS.V_1_3)
							.build())
					.build();
			httpClient = HttpClients.custom().setConnectionManager(cm).build();
		}
		if (httpClient != null) {
			ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			result = new RestTemplate(requestFactory);
		} else {
			result = new RestTemplate();
		}
		return result;
	}
}
