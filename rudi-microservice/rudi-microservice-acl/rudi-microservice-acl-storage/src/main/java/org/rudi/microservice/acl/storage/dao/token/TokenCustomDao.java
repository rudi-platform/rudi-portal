/** 
 * RUDI
 */
package org.rudi.microservice.acl.storage.dao.token;

import org.rudi.microservice.acl.core.bean.TokenSearchCritera;
import org.rudi.microservice.acl.storage.entity.token.TokenEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TokenCustomDao {

	Page<TokenEntity> searchTokens(TokenSearchCritera searchCriteria, Pageable pageable);

}
