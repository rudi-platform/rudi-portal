/**
 * RUDI Portail
 */
package org.rudi.microservice.acl.facade.config.security.oauth2.cas;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.core.security.RoleCodes;
import org.rudi.common.facade.config.filter.AbstractJwtTokenUtil;
import org.rudi.common.facade.config.filter.CommonSecurityConstants;
import org.rudi.common.facade.config.filter.Tokens;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.acl.core.bean.OAuth2AttributeMapping;
import org.rudi.microservice.acl.core.bean.OAuth2AuthenticatorDescription;
import org.rudi.microservice.acl.core.bean.Role;
import org.rudi.microservice.acl.core.bean.RoleSearchCriteria;
import org.rudi.microservice.acl.core.bean.Token;
import org.rudi.microservice.acl.core.bean.TokenManagement;
import org.rudi.microservice.acl.core.bean.TokenType;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.core.bean.UserAttribute;
import org.rudi.microservice.acl.core.bean.UserType;
import org.rudi.microservice.acl.facade.config.security.AbstractAuthenticationSuccessHandler;
import org.rudi.microservice.acl.facade.config.security.TokenManager;
import org.rudi.microservice.acl.facade.config.security.cache.AccessTokenManager;
import org.rudi.microservice.acl.facade.config.security.oauth2.authenticator.OAuth2AuthenticatorHelper;
import org.rudi.microservice.acl.service.role.RoleService;
import org.rudi.microservice.acl.service.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Component
@Slf4j
public class CasOAuth2AuthenticationSuccessHandler extends AbstractAuthenticationSuccessHandler {

	private static final String DEFAULT_EMAIL_OAUTH2_ATTRIBUTE = "email";

	private static final String DEFAULT_LASTNAME_OAUTH2_ATTRIBUTE = "family_name";

	private static final String DEFAULT_FISRTNAME_OAUTH2_ATTRIBUTE = "given_name";

	@Value("${frontend.urlServer:/}")
	private String frontEndUrlServer;

	@Value("${rudi.oauth2.cas.rewrite:true}")
	private boolean oauthCasRewrite;

	private final UserService userService;

	private final RoleService roleService;

	private final AccessTokenManager accessTokenManager;

	private final TokenManager tokenManager;

	private final OAuth2AuthenticatorHelper oAuth2AuthenticatorHelper;

	private final OAuth2AuthorizedClientRepository authorizedClientRepository;

	public CasOAuth2AuthenticationSuccessHandler(UserService userService, RoleService roleService,
			AccessTokenManager accessTokenManager, TokenManager tokenHelper,
			OAuth2AuthenticatorHelper oAuth2AuthenticatorHelper,
			OAuth2AuthorizedClientRepository authorizedClientRepository) {
		super();
		this.userService = userService;
		this.roleService = roleService;
		this.accessTokenManager = accessTokenManager;
		this.tokenManager = tokenHelper;
		this.oAuth2AuthenticatorHelper = oAuth2AuthenticatorHelper;
		this.authorizedClientRepository = authorizedClientRepository;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		if (authentication instanceof OAuth2AuthenticationToken customToken) {

			try {
				// on récupère les infos de l'utilisateur
				String registrationId = customToken.getAuthorizedClientRegistrationId();
				TokenManagement tokenManagement = getTokenManagement(registrationId);
				AuthenticatedUser authenticatedUser = convert(customToken, registrationId);
				updateUser(authenticatedUser);

				customToken.setDetails(authenticatedUser);
				SecurityContextHolder.getContext().setAuthentication(customToken);
				OAuth2AuthorizedClient authorizedClient = authorizedClientRepository
						.loadAuthorizedClient(registrationId, customToken, request);
				// On ajoute l'issuer d'origine dans les données de l'utilisateur pour pouvoir le réutiliser dans les autres services
				assignOriginalIssuer(authorizedClient, authenticatedUser);
				assignOriginalToken(authentication, authenticatedUser);

				Tokens tokens = null;
				// Si on utilise CAS avec gestion des tokens par le provider, on réutilise les tokens OIDC
				if (oauthCasRewrite
						&& (tokenManagement == TokenManagement.ALWAYS_PROVIDER
								|| tokenManagement == TokenManagement.IF_POSSIBLE_PROVIDER)
						&& customToken.getPrincipal() instanceof DefaultOidcUser defaultOidcUser
						&& authorizedClient != null && authorizedClient.getRefreshToken() != null) {

					tokens = new Tokens(
							CommonSecurityConstants.HEADER_TOKEN_JWT_PREFIX
									+ defaultOidcUser.getIdToken().getTokenValue(),
							CommonSecurityConstants.HEADER_TOKEN_JWT_PREFIX
									+ authorizedClient.getRefreshToken().getTokenValue());
				} else {
					tokens = generateTokens(authenticatedUser);
				}

				log.debug("Token generated");
				String accessToken = accessTokenManager.storeTokens(tokens);
				log.debug("Access token generated {}", accessToken);

				log.debug("Front redirect...");
				response.sendRedirect(computeFrontURL(accessToken));

				clearAuthenticationAttributes(request);

				log.debug("Authentication sucessful");
			} catch (Exception e) {
				log.error("Failed to convert token", e);
			}
		} else {
			log.warn("Could not handle  {}", authentication);
		}
	}

