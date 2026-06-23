package org.rudi.microservice.acl.service.datafactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.rudi.common.service.datafactory.AbstractDataFactory;
import org.rudi.microservice.acl.core.bean.Token;
import org.rudi.microservice.acl.core.bean.TokenType;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Factory pour créer des objets Token et User de test
 */
@Component
@RequiredArgsConstructor
public class TokenDataFactory extends AbstractDataFactory {

	/**
	 * Crée un token de test
	 */
	public Token createTestToken(String userLogin, TokenType type, String value) {
		Token token = new Token();
		token.setType(type);
		token.setValue(value);
		token.setUserId(userLogin);
		token.setRegisteredClientId("test-client");
		token.setAuthorizationGrantType("authorization_code");

		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		token.setIssuedAt(now);
		token.setExpriresAt(now.plusHours(1));

		return token;
	}

	/**
	 * Crée un token expiré depuis un certain nombre d'heures
	 */
	public Token createExpiredToken(String userLogin, TokenType type, String value, int hoursAgo) {
		Token token = createTestToken(userLogin, type, value);
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		token.setExpriresAt(now.minusHours(hoursAgo));
		return token;
	}

	/**
	 * Crée un token valide (expire dans le futur)
	 */
	public Token createValidToken(String userLogin, TokenType type, String value, int hoursInFuture) {
		Token token = createTestToken(userLogin, type, value);
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		token.setExpriresAt(now.plusHours(hoursInFuture));
		return token;
	}
}
