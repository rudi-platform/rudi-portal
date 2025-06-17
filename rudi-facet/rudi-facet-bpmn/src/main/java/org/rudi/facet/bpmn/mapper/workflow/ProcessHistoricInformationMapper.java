package org.rudi.facet.bpmn.mapper.workflow;

import java.util.Collection;
import java.util.List;

import org.activiti.engine.history.HistoricProcessInstance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.rudi.bpmn.core.bean.ProcessHistoricInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProcessHistoricInformationMapper {

	/**
	 * @param entity entity to transform to dto
	 * @return dto
	 */
	@Mapping(target = "startUser", source = "startUserId")
	ProcessHistoricInformation entityToDto(HistoricProcessInstance entity);

	/**
	 * @param entities
	 * @return la liste de dtos convertis
	 */
	List<ProcessHistoricInformation> entitiesToDto(Collection<HistoricProcessInstance> entities);

	default Page<ProcessHistoricInformation> entitiesToDto(Page<HistoricProcessInstance> entities, Pageable pageable) {
		return new PageImpl<>(entitiesToDto(entities.getContent()), pageable, entities.getTotalElements());
	}

}
