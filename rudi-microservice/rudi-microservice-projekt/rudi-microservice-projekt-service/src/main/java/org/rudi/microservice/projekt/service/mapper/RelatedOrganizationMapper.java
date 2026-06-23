package org.rudi.microservice.projekt.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.rudi.common.service.mapper.AbstractMapper;
import org.rudi.common.service.mapper.MapperUtils;
import org.rudi.microservice.projekt.core.bean.RelatedOrganization;
import org.rudi.microservice.projekt.storage.entity.relatedorganization.RelatedOrganizationEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { MapperUtils.class })
public interface RelatedOrganizationMapper extends AbstractMapper<RelatedOrganizationEntity, RelatedOrganization> {

	@Override
	RelatedOrganizationEntity dtoToEntity(RelatedOrganization dto);

	@Override
	RelatedOrganization entityToDto(RelatedOrganizationEntity entity);
}
