package org.rudi.microservice.acl.facade.config.security.oauth2.cas;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequestEntityConverter;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.UnknownContentTypeException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;

/**
 * @see org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
 */
@RequiredArgsConstructor
public class CasOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private static final String ERROR_USER_INFO = "An error occurred while attempting to retrieve the UserInfo Resource from '";

	private static final String MISSING_USER_INFO_URI_ERROR_CODE = "missing_user_info_uri";

	private static final String MISSING_USER_NAME_ATTRIBUTE_ERROR_CODE = "missing_user_name_attribute";

	private static final String INVALID_USER_INFO_RESPONSE_ERROR_CODE = "invalid_user_info_response";

	private static final MediaType JWT_MEDIA_TYPE = MediaType.valueOf("application/jwt");

	private static final TypeReference<Map<String, Object>> PARAMETERIZED_RESPONSE_TYPE = new TypeReference<>() {
	};

	private Converter<OAuth2UserRequest, RequestEntity<?>> requestEntityConverter = new OAuth2UserRequestEntityConverter();

	private Converter<OAuth2UserRequest, Converter<Map<String, Object>, Map<String, Object>>> attributesConverter = request -> attributes -> attributes;

	private final ObjectMapper objectMapper;

	private final RestOperations restOperations;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		Assert.notNull(userRequest, "userRequest cannot be null");
		String userNameAttributeName = getUserNameAttributeName(userRequest);

		RequestEntity<?> request = requestEntityConverter.convert(userRequest);

		ResponseEntity<String> response = getResponse(userRequest, request);

		MediaType mediaType = response.getHeaders().getContentType();
		if (MediaType.APPLICATION_JSON.includes(mediaType)) {
			return loadJsonUser(userRequest, userNameAttributeName, response);
		} else if (JWT_MEDIA_TYPE.includes(mediaType)) {
			return loadJwtUser(userRequest, userNameAttributeName, response);
		} else {
			String errorMessage = ERROR_USER_INFO
					+ userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri()
					+ "': response contains invalid content type '" + mediaType + "'. "
					+ "The UserInfo Response should return a JSON object (content type 'application/json') "
					+ "that contains a collection of name and value pairs of the claims about the authenticated End-User. ";
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE, errorMessage, null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
		}

	}

	/**
	 * Loads the user attributes from the UserInfo Response as a JSON object and constructs an {@link OAuth2User} with the attributes and authorities.
	 * 
	 * @param userRequest           the user request
	 * @param userNameAttributeName the name of the user attribute used to access the username from the user attributes
	 * @param response              the response containing the user attributes as a JSON object
	 * @return the user
	 */
	protected OAuth2User loadJsonUser(OAuth2UserRequest userRequest, String userNameAttributeName,
			ResponseEntity<String> response) {
		OAuth2AccessToken token = userRequest.getAccessToken();
		try {
			Map<String, Object> originalAttributes = objectMapper.readerFor(PARAMETERIZED_RESPONSE_TYPE)
					.readValue(response.getBody());
			Converter<Map<String, Object>, Map<String, Object>> converter = attributesConverter.convert(userRequest);
			if (converter != null) {
				Map<String, Object> attributes = converter.convert(originalAttributes);
				Collection<GrantedAuthority> authorities = getAuthorities(token, attributes, userNameAttributeName);
				return new DefaultOAuth2User(authorities, attributes, userNameAttributeName);
			} else {
				throw new IllegalStateException("The attributesConverter returned a null Converter");
			}
		} catch (Exception e) {
			String errorMessage = ERROR_USER_INFO
					+ userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri()
					+ "' as JSON";
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE, errorMessage, null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), e);
		}
	}

	/**
	 * Loads the user attributes from the UserInfo Response as a JWT and constructs an {@link OAuth2User} with the attributes and authorities.
	 * 
	 * @param userRequest           the user request
	 * @param userNameAttributeName the name of the user attribute used to access the username from the user attributes
	 * @param response              the response containing the user attributes as a JWT
	 * @return the user
	 */
	protected OAuth2User loadJwtUser(OAuth2UserRequest userRequest, String userNameAttributeName,
			ResponseEntity<String> response) {
		OAuth2AccessToken token = userRequest.getAccessToken();
		try {
			SignedJWT signedJwt = SignedJWT.parse(response.getBody());
			Map<String, Object> originalAttributes = signedJwt.getJWTClaimsSet().getClaims();
			Converter<Map<String, Object>, Map<String, Object>> converter = attributesConverter.convert(userRequest);
			if (converter != null) {
				Map<String, Object> attributes = converter.convert(originalAttributes);
				Collection<GrantedAuthority> authorities = getAuthorities(token, attributes, userNameAttributeName);
				return new DefaultOAuth2User(authorities, attributes, userNameAttributeName);
			} else {
				throw new IllegalStateException("The attributesConverter returned a null Converter");
			}
		} catch (Exception e) {
			String errorMessage = ERROR_USER_INFO
					+ userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri()
					+ "' as a JWT";
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE, errorMessage, null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), e);
		}
	}

	/**
	 * Use this strategy to adapt user attributes into a format understood by Spring Security; by default, the original attributes are preserved.
	 *
	 * <p>
	 * This can be helpful, for example, if the user attribute is nested. Since Spring Security needs the username attribute to be at the top level, you
	 * can use this method to do:
	 *
	 * <pre>
	 *     DefaultOAuth2UserService userService = new DefaultOAuth2UserService();
	 *     userService.setAttributesConverter((userRequest) -> (attributes) ->
	 *         Map&lt;String, Object&gt; userObject = (Map&lt;String, Object&gt;) attributes.get("user");
	 *         attributes.put("user-name", userObject.get("user-name"));
	 *         return attributes;
	 *     });
	 * </pre>
	 * 
	 * @param attributesConverter the attribute adaptation strategy to use
	 * @since 6.3
	 */
	public void setAttributesConverter(
			Converter<OAuth2UserRequest, Converter<Map<String, Object>, Map<String, Object>>> attributesConverter) {
		Assert.notNull(attributesConverter, "attributesConverter cannot be null");
		this.attributesConverter = attributesConverter;
	}

	private ResponseEntity<String> getResponse(OAuth2UserRequest userRequest, RequestEntity<?> request) {
		try {
			return restOperations.exchange(request, String.class);
		} catch (OAuth2AuthorizationException ex) {
			OAuth2Error oauth2Error = ex.getError();
			StringBuilder errorDetails = new StringBuilder();
			errorDetails.append("Error details: [");
			errorDetails.append("UserInfo Uri: ")
					.append(userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri());
			errorDetails.append(", Error Code: ").append(oauth2Error.getErrorCode());
			if (oauth2Error.getDescription() != null) {
				errorDetails.append(", Error Description: ").append(oauth2Error.getDescription());
			}
			errorDetails.append("]");
			oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE,
					"An error occurred while attempting to retrieve the UserInfo Resource: " + errorDetails.toString(),
					null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
		} catch (UnknownContentTypeException ex) {
			String errorMessage = ERROR_USER_INFO
					+ userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri()
					+ "': response contains invalid content type '" + ex.getContentType().toString() + "'. "
					+ "The UserInfo Response should return a JSON object (content type 'application/json') "
					+ "that contains a collection of name and value pairs of the claims about the authenticated End-User. "
					+ "Please ensure the UserInfo Uri in UserInfoEndpoint for Client Registration '"
					+ userRequest.getClientRegistration().getRegistrationId() + "' conforms to the UserInfo Endpoint, "
					+ "as defined in OpenID Connect 1.0: 'https://openid.net/specs/openid-connect-core-1_0.html#UserInfo'";
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE, errorMessage, null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
		} catch (RestClientException ex) {
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE,
					"An error occurred while attempting to retrieve the UserInfo Resource: " + ex.getMessage(), null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
		}
	}

	private String getUserNameAttributeName(OAuth2UserRequest userRequest) {
		if (!StringUtils
				.hasText(userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUri())) {
			OAuth2Error oauth2Error = new OAuth2Error(MISSING_USER_INFO_URI_ERROR_CODE,
					"Missing required UserInfo Uri in UserInfoEndpoint for Client Registration: "
							+ userRequest.getClientRegistration().getRegistrationId(),
					null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
		}
		String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
				.getUserNameAttributeName();
		if (!StringUtils.hasText(userNameAttributeName)) {
			OAuth2Error oauth2Error = new OAuth2Error(MISSING_USER_NAME_ATTRIBUTE_ERROR_CODE,
					"Missing required \"user name\" attribute name in UserInfoEndpoint for Client Registration: "
							+ userRequest.getClientRegistration().getRegistrationId(),
					null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
		}
		return userNameAttributeName;
	}

	private Collection<GrantedAuthority> getAuthorities(OAuth2AccessToken token, Map<String, Object> attributes,
			String userNameAttributeName) {
		Collection<GrantedAuthority> authorities = new LinkedHashSet<>();
		authorities.add(new OAuth2UserAuthority(attributes, userNameAttributeName));
		for (String authority : token.getScopes()) {
			authorities.add(new SimpleGrantedAuthority("SCOPE_" + authority));
		}
		return authorities;
	}

	/**
	 * Sets the {@link Converter} used for converting the {@link OAuth2UserRequest} to a {@link RequestEntity} representation of the UserInfo Request.
	 * 
	 * @param requestEntityConverter the {@link Converter} used for converting to a {@link RequestEntity} representation of the UserInfo Request
	 * @since 5.1
	 */
	public final void setRequestEntityConverter(Converter<OAuth2UserRequest, RequestEntity<?>> requestEntityConverter) {
		Assert.notNull(requestEntityConverter, "requestEntityConverter cannot be null");
		this.requestEntityConverter = requestEntityConverter;
	}

}
