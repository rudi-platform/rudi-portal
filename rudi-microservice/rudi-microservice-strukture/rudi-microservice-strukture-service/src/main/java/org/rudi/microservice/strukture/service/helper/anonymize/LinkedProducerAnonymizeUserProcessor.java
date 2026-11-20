package org.rudi.microservice.strukture.service.helper.anonymize;

import org.rudi.facet.bpmn.helper.AbstractAnonymizeUserProcessor;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.microservice.strukture.core.bean.criteria.LinkedProducerSearchCriteria;
import org.rudi.microservice.strukture.service.workflow.LinkedProducerTaskServiceImpl;
import org.rudi.microservice.strukture.storage.dao.provider.LinkedProducerDao;
import org.rudi.microservice.strukture.storage.dao.provider.impl.LinkedProducerCustomDaoImpl;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class LinkedProducerAnonymizeUserProcessor extends AbstractAnonymizeUserProcessor<LinkedProducerEntity, LinkedProducerSearchCriteria, LinkedProducerCustomDaoImpl, LinkedProducerDao> {

	protected LinkedProducerAnonymizeUserProcessor(LinkedProducerCustomDaoImpl assetDescriptionCustomDao, LinkedProducerDao assetDescriptionDao, BpmnHelper bpmnHelper) {
		super(assetDescriptionCustomDao, assetDescriptionDao, bpmnHelper);
	}

	@Override
	protected LinkedProducerSearchCriteria createAssetDescriptionCriteria(String login) {
		return LinkedProducerSearchCriteria.builder().initiator(login).build();
	}

	@Override
	protected Page<LinkedProducerEntity> search(LinkedProducerSearchCriteria criteria, Pageable pageable) {
		return getAssetDescriptionCustomDao().searchLinkedProducers(criteria, pageable);
	}

	@Override
	protected String getProcessDefinitionId() {
		return LinkedProducerTaskServiceImpl.PROCESS_DEFINITION_ID;
	}
}
