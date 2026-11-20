package org.rudi.microservice.projekt.service.helper.anonymize;

import org.rudi.facet.bpmn.helper.AbstractAnonymizeUserProcessor;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.microservice.projekt.core.bean.criteria.NewDatasetRequestSearchCriteria;
import org.rudi.microservice.projekt.service.workflow.NewDatasetRequestTaskServiceImpl;
import org.rudi.microservice.projekt.storage.dao.newdatasetrequest.NewDatasetRequestDao;
import org.rudi.microservice.projekt.storage.dao.newdatasetrequest.impl.NewDatasetRequestCustomDaoImpl;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class NewDatasetRequestAnonymizeUserProcessor extends AbstractAnonymizeUserProcessor<NewDatasetRequestEntity, NewDatasetRequestSearchCriteria, NewDatasetRequestCustomDaoImpl, NewDatasetRequestDao> {

	protected NewDatasetRequestAnonymizeUserProcessor(NewDatasetRequestCustomDaoImpl assetDescriptionCustomDao, NewDatasetRequestDao assetDescriptionDao, BpmnHelper bpmnHelper) {
		super(assetDescriptionCustomDao, assetDescriptionDao, bpmnHelper);
	}

	@Override
	protected NewDatasetRequestSearchCriteria createAssetDescriptionCriteria(String login) {
		return NewDatasetRequestSearchCriteria.builder().initiator(login).build();
	}

	@Override
	protected Page<NewDatasetRequestEntity> search(NewDatasetRequestSearchCriteria criteria, Pageable pageable) {
		return getAssetDescriptionCustomDao().searchNewDatasetRequest(criteria, pageable);
	}

	@Override
	protected String getProcessDefinitionId() {
		return NewDatasetRequestTaskServiceImpl.PROCESS_DEFINITION_ID;
	}
}
