/**
 * RUDI Portail
 */
package org.rudi.facet.rva.impl.ban;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.rudi.common.service.mapper.MapperUtils;
import org.rudi.facet.rva.impl.ban.bean.BanContext;
import org.rudi.facet.rva.impl.ban.bean.BanFeature;

/**
 * @author FNI18300
 *
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { MapperUtils.class })
public interface BanAddressMapper {

	/**
	 * @param dtos
	 * @return la liste d'entité converties
	 */
	List<org.rudi.rva.core.bean.Address> apiToDtos(List<BanFeature> dtos, @Context BanContext context);

	/**
	 * @param entity entity to transform to dto
	 * @return dto
	 */
	@Mapping(source = "properties.label", target = "label")
	@Mapping(source = "properties.x", target = "x")
	@Mapping(source = "properties.y", target = "y")
	org.rudi.rva.core.bean.Address apiToDto(BanFeature entity, @Context BanContext context);

	@AfterMapping
	default void mapId(BanFeature entity, @MappingTarget org.rudi.rva.core.bean.Address dto,
			@Context BanContext context) {
		if (dto != null && entity != null) {
			String id = StringUtils.join(new Object[] { entity.getProperties().getId(),
					URLEncoder.encode(context.getQuery(), StandardCharsets.UTF_8), context.getType(),
					context.getLimit() }, '|');
			dto.setId(id);
		}
	}
}
