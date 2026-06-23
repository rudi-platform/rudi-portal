package org.rudi.microservice.acl.service.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.acl.core.bean.Token;
import org.rudi.microservice.acl.core.bean.TokenSearchCritera;
import org.rudi.microservice.acl.core.bean.TokenType;
import org.rudi.microservice.acl.core.bean.User;
import org.rudi.microservice.acl.service.AclSpringBootTest;
import org.rudi.microservice.acl.service.datafactory.TokenDataFactory;
import org.rudi.microservice.acl.service.datafactory.UserDataFactory;
import org.rudi.microservice.acl.storage.dao.user.UserDao;
import org.rudi.microservice.acl.storage.entity.user.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class de test du service TokenService
 *
 */
@AclSpringBootTest
class TokenServiceUT {

	@Autowired
	private TokenService tokenService;

	@Autowired
	private TokenDataFactory tokenDataFactory;

	@Autowired
	private UserDataFactory userDataFactory;

	@Autowired
	private UserDao userDao;

	@Test
	void testSaveToken() throws AppServiceException {
		assertNotNull(tokenService);

		// Création d'un utilisateur unique pour ce test
		String testLogin = "testSaveToken-" + UUID.randomUUID().toString().substring(0, 8);
		User testUser = userDataFactory.getOrCreateTestUser(testLogin);

		// Comptage initial des tokens pour cet utilisateur
		TokenSearchCritera initialCriteria = TokenSearchCritera.builder().userLogin(testUser.getLogin()).build();
		int nbTokenBefore = tokenService.searchTokens(initialCriteria).size();

		// Création d'un token
		Token token = tokenDataFactory.createTestToken(testUser.getLogin(), TokenType.ACCESS_TOKEN,
				"test-access-token-value-" + UUID.randomUUID());

		Token savedToken = tokenService.saveToken(token);

		assertNotNull(savedToken);
		assertNotNull(savedToken.getId());
		assertEquals(testUser.getLogin(), savedToken.getUserId());
		assertEquals(TokenType.ACCESS_TOKEN, savedToken.getType());
		assertNotNull(savedToken.getValue());

		// Vérification du nombre de tokens
		int nbTokenAfter = tokenService.searchTokens(initialCriteria).size();
		assertEquals(nbTokenBefore + 1, nbTokenAfter);
	}

	@Test
	void testSaveTokenWithInvalidUser() {
		// Tentative de sauvegarde avec un utilisateur inexistant
		Token token = tokenDataFactory.createTestToken("unknownUser-" + UUID.randomUUID(), TokenType.ACCESS_TOKEN,
				"test-token-" + UUID.randomUUID());

		assertThrows(AppServiceException.class, () -> tokenService.saveToken(token));
	}

	@Test
	void testGetToken() throws AppServiceException {
		String testLogin = "testGetToken-" + UUID.randomUUID().toString().substring(0, 8);
		User testUser = userDataFactory.getOrCreateTestUser(testLogin);

		// Création d'un token
		Token token = tokenDataFactory.createTestToken(testUser.getLogin(), TokenType.REFRESH_TOKEN,
				"test-refresh-token-" + UUID.randomUUID());
		Token savedToken = tokenService.saveToken(token);

		// Récupération du token
		Token retrievedToken = tokenService.getToken(savedToken.getId());

		assertNotNull(retrievedToken);
		assertEquals(savedToken.getId(), retrievedToken.getId());
		assertEquals(savedToken.getValue(), retrievedToken.getValue());
		assertEquals(savedToken.getType(), retrievedToken.getType());

	}

	@Test
	void testGetTokenNotFound() {
		// Tentative de récupération d'un token inexistant
		Token token = tokenService.getToken(999999999L);
		assertNull(token);
	}

