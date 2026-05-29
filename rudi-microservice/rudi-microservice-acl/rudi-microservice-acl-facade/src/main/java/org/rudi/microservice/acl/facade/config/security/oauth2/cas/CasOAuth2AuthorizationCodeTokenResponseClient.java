/**
 * 
 */
package org.rudi.microservice.acl.facade.config.security.oauth2.cas;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestHeadersConverter;
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestParametersConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CasOAuth2AuthorizationCodeTokenResponseClient
		extends AbstractResponseClient<OAuth2AuthorizationCodeGrantRequest>
		implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

	public CasOAuth2AuthorizationCodeTokenResponseClient(boolean sslVerifier)
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		super(sslVerifier);
	}

	/**
	 * @see OAuth2AuthorizationCodeGrantRequestEntityConverter
	 * @return le converter
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Converter<OAuth2AuthorizationCodeGrantRequest, MultiValueMap<String, String>> buildParametersConverter() {
		return new DefaultOAuth2TokenRequestParametersConverter();
	}

	@Override
	protected Converter<OAuth2AuthorizationCodeGrantRequest, HttpHeaders> buildHeadersConverter() {
		return new DefaultOAuth2TokenRequestHeadersConverter<>();
	}

	@Override
	protected RequestEntity<MultiValueMap<String, String>> convert(
			AbstractOAuth2AuthorizationGrantRequest authorizationGrantRequest) {
		Assert.isAssignable(authorizationGrantRequest.getClass(), OAuth2AuthorizationCodeGrantRequest.class);
		OAuth2AuthorizationCodeGrantRequest request = (OAuth2AuthorizationCodeGrantRequest) authorizationGrantRequest;
		HttpHeaders headers = getHeadersConverter().convert(request);
		MultiValueMap<String, String> parameters = getParametersConverter().convert(request);
		URI uri = UriComponentsBuilder
				.fromUriString(authorizationGrantRequest.getClientRegistration().getProviderDetails().getTokenUri())
				.build().toUri();
		return new RequestEntity<>(parameters, headers, HttpMethod.POST, uri);
	}

}
