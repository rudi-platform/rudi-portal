package org.rudi.facet.bpmn.helper;

import java.util.UUID;

import org.rudi.common.storage.dao.AbstractCustomDaoImpl;
import org.rudi.facet.bpmn.dao.workflow.AssetDescriptionDao;
import org.rudi.facet.bpmn.entity.workflow.AbstractAssetDescriptionEntity;
import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAnonymizeUserProcessor<E extends AbstractAssetDescriptionEntity, C, R extends AbstractCustomDaoImpl<E, C>, D extends AssetDescriptionDao<E>> implements AssetDescriptionAnonymizeUserProcessor {

	@Getter(value = AccessLevel.PROTECTED)
	private final R assetDescriptionCustomDao;
	@Getter(value = AccessLevel.PROTECTED)
	private final D assetDescriptionDao;
	private final BpmnHelper bpmnHelper;

	protected AbstractAnonymizeUserProcessor(R assetDescriptionCustomDao, D assetDescriptionDao, BpmnHelper bpmnHelper) {
		this.assetDescriptionCustomDao = assetDescriptionCustomDao;
		this.assetDescriptionDao = assetDescriptionDao;
		this.bpmnHelper = bpmnHelper;
	}

	public void process(String login, UUID userUuid) throws FormDefinitionException, InvalidDataException {
		bpmnHelper.handleUserTasks(login, getProcessDefinitionId());
		anonymizeUser(login, userUuid);
	}

	private void anonymizeUser(String login, UUID userUuid) {
		C criteria = createAssetDescriptionCriteria(login);
		search(criteria, Pageable.unpaged()).forEach(entity -> {

			if (login.equals(entity.getInitiator())) { // par précaution. Parce qu'au moindre bug dans un Criteria, ça anonymiserait l'intégralité des initiators de la table concernée, c'est arrivé lors des tests.
				entity.setInitiator(userUuid.toString());
			}

			if (login.equals(entity.getAssignee())) {
				entity.setAssignee(userUuid.toString());
			}

			if (login.equals(entity.getUpdator())) {
				entity.setUpdator(userUuid.toString());
			}

			anonymizeSpecificFields(entity, login, userUuid);

			assetDescriptionDao.save(entity);
		});
	}

	protected void anonymizeSpecificFields(E entity, String login, UUID userUuid) {
	}

	protected abstract C createAssetDescriptionCriteria(String login);

	protected abstract Page<E> search(C criteria, Pageable pageable);

	protected abstract String getProcessDefinitionId();
}
