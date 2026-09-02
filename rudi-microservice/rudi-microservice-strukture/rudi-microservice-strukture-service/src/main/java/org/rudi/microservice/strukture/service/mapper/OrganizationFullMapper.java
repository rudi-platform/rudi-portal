package org.rudi.microservice.strukture.service.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.rudi.common.service.mapper.MapperUtils;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { MapperUtils.class,
		AbstractAddressMapper.class })
public abstract class OrganizationFullMapper extends OrganizationMapper {

	@Override
	@InheritInverseConfiguration
	@Mapping(source = "position", target = "position", ignore = true)
	@Mapping(source = "addresses", target = "addresses", ignore = true)
	public abstract OrganizationEntity dtoToEntity(Organization dto);

	@Override
	@Mapping(source = "position", target = "position", ignore = true)
	public abstract Organization entityToDto(OrganizationEntity entity);

	@Override
	@Mapping(source = "uuid", target = "uuid", ignore = true)
	@Mapping(source = "position", target = "position", ignore = true)
	@Mapping(source = "addresses", target = "addresses", ignore = true)
	public abstract void dtoToEntity(Organization dto, @MappingTarget OrganizationEntity entity);

}
