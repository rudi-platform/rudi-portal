package org.rudi.facet.bpmn.datafactory;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.UUID;

import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.service.datafactory.AbstractDataFactory;
import org.rudi.facet.bpmn.dao.workflow.AssetDescriptionDao;
import org.rudi.facet.bpmn.entity.workflow.AbstractAssetDescriptionEntity;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional
@RequiredArgsConstructor
public abstract class AbstractAssetDescriptionDataFactory <E extends AbstractAssetDescriptionEntity, R extends AssetDescriptionDao<E>> extends AbstractDataFactory {

	protected final R repository;
	private final Class<E> type;

	public E create(UUID uuid, String processDefinitionKey, Status status, String functionnalStatus, String initiator, LocalDateTime creationDate, String description) {

		try {
			Constructor<E> constructor = type.getConstructor(new Class<?>[0]);
			E item = constructor.newInstance(new Object[0]);
			item.setUuid(uuid);
			item.setProcessDefinitionKey(processDefinitionKey);
			item.setStatus(status);
			item.setFunctionalStatus(functionnalStatus);
			item.setInitiator(initiator);
			item.setCreationDate(handleDate(creationDate));
			item.setDescription(description);
			assignData(item);
			return repository.save(item);
		} catch(Exception e){
			throw new IllegalArgumentException("Failed to create item for " + type, e);
		}
	}

	protected void assignData(E item){}

	public long countAll() {
		return repository.count();
	}
}