	@Test
	void testRemoveToken() throws AppServiceException {
		String testLogin = "testRemoveToken-" + UUID.randomUUID().toString().substring(0, 8);
		User testUser = userDataFactory.getOrCreateTestUser(testLogin);

		// Création d'un token
		Token token = tokenDataFactory.createTestToken(testUser.getLogin(), TokenType.ACCESS_TOKEN,
				"token-to-delete-" + UUID.randomUUID());
		Token savedToken = tokenService.saveToken(token);
		assertNotNull(savedToken.getId());

		TokenSearchCritera criteria = TokenSearchCritera.builder().userLogin(testUser.getLogin()).build();
		int nbTokenBefore = tokenService.searchTokens(criteria).size();

		// Suppression du token
		tokenService.removeToken(savedToken.getId());

		int nbTokenAfter = tokenService.searchTokens(criteria).size();
		assertEquals(nbTokenBefore - 1, nbTokenAfter);
		assertNull(tokenService.getToken(savedToken.getId()));

	}

	@Test
	void testRemoveTokenByUserId() throws AppServiceException {
		String testLogin = "testRemoveByUserId-" + UUID.randomUUID().toString().substring(0, 8);
		User testUser = userDataFactory.getOrCreateTestUser(testLogin);
		UserEntity userEntity = userDao.findByLogin(testUser.getLogin());

		// Création de plusieurs tokens pour le même utilisateur
		Token token1 = tokenDataFactory.createTestToken(testUser.getLogin(), TokenType.ACCESS_TOKEN,
				"access-token-" + UUID.randomUUID());
		Token token2 = tokenDataFactory.createTestToken(testUser.getLogin(), TokenType.REFRESH_TOKEN,
				"refresh-token-" + UUID.randomUUID());
		Token token3 = tokenDataFactory.createTestToken(testUser.getLogin(), TokenType.AUTHORIZATION_TOKEN,
				"auth-token-" + UUID.randomUUID());

		tokenService.saveToken(token1);
		tokenService.saveToken(token2);
		tokenService.saveToken(token3);

		// Vérification que les 3 tokens existent
		TokenSearchCritera criteria = TokenSearchCritera.builder().userLogin(testUser.getLogin()).build();
		int nbTokenBefore = tokenService.searchTokens(criteria).size();
		assertEquals(3, nbTokenBefore);

		// Suppression de tous les tokens de l'utilisateur
		tokenService.removeTokenByUserId(userEntity.getId().toString());

		// Vérification qu'aucun token n'existe pour cet utilisateur
		List<Token> remainingTokens = tokenService.searchTokens(criteria);
		assertTrue(remainingTokens.isEmpty());

	}

	@Test
	void testRemoveTokenByUserIdAndValue() throws AppServiceException {
		String testLogin = "testRemoveByIdValue-" + UUID.randomUUID().toString().substring(0, 8);
		User testUser = userDataFactory.getOrCreateTestUser(testLogin);
		UserEntity userEntity = userDao.findByLogin(testUser.getLogin());

		// Création de plusieurs tokens pour le même utilisateur
		String valueToDelete = "token-to-delete-value-" + UUID.randomUUID();
		Token token1 = tokenDataFactory.createTestToken(testUser.getLogin(), TokenType.ACCESS_TOKEN, valueToDelete);
		Token token2 = tokenDataFactory.createTestToken(testUser.getLogin(), TokenType.REFRESH_TOKEN,
				"token-to-keep-" + UUID.randomUUID());

		tokenService.saveToken(token1);
		tokenService.saveToken(token2);

		TokenSearchCritera criteria = TokenSearchCritera.builder().userLogin(testUser.getLogin()).build();
		int nbTokenBefore = tokenService.searchTokens(criteria).size();
		assertEquals(2, nbTokenBefore);

		// Suppression d'un token spécifique
		tokenService.removeTokenByUserId(userEntity.getId().toString(), valueToDelete);

		int nbTokenAfter = tokenService.searchTokens(criteria).size();
		assertEquals(1, nbTokenAfter);

		// Vérification que le bon token a été supprimé
		criteria = TokenSearchCritera.builder().userLogin(testUser.getLogin()).token(valueToDelete).build();
		List<Token> tokens = tokenService.searchTokens(criteria);
		assertTrue(tokens.isEmpty());

		// Vérification que l'autre token existe encore
		criteria = TokenSearchCritera.builder().userLogin(testUser.getLogin()).build();
		tokens = tokenService.searchTokens(criteria);
		assertEquals(1, tokens.size());

	}

