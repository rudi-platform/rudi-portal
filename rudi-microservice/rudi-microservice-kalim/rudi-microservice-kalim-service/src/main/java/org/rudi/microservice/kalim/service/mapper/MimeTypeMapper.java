package org.rudi.microservice.kalim.service.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.rudi.common.service.mapper.AbstractMapper;
import org.rudi.common.service.mapper.MapperUtils;
import org.rudi.microservice.kalim.core.bean.MimeType;
import org.rudi.microservice.kalim.storage.entity.mimetype.MimeTypeEntity;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { MapperUtils.class })
public interface MimeTypeMapper extends AbstractMapper<MimeTypeEntity, MimeType> {

	/**
	 * @param dto to transform to entity
	 * @return entity
	 */
	@Override
	@InheritInverseConfiguration
	MimeTypeEntity dtoToEntity(MimeType dto);

	/**
	 * @param entity entity to transform to dto
	 * @return dto
	 */
	@Override
	MimeType entityToDto(MimeTypeEntity entity);
}
