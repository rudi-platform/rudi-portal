package org.rudi.microservice.projekt.service.helper.project.processor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.rudi.bpmn.core.bean.Field;
import org.rudi.bpmn.core.bean.FieldDefinition;
import org.rudi.bpmn.core.bean.FieldType;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Log4j2
public abstract class AbstractProjectTastUpdateProjectStringFieldProcessor implements ProjectTaskUpdateProjectProcessor {

	@Getter
	private final String acceptedField;

	protected abstract void assignEntities(Object value, ProjectEntity projectEntity);

	protected Object validate(String fieldValue, FieldDefinition definition){
		if(StringUtils.isNotEmpty(fieldValue)){
			if(definition.getType().equals(FieldType.DATE)){
				try {
					return LocalDateTime.parse(fieldValue, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
				}catch(Exception e){
					log.error("Une erreur est survenur lors de la conversion du champ {}", fieldValue, e);
				}
			}
			return fieldValue;
		}
		return null;
	}

	@Override
	public void process(Field field, ProjectEntity projectEntity) {
		Object value = validate(field.getValues()!= null ? field.getValues().get(0) : null , field.getDefinition());
		assignEntities(value, projectEntity);
	}
}
