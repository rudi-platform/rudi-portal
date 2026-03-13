/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.jwt;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.core.security.UserType;
import org.rudi.common.facade.config.filter.AbstractJwtTokenUtil;
import org.rudi.common.facade.config.filter.CommonSecurityConstants;
import org.rudi.common.facade.config.filter.JwtTokenData;
import org.rudi.microservice.acl.core.bean.AbstractAddress;
import org.rudi.microservice.acl.core.bean.AddressType;
import org.rudi.microservice.acl.core.bean.EmailAddress;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.facade.config.security.AbstractDetailServiceImpl;
import org.rudi.microservice.acl.service.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProvider extends AbstractDetailServiceImpl implements AuthenticationProvider {
	@Value("${security.anonymous.login:anonymous}")
	private String anonymousUsername;

	private final UserService userService;

	private final PasswordEncoder passwordEncoder;

	private final JwtDecoder jwtDecoder;

	private final JwtTokenUtil jwtTokenUtil;

	private final JwtAuthenticationConverter jwtAuthenticationConverter;

	@Override
	public Authentication authenticate(Authentication authentication) {
		if (authentication instanceof UsernamePasswordAuthenticationToken) {
			return checkCredential(authentication);
		} else if (authentication instanceof BearerTokenAuthenticationToken bearer) {
			Jwt jwt = getJwt(bearer);
			String iss = jwt.getClaim(JwtClaimNames.ISS);
			if (AbstractJwtTokenUtil.ISSUER_RUDI.equals(iss)) {
				return authenticateRudiToken(bearer, jwt);
			} else {
				return authenticateCasToken(bearer, jwt);
			}

		} else {
			throw new IllegalArgumentException(
					"Only UsernamePasswordAuthenticationToken and BearerTokenAuthenticationToken are supported");
		}

	}

	private Authentication authenticateCasToken(BearerTokenAuthenticationToken bearer, Jwt jwt) {
		log.debug("Token with unknown issuer: {}", jwt.getIssuer());

		return null;
	}

	protected Authentication authenticateRudiToken(BearerTokenAuthenticationToken bearer, Jwt jwt) {
		log.debug("Token with RUDI issuer");
		String token = bearer.getToken();
		JwtTokenData jwtTokenData = jwtTokenUtil.validateToken(CommonSecurityConstants.HEADER_TOKEN_JWT_PREFIX + token);

		if (jwtTokenData != null && !jwtTokenData.isHasError() && !jwtTokenData.isExpired()) {
			return convertToken(jwtTokenData);
		} else {
			AbstractAuthenticationToken authenticationToken = jwtAuthenticationConverter.convert(jwt);
			if (authenticationToken != null && authenticationToken.getDetails() == null) {
				authenticationToken.setDetails(bearer.getDetails());
			}
			return authenticationToken;
		}
	}

	/**
	 * Check credential and return authentication objet
	 *
	 * @param authentication
	 * @return
	 */
	public Authentication checkCredential(Authentication authentication) {
		String login = authentication.getName();
		String password = (String) authentication.getCredentials();

		User user = userService.getUserByLogin(login, true);
		checkUser(user, login);
		checkPassword(authentication, user);

		AuthenticatedUser authenticatedUser = createAuthenticatedUser(user);

		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
				login, password, computeGrantedAuthorities(user));
		usernamePasswordAuthenticationToken.setDetails(authenticatedUser);
		return usernamePasswordAuthenticationToken;
	}

	public Authentication convertToken(JwtTokenData jwtTokenData) {
		try {
			String login = ((AuthenticatedUser) jwtTokenData.getAccount()).getLogin();

			User user = userService.getUserByLogin(login, true);
			checkUser(user, login);

			AuthenticatedUser authenticatedUser = createAuthenticatedUser(user);

			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
					login, null, computeGrantedAuthorities(user));
			usernamePasswordAuthenticationToken.setDetails(authenticatedUser);
			return usernamePasswordAuthenticationToken;
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid token", e);
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class)
				|| authentication.equals(BearerTokenAuthenticationToken.class);
	}

	private void checkUser(User user, String login) {
		if (user == null) {
			log.info("Impossible de trouver l'utilisateur: {}", login);
			userService.addFailedAttempt(login);
			throw new UsernameNotFoundException("Impossible de trouver l'utilisateur : " + login + ".");
		} else if (Boolean.TRUE.equals(user.getAccountLocked())) {
			log.warn("Le compte est vérouillé l'utilisateur: {}", login);
			throw new LockedException("Le compte est vérouillé");
		}
	}

	private void checkPassword(Authentication authentication, User user) {
		if (!isAnonymous(user)
				&& !passwordEncoder.matches(authentication.getCredentials().toString(), user.getPassword())) {
			if (!userService.recordAuthentication(user.getUuid(), false)) {
				log.info("Mot de passe erroné pour l'utilisateur: {}", user.getLogin());
				throw new BadCredentialsException("Mot de passe erroné.");
			} else {
				log.info("Compte verouillé: {}", user.getLogin());
				throw new LockedException("Account is locked");
			}
		} else {
			userService.recordAuthentication(user.getUuid(), true);
		}
	}

	private AuthenticatedUser createAuthenticatedUser(User user) {
		UserType userType;
		try {
			userType = UserType.valueOf(user.getType().name());
		} catch (Exception e) {
			userType = UserType.PERSON;
		}
		AuthenticatedUser authenticatedUser = new AuthenticatedUser(user.getLogin(), userType);
		authenticatedUser.setFirstname(user.getFirstname());
		authenticatedUser.setLastname(user.getLastname());
		authenticatedUser.setOrganization(user.getCompany());
		authenticatedUser.setRoles(computeRoles(user));
		if (CollectionUtils.isNotEmpty(user.getAddresses())) {
			for (AbstractAddress abstractAddress : user.getAddresses()) {
				if (abstractAddress.getType() == AddressType.EMAIL) {
					authenticatedUser.setEmail(((EmailAddress) abstractAddress).getEmail());
					break;
				}
			}
		}
		return authenticatedUser;
	}

	private boolean isAnonymous(User user) {
		if (CollectionUtils.isNotEmpty(user.getRoles())) {
			return user.getRoles().stream().anyMatch(role -> role.getCode().equalsIgnoreCase(anonymousUsername));
		} else {
			return false;
		}
	}

	private Jwt getJwt(BearerTokenAuthenticationToken bearer) {
		try {
			return jwtDecoder.decode(bearer.getToken());
		} catch (BadJwtException failed) {
			log.debug("Failed to authenticate since the JWT was invalid");
			throw new InvalidBearerTokenException(failed.getMessage(), failed);
		} catch (JwtException failed) {
			throw new AuthenticationServiceException(failed.getMessage(), failed);
		}
	}

}
