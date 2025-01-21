/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.oauth2;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.tika.utils.StringUtils;
import org.rudi.common.facade.config.filter.OAuth2TokenData;
import org.rudi.microservice.acl.core.bean.Token;
import org.rudi.microservice.acl.core.bean.TokenSearchCritera;
import org.rudi.microservice.acl.service.token.TokenService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class OAuthCheckTokenController {

	private final TokenService tokenService;

	private final JwtDecoder jwtDecoder;

	@PostMapping(value = "/oauth2/check_token")
	@ResponseBody
	public OAuth2TokenData checkToken(@RequestParam("token") String value) {
		if (StringUtils.isEmpty(value)) {
			return OAuth2TokenData.builder().active(false).build();
		}

		OAuth2TokenData response = null;
		String clientId = null;
		String tokenValue = value;

		TokenSearchCritera tokenSearchCritera = TokenSearchCritera.builder().token(tokenValue).build();
		List<Token> tokens = tokenService.searchTokens(tokenSearchCritera);
		if (CollectionUtils.isEmpty(tokens)) {
			log.warn("================>Token not present in store");
			return OAuth2TokenData.builder().active(false).clientId(clientId).build();
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
			Jwt jwt = jwtDecoder.decode(tokenValue);
			response = OAuth2TokenData.builder().active(true).clientId(jwt.getSubject()).userName(jwt.getSubject())
					.scope(collectScopes(jwt))
					.exp(Optional.ofNullable(jwt.getExpiresAt()).orElse(Instant.now()).getEpochSecond())
					.authorities(collectAuthorities(jwt)).jti(jwt.getClaimAsString("jti")).build();
		} catch (JwtValidationException e) {
			log.info("================>Expired token {}", e.getMessage());
			response = OAuth2TokenData.builder().active(false).clientId(clientId).build();
		} catch (Exception e) {
			log.info("================>Invalid token {}", e.getMessage());
			response = OAuth2TokenData.builder().active(false).clientId(clientId).build();
		}

		return response;
	}

	private List<String> collectScopes(Jwt jwt) {
		return jwt.getClaimAsStringList("scope");
	}

	private List<String> collectAuthorities(Jwt jwt) {
		return jwt.getClaimAsStringList("authorities");
	}

}