	protected void assignOriginalIssuer(OAuth2AuthorizedClient authorizedClient, AuthenticatedUser authenticatedUser) {
		if (authorizedClient == null || authenticatedUser == null) {
			return;
		}
		if (authorizedClient.getClientRegistration() == null
				|| authorizedClient.getClientRegistration().getProviderDetails() == null) {
			return;
		}
		String issuer = authorizedClient.getClientRegistration().getProviderDetails().getIssuerUri();
		authenticatedUser.addData(AbstractJwtTokenUtil.ORIGINAL_ISSUER, issuer);

	}

	protected void assignOriginalToken(Authentication authentication, AuthenticatedUser authenticatedUser)
			throws JsonProcessingException, ParseException, AppServiceException {
		if (authentication == null || authenticatedUser == null) {
			return;
		}
		if (authentication.getPrincipal() instanceof DefaultOidcUser defaultOidcUser) {
			String tokenValue = defaultOidcUser.getIdToken().getTokenValue();
			Token token = tokenManager.saveToken(TokenType.AUTHORIZATION_TOKEN, authenticatedUser, tokenValue);
			authenticatedUser.addData(AbstractJwtTokenUtil.ORIGINAL_TOKEN_ID, String.valueOf(token.getId()));
		}
	}

	protected AuthenticatedUser convert(OAuth2AuthenticationToken token, String registrationId) {
		AuthenticatedUser authenticatedUser = new AuthenticatedUser(token.getPrincipal().getName());
		OAuth2AuthenticatorDescription oAuth2AuthenticatorDescription = oAuth2AuthenticatorHelper
				.getOAuth2AuthenticatorDescription(registrationId, true);
		authenticatedUser.setFirstname(extractAttribute(String.class, oAuth2AuthenticatorDescription, token,
				UserAttribute.FIRSTNAME, DEFAULT_FISRTNAME_OAUTH2_ATTRIBUTE));
		authenticatedUser.setLastname(extractAttribute(String.class, oAuth2AuthenticatorDescription, token,
				UserAttribute.LASTNAME, DEFAULT_LASTNAME_OAUTH2_ATTRIBUTE));
		authenticatedUser.setEmail(extractAttribute(String.class, oAuth2AuthenticatorDescription, token,
				UserAttribute.EMAIL, DEFAULT_EMAIL_OAUTH2_ATTRIBUTE));

		authenticatedUser.setRoles(new ArrayList<>());
		token.getAuthorities().forEach(authority -> {
			addRole(authenticatedUser, authority.getAuthority());
			addRole(authenticatedUser, authority.getAuthority().toUpperCase());

			if (StringUtils.isNoneEmpty(oAuth2AuthenticatorDescription.getProvider().getRoleConvertExpression())) {
				Pattern p = Pattern.compile(oAuth2AuthenticatorDescription.getProvider().getRoleConvertExpression());
				Matcher m = p.matcher(authority.getAuthority());
				if (m.find() && m.groupCount() >= 1) {
					String role = m.group(1);
					addRole(authenticatedUser, role);
					addRole(authenticatedUser, role.toUpperCase());
				}
			}
		});
		if (CollectionUtils.isEmpty(authenticatedUser.getRoles())
				|| !authenticatedUser.getRoles().contains(RoleCodes.USER)) {
			authenticatedUser.getRoles().add(RoleCodes.USER);
		}
		return authenticatedUser;
	}