	@Test
	void testRemoveTokenByValue() throws AppServiceException {
		String testLogin = "testRemoveByValue-" + UUID.randomUUID().toString().substring(0, 8);
		User testUser = userDataFactory.getOrCreateTestUser(testLogin);

		// Création d'un token
		String tokenValue = "unique-token-value-" + UUID.randomUUID();
		Token token = tokenDataFactory.createTestToken(testUser.getLogin(), TokenType.ACCESS_TOKEN, tokenValue);
		tokenService.saveToken(token);

		TokenSearchCritera criteria = TokenSearchCritera.builder().userLogin(testUser.getLogin()).build();
		int nbTokenBefore = tokenService.searchTokens(criteria).size();

		// Suppression par la valeur
		boolean removed = tokenService.removeTokenByValue(tokenValue);

		assertTrue(removed);
		int nbTokenAfter = tokenService.searchTokens(criteria).size();
		assertEquals(nbTokenBefore - 1, nbTokenAfter);

	}

	@Test
	void testRemoveTokenByValueNotFound() {
		// Tentative de suppression d'un token inexistant
		boolean removed = tokenService.removeTokenByValue("non-existent-token-" + UUID.randomUUID());
		assertFalse(removed);
	}

	@Test
	void testSearchTokens() throws AppServiceException {
		String testLogin = "testSearchTokens-" + UUID.randomUUID().toString().substring(0, 8);
		User testUser = userDataFactory.getOrCreateTestUser(testLogin);

		// Création de plusieurs tokens de différents types
		Token accessToken = tokenDataFactory.createTestToken(testUser.getLogin(), TokenType.ACCESS_TOKEN,
				"access-token-search-" + UUID.randomUUID());
		Token refreshToken = tokenDataFactory.createTestToken(testUser.getLogin(), TokenType.REFRESH_TOKEN,
				"refresh-token-search-" + UUID.randomUUID());

		accessToken = tokenService.saveToken(accessToken);
		refreshToken = tokenService.saveToken(refreshToken);

		// Recherche par userLogin
		TokenSearchCritera criteria = TokenSearchCritera.builder().userLogin(testUser.getLogin()).build();
		List<Token> userTokens = tokenService.searchTokens(criteria);
		assertEquals(2, userTokens.size());

		// Recherche par type
		criteria = TokenSearchCritera.builder().userLogin(testUser.getLogin()).types(List.of(TokenType.ACCESS_TOKEN))
				.build();
		List<Token> accessTokens = tokenService.searchTokens(criteria);
		assertEquals(1, accessTokens.size());
		assertTrue(accessTokens.stream().allMatch(t -> t.getType() == TokenType.ACCESS_TOKEN));

		// Recherche par valeur
		criteria = TokenSearchCritera.builder().token(accessToken.getValue()).build();
		List<Token> specificTokens = tokenService.searchTokens(criteria);
		assertEquals(1, specificTokens.size());
		assertEquals(accessToken.getValue(), specificTokens.get(0).getValue());

		// Recherche combinée
		criteria = TokenSearchCritera.builder().userLogin(testUser.getLogin()).types(List.of(TokenType.REFRESH_TOKEN))
				.build();
		List<Token> combinedSearch = tokenService.searchTokens(criteria);
		assertEquals(1, combinedSearch.size());
		assertEquals(TokenType.REFRESH_TOKEN, combinedSearch.get(0).getType());

	}

