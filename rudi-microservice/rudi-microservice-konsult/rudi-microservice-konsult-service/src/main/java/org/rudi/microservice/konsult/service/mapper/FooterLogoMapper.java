package org.rudi.microservice.konsult.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.rudi.microservice.konsult.core.bean.FooterLogo;
import org.rudi.microservice.konsult.core.customization.FooterLogoData;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FooterLogoMapper {
	FooterLogo dataToDto(FooterLogoData data);
}
