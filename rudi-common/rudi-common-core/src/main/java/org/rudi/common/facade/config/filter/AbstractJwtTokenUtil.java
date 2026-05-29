package org.rudi.common.facade.config.filter;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
public abstract class AbstractJwtTokenUtil implements Serializable {

	private static final long serialVersionUID = -7253285508907149002L;

	public static final String ISSUER_RUDI = "Rudi#";

	public static final String CONNECTED_USER = "connectedUser";

	public static final String ORIGINAL_ISSUER = "original_issuer";

	public static final String ORIGINAL_IDTOKEN = "original_idtoken";

	/**
	 * Validité du token par défaut. Valeur par défaut : 10 minutes
	 */
	@Value("${security.jwt.validity:600}")
	private int tokenValidity;

	/**
	 * Validité du refresh. Valeur par défaut : 30 minutes
	 */
	@Value("${security.jwt.refresh.validity:1800}")
	private int refreshTokenValidity;

	@Getter(value = AccessLevel.PROTECTED)
	private ObjectMapper mapper = new ObjectMapper().configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	/**
	 * récupération une propriété d'un token
	 *
	 * @throws ParseException
	 */
	@SuppressWarnings("unchecked")
	public <T> T getTokenProperty(final String token, final String propertyName) throws ParseException {
		return (T) getAllClaimsFromToken(token).getClaim(propertyName);
	}

	public Collection<GrantedAuthority> getAuthoritiesFromToken(final String token) throws ParseException {
		return getTokenProperty(token, "authorities");
	}

	/**
	 * Retourne sujet associé au token
	 *
	 * @throws ParseException
	 */
	public String getSubjectFromToken(final String token) throws ParseException {
		return getClaimFromToken(token, getSubjectFunction());
	}

	/**
	 * Retourne la date d'expiration du token
	 *
	 * @throws ParseException
	 */
	public Date getExpirationDateFromToken(final String token) throws ParseException {
		return getClaimFromToken(token, getExpirationFunction());
	}

	public Date getIssuedAtDateFromToken(final String token) throws ParseException {
		return getClaimFromToken(token, getIssuedAtFunction());
	}

	/**
	 * Genration du token JWT et du token de refresh
	 */
	public Tokens generateTokens(final String accountLogin, final Object connectedUser)
			throws JOSEException, JsonProcessingException {
		// Génération du token d'authentification et de refesh
		final Tokens tokens = new Tokens();
		tokens.setJwtToken(
				CommonSecurityConstants.HEADER_TOKEN_JWT_PREFIX + generateJwtToken(accountLogin, connectedUser));
		tokens.setRefreshToken(
				CommonSecurityConstants.HEADER_TOKEN_JWT_PREFIX + generateRefreshToken(accountLogin, connectedUser));
		return tokens;
	}

	/**
	 * Generation de nouveau token à partir du refresh token
	 *
	 * @param refreshToken
	 * @return
	 * @throws RefreshTokenExpiredException
	 * @throws JsonProcessingException
	 * @throws JOSEException
	 */
	public Tokens generateNewJwtTokens(final String refreshToken)
			throws RefreshTokenExpiredException, JsonProcessingException, JOSEException {
		// Récupération des données du token de refresh
		final JwtTokenData refreshJtd = validateToken(refreshToken);
		// Si le token de refresh existe et qu'il n'est pas expiré
		if (!refreshJtd.isHasError() && !refreshJtd.isExpired()) {
			// Generation de nouveaux token (jwt et refresh)
			return generateTokens(refreshJtd.getSubject(), refreshJtd.getAccount());
		} else {
			// Exception car le tocken de refresh n'est pas valide
			throw new RefreshTokenExpiredException("Refresh token invalide");
		}
	}

	public String removePrefix(String tokenValue) {
		if (tokenValue != null && tokenValue.startsWith(CommonSecurityConstants.HEADER_TOKEN_JWT_PREFIX)) {
			return tokenValue.substring(CommonSecurityConstants.HEADER_TOKEN_JWT_PREFIX.length());
		} else {
			return tokenValue;
		}
	}

