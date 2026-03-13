/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.oauth2;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.tika.utils.StringUtils;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.core.security.UserType;
import org.rudi.common.core.util.AnonymizerUtils;
import org.rudi.common.facade.config.filter.AbstractJwtTokenUtil;
import org.rudi.common.facade.config.filter.CommonSecurityConstants;
import org.rudi.common.facade.config.filter.JwtTokenData;
import org.rudi.common.facade.config.filter.OAuth2TokenData;
import org.rudi.microservice.acl.core.bean.Token;
import org.rudi.microservice.acl.core.bean.TokenSearchCritera;
import org.rudi.microservice.acl.facade.config.security.oauth2.cas.CasOAuth2CheckHelper;
import org.rudi.microservice.acl.service.token.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class OAuthCheckTokenController {

	private static final String EMAIL_CLAIM = "email";

	private static final String LASTNAME_CLAIM = "lastname";

	private static final String FIRSTNAME_CLAIM = "firstname";

	private static final String TYPE_CLAIM = "type";

	private static final String CONNECTED_USER_CLAIM = "connectedUser";

	private final TokenService tokenService;

	private final JwtDecoder jwtDecoder;

	private final AbstractJwtTokenUtil jwtTokenUtil;

	private final CasOAuth2CheckHelper casOAuth2CheckHelper;

	private final ObjectMapper objectMapper;

	@PostMapping(value = "/oauth2/check_token")
	public OAuth2TokenData checkToken(@RequestParam("token") String value) {
		if (StringUtils.isEmpty(value)) {
			return OAuth2TokenData.builder().active(false).errorCode(HttpStatus.BAD_REQUEST.value()).build();
		}

		OAuth2TokenData response = null;
		String clientId = null;
		String tokenValue = value;

		TokenSearchCritera tokenSearchCritera = TokenSearchCritera.builder().token(tokenValue).build();
		List<Token> tokens = tokenService.searchTokens(tokenSearchCritera);
		if (CollectionUtils.isEmpty(tokens)) {
			if (!isPortalIssuer(value)) {
				return casOAuth2CheckHelper.checkToken(value);
			} else {
				log.warn("================>Token not present in store {} and not delegate to CAS",
						AnonymizerUtils.anonymize(value));
			}
		} else {
			Token token = tokens.get(0);
			tokenValue = token.getValue();
			clientId = token.getRegisteredClientId();
		}
		if (tokens.size() > 1) {
			log.warn("Several identical token found {}", tokens.size());
			throw new InvalidBearerTokenException("Mismashuped token");
		}

		try {
			// décodage du token pour vérifier sa validité et récupérer les informations d'authentification
			Jwt jwt = jwtDecoder.decode(tokenValue);
			// construction de la réponse pour les attributs standard d'OAuth2 et les informations d'authentification
			OAuth2TokenData.OAuth2TokenDataBuilder builder = OAuth2TokenData.builder().active(true)
					.clientId(jwt.getSubject()).userName(jwt.getSubject()).scope(collectScopes(jwt))
					.exp(Optional.ofNullable(jwt.getExpiresAt()).orElse(Instant.now()).getEpochSecond())
					.jti(jwt.getClaimAsString("jti"));
			// prise en compte des attributs spécifiques à RUDI pour les utilisateurs authentifiés
			initAuthenticatedUser(jwt, builder);
			response = builder.build();
			log.debug("================>Validate token {}", AnonymizerUtils.anonymize(value));
		} catch (JwtValidationException e) {
			log.info("================>Expired token {}", e.getMessage());
			response = OAuth2TokenData.builder().active(false)
					.errorCode(CommonSecurityConstants.HTTP_CODE_TOKEN_EXPIRED).clientId(clientId).build();
		} catch (BadJwtException e) {
			log.info("================>Invalid signature token {}", e.getMessage());
			response = OAuth2TokenData.builder().active(false).errorCode(HttpStatus.UNAUTHORIZED.value())
					.clientId(clientId).build();
		} catch (Exception e) {
			log.info("================>Invalid token {}", e.getMessage());
			response = OAuth2TokenData.builder().active(false).errorCode(HttpStatus.UNAUTHORIZED.value())
					.clientId(clientId).build();
		}

		return response;
	}

	private void initAuthenticatedUser(Jwt jwt, OAuth2TokenData.OAuth2TokenDataBuilder builder)
			throws JsonMappingException, JsonProcessingException {
		if (jwt.hasClaim(CONNECTED_USER_CLAIM)) {
			String connectedUser = jwt.getClaim(CONNECTED_USER_CLAIM);
			AuthenticatedUser authenticatedUser = objectMapper.readerFor(AuthenticatedUser.class)
					.readValue(connectedUser);
			builder.firstname(authenticatedUser.getFirstname()).lastname(authenticatedUser.getLastname())
					.type(authenticatedUser.getType().toString()).email(authenticatedUser.getEmail())
					.authorities(collectAuthorities(authenticatedUser));
		} else {
			String type = jwt.getClaimAsString(TYPE_CLAIM);
			builder.firstname(jwt.getClaimAsString(FIRSTNAME_CLAIM)).lastname(jwt.getClaimAsString(LASTNAME_CLAIM))
					.type(type != null ? type : UserType.ROBOT.toString()).email(jwt.getClaimAsString(EMAIL_CLAIM))
					.authorities(collectAuthorities(jwt));
		}
	}

	private List<String> collectAuthorities(AuthenticatedUser authenticatedUser) {
		if (CollectionUtils.isNotEmpty(authenticatedUser.getRoles())) {
			return authenticatedUser.getRoles();
		} else {
			return List.of();
		}
	}

	private List<String> collectScopes(Jwt jwt) {
		return jwt.getClaimAsStringList("scope");
	}

	private List<String> collectAuthorities(Jwt jwt) {
		return jwt.getClaimAsStringList("authorities");
	}

	private boolean isPortalIssuer(String token) {
		try {
			SignedJWT jwt = jwtTokenUtil.getJWS(token);
			return jwtTokenUtil
					.isPortailIssuer(JwtTokenData.builder().issuer(jwt.getJWTClaimsSet().getIssuer()).build());
		} catch (Exception e) {
			log.debug("Token not issued by RUDI Portal");
		}
		return false;
	}
}
