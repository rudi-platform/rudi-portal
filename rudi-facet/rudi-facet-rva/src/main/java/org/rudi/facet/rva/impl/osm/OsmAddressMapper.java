/**
 * RUDI Portail
 */
package org.rudi.facet.rva.impl.osm;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.rudi.common.service.mapper.MapperUtils;
import org.rudi.facet.rva.impl.osm.bean.OsmAddress;

/**
 * @author FNI18300
 *
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { MapperUtils.class })
public interface OsmAddressMapper {

	/**
	 * @param dtos
	 * @return la liste d'entité converties
	 */
	List<org.rudi.rva.core.bean.Address> apiToDtos(List<OsmAddress> dtos);

	/**
	 * @param entity entity to transform to dto
	 * @return dto
	 */
	@Mapping(source = "display_name", target = "label")
	@Mapping(source = "lon", target = "x")
	@Mapping(source = "lat", target = "y")
	org.rudi.rva.core.bean.Address apiToDto(OsmAddress entity);

	@AfterMapping
	default void mapId(OsmAddress entity, @MappingTarget org.rudi.rva.core.bean.Address dto) {
		if (dto != null && entity != null) {
			String id = StringUtils.join(new Object[] { entity.getOsm_type(), entity.getOsm_id(), }, '|');
			dto.setId(id);
		}
	}

}
