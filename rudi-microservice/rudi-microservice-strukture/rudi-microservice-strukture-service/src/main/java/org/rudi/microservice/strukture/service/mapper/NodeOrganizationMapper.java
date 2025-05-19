package org.rudi.microservice.strukture.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.rudi.common.service.geo.GeometryHelper;
import org.rudi.common.service.mapper.MapperUtils;
import org.rudi.microservice.strukture.core.bean.Feature;
import org.rudi.microservice.strukture.core.bean.GeoJsonObject;
import org.rudi.microservice.strukture.core.bean.NodeOrganization;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
		MapperUtils.class,
})
@Component
public abstract class NodeOrganizationMapper  {
	@Autowired
	private GeometryHelper geojsonHelper;

	@Autowired
	private ObjectMapper objectMapper;

	@Mapping(source = "uuid", target = "organizationId")
	@Mapping(source = "name", target = "organizationName")
	@Mapping(source = "openingDate", target = "organizationOpeningDate")
	@Mapping(source = "closingDate", target = "organizationClosingDate")
	@Mapping(source = "description", target = "organizationSummary")
	@Mapping(source = "address", target = "organizationAddress")
	@Mapping(source = "url", target = "organizationUrl")
	@Mapping(source = "position", target = "organizationCoordinates", ignore = true)
	@Mapping(source = "creationDate", target = "organizationDates.created")
	@Mapping(source = "updatedDate", target = "organizationDates.modified")
	public abstract NodeOrganization dtoToNodeDto(Organization organization);



	@InheritInverseConfiguration
	public abstract Organization nodeDtoToDTO(NodeOrganization nodeOrganization);

	@Mapping(source = "uuid", target = "organizationId")
	@Mapping(source = "name", target = "organizationName")
	@Mapping(source = "openingDate", target = "organizationOpeningDate")
	@Mapping(source = "closingDate", target = "organizationClosingDate")
	@Mapping(source = "description", target = "organizationSummary")
	@Mapping(source = "address", target = "organizationAddress")
	@Mapping(source = "url", target = "organizationUrl")
	@Mapping(source = "position", target = "organizationCoordinates", ignore = true)
	@Mapping(source = "creationDate", target = "organizationDates.created")
	@Mapping(source = "updatedDate", target = "organizationDates.modified")
	public abstract NodeOrganization entityToNodeDto(OrganizationEntity entity);

	@InheritInverseConfiguration
	public abstract OrganizationEntity nodeDtoToEntity(NodeOrganization nodeOrganization);

	@AfterMapping
	private void handlePosition(NodeOrganization dto, @MappingTarget OrganizationEntity entity) {
		int srid = 4326;
		JSONObject geometry = buildJsonObject(dto.getOrganizationCoordinates());

		if (geometry != null && !geometry.isEmpty()) {
			entity.setPosition(geojsonHelper.convertGeometryFromGeoJson(geometry, srid));
		}
	}

	private JSONObject buildJsonObject(GeoJsonObject geoJsonObject) {
		if (geoJsonObject == null) {
			return new JSONObject();
		}
		if (geoJsonObject.getType().equals("Feature")) {
			return objectMapper.convertValue(((Feature) geoJsonObject).getGeometry(), JSONObject.class);
		}
		return objectMapper.convertValue(geoJsonObject, JSONObject.class);
	}
}
