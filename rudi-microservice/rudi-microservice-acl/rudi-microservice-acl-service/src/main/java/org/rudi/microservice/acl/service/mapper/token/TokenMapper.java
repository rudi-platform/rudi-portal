/**
 * RUDI
 */
package org.rudi.microservice.acl.service.mapper.token;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.rudi.common.service.mapper.AbstractMapper;
import org.rudi.microservice.acl.core.bean.Token;
import org.rudi.microservice.acl.storage.entity.token.TokenEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TokenMapper extends AbstractMapper<TokenEntity, Token> {

	@Override
	@Mapping(source = "user.login", target = "userId")
	Token entityToDto(TokenEntity entity);

}
