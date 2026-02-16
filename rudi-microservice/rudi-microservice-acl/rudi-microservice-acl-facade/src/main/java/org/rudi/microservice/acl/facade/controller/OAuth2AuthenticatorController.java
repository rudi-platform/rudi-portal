/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.controller;

import java.util.List;
import java.util.Optional;

import org.rudi.common.facade.helper.ControllerHelper;
import org.rudi.microservice.acl.core.bean.OAuth2AuthenticatorDescription;
import org.rudi.microservice.acl.facade.config.security.oauth2.authenticator.OAuth2AuthenticatorHelper;
import org.rudi.microservice.acl.facade.controller.api.Oauth2AuthenticatorsApi;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import lombok.RequiredArgsConstructor;

/**
 * 
 */
@RestController
@RequiredArgsConstructor
public class OAuth2AuthenticatorController implements Oauth2AuthenticatorsApi {

	private final OAuth2AuthenticatorHelper oAuth2AuthenticatorHelper;

	private final ControllerHelper controllerHelper;

	@Override
	public ResponseEntity<List<OAuth2AuthenticatorDescription>> getOAuth2Authenticators() throws Exception {
		return ResponseEntity.ok(oAuth2AuthenticatorHelper.getOAuth2AuthenticatorDescriptions(false));
	}

	@Override
	public ResponseEntity<Resource> getOAuth2AuthenticatorIcon(String authenticatorName) throws Exception {
		return controllerHelper.downloadableResponseEntity(
				oAuth2AuthenticatorHelper.getOAuth2AuthenticatorDescriptionIcon(authenticatorName));
	}

	@Override
	public ResponseEntity<Void> authenticateThroughAuthenticator(String authenticatorName) throws Exception {
		oAuth2AuthenticatorHelper.authenticateThroughAuthenticator(authenticatorName);
		return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).build();
	}

	@Override
	public Optional<NativeWebRequest> getRequest() {
		return Oauth2AuthenticatorsApi.super.getRequest();
	}

}