	protected void addRole(AuthenticatedUser authenticatedUser, String role) {
		if (!authenticatedUser.getRoles().contains(role)) {
			authenticatedUser.getRoles().add(role);
		}
	}

	protected TokenManagement getTokenManagement(String registrationId) {
		OAuth2AuthenticatorDescription oAuth2AuthenticatorDescription = oAuth2AuthenticatorHelper
				.getOAuth2AuthenticatorDescription(registrationId, true);
		if (oAuth2AuthenticatorDescription != null) {
			return oAuth2AuthenticatorDescription.getProvider().getTokenManagement();
		}
		return null;
	}

	protected <T> T extractAttribute(Class<T> clazz, OAuth2AuthenticatorDescription oAuth2AuthenticatorDescription,
			OAuth2AuthenticationToken token, UserAttribute userAttribute, String defaultAttributeName) {

		String oauth2Attribute = null;
		OAuth2AttributeMapping loginMapping = lookupAttributeMapping(oAuth2AuthenticatorDescription, userAttribute);
		if (loginMapping != null) {
			oauth2Attribute = loginMapping.getOauth2Attribute();
		}
		if (oauth2Attribute == null) {
			oauth2Attribute = defaultAttributeName;
		}
		return clazz.cast(token.getPrincipal().getAttributes().get(oauth2Attribute));
	}

	protected OAuth2AttributeMapping lookupAttributeMapping(
			OAuth2AuthenticatorDescription oAuth2AuthenticatorDescription, UserAttribute attributeName) {
		if (oAuth2AuthenticatorDescription != null && oAuth2AuthenticatorDescription.getProvider() != null
				&& oAuth2AuthenticatorDescription.getProvider().getAttributeMappings() != null) {
			return oAuth2AuthenticatorDescription.getProvider().getAttributeMappings().stream()
					.filter(mapping -> mapping.getUserAttribute() == attributeName).findFirst().orElse(null);
		}
		return null;
	}

	protected String computeFrontURL(String token) {
		String prefix = frontEndUrlServer;
		StringBuilder url = new StringBuilder(prefix);
		if (prefix.contains("?")) {
			url.append("&token=").append(token);
		} else {
			url.append("?token=").append(token);
		}
		return url.toString();
	}

	protected void updateUser(AuthenticatedUser authenticatedUser) {
		User user = userService.getUserByLogin(authenticatedUser.getLogin(), false);
		if (user != null) {
			assignUserData(user, authenticatedUser);
			userService.updateUser(user);
			userService.recordAuthentication(user.getUuid(), true);
		} else {
			user = new User();
			// Le mot de passe n'est pas utilisé dans ce contexte
			user.setPassword("{noop}cas");
			user.setType(UserType.PERSON);
			user.setLogin(authenticatedUser.getLogin());
			assignUserData(user, authenticatedUser);
			userService.createUser(user);
			userService.recordAuthentication(user.getUuid(), true);
		}
	}

	protected void assignUserData(User user, AuthenticatedUser authenticatedUser) {
		user.setFirstname(authenticatedUser.getFirstname());
		user.setLastname(authenticatedUser.getLastname());
		user.setRoles(convertAuthorities(authenticatedUser.getRoles()));
	}

	protected List<Role> convertAuthorities(List<String> roleCodes) {
		List<Role> roles = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(roleCodes)) {
			for (String roleCode : roleCodes) {
				RoleSearchCriteria roleSearchCriteria = RoleSearchCriteria.builder().active(true).code(roleCode)
						.build();
				roles.addAll(roleService.searchRoles(roleSearchCriteria));
			}
		}
		return roles;
	}

	protected Tokens generateTokens(AuthenticatedUser user) throws IOException {
		Tokens tokens = null;
		try {
			tokens = getJwtTokenUtil().generateTokens(user.getLogin(), user);
			tokenManager.saveToken(TokenType.AUTHORIZATION_TOKEN, user, tokens.getJwtToken());
			tokenManager.saveToken(TokenType.REFRESH_TOKEN, user, tokens.getRefreshToken());
		} catch (Exception e) {
			throw new IOException("Failed to generate tokens", e);
		}
		return tokens;
	}

	/**
	 * Removes temporary authentication-related data which may have been stored in the session during the authentication process..
	 *
	 * @param request - HTTP request
	 */
	protected final void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return;
		}
		session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	}

}
