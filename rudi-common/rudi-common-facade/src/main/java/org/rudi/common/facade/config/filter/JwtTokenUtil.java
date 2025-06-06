package org.rudi.common.facade.config.filter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.rudi.common.core.security.AuthenticatedUser;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
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
		JWSSigner signer = new MACSigner(getSecretKey());
		JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder().subject(subject).issuer(ISSUER_RUDI)
				.jwtID(getJWTId()).expirationTime(new Date(System.currentTimeMillis() + validity * 1000))
				.issueTime(new Date(System.currentTimeMillis()));

		if (MapUtils.isNotEmpty(claims)) {
			for (Map.Entry<String, Object> claim : claims.entrySet()) {
				jwtClaimsSetBuilder.claim(claim.getKey(), claim.getValue());
			}
		}

		JWTClaimsSet claimsSet = jwtClaimsSetBuilder.build();

		SignedJWT signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS512).build(), claimsSet);
		signedJWT.sign(signer);

		return signedJWT.serialize();
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

}