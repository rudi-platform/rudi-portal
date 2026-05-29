package org.rudi.microservice.acl.facade.config.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.common.facade.config.filter.OAuth2RequestFilter;
import org.rudi.common.facade.config.filter.PreAuthenticationFilter;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.microservice.acl.facade.config.security.anonymous.AnonymousAuthenticationProcessingFilter;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtAuthenticationLoginFailureHandler;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtAuthenticationLoginSuccessHandler;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtAuthenticationProcessingFilter;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtAuthenticationProvider;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtRequestFilter;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtTokenUtil;
import org.rudi.microservice.acl.facade.config.security.oauth2.RudiAuthorizationService;
import org.rudi.microservice.acl.facade.config.security.oauth2.RudiRegisteredClient;
import org.rudi.microservice.acl.facade.config.security.oauth2.RudiRegisteredClientRepository;
import org.rudi.microservice.acl.facade.config.security.oauth2.cas.CasBearerTokenAuthenticationFilter;
import org.rudi.microservice.acl.facade.config.security.oauth2.cas.CasOAuth2AccessTokenResponseClient;
import org.rudi.microservice.acl.facade.config.security.oauth2.cas.CasOAuth2AuthenticationSuccessHandler;
import org.rudi.microservice.acl.facade.config.security.oauth2.cas.CasOAuth2AuthorizationAdditionalParameterCustomizer;
import org.rudi.microservice.acl.facade.config.security.oauth2.cas.CasOAuth2AuthorizationCodeTokenResponseClient;
import org.rudi.microservice.acl.facade.config.security.oauth2.configurer.ClientSecretBasicAuthenticationConverter;
import org.rudi.microservice.acl.service.user.UserService;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig {

	/** OAuth2 check token URL */
	@Value("${module.oauth2.check-token-uri}")
	private String checkTokenUri;

	/** Code du rôle administrateur */
	@Value("${application.role.administrateur.code}")
	private String administrateurRoleCode;

	/** Désactive l'authentification */
	@Value("${rudi.acl.security.authentication.disabled:false}")
	private boolean disableAuthentification = false;

	/** Désactive la pré-authentification */
	@Value("${rudi.acl.security.pre-authentication.disabled:true}")
	private boolean disablePreAuthentification = true;

	/** Login utilisé pour l'authentification anonyme */
	@Value("${security.anonymous.login:anonymous}")
	private String loginAnonymous;

	/** Nom du paramètre login dans les requêtes d'authentification */
	@Value("${security.jwt.parameter.login:login}")
	private String loginParameter;

	/** Nom du paramètre password dans les requêtes d'authentification */
	@Value("${security.jwt.parameter.password:password}")
	private String passwordParameter;

	/** Key identiuantant (kid) pour les tokens JWT **/
	@Value("${security.jwt.kid:52702736-ceb9-4544-a37d-56e981877899}")
	private String jwtKid;

	/** Chemin du keystore contenant la clé RSA pour la signature des JWT */
	@Value("${security.jwt.keystore:}")
	private String jwtKeystore;

	/** Mot de passe du keystore contenant la clé RSA pour la signature des JWT */
	@Value("${security.jwt.keystore.password:}")
	private String jwtKeystorePassword;

	/** Alias de la clé RSA dans le keystore pour la signature des JWT */
	@Value("${security.jwt.keystore.alias:rudi-jwt}")
	private String jwtKeystoreAlias;

	/** Désactive la vérification SSL pour les appels OAuth2 */
	@Value("${security.authentication.oauth2.sslVerifier:true}")
	private boolean disableOAuth2SslVerifier = true;

	/** Active l'authentification OAuth2 externe pour les applications web */
	@Value("${security.web.external-oauth2:true}")
	private boolean webExternalOAuth2 = false;

	/** Active le mode debug de la sécurité web */
	@Value("${security.web.debug:true}")
	private boolean webSecurityDebug = true;

	/** Méthode OAuth2 utilisée pour les applications web (authorization_code ou access_token) */
	@Value("${security.authentication.oauth2.method:authorization_code}")
	private String oAuth2Method;

	private final RudiDelegatorEntryPoint authenticationEntryPoint;

	private final JwtAuthenticationLoginSuccessHandler loginSuccessHandler;

	private final CasOAuth2AuthenticationSuccessHandler casSuccessHandler;

	private final JwtAuthenticationLoginFailureHandler loginFailureHandler;

	private final AuthenticationManagerResolver<HttpServletRequest> trustedIssuerJwtAuthenticationManagerResolver;

	private final UtilContextHelper utilContextHelper;

	private final TokenManager tokenManager;

	private final RestTemplate oAuth2RestTemplate;

	private final CasOAuth2AuthorizationAdditionalParameterCustomizer casOAuth2AuthorizationAdditionalParameterCustomizer;

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.debug(webSecurityDebug);
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager,
			RegisteredClientRepository registeredClientRepository,
			ClientRegistrationRepository clientRegistrationRepository,
			JwtAuthenticationProvider jwtAuthenticationProvider) throws Exception {
		log.debug("RudiAcl-filterChain...");
		if (!disableAuthentification) {

			/**
			 * https://docs.spring.io/spring-authorization-server/reference/configuration-model.html#configuring-client-authentication
			 */
			OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer
					.authorizationServer();
			RequestMatcher requestMatcher = new OrRequestMatcher(
					Arrays.asList(authorizationServerConfigurer.getEndpointsMatcher(),
							new NegatedRequestMatcher(permitAllRequestMatcher())));
			http.securityMatcher(requestMatcher).with(authorizationServerConfigurer,
					authorizationServer -> authorizationServer.clientAuthentication(clientAuthentication -> {
						clientAuthentication.authenticationConverter(new ClientSecretBasicAuthenticationConverter());
						clientAuthentication.authenticationProvider(jwtAuthenticationProvider);
					}));

			http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
					.registeredClientRepository(registeredClientRepository).authorizationService(authorizationService())
					.oidc(Customizer.withDefaults()).tokenIntrospectionEndpoint(Customizer.withDefaults());

			http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
					.csrf(csrf -> csrf.ignoringRequestMatchers(csrfRequestMatcher())).exceptionHandling(exception -> {
						exception.configure(http);
						exception.authenticationEntryPoint(authenticationEntryPoint);// -->
					}).authorizeHttpRequests(authorizeHttpReq -> {
						// starts authorizing configurations
						authorizeHttpReq.requestMatchers(SecurityConstants.SB_PERMIT_ALL_URL).permitAll();
						// autorisatio des actuators aux seuls role admin
						authorizeHttpReq.requestMatchers(SecurityConstants.ACTUATOR_URL)
								.hasRole(administrateurRoleCode);
						// authenticate all remaining URLS
						authorizeHttpReq.anyRequest().fullyAuthenticated();
					}).sessionManagement(
							httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
									.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

			if (webExternalOAuth2) {
				http.oauth2Login(oauth2Login -> oauth2Login.tokenEndpoint(tokenEndpoint -> {
					try {
						tokenEndpoint.accessTokenResponseClient(accessTokenResponseClient());
					} catch (Exception e) {
						log.error("Failed to configure", e);
					}
				}).authorizationEndpoint(endpoint -> endpoint.authorizationRequestResolver(
						createOAuth2AuthorizationRequestResolver(clientRegistrationRepository, http)))
						.successHandler(casSuccessHandler)).addFilterAfter(createOAuth2Filter(), LogoutFilter.class)
						.addFilterAfter(createAnonymousFilter(authenticationManager), LogoutFilter.class)
						.addFilterAfter(createJwtAuthenticationFilter(authenticationManager, jwtAuthenticationProvider),
								LogoutFilter.class)
						.addFilterAfter(createJwtRequestFilter(), LogoutFilter.class);

				http.oauth2ResourceServer(oauth2 -> oauth2// .authenticationManagerResolver(trustedIssuerJwtAuthenticationManagerResolver)
						.withObjectPostProcessor(new ObjectPostProcessor<BearerTokenAuthenticationFilter>() {
							@SuppressWarnings("unchecked")
							@Override
							public BearerTokenAuthenticationFilter postProcess(BearerTokenAuthenticationFilter object) {
								log.debug("postProcess CasBearerTokenAuthenticationFilter in case of");
								return new CasBearerTokenAuthenticationFilter(
										trustedIssuerJwtAuthenticationManagerResolver);
							}
						}));
			}
		} else {
			log.warn("Acl authentication disabled");
			http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(AbstractHttpConfigurer::disable)
					.authorizeHttpRequests(authorizeHttpReq -> authorizeHttpReq.anyRequest().permitAll());
		}
		return http.build();
	}

	protected OAuth2AuthorizationRequestResolver createOAuth2AuthorizationRequestResolver(
			ClientRegistrationRepository clientRegistrationRepository, HttpSecurity http) {
		ResolvableType resolvableType = ResolvableType.forClass(OAuth2AuthorizationRequestResolver.class);
		OAuth2AuthorizationRequestResolver bean = getBeanOrNull(http, resolvableType);
		if (bean != null) {
			return bean;
		}
		DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
				clientRegistrationRepository,
				OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
		resolver.setAuthorizationRequestCustomizer(
				casOAuth2AuthorizationAdditionalParameterCustomizer::handleAdditionnalParameters);
		return resolver;
	}

	@SuppressWarnings("unchecked")
	private <T> T getBeanOrNull(HttpSecurity http, ResolvableType type) {
		ApplicationContext context = http.getSharedObject(ApplicationContext.class);
		if (context == null) {
			return null;
		}
		return (T) context.getBeanProvider(type).getIfUnique();
	}

	protected RequestMatcher permitAllRequestMatcher() {
		List<RequestMatcher> permitAllRequestMatchers = new ArrayList<>();
		Arrays.asList(SecurityConstants.SB_PERMIT_ALL_URL)
				.forEach(url -> permitAllRequestMatchers.add(PathPatternRequestMatcher.withDefaults().matcher(url)));
		return new OrRequestMatcher(permitAllRequestMatchers);
	}

	protected RequestMatcher csrfRequestMatcher() {
		return PathPatternRequestMatcher.withDefaults().matcher("/**");
	}

	protected OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient()
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		if ("authorization_code".equalsIgnoreCase(oAuth2Method)) {
			return new CasOAuth2AuthorizationCodeTokenResponseClient(disableOAuth2SslVerifier);
		} else {
			// access_token
			return new CasOAuth2AccessTokenResponseClient(disableOAuth2SslVerifier);
		}
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http,
			JwtAuthenticationProvider jwtAuthenticationProvider, UserDetailsService userDetailsService)
			throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder = http
				.getSharedObject(AuthenticationManagerBuilder.class);

		authenticationManagerBuilder.userDetailsService(userDetailsServiceBean());
		authenticationManagerBuilder.authenticationProvider(jwtAuthenticationProvider);
		// Permet d'éviter le double appel au userAuthenticationProvider lors d'une demande d'authentification
		authenticationManagerBuilder.parentAuthenticationManager(null);

		return authenticationManagerBuilder.build();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter rudiJwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
		rudiJwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
		rudiJwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");
		JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
		jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(rudiJwtGrantedAuthoritiesConverter);
		return jwtAuthenticationConverter;
	}

	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
		return context -> {
			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				context.getClaims().claims(claims -> {
					RegisteredClient registeredClient = context.getRegisteredClient();
					if (registeredClient instanceof RudiRegisteredClient rudiRegisteredClient) {
						claims.put("authorities", rudiRegisteredClient.getAutorities());
					}
				});
			}
		};
	}

	/**
	 * Configuration du CORS
	 * 
	 * @return
	 */
	@Bean
	protected CorsConfigurationSource corsConfigurationSource() {
		log.debug("Rudi-corsConfigurationSource...");
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "PUT", "DELETE"));
		configuration.addAllowedHeader("*");
		configuration.setAllowCredentials(false);

		// Url autorisées
		// 4200 pour les développement | 8080 pour le déploiement
		configuration.setAllowedOriginPatterns(List.of("*"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public RegisteredClientRepository registeredClientRepository(UserService userService) {
		return new RudiRegisteredClientRepository(userService);
	}

	@Bean
	public OAuth2AuthorizationService authorizationService() {
		return new RudiAuthorizationService();
	}

	/**
	 * Suppression du prefixe sur les rôles
	 * 
	 * @return
	 */
	@Bean
	protected MethodSecurityExpressionHandler createExpressionHandler() {
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setDefaultRolePrefix("");
		return expressionHandler;
	}

	@Bean
	public JWKSet jwkSet() throws NoSuchAlgorithmException {
		JWKSet jwkSet = null;
		KeyPair keyPair = null;
		if (StringUtils.isEmpty(jwtKeystore)) {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(2048);
			keyPair = generator.generateKeyPair();
		} else {
			keyPair = getRsaKey();
		}

		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		// lors du chargement de la clé RSA, on associe un kid.
		// si le kid venant des propriétés est vide alors on utilise un kid généré et
		// dans ce cas au redémarrage du serveur tous les tokens sont condidérés comme
		// invalid
		RSAKey key = new RSAKey.Builder(publicKey).privateKey(privateKey)
				.keyID(StringUtils.isNotEmpty(jwtKid) ? jwtKid : UUID.randomUUID().toString()).build();
		jwkSet = new JWKSet(key);
		return jwkSet;
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource(JWKSet jwkSet) {
		return new ImmutableJWKSet<>(jwkSet);
	}

	private KeyPair getRsaKey() {
		log.debug("Load key {} from {}", jwtKeystoreAlias, jwtKeystore);

		KeyPair keyPair = null;
		try (InputStream is = openKeystore()) {
			KeyStore keystore = KeyStore.getInstance("pkcs12");
			keystore.load(is, jwtKeystorePassword.toCharArray());

			PrivateKey privateKey = (PrivateKey) keystore.getKey(jwtKeystoreAlias, jwtKeystorePassword.toCharArray());

			Certificate cert = keystore.getCertificate(jwtKeystoreAlias);
			PublicKey publicKey = cert.getPublicKey();

			keyPair = new KeyPair(publicKey, privateKey);
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}

	private InputStream openKeystore() throws FileNotFoundException {
		InputStream is = null;
		File file = new File(jwtKeystore);
		if (file.exists()) {
			is = new FileInputStream(file);
		} else {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(jwtKeystore);
		}
		return is;
	}

	@Bean(BeanIds.USER_DETAILS_SERVICE)
	public UserDetailsService userDetailsServiceBean() {
		return new UserDetailServiceImpl();
	}

	@Bean
	public JwtRequestFilter createJwtRequestFilter() {
		return new JwtRequestFilter(
				ArrayUtils.addAll(SecurityConstants.SB_PERMIT_ALL_URL2, SecurityConstants.AUTHENTICATION_PERMIT_URL),
				SecurityConstants.LOGOUT_URL, tokenManager, utilContextHelper, oAuth2RestTemplate);
	}

	@Bean
	public Filter createOAuth2Filter() {
		return new OAuth2RequestFilter(SecurityConstants.SB_PERMIT_ALL_URL2, checkTokenUri, utilContextHelper,
				oAuth2RestTemplate);
	}

	@Bean
	public JwtAuthenticationProvider jwtAuthenticationProvider(UserService userService, PasswordEncoder passwordEncoder,
			JwtDecoder jwtDecoder, JwtTokenUtil jwtTokenUtil, JwtAuthenticationConverter jwtAuthenticationConverter) {
		return new JwtAuthenticationProvider(userService, passwordEncoder, jwtDecoder, jwtTokenUtil,
				jwtAuthenticationConverter);
	}

	@Bean
	public Filter createJwtAuthenticationFilter(AuthenticationManager authenticationManager,
			JwtAuthenticationProvider jwtAuthenticationProvider) {
		return new JwtAuthenticationProcessingFilter(SecurityConstants.AUTHENTICATE_URL, loginParameter,
				passwordParameter, SecurityConstants.CHECK_CREDENTIAL_URL, jwtAuthenticationProvider,
				loginSuccessHandler, loginFailureHandler, authenticationManager);
	}

	@Bean
	public Filter createAnonymousFilter(AuthenticationManager authenticationManager) {
		return new AnonymousAuthenticationProcessingFilter(loginAnonymous, loginSuccessHandler, loginFailureHandler,
				authenticationManager);
	}

	@Bean
	protected Filter createPreAuthenticationFilter() {
		if (!disablePreAuthentification) {
			log.warn("Acl pre-authentication enabled");
		}
		return new PreAuthenticationFilter(disablePreAuthentification);
	}

	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		Set<JWSAlgorithm> jwsAlgs = new HashSet<>();
		jwsAlgs.addAll(JWSAlgorithm.Family.RSA);
		jwsAlgs.addAll(JWSAlgorithm.Family.EC);
		jwsAlgs.addAll(JWSAlgorithm.Family.HMAC_SHA);

		ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
		JWSKeySelector<SecurityContext> jwsKeySelector = new JWSVerificationKeySelector<>(jwsAlgs, jwkSource);
		jwtProcessor.setJWSKeySelector(jwsKeySelector);
		// Override the default Nimbus claims set verifier as NimbusJwtDecoder handles it
		// instead
		jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {
		});
		return new NimbusJwtDecoder(jwtProcessor);
	}

	@Bean
	public JwtEncoder getJwtEncoder(HttpSecurity httpSecurity, JWKSource<SecurityContext> jwkSource) {
		JwtEncoder jwtEncoder = httpSecurity.getSharedObject(JwtEncoder.class);
		if (jwtEncoder == null) {
			jwtEncoder = getOptionalBean(httpSecurity, JwtEncoder.class);
			if (jwtEncoder == null) {
				jwtEncoder = new NimbusJwtEncoder(jwkSource);
				httpSecurity.setSharedObject(JwtEncoder.class, jwtEncoder);
			}
		}
		return jwtEncoder;
	}

	static <T> T getOptionalBean(HttpSecurity httpSecurity, Class<T> type) {
		Map<String, T> beansMap = BeanFactoryUtils
				.beansOfTypeIncludingAncestors(httpSecurity.getSharedObject(ApplicationContext.class), type);
		if (beansMap.size() > 1) {
			throw new NoUniqueBeanDefinitionException(type, beansMap.size(), "Expected single matching bean of type '"
					+ type.getName() + "' but found " + beansMap.size() + ": " + StringUtils.join(beansMap.keySet()));
		}
		return (!beansMap.isEmpty() ? beansMap.values().iterator().next() : null);
	}
}
