package org.rudi.microservice.acl.facade.config.security.oauth2.cas;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestParametersConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

/**
 * @author FNI18300
 * @see org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
 */
public class CasOAuth2AccessTokenResponseClient extends AbstractResponseClient
		implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

	private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";

	@SuppressWarnings("unchecked")
	private Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> requestEntityConverter = new DefaultOAuth2TokenRequestParametersConverter();

	private RestOperations restOperations;

	public CasOAuth2AccessTokenResponseClient(boolean sslVerifier)
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		super(sslVerifier);
		this.restOperations = buildRestTemplate();
	}

	@Override
	public OAuth2AccessTokenResponse getTokenResponse(
			OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
		Assert.notNull(authorizationCodeGrantRequest, "authorizationCodeGrantRequest cannot be null");
		RequestEntity<?> request = this.requestEntityConverter.convert(authorizationCodeGrantRequest);
		ResponseEntity<OAuth2AccessTokenResponse> response = getResponse(request);
		OAuth2AccessTokenResponse tokenResponse = response.getBody();
		if (tokenResponse != null && CollectionUtils.isEmpty(tokenResponse.getAccessToken().getScopes())) {
			// As per spec, in Section 5.1 Successful Access Token Response
			// https://tools.ietf.org/html/rfc6749#section-5.1
			// If AccessTokenResponse.scope is empty, then default to the scope
			// originally requested by the client in the Token Request
			// @formatter:off
			tokenResponse = OAuth2AccessTokenResponse.withResponse(tokenResponse)
					.scopes(authorizationCodeGrantRequest.getClientRegistration().getScopes())
					.build();
			// @formatter:on
		}
		return tokenResponse;
	}

	private ResponseEntity<OAuth2AccessTokenResponse> getResponse(RequestEntity<?> request) {
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
	 * Sets the {@link Converter} used for converting the {@link OAuth2AuthorizationCodeGrantRequest} to a {@link RequestEntity} representation of the
	 * OAuth 2.0 Access Token Request.
	 * 
	 * @param requestEntityConverter the {@link Converter} used for converting to a {@link RequestEntity} representation of the Access Token Request
	 */
	public void setRequestEntityConverter(
			Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> requestEntityConverter) {
		Assert.notNull(requestEntityConverter, "requestEntityConverter cannot be null");
		this.requestEntityConverter = requestEntityConverter;
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
