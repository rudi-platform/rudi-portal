/**
 * 
 */
package org.rudi.microservice.acl.service.token;

import java.util.List;

import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.acl.core.bean.Token;
import org.rudi.microservice.acl.core.bean.TokenSearchCritera;

/**
 * @author FNI18300
 *
 */
public interface TokenService {

	Token saveToken(Token token) throws AppServiceException;

	void removeToken(long id);

	void removeTokenByUserId(String userId);

	void removeTokenByUserId(String userId, String value);

	List<Token> searchTokens(TokenSearchCritera searchCritera);

	void cleanup();
}
