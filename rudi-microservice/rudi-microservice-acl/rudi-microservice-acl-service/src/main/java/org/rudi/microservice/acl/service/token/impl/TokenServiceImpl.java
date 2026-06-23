/**
 * RUDI 
 */
package org.rudi.microservice.acl.service.token.impl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.rudi.common.core.util.AnonymizerUtils;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.AppServiceExceptionsStatus;
import org.rudi.microservice.acl.core.bean.Token;
import org.rudi.microservice.acl.core.bean.TokenSearchCritera;
import org.rudi.microservice.acl.service.mapper.token.TokenMapper;
import org.rudi.microservice.acl.service.token.TokenService;
import org.rudi.microservice.acl.storage.dao.token.TokenCustomDao;
import org.rudi.microservice.acl.storage.dao.token.TokenDao;
import org.rudi.microservice.acl.storage.dao.user.UserDao;
import org.rudi.microservice.acl.storage.entity.token.TokenEntity;
import org.rudi.microservice.acl.storage.entity.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author FNI18300
 *
 */
@RequiredArgsConstructor
@Component
@Transactional(readOnly = true)
@Slf4j
public class TokenServiceImpl implements TokenService {

	private final TokenDao tokenRepository;

	private final TokenCustomDao tokenCustomRepository;

	private final TokenMapper tokenMapper;

	private final UserDao userRepository;

	@Override
	@Transactional(readOnly = false)
	public Token saveToken(Token token) throws AppServiceException {
		TokenEntity entity = tokenMapper.dtoToEntity(token);
		UserEntity user = userRepository.findByLogin(token.getUserId());
		if (user != null) {
			entity.setUser(user);
		} else {
			throw new AppServiceException("Invalid token", AppServiceExceptionsStatus.BAD_REQUEST);
		}
		log.info("=====>Saving token for user {} {}", user.getLogin(), AnonymizerUtils.anonymize(token.getValue()));
		return tokenMapper.entityToDto(tokenRepository.save(entity));
	}

	@Override
	public Token getToken(long id) {
		TokenEntity token = tokenRepository.findById(id).orElse(null);
		return tokenMapper.entityToDto(token);
	}

	@Override
	@Transactional(readOnly = false)
	public void removeToken(long id) {
		TokenEntity entity = tokenRepository.findById(id).orElse(null);
		if (entity != null) {
			tokenRepository.delete(entity);
		}
	}

	@Override
	public List<Token> searchTokens(TokenSearchCritera searchCritera) {
		return tokenMapper.entitiesToDto(tokenCustomRepository.searchTokens(searchCritera, Pageable.unpaged()),
				Pageable.unpaged()).getContent();
	}

	@Override
	@Transactional(readOnly = false)
	public void removeTokenByUserId(String userId) {
		removeToken(userId, null);
	}

	@Override
	@Transactional(readOnly = false)
	public void removeTokenByUserId(String userId, String value) {
		removeToken(userId, value);
	}

	private void removeToken(String userId, String value) {
		TokenSearchCritera searchCritera = TokenSearchCritera.builder().userId(userId).token(value).build();
		Page<TokenEntity> tokens = tokenCustomRepository.searchTokens(searchCritera, Pageable.unpaged());
		if (!tokens.isEmpty()) {
			tokens.forEach(tokenRepository::delete);
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void cleanup() {
		LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
		List<TokenEntity> tokens = tokenRepository.findAll();
		if (CollectionUtils.isNotEmpty(tokens)) {
			for (TokenEntity token : tokens) {
				// on doit laisser ici les tokens expirés un certain temps pour gérer les tockens avec refresh token
				if (now.isAfter(token.getExpriresAt().plusHours(5))) {
					tokenRepository.delete(token);
				}
			}
		}
	}

	@Override
	@Transactional(readOnly = false)
	public boolean removeTokenByValue(String value) {
		TokenEntity token = tokenRepository.findByValue(value);
		if (token != null) {
			tokenRepository.delete(token);
			return true;
		}
		return false;
	}
}
