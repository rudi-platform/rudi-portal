package org.rudi.microservice.projekt.service.workflow.fieldcomputer;

import org.rudi.bpmn.core.bean.Field;
import org.rudi.common.core.bean.criteria.AbstractStampedSearchCriteria;
import org.rudi.common.storage.entity.AbstractStampedEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractListFormFieldProcessor<E extends AbstractStampedEntity, C extends AbstractStampedSearchCriteria> implements ListFormFieldProcessor {

	@Getter
	private final String acceptedFieldName;

	protected abstract Page<E> searchElements(C criteria, Pageable pageable);

	protected abstract C getCriteria();

	@Override
	public boolean accept(Field field) {
		return field.getDefinition().getName().equals(acceptedFieldName);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void process(Field field) {
		StringBuilder extendedFieldType = new StringBuilder();
		extendedFieldType.append("[");
		Page<E> entities = searchElements(getCriteria(), Pageable.unpaged());
		for (E entity : entities.getContent()) {
			extendedFieldType.append("{");
			extendedFieldType.append("\"code\":\"").append(entity.getCode()).append("\"");
			extendedFieldType.append(", \"label\":\"").append(entity.getLabel()).append("\"");
			extendedFieldType.append("},");
		}
		extendedFieldType.deleteCharAt(extendedFieldType.length() - 1);
		extendedFieldType.append("]");

		field.getDefinition().setExtendedType(extendedFieldType.toString());
	}
}
