/**
 * RUDI Portail
 */
package org.rudi.facet.rva.impl.rva;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.rudi.common.service.mapper.MapperUtils;
import org.rudi.facet.rva.impl.rva.bean.Address;

/**
 * @author FNI18300
 *
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { MapperUtils.class })
public interface RvaAddressMapper {

	/**
	 * @param dtos
	 * @return la liste d'entité converties
	 */
	List<org.rudi.rva.core.bean.Address> apiToDtos(List<Address> dtos);

	/**
	 * @param entity entity to transform to dto
	 * @return dto
	 */
	@Mapping(source = "idaddress", target = "id")
	org.rudi.rva.core.bean.Address apiToDto(Address entity);

	@AfterMapping
	default void mapLabel(Address api, @MappingTarget org.rudi.rva.core.bean.Address dto) {
		if (api != null && dto != null) {
			StringBuilder label = new StringBuilder();

			appendPart(label, api.getAddr3());

			dto.setLabel(label.toString());
		}
	}

	default void appendPart(StringBuilder buffer, String value) {
		if (StringUtils.isNotEmpty(value)) {
			if (buffer.length() > 0) {
				buffer.append(' ');
			}
			buffer.append(value);
		}
	}

}
