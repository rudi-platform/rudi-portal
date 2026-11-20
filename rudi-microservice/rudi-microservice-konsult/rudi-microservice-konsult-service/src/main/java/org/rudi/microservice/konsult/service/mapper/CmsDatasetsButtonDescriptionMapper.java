package org.rudi.microservice.konsult.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.rudi.microservice.konsult.core.bean.CmsDatasetsButtonDescription;
import org.rudi.microservice.konsult.core.customization.CmsDatasetsButtonDescriptionData;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CmsDatasetsButtonDescriptionMapper {

	CmsDatasetsButtonDescription dataToDto(CmsDatasetsButtonDescriptionData data);

}
