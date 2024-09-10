package org.rudi.microservice.projekt.service.helper.project.processor;

import java.util.HashSet;
import java.util.Set;

import org.rudi.bpmn.core.bean.Field;
import org.rudi.common.core.bean.criteria.AbstractStampedSearchCriteria;
import org.rudi.common.facade.util.UtilPageable;
import org.rudi.common.storage.entity.AbstractStampedEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractProjectTaskUpdateProjectStampedEntityProcessor<C extends AbstractStampedSearchCriteria, E extends AbstractStampedEntity> implements ProjectTaskUpdateProjectProcessor {

	@Getter
	private final String acceptedField;
	private final UtilPageable utilPageable;

	protected abstract Page<E> getSearch(C criteria, Pageable pageable);

	protected abstract void assignEntities(Set<E> set, ProjectEntity projectEntity);

	protected abstract C getCriteria(String value);

	/**
	 * @param field
	 * @param projectEntity
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void process(Field field, ProjectEntity projectEntity) {
		Pageable pageable = utilPageable.getPageable(0,1,null);
		Set<E> entitiesToAdd = new HashSet<>();
		if(field.getValues() != null){
			for(final String value : field.getValues()){
				C criteria = getCriteria(value);
				Page<E> entities = getSearch(criteria, pageable);
				if(entities != null){
					entitiesToAdd.add(entities.getContent().get(0));
				}
			}
		}

		assignEntities(entitiesToAdd, projectEntity);
	}
}
