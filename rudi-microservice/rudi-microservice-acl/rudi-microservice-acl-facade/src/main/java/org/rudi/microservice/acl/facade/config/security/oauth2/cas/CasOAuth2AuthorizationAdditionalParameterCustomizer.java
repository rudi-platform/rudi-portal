/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.oauth2.cas;

import org.apache.commons.collections.MapUtils;
import org.rudi.microservice.acl.core.bean.OAuth2AuthenticatorDescription;
import org.rudi.microservice.acl.facade.config.security.oauth2.authenticator.OAuth2AuthenticatorHelper;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 
 */
@Component
@RequiredArgsConstructor
public class CasOAuth2AuthorizationAdditionalParameterCustomizer {

	private final OAuth2AuthenticatorHelper authenticatorHelper;

	public void handleAdditionnalParameters(OAuth2AuthorizationRequest.Builder builder) {
		Object registrationId = builder.build().getAttribute("registration_id");
		OAuth2AuthenticatorDescription authenticatorDescription = authenticatorHelper
				.getOAuth2AuthenticatorDescription(registrationId.toString(), true);
		if (authenticatorDescription != null && authenticatorDescription.getProvider() != null
				&& MapUtils.isNotEmpty(authenticatorDescription.getProvider().getAdditionalParameters())) {
			authenticatorDescription.getProvider().getAdditionalParameters()
					.forEach((k, v) -> builder.additionalParameters(attrs -> attrs.put(k, v)));
		}
	}

}
