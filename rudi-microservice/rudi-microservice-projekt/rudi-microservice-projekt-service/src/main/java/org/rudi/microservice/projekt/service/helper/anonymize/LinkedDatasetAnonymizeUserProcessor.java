package org.rudi.microservice.projekt.service.helper.anonymize;

import org.rudi.facet.bpmn.helper.AbstractAnonymizeUserProcessor;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.microservice.projekt.core.bean.criteria.LinkedDatasetSearchCriteria;
import org.rudi.microservice.projekt.service.workflow.LinkedDatasetTaskServiceImpl;
import org.rudi.microservice.projekt.storage.dao.linkeddataset.LinkedDatasetDao;
import org.rudi.microservice.projekt.storage.dao.linkeddataset.impl.LinkedDatasetCustomDaoImpl;
import org.rudi.microservice.projekt.storage.entity.linkeddataset.LinkedDatasetEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class LinkedDatasetAnonymizeUserProcessor extends AbstractAnonymizeUserProcessor<LinkedDatasetEntity, LinkedDatasetSearchCriteria, LinkedDatasetCustomDaoImpl, LinkedDatasetDao> {
	protected LinkedDatasetAnonymizeUserProcessor(LinkedDatasetCustomDaoImpl assetDescriptionCustomDao, LinkedDatasetDao assetDescriptionDao, BpmnHelper bpmnHelper) {
		super(assetDescriptionCustomDao, assetDescriptionDao, bpmnHelper);
	}

	@Override
	protected LinkedDatasetSearchCriteria createAssetDescriptionCriteria(String login) {
		return LinkedDatasetSearchCriteria.builder().initiator(login).build();
	}

	@Override
	protected Page<LinkedDatasetEntity> search(LinkedDatasetSearchCriteria criteria, Pageable pageable) {
		return getAssetDescriptionCustomDao().searchLinkedDatasets(criteria, pageable);
	}

	@Override
	protected String getProcessDefinitionId() {
		return LinkedDatasetTaskServiceImpl.PROCESS_DEFINITION_ID;
	}
}
