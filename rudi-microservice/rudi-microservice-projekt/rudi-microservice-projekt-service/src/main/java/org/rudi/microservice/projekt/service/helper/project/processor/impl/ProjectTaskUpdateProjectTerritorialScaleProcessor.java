package org.rudi.microservice.projekt.service.helper.project.processor.impl;

import java.util.Set;

import org.rudi.common.facade.util.UtilPageable;
import org.rudi.microservice.projekt.core.bean.criteria.TerritorialScaleSearchCriteria;
import org.rudi.microservice.projekt.service.helper.project.processor.AbstractProjectTaskUpdateProjectStampedEntityProcessor;
import org.rudi.microservice.projekt.storage.dao.territory.TerritorialScaleCustomDao;
import org.rudi.microservice.projekt.storage.entity.TerritorialScaleEntity;
import org.rudi.microservice.projekt.storage.entity.project.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import static org.rudi.microservice.projekt.service.workflow.ProjektWorkflowConstants.TERRITORIAL_SCALE_FIELD_NAME;

@Component
public class ProjectTaskUpdateProjectTerritorialScaleProcessor extends AbstractProjectTaskUpdateProjectStampedEntityProcessor<TerritorialScaleSearchCriteria, TerritorialScaleEntity> {

	private final TerritorialScaleCustomDao territorialScaleCustomDao;

	protected ProjectTaskUpdateProjectTerritorialScaleProcessor( UtilPageable utilPageable, TerritorialScaleCustomDao territorialScaleCustomDao) {
		super(TERRITORIAL_SCALE_FIELD_NAME, utilPageable);
		this.territorialScaleCustomDao = territorialScaleCustomDao;
	}


	@Override
	protected Page<TerritorialScaleEntity> getSearch(TerritorialScaleSearchCriteria criteria, Pageable pageable) {
		return territorialScaleCustomDao.searchTerritorialScales(criteria, pageable);
	}


	@Override
	protected void assignEntities(Set<TerritorialScaleEntity> set, ProjectEntity projectEntity) {
		if(!set.isEmpty()) {
			set.stream().findFirst().ifPresent(projectEntity::setTerritorialScale);
		}
		else {
			projectEntity.setTerritorialScale(null);
		}
	}

	/**
	 * @param value
	 * @return
	 */
	@Override
	protected TerritorialScaleSearchCriteria getCriteria(String value) {
		return TerritorialScaleSearchCriteria.builder().active(true).code(value).build();
	}
}
