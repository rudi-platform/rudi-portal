/** 
 * RUDI
 */
package org.rudi.microservice.acl.storage.dao.token;

import org.rudi.microservice.acl.storage.entity.token.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author FNI18300
 *
 */
public interface TokenDao extends JpaRepository<TokenEntity, Long> {

}
