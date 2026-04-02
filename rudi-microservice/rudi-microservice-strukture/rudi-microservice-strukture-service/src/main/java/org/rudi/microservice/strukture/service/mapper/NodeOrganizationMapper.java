package org.rudi.microservice.strukture.service.mapper;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.service.geo.GeometryHelper;
import org.rudi.common.service.mapper.MapperUtils;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.microservice.strukture.core.bean.Feature;
import org.rudi.microservice.strukture.core.bean.GeoJsonObject;
import org.rudi.microservice.strukture.core.bean.NodeLinkedProducerStatus;
import org.rudi.microservice.strukture.core.bean.NodeOrganization;
import org.rudi.microservice.strukture.core.bean.NodeOrganizationStatus;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationWorkflowHelper;
import org.rudi.microservice.strukture.storage.bean.NodeOrganizationProjectionBean;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { MapperUtils.class, })
@Component
public abstract class NodeOrganizationMapper {
	@Autowired
	private GeometryHelper geojsonHelper;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private OrganizationWorkflowHelper organizationWorkflowHelper;

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
	@Mapping(source = "organizationStatus", target = "organizationStatus", ignore = true)
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
	@Mapping(source = "organizationStatus", target = "organizationStatus", ignore = true)
	public abstract NodeOrganization entityToNodeDto(OrganizationEntity entity);

	@InheritInverseConfiguration
	public abstract OrganizationEntity nodeDtoToEntity(NodeOrganization nodeOrganization);

	public Page<NodeOrganization> beansToNodeDto(Page<NodeOrganizationProjectionBean> beans, Pageable pageable) {
		return new PageImpl<>(beansToNodeDto(beans.getContent()), pageable, beans.getTotalElements());
	}

	public abstract List<NodeOrganization> beansToNodeDto(List<NodeOrganizationProjectionBean> beans);

	@Mapping(source = "organizationStatus", target = "organizationStatus", ignore = true)
	@Mapping(source = "linkedProducerStatus", target = "linkedProducerStatus", ignore = true)
	public abstract NodeOrganization beanToNodeDto(NodeOrganizationProjectionBean bean);

	@AfterMapping
	public void handlePosition(NodeOrganization dto, @MappingTarget OrganizationEntity entity) {
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

	@AfterMapping
	public void handleStatus(NodeOrganizationProjectionBean bean, @MappingTarget NodeOrganization nodeDto) {
		if (bean.getOrganizationStatus() != null) {
			// Cas d'un workflow relancé sur un objet déjà validé
			if (bean.getOrganizationStatus().equals(OrganizationStatus.VALIDATED) && !bean.getStatus().equals(Status.COMPLETED)) {
				try {
					// Récupération du type de draft (différence entre archivage et modification)
					String draftType = organizationWorkflowHelper.getDraftType(bean.getData());

					// Cas de l'archivage
					if (organizationWorkflowHelper.isDraftTypeArchive(draftType)) {
						nodeDto.setOrganizationStatus(NodeOrganizationStatus.ARCHIVE_IN_PROGRESS);
					}
				} catch (InvalidDataException e) {
					log.error("Error while getting draft type for organization status", e);
				}
			} else {
				nodeDto.setOrganizationStatus(NodeOrganizationStatus.valueOf(bean.getOrganizationStatus().name()));
			}

			if (bean.getLinkedProducerStatus() != null) {
				// Cas d'un workflow relancé sur un objet déjà validé : ici cas du detach uniquement
				if (bean.getLinkedProducerStatus().equals(LinkedProducerStatus.VALIDATED) && !bean.getLinkedStatus().equals(Status.COMPLETED)) {
					nodeDto.setLinkedProducerStatus(NodeLinkedProducerStatus.DETACH_IN_PROGRESS);
				} else {
					nodeDto.setLinkedProducerStatus(NodeLinkedProducerStatus.valueOf(bean.getLinkedProducerStatus().name()));
				}
			}
		}
	}
}
