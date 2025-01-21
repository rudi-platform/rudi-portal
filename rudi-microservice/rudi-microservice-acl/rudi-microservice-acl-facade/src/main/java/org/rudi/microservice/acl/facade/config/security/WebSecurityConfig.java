package org.rudi.microservice.acl.facade.config.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.rudi.common.facade.config.filter.JwtRequestFilter;
import org.rudi.common.facade.config.filter.OAuth2RequestFilter;
import org.rudi.common.facade.config.filter.PreAuthenticationFilter;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.microservice.acl.facade.config.security.anonymous.AnonymousAuthenticationProcessingFilter;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtAuthenticationEntryPoint;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtAuthenticationLoginFailureHandler;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtAuthenticationLoginSuccessHandler;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtAuthenticationProcessingFilter;
import org.rudi.microservice.acl.facade.config.security.jwt.JwtAuthenticationProvider;
import org.rudi.microservice.acl.facade.config.security.oauth2.RudiAuthorizationService;
import org.rudi.microservice.acl.facade.config.security.oauth2.RudiRegisteredClient;
import org.rudi.microservice.acl.facade.config.security.oauth2.RudiRegisteredClientRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig {

	@Value("${module.oauth2.check-token-uri}")
	private String checkTokenUri;

	@Value("${application.role.administrateur.code}")
	private String administrateurRoleCode;

	@Value("${rudi.acl.security.authentication.disabled:false}")
	private boolean disableAuthentification = false;

	@Value("${rudi.acl.security.pre-authentication.disabled:true}")
	private boolean disablePreAuthentification = true;

	@Value("${security.anonymous.login:anonymous}")
	private String loginAnonymous;

	@Value("${security.jwt.parameter.login:login}")
	private String loginParameter;

	@Value("${security.jwt.parameter.password:password}")
	private String passwordParameter;

	@Value("${security.jwt.access.tokenKey:}")
	private String jwtAccessTokenKey;

	@Value("${security.jwt.kid:52702736-ceb9-4544-a37d-56e981877899}")
	private String jwtKid;

	@Value("${security.jwt.keystore:}")
	private String jwtKeystore;

	@Value("${security.jwt.keystore.password:}")
	private String jwtKeystorePassword;

	@Value("${security.jwt.keystore.alias:rudi}")
	private String jwtKeystoreAlias;

	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	private final JwtAuthenticationProvider userAuthenticationProvider;

	private final JwtAuthenticationLoginSuccessHandler loginSuccessHandler;

	private final JwtAuthenticationLoginFailureHandler loginFailureHandler;

	private final UtilContextHelper utilContextHelper;

	private final RestTemplate oAuth2RestTemplate;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager,
			RegisteredClientRepository registeredClientRepository) throws Exception {
		log.debug("RudiAcl-filterChain...");
		if (!disableAuthentification) {

			OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
			RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();
			http.securityMatcher(endpointsMatcher).apply(authorizationServerConfigurer);

			http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
					.registeredClientRepository(registeredClientRepository).authorizationService(authorizationService())
					.oidc(Customizer.withDefaults()).tokenIntrospectionEndpoint(Customizer.withDefaults());

			http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(AbstractHttpConfigurer::disable)
					.authorizeHttpRequests(authorizeHttpReq -> {
						// starts authorizing configurations
						authorizeHttpReq.requestMatchers(SecurityConstants.SB_PERMIT_ALL_URL).permitAll();
						// autorisatio des actuators aux seuls role admin
						authorizeHttpReq.requestMatchers(SecurityConstants.ACTUATOR_URL)
								.hasRole(administrateurRoleCode);
						// authenticate all remaining URLS
						authorizeHttpReq.anyRequest().fullyAuthenticated();
					}).exceptionHandling(exception -> {
						exception.configure(http);
						exception.authenticationEntryPoint(jwtAuthenticationEntryPoint);
					}).sessionManagement(
							httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
									.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		} else {
			log.warn("Acl authentication disabled");
			http.cors(cors -> cors.configurationSource(corsConfigurationSource())).csrf(AbstractHttpConfigurer::disable)
					.authorizeHttpRequests(authorizeHttpReq -> authorizeHttpReq.anyRequest().permitAll());
		}
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder = http
				.getSharedObject(AuthenticationManagerBuilder.class);
		authenticationManagerBuilder.userDetailsService(userDetailsServiceBean());
		authenticationManagerBuilder.authenticationProvider(userAuthenticationProvider);
		return authenticationManagerBuilder.build();
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
	public RegisteredClientRepository registeredClientRepository() {
		return new RudiRegisteredClientRepository();
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
				ArrayUtils.addAll(SecurityConstants.SB_PERMIT_ALL_URL, SecurityConstants.AUTHENTICATION_PERMIT_URL),
				SecurityConstants.LOGOUT_URL, utilContextHelper, oAuth2RestTemplate);
	}

	@Bean
	public Filter createOAuth2Filter() {
		return new OAuth2RequestFilter(SecurityConstants.SB_PERMIT_ALL_URL, checkTokenUri, utilContextHelper,
				oAuth2RestTemplate);
	}

	@Bean
	public Filter createJwtAuthenticationFilter(AuthenticationManager authenticationManager) {
		return new JwtAuthenticationProcessingFilter(SecurityConstants.AUTHENTICATE_URL, loginParameter,
				passwordParameter, SecurityConstants.CHECK_CREDENTIAL_URL, userAuthenticationProvider,
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
}