	/**
	 * Récupération des données du token tout en le validant
	 *
	 * @param requestToken token
	 */
	public JwtTokenData validateToken(final String requestToken) {
		final JwtTokenData token = new JwtTokenData();

		// Contrôle du préfixe dans la requete
		if (requestToken == null || !requestToken.startsWith(CommonSecurityConstants.HEADER_TOKEN_JWT_PREFIX)) {
			log.error("Le token ne commence pas avec la chaine Bearer");
			token.setHasError(true);
		} else {
			final String tokenJwt = requestToken.substring(CommonSecurityConstants.HEADER_TOKEN_JWT_PREFIX.length());
			token.setToken(tokenJwt);

			try {
				SignedJWT jwt = getJWS(tokenJwt);
				JWTClaimsSet claims = jwt.getJWTClaimsSet();

				handleSubject(token, claims);
				handleIssuer(token, claims);
				handleExpired(token, claims);
				handleAccount(token, claims);
				token.getProperties().putAll(claims.getClaims());

				verify(token, jwt);
			} catch (final Exception e) {
				log.error("impossible de récupérer le token JWT", e);
				token.setHasError(true);
			}
		}

		return token;
	}

	protected void handleIssuer(JwtTokenData token, JWTClaimsSet claims) {
		token.setIssuer(claims.getIssuer());
	}

	protected void handleAccount(JwtTokenData token, JWTClaimsSet claims) throws JsonProcessingException {
		if (isPortailIssuer(token)) {
			handlePortailAccount(token, claims);
		} else {
			handleExternalAccount(token, claims);
		}
	}

	protected void handleExpired(JwtTokenData token, JWTClaimsSet claims) {
		token.setExpired(isTokenExpired(claims));
	}

	/**
	 * Extraction du sujet
	 *
	 * @param token
	 * @param claims
	 */
	protected void handleSubject(JwtTokenData token, JWTClaimsSet claims) {
		token.setSubject(getSubjectFromClaims(claims));
	}

	/**
	 * Generation d'un token pour un utilisateur
	 *
	 * @param accountUid id de l'iutilisateur
	 * @throws JsonProcessingException
	 */
	protected String generateJwtToken(final String accountUid, final Object connectedUser)
			throws JOSEException, JsonProcessingException {
		return doGenerateToken(prepareClaims(connectedUser), accountUid, tokenValidity);
	}

	/**
	 * Prépare la map contenant les claims
	 *
	 * @param connectedUser
	 * @return
	 * @throws JsonProcessingException
	 */
	protected Map<String, Object> prepareClaims(final Object connectedUser) throws JsonProcessingException {
		Map<String, Object> claims = new HashMap<>();
		String serialiedConnectedUser = mapper.writeValueAsString(connectedUser);
		claims.put(CONNECTED_USER, serialiedConnectedUser);
		return claims;
	}

	/**
	 * Generation d'un token de refresh pour un utilisateur un tokeb de refresh permet de redemander un token
	 *
	 * @throws JOSEException
	 * @throws JsonProcessingException
	 */
	protected String generateRefreshToken(final String accountUid, final Object connectedUser)
			throws JOSEException, JsonProcessingException {
		return doGenerateToken(prepareClaims(connectedUser), accountUid, refreshTokenValidity);
	}

