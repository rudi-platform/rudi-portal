package org.rudi.microservice.acl.service.mapper.externalaccountrequest;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.rudi.common.service.mapper.MapperUtils;
import org.rudi.facet.bpmn.mapper.workflow.AssetDescriptionMapper;
import org.rudi.microservice.acl.core.bean.ExternalAccountRequest;
import org.rudi.microservice.acl.storage.entity.externalaccountrequest.ExternalAccountRequestEntity;
import org.rudi.microservice.acl.storage.entity.externalaccountrequest.ExternalAccountRequestStatus;
import org.springframework.stereotype.Component;

/**
 * Mapper entité/DTO pour la demande de création de compte dans le système d'authentification externe
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = { MapperUtils.class })
@Component
public abstract class ExternalAccountRequestMapper
		implements AssetDescriptionMapper<ExternalAccountRequestEntity, ExternalAccountRequest> {

	@Override
	@InheritInverseConfiguration
	public abstract ExternalAccountRequestEntity dtoToEntity(ExternalAccountRequest dto);

	/**
	 * Utilisé uniquement pour la modification d'une entité existante (on ignore l'uuid pour ne pas l'écraser)
	 */
	@Mapping(source = "uuid", target = "uuid", ignore = true)
	public abstract void dtoToEntity(ExternalAccountRequest dto, @MappingTarget ExternalAccountRequestEntity entity);

	// Renseigne object_type (nom de classe) requis pour la désérialisation polymorphe côté front
	@AfterMapping
	@Mapping(target = "processDefinitionKey", ignore = true)
	@Mapping(target = "processDefinitionVersion", ignore = true)
	@Mapping(target = "creationDate", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "functionalStatus", ignore = true)
	@Mapping(target = "initiator", ignore = true)
	@Mapping(target = "updator", ignore = true)
	@Mapping(target = "updatedDate", ignore = true)
	@Mapping(target = "assignee", ignore = true)
	public void updateType(@MappingTarget ExternalAccountRequest dto, ExternalAccountRequestEntity entity) {
		dto.setObjectType(dto.getClass().getSimpleName());
	}

	@AfterMapping
	protected void generateDescriptionIfMissing(@MappingTarget ExternalAccountRequestEntity entity) {
		if (StringUtils.isBlank(entity.getDescription())) {
			entity.setDescription(buildDescription(entity));
		}
	}

	@AfterMapping
	protected void generateFunctionalStatusIfMissing(@MappingTarget ExternalAccountRequestEntity entity) {
		if (entity.getFunctionalStatus() == null) {
			entity.setFunctionalStatus("Demande créée");
		}
	}

	@AfterMapping
	protected void generateExternalAccountRequestStatusIfMissing(@MappingTarget ExternalAccountRequestEntity entity) {
		if (entity.getExternalAccountRequestStatus() == null) {
			entity.setExternalAccountRequestStatus(ExternalAccountRequestStatus.PENDING);
		}
	}

	private String buildDescription(ExternalAccountRequestEntity entity) {
		return String.format("Demande de création de compte de %s %s (%s)",
				StringUtils.defaultString(entity.getFirstname()), StringUtils.defaultString(entity.getLastname()),
				StringUtils.defaultString(entity.getEmail()));
	}

}
