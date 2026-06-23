package org.rudi.microservice.acl.facade.controller;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.facade.config.filter.AbstractJwtTokenUtil;
import org.rudi.common.facade.config.filter.CommonSecurityConstants;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.acl.core.bean.Account;
import org.rudi.microservice.acl.core.bean.OAuth2AuthenticatorDescription;
import org.rudi.microservice.acl.core.bean.PasswordChange;
import org.rudi.microservice.acl.core.bean.Tokens;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.facade.config.security.TokenManager;
import org.rudi.microservice.acl.facade.config.security.cache.AccessTokenManager;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtTokenUtil;
import org.rudi.microservice.acl.facade.config.security.oauth2.authenticator.OAuth2AuthenticatorHelper;
import org.rudi.microservice.acl.facade.controller.api.AccountApi;
import org.rudi.microservice.acl.service.account.AccountService;
import org.rudi.microservice.acl.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AccountController implements AccountApi {

	private final AccountService accountService;
	private final UserService userService;
	private final AccessTokenManager accessTokenManager;
	private final TokenManager tokenManager;
	private final JwtTokenUtil jwtTokenUtil;
	private final OAuth2AuthenticatorHelper oAuth2AuthenticatorHelper;
	private final ObjectMapper objectMapper;

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
	public ResponseEntity<String> accountLogout(String refreshToken, String authorizationToken) throws Exception {
		String authorizationHeader = extractAuthorizationHeader(authorizationToken);
		String logoutUri = null;
		if (authorizationHeader != null) {
			try {
				OAuth2AuthenticatorDescription oAuth2AuthenticatorDescription = lookupOAuth2AuthenticatorDescription(
						authorizationHeader);
				String token = lookpupOriginalToken(authorizationHeader);
				log.info("Déconnexion du compte, oAuth2AuthenticatorDescription : {}",
						oAuth2AuthenticatorDescription != null ? oAuth2AuthenticatorDescription.getName()
								: "no provider");
				logoutUri = extractLogoutUri(oAuth2AuthenticatorDescription, token);
				log.info("Déconnexion du compte, logoutUri : {}", logoutUri);
			} catch (Exception e) {
				// En cas d'erreur, on continue la déconnexion locale
				log.warn("Erreur lors de la déconnexion du compte", e);
			}
		}
		if (logoutUri == null) {
			log.info("Déconnexion du compte, pas de logoutUri, déconnexion locale");
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.ok(objectMapper.writer().forType(String.class).writeValueAsString(logoutUri));
		}
	}

	protected String extractAuthorizationHeader(String authorizationToken) {
		if (authorizationToken != null) {
			return jwtTokenUtil.removePrefix(authorizationToken);
		}
		return null;
	}

	protected OAuth2AuthenticatorDescription lookupOAuth2AuthenticatorDescription(String authorizationHeader)
			throws ParseException {
		SignedJWT signedJWT = jwtTokenUtil.getJWS(authorizationHeader);
		String issuer = signedJWT.getJWTClaimsSet().getIssuer();
		OAuth2AuthenticatorDescription oAuth2AuthenticatorDescription = oAuth2AuthenticatorHelper
				.getOAuth2AuthenticatorDescriptionByIssuer(issuer, true);
		if (oAuth2AuthenticatorDescription == null) {
			log.info(
					"Aucun OAuth2AuthenticatorDescription trouvé pour l'issuer {}, tentative de lookup avec original_issuer",
					issuer);
			issuer = lookpupOriginalIssuer(signedJWT);
			if (StringUtils.isNotEmpty(issuer)) {
				log.info("Lookup OAuth2AuthenticatorDescription avec original_issuer {}", issuer);
				oAuth2AuthenticatorDescription = oAuth2AuthenticatorHelper
						.getOAuth2AuthenticatorDescriptionByIssuer(issuer, true);
			} else {
				log.info("Aucun original_issuer trouvé dans le token JWT");
			}
		}
		return oAuth2AuthenticatorDescription;
	}

	protected String lookpupOriginalIssuer(SignedJWT signedJWT) throws ParseException {
		log.info("Lookup original_issuer dans le token JWT");
		String issuer = signedJWT.getJWTClaimsSet().getStringClaim(AbstractJwtTokenUtil.ORIGINAL_ISSUER);
		if (issuer == null) {
			log.info(
					"Aucun original_issuer trouvé dans les claims du token JWT, tentative de lookup dans connectedUser");
			Object connectedUser = signedJWT.getJWTClaimsSet().getClaims().get(AbstractJwtTokenUtil.CONNECTED_USER);
			if (connectedUser instanceof AuthenticatedUser authenticatedUser) {
				log.info("Lookup original_issuer dans connectedUser");
				issuer = authenticatedUser.getData(AbstractJwtTokenUtil.ORIGINAL_ISSUER);
			} else if (connectedUser instanceof String connectedUserStr) {
				log.info("connectedUser est une chaîne de caractères, tentative de parsing en AuthenticatedUser");
				try {
					AuthenticatedUser authenticatedUser = objectMapper.readValue(connectedUserStr,
							AuthenticatedUser.class);
					issuer = authenticatedUser.getData(AbstractJwtTokenUtil.ORIGINAL_ISSUER);
				} catch (Exception e) {
					log.warn("Erreur lors du parsing de connectedUser en AuthenticatedUser", e);
				}
			} else {
				log.info("Aucun original_issuer trouvé dans les claims du token JWT");
			}
		}
		return issuer;
	}

	protected String lookpupOriginalToken(String authorizationHeader)
			throws ParseException, JsonMappingException, JsonProcessingException {
		SignedJWT signedJWT = jwtTokenUtil.getJWS(authorizationHeader);
		String tokenId = null;
		AuthenticatedUser authenticatedUser = null;
		log.info("Lookup original id token from token JWT");
		Object connectedUserObject = signedJWT.getJWTClaimsSet().getClaims().get(AbstractJwtTokenUtil.CONNECTED_USER);
		if (connectedUserObject instanceof String connectedUserStr) {
			log.info("connectedUser est une chaîne de caractères, tentative de parsing en AuthenticatedUser");
			authenticatedUser = objectMapper.readValue(connectedUserStr, AuthenticatedUser.class);
		} else if (connectedUserObject instanceof AuthenticatedUser connectedUserAuth) {
			log.info("Lookup original id token dans connectedUser");
			authenticatedUser = connectedUserAuth;
		} else {
			log.info("Aucun original id token trouvé dans les claims du token JWT");
		}
		if (authenticatedUser != null) {
			tokenId = authenticatedUser.getData(AbstractJwtTokenUtil.ORIGINAL_TOKEN_ID);
			if (tokenId != null) {
				return tokenManager.getTokenValue(tokenId);
			}
		}
		return null;
	}

	protected String extractLogoutUri(OAuth2AuthenticatorDescription oAuth2AuthenticatorDescription, String token) {
		if (oAuth2AuthenticatorDescription != null && oAuth2AuthenticatorDescription.getProvider() != null) {
			String logoutUri = oAuth2AuthenticatorDescription.getProvider().getLogoutUri();
			Map<String, String> parameters = new HashMap<>();
			parameters.put(OAuth2AuthenticatorHelper.SERVER_URL_PARAMETER,
					oAuth2AuthenticatorDescription.getServerUrl());
			if (StringUtils.isNotEmpty(token)) {
				parameters.put(OAuth2AuthenticatorHelper.TOKEN_PARAMETER, token);
			}
			return oAuth2AuthenticatorHelper.convertUrl(logoutUri, parameters);
		}
		return null;
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
