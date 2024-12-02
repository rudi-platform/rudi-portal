package org.rudi.microservice.strukture.service.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.rudi.common.service.geo.GeometryHelper;
import org.rudi.common.service.mapper.MapperUtils;
import org.rudi.facet.bpmn.mapper.workflow.AssetDescriptionMapper;
import org.rudi.microservice.strukture.core.bean.Feature;
import org.rudi.microservice.strukture.core.bean.GeoJsonObject;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.minidev.json.JSONObject;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { MapperUtils.class,
		AbstractAddressMapper.class })
@Component
public abstract class OrganizationMapper implements AssetDescriptionMapper<OrganizationEntity, Organization> {

	@Autowired
	private GeometryHelper geojsonHelper;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	@InheritInverseConfiguration
	@Mapping(source = "position", target = "position", ignore = true)
	public abstract OrganizationEntity dtoToEntity(Organization dto);

	@Override
	@Mapping(source = "position", target = "position", ignore = true)
	public abstract Organization entityToDto(OrganizationEntity entity);

	/**
	 * Utilisé uniquement pour la modification d'une entité. On ignore toutes les entités filles (sinon l'id de chaque entité fille est supprimé et elles
	 * sont recréées en base)
	 */
	@Mapping(source = "uuid", target = "uuid", ignore = true)
	@Mapping(source = "position", target = "position", ignore = true)
	public abstract void dtoToEntity(Organization dto, @MappingTarget OrganizationEntity entity);

	@AfterMapping
	@Mapping(source = "position", target = "position", ignore = true)
	@Mapping(target = "processDefinitionKey", ignore = true)
	@Mapping(target = "processDefinitionVersion", ignore = true)
	@Mapping(target = "creationDate", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "functionalStatus", ignore = true)
	@Mapping(target = "initiator", ignore = true)
	@Mapping(target = "updator", ignore = true)
	@Mapping(target = "updatedDate", ignore = true)
	@Mapping(target = "assignee", ignore = true)
	public void updateType(@MappingTarget Organization dto, OrganizationEntity entity) {
		dto.setObjectType(dto.getClass().getSimpleName());
	}

	@AfterMapping
	public void handlePosition(Organization dto, @MappingTarget OrganizationEntity entity) {
		// EPSG 4326 ?
		int srid = 4326;
		JSONObject geometry = buildJsonObject(dto.getPosition());

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
