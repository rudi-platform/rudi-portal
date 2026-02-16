package org.rudi.microservice.acl.facade.config.security.jwt;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections4.MapUtils;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.facade.config.filter.AbstractJwtTokenUtil;
import org.rudi.common.facade.config.filter.JwtTokenData;
import org.rudi.common.service.util.ApplicationContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.extern.slf4j.Slf4j;

/**
 * Classe utilitaire des gestion de token JWT
 */
@Component
@Slf4j
public class JwtTokenUtil extends AbstractJwtTokenUtil implements Serializable {

	private static final long serialVersionUID = -2550185165626007488L;

	@Value("${module.oauth2.check-token-uri}")
	private String checkTokenUri;

	private transient JwtEncoder jwtEncoder;

	private transient JwtDecoder jwtDecoder;

	/**
	 * Generation d'un token
	 *
	 * @param claims   Issuer, Expiration, Subject, and the ID
	 * @param subject  sujet du token
	 * @param validity validity in seconde
	 * @return le token généré
	 */
	@Override
	protected String doGenerateToken(final Map<String, Object> claims, final String subject, final int validity)
			throws JOSEException {
		JwtClaimsSet.Builder jwtClaimsSetBuilder = JwtClaimsSet.builder().subject(subject).issuer(ISSUER_RUDI)
				.issuedAt(Instant.now()).id(UUID.randomUUID().toString())
				.expiresAt(Instant.now().plusMillis(validity * 1000));
		if (MapUtils.isNotEmpty(claims)) {
			for (Map.Entry<String, Object> claim : claims.entrySet()) {
				jwtClaimsSetBuilder.claim(claim.getKey(), claim.getValue());
			}
		}
		JwtEncoderParameters parameters = JwtEncoderParameters.from(jwtClaimsSetBuilder.build());

		Jwt jwt = getJwtEncoder().encode(parameters);
		return jwt.getTokenValue();
	}

	@Override
	protected void handleExternalAccount(JwtTokenData token, JWTClaimsSet claims) {
		// Nothing to do
		log.warn("External account not handled in common facade");
	}

	@Override
	protected void handlePortailAccount(JwtTokenData token, JWTClaimsSet claims) throws JsonProcessingException {
		String serializedConnectedUser = getTokenProperty(claims, CONNECTED_USER);
		AuthenticatedUser connectedUser = getMapper().readValue(serializedConnectedUser, AuthenticatedUser.class);
		token.setAccount(connectedUser);
	}

	@Override
	protected void verify(JwtTokenData token, SignedJWT jwt) throws JOSEException, MalformedURLException {
		if (isPortailIssuer(token) && checkTokenUri.startsWith(token.getIssuer())) {
			try {
				Jwt decodedJwt = getJwtDecoder().decode(token.getToken());
				log.debug("Decoded JWT success: {}", decodedJwt.getId());
				token.setHasError(false);
			} catch (JwtValidationException e) {
				log.debug("Decoded JWT validation failure: {}", e.getMessage());
				token.setHasError(true);
			} catch (JwtException e) {
				log.debug("Decoded JWT failure: {}", e.getMessage());
				token.setHasError(true);
			}

			log.debug("Verify Rudi issuer {}", token.isHasError());
		} else {
			token.setHasError(true);
			log.error("Unexpected issuer {} in provided token.", token.getIssuer());
		}
	}

	protected JwtEncoder getJwtEncoder() {
		if (jwtEncoder == null) {
			jwtEncoder = ApplicationContext.getBean(JwtEncoder.class);
		}
		return jwtEncoder;
	}

	protected JwtDecoder getJwtDecoder() {
		if (jwtDecoder == null) {
			jwtDecoder = ApplicationContext.getBean(JwtDecoder.class);
		}
		return jwtDecoder;
	}

}