package org.rudi.microservice.acl.facade.controller;

import java.util.UUID;

import org.rudi.common.facade.config.filter.CommonSecurityConstants;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.acl.core.bean.Account;
import org.rudi.microservice.acl.core.bean.PasswordChange;
import org.rudi.microservice.acl.core.bean.Tokens;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.facade.config.security.cache.AccessTokenManager;
import org.rudi.microservice.acl.facade.controller.api.AccountApi;
import org.rudi.microservice.acl.service.account.AccountService;
import org.rudi.microservice.acl.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AccountController implements AccountApi {

	private final AccountService accountService;
	private final UserService userService;
	private final AccessTokenManager accessTokenManager;

	// Méthode accessible derrière authent si on est authentifié ça passe (anonymous = OK)
	@Override
	public ResponseEntity<Void> requestAccountCreation(Account account) throws AppServiceException {
		// Création du compte utilisateur
		accountService.registerAccount(account);
		return ResponseEntity.ok().build();
	}

	@Override
	public ResponseEntity<User> validateAccount(String token) throws Exception {
		return ResponseEntity.ok(accountService.validateAccount(token));
	}

	@Override
	public ResponseEntity<Void> requestPasswordChange(String email) {
		accountService.requestPasswordChange(email);
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<Void> checkPasswordChangeToken(UUID token) throws Exception {
		accountService.checkPasswordChangeToken(token);
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<Void> validatePasswordChange(PasswordChange change) throws AppServiceException {
		accountService.validatePasswordChange(change);
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<Boolean> isCreatedNotValidated(String login) throws Exception {
		return ResponseEntity.ok(accountService.isAccountCreatedNotValidated(login));
	}

	@Override
	public ResponseEntity<Void> accountLogout(String token) throws Exception {
		return ResponseEntity.noContent().build();
	}

	/**
	 * GET /account/{login}/must-validate-captcha : Indique si un compte doit valider le captcha pour se connecter Indique si un compte doit valider le
	 * captcha pour se connecter
	 *
	 * @param login Le login du compte (required)
	 * @return OK (status code 200) or Internal server error (status code 500)
	 */
	@Override
	public ResponseEntity<Boolean> mustValidateCaptcha(String login) throws Exception {
		return ResponseEntity.ok(userService.mustValidateCaptcha(login));
	}

	@Override
	public ResponseEntity<Tokens> authenticate1(String token) throws Exception {

		org.rudi.common.facade.config.filter.Tokens tokens = accessTokenManager.lookupTokens(token);
		if (tokens != null) {
			Tokens result = new Tokens().jwtToken(tokens.getJwtToken()).refreshToken(tokens.getRefreshToken());
			return ResponseEntity.status(HttpStatus.OK)
					.header(CommonSecurityConstants.HEADER_TOKEN_JWT_AUTHENT_KEY, result.getJwtToken())
					.header(CommonSecurityConstants.HEADER_X_TOKEN_KEY, result.getRefreshToken()).body(result);
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@Override
	public ResponseEntity<Tokens> authenticate2(String token) throws Exception {
		return authenticate1(token);
	}
}
