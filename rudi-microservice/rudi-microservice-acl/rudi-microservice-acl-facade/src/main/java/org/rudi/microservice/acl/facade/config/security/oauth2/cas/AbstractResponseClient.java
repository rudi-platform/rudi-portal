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
import org.rudi.common.core.util.AnonymizerUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract class for response client, to factorize code between {@link CasOAuth2AccessTokenResponseClient} and
 * 
 * @author FNI18300
 * @see org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
 */
@Slf4j
public abstract class AbstractResponseClient<T extends AbstractOAuth2AuthorizationGrantRequest> {

	private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";

	@Getter
	private boolean sslVerifier = true;

	@Getter(lombok.AccessLevel.PROTECTED)
	private RestOperations restOperations;

	@Getter(lombok.AccessLevel.PROTECTED)
	@Setter
	private Converter<T, MultiValueMap<String, String>> parametersConverter;

	@Getter(lombok.AccessLevel.PROTECTED)
	@Setter
	private Converter<T, HttpHeaders> headersConverter;

	protected AbstractResponseClient(boolean sslVerifier)
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		super();
		this.sslVerifier = sslVerifier;
		this.restOperations = buildRestTemplate();
		this.parametersConverter = buildParametersConverter();
		this.headersConverter = buildHeadersConverter();
	}

	public OAuth2AccessTokenResponse getTokenResponse(T authorizationCodeGrantRequest) {
		Assert.notNull(authorizationCodeGrantRequest, "authorizationCodeGrantRequest cannot be null");
		RequestEntity<?> request = convert(authorizationCodeGrantRequest);
		ResponseEntity<OAuth2AccessTokenResponse> response = getResponse(request);
		// As per spec, in Section 5.1 Successful Access Token Response
		// https://tools.ietf.org/html/rfc6749#section-5.1
		// If AccessTokenResponse.scope is empty, then we assume all requested scopes
		// were
		// granted.
		// However, we use the explicit scopes returned in the response (if any).
		OAuth2AccessTokenResponse body = response.getBody();
		if (log.isInfoEnabled() && body != null) {
			log.info("getTokenResponse {} {}", body.getAdditionalParameters(),
					body.getAccessToken() != null
							? body.getAccessToken().getTokenType().getValue() + " "
									+ AnonymizerUtils.anonymize(body.getAccessToken().getTokenValue())
							: "");
		} else if (log.isInfoEnabled()) {
			log.info("getTokenResponse : null");
		}
		return body;
	}

	protected abstract RequestEntity<?> convert(AbstractOAuth2AuthorizationGrantRequest authorizationCodeGrantRequest);

	protected abstract Converter<T, HttpHeaders> buildHeadersConverter();

	protected abstract Converter<T, MultiValueMap<String, String>> buildParametersConverter();

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

	protected ResponseEntity<OAuth2AccessTokenResponse> getResponse(RequestEntity<?> request) {
		try {
			return this.restOperations.exchange(request, OAuth2AccessTokenResponse.class);
		} catch (RestClientException ex) {
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE,
					"An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: "
							+ ex.getMessage(),
					null);
			throw new OAuth2AuthorizationException(oauth2Error, ex);
		}
	}

	/**
	 * Sets the {@link RestOperations} used when requesting the OAuth 2.0 Access Token Response.
	 *
	 * <p>
	 * <b>NOTE:</b> At a minimum, the supplied {@code restOperations} must be configured with the following:
	 * <ol>
	 * <li>{@link HttpMessageConverter}'s - {@link FormHttpMessageConverter} and {@link OAuth2AccessTokenResponseHttpMessageConverter}</li>
	 * <li>{@link ResponseErrorHandler} - {@link OAuth2ErrorResponseErrorHandler}</li>
	 * </ol>
	 * 
	 * @param restOperations the {@link RestOperations} used when requesting the Access Token Response
	 */
	public void setRestOperations(RestOperations restOperations) {
		Assert.notNull(restOperations, "restOperations cannot be null");
		this.restOperations = restOperations;
	}
}
