package org.rudi.microservice.strukture.service.mapper;

import java.util.Collection;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.rudi.common.service.mapper.MapperUtils;
import org.rudi.facet.bpmn.mapper.workflow.AssetDescriptionMapper;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {
		MapperUtils.class,
		OrganizationMapper.class
})
public interface LinkedProducerMapper extends AssetDescriptionMapper<LinkedProducerEntity, LinkedProducer> {
	/**
	 * @param dto to transform to entity
	 * @return entity
	 */
	@Override
	@InheritInverseConfiguration
	LinkedProducerEntity dtoToEntity(LinkedProducer dto);

	/**
	 * update entity with dto
	 *
	 * @param dto
	 * @param entity
	 */
	@Override
	@Mapping(target = "processDefinitionKey", ignore = true)
	@Mapping(target = "processDefinitionVersion", ignore = true)
	@Mapping(target = "creationDate", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "functionalStatus", ignore = true)
	@Mapping(target = "initiator", ignore = true)
	@Mapping(target = "updator", ignore = true)
	@Mapping(target = "updatedDate", ignore = true)
	@Mapping(target = "assignee", ignore = true)
	@Mapping(target="organization", ignore = true)
	void dtoToEntity(LinkedProducer dto, @MappingTarget LinkedProducerEntity entity);

	/**
	 * @param dtos
	 * @return la liste d'entité converties
	 */
	@Override
	List<LinkedProducerEntity> dtoToEntities(List<LinkedProducer> dtos);

	/**
	 * @param entity entity to transform to dto
	 * @return dto
	 */
	@Override
	LinkedProducer entityToDto(LinkedProducerEntity entity);

	/**
	 * @param entities
	 * @return la liste de dtos convertis
	 */
	@Override
	List<LinkedProducer> entitiesToDto(Collection<LinkedProducerEntity> entities);


	@AfterMapping
	default void updateType(@MappingTarget LinkedProducer dto, LinkedProducerEntity entity){
		dto.setObjectType(dto.getClass().getSimpleName());
	}
}
