package org.rudi.microservice.konsult.service.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.rudi.microservice.konsult.core.bean.FooterDescription;
import org.rudi.microservice.konsult.core.customization.FooterDescriptionData;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { FooterLogoMapper.class, SocialNetworkMapper.class })

public interface FooterDescriptionMapper {

	FooterDescription dataToDto(FooterDescriptionData data);
}