	@Test
	void testCleanup() throws AppServiceException {
		String testLogin = "testCleanup-" + UUID.randomUUID().toString().substring(0, 8);
		User testUser = userDataFactory.getOrCreateTestUser(testLogin);

		// Création d'un token expiré (expiré il y a plus de 5 heures)
		Token expiredToken = tokenDataFactory.createExpiredToken(testUser.getLogin(), TokenType.ACCESS_TOKEN,
				"expired-token-" + UUID.randomUUID(), 6);
		tokenService.saveToken(expiredToken);

		// Création d'un token récemment expiré (expiré il y a moins de 5 heures)
		Token recentlyExpiredToken = tokenDataFactory.createExpiredToken(testUser.getLogin(), TokenType.REFRESH_TOKEN,
				"recently-expired-token-" + UUID.randomUUID(), 3);
		tokenService.saveToken(recentlyExpiredToken);

		// Création d'un token valide
		Token validToken = tokenDataFactory.createValidToken(testUser.getLogin(), TokenType.AUTHORIZATION_TOKEN,
				"valid-token-" + UUID.randomUUID(), 1);
		tokenService.saveToken(validToken);

		TokenSearchCritera criteria = TokenSearchCritera.builder().userLogin(testUser.getLogin()).build();
		int nbTokenBefore = tokenService.searchTokens(criteria).size();
		assertEquals(3, nbTokenBefore);

		// Exécution du cleanup
		tokenService.cleanup();

		// Vérification : seul le token expiré depuis plus de 5 heures doit être supprimé
		int nbTokenAfter = tokenService.searchTokens(criteria).size();
		assertEquals(2, nbTokenAfter);

		// Vérification que le bon token a été supprimé
		List<Token> remainingTokens = tokenService.searchTokens(criteria);
		assertTrue(remainingTokens.stream().anyMatch(t -> t.getValue().contains("recently-expired-token")));
		assertTrue(remainingTokens.stream().anyMatch(t -> t.getValue().contains("valid-token")));

	}

	@Test
	void testSearchTokensEmptyCriteria() {
		// Recherche avec des critères vides
		TokenSearchCritera criteria = TokenSearchCritera.builder().build();
		List<Token> tokens = tokenService.searchTokens(criteria);
		assertNotNull(tokens);
		// Le résultat peut être vide ou contenir des tokens selon l'état de la base
	}

	@Test
	void testMultipleTokensForSameUser() throws AppServiceException {
		String testLogin = "testMultipleTokens-" + UUID.randomUUID().toString().substring(0, 8);
		User testUser = userDataFactory.getOrCreateTestUser(testLogin);

		// Test de création de multiples tokens pour le même utilisateur
		Token token1 = tokenDataFactory.createTestToken(testUser.getLogin(), TokenType.ACCESS_TOKEN,
				"multi-access-1-" + UUID.randomUUID());
		Token token2 = tokenDataFactory.createTestToken(testUser.getLogin(), TokenType.ACCESS_TOKEN,
				"multi-access-2-" + UUID.randomUUID());
		Token token3 = tokenDataFactory.createTestToken(testUser.getLogin(), TokenType.REFRESH_TOKEN,
				"multi-refresh-1-" + UUID.randomUUID());

		tokenService.saveToken(token1);
		tokenService.saveToken(token2);
		tokenService.saveToken(token3);

		// Vérification
		TokenSearchCritera criteria = TokenSearchCritera.builder().userLogin(testUser.getLogin()).build();
		List<Token> userTokens = tokenService.searchTokens(criteria);
		assertEquals(3, userTokens.size());

		// Vérification par type
		criteria = TokenSearchCritera.builder().userLogin(testUser.getLogin()).types(List.of(TokenType.ACCESS_TOKEN))
				.build();
		List<Token> accessTokens = tokenService.searchTokens(criteria);
		assertEquals(2, accessTokens.size());

	}
}