	/**
	 * Retourne les informations contenu dans un token
	 *
	 * @param token          token jwt
	 * @param claimsResolver Nom de la propriété
	 * @throws ParseException
	 */
	protected <T> T getClaimFromToken(final String token, final Function<JWTClaimsSet, T> claimsResolver)
			throws ParseException {
		final JWTClaimsSet claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * récupération de toutes les propriétes d'un token
	 *
	 * @param token
	 * @return
	 * @throws ParseException
	 */
	protected JWTClaimsSet getAllClaimsFromToken(final String token) throws ParseException {
		return getJWS(token).getJWTClaimsSet();
	}

	/**
	 * Détermine si le token est expiré
	 *
	 * @param token
	 * @return
	 */
	protected boolean isTokenExpired(final String token) {
		boolean result = false;
		try {
			Date d = getExpirationDateFromToken(token);
			Date now = new Date();
			if (d.after(now)) {
				result = true;
			}
		} catch (Exception e) {
			result = true;
		}
		return result;
	}

	/**
	 * Parsing d'un token (pas de validation)
	 *
	 * @param token
	 * @return
	 * @throws ParseException
	 */
	public SignedJWT getJWS(final String token) throws ParseException {
		return SignedJWT.parse(token);
	}

	/**
	 * Récupération une propriété d'un claims
	 *
	 * @param <T>
	 * @param claims
	 * @param propertyName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected <T> T getTokenProperty(final JWTClaimsSet claims, final String propertyName) {
		return (T) claims.getClaim(propertyName);
	}

	/**
	 * Retourne les informations contenu dans un token
	 *
	 * @param <T>
	 * @param claims         la liste des claims
	 * @param claimsResolver le resolver
	 * @return
	 */
	protected <T> T getClaimFromClaims(final JWTClaimsSet claims, final Function<JWTClaimsSet, T> claimsResolver) {
		return claimsResolver.apply(claims);
	}

	/**
	 * Retourne la date d'expiration du token
	 *
	 * @param claims
	 * @return
	 * @throws ParseException
	 */
	protected Date getExpirationDateFromToken(final JWTClaimsSet claims) {
		return claims.getExpirationTime();
	}

	/**
	 * Détermine si le token est expiré
	 *
	 * @param claims
	 * @return vrai si le token est expéré
	 */
	protected boolean isTokenExpired(final JWTClaimsSet claims) {
		boolean result = false;
		Date d = getExpirationDateFromToken(claims);
		Date now = new Date();
		if (d.before(now)) {
			result = true;
		}
		return result;
	}

	/**
	 * @param claims
	 * @return le subject
	 */
	protected String getSubjectFromClaims(JWTClaimsSet claims) {
		return getClaimFromClaims(claims, getSubjectFunction());
	}

	protected Function<JWTClaimsSet, String> getSubjectFunction() {
		return JWTClaimsSet::getSubject;
	}

	protected Function<JWTClaimsSet, Date> getExpirationFunction() {
		return JWTClaimsSet::getExpirationTime;
	}

	protected Function<JWTClaimsSet, Date> getIssuedAtFunction() {
		return JWTClaimsSet::getIssueTime;
	}

	/**
	 * Retoure si token parsé a été produit par le portail ou pas
	 *
	 * @param token
	 * @return
	 */
	public boolean isPortailIssuer(JwtTokenData token) {
		return ISSUER_RUDI.equals(token.getIssuer());
	}

	/**
	 * Generation d'un token
	 *
	 * @param claims   Issuer, Expiration, Subject, and the ID
	 * @param validity validity in seconde
	 */
	protected abstract String doGenerateToken(final Map<String, Object> claims, final String subject,
			final int validity) throws JOSEException;

	/**
	 * https://connect2id.com/products/nimbus-jose-jwt/examples/jwt-with-rsa-signature
	 *
	 * @param token
	 * @param jwt
	 * @throws JOSEException
	 * @throws MalformedURLException
	 * @throws BadJOSEException
	 */
	protected abstract void verify(JwtTokenData token, SignedJWT jwt) throws JOSEException, MalformedURLException;

	/**
	 * Traite un compte externe au portail
	 *
	 * @param token
	 * @param claims
	 * @throws JsonProcessingException
	 */
	protected abstract void handleExternalAccount(JwtTokenData token, JWTClaimsSet claims)
			throws JsonProcessingException;

	/**
	 * Traite un compte portail
	 * 
	 * @param token
	 * @param claims
	 * @throws JsonProcessingException
	 */
	protected abstract void handlePortailAccount(JwtTokenData token, JWTClaimsSet claims)
			throws JsonProcessingException;

}
