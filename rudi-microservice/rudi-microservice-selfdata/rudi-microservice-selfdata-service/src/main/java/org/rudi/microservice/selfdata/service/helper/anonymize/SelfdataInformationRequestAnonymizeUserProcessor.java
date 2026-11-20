package org.rudi.microservice.selfdata.service.helper.anonymize;

import org.rudi.facet.bpmn.helper.AbstractAnonymizeUserProcessor;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.microservice.selfdata.core.bean.criteria.SelfdataInformationRequestSearchCriteria;
import org.rudi.microservice.selfdata.service.helper.selfdatamatchingdata.SelfdataMatchingDataHelper;
import org.rudi.microservice.selfdata.storage.dao.selfdatainformationrequest.SelfdataInformationRequestDao;
import org.rudi.microservice.selfdata.storage.dao.selfdatainformationrequest.impl.SelfdataInformationRequestCustomDaoImpl;
import org.rudi.microservice.selfdata.storage.entity.selfdatainformationrequest.SelfdataInformationRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class SelfdataInformationRequestAnonymizeUserProcessor extends AbstractAnonymizeUserProcessor<SelfdataInformationRequestEntity, SelfdataInformationRequestSearchCriteria, SelfdataInformationRequestCustomDaoImpl, SelfdataInformationRequestDao> {

	protected SelfdataInformationRequestAnonymizeUserProcessor(SelfdataInformationRequestCustomDaoImpl assetDescriptionCustomDao, SelfdataInformationRequestDao assetDescriptionDao, BpmnHelper bpmnHelper) {
		super(assetDescriptionCustomDao, assetDescriptionDao, bpmnHelper);
	}

	@Override
	protected SelfdataInformationRequestSearchCriteria createAssetDescriptionCriteria(String login) {
		return SelfdataInformationRequestSearchCriteria.builder().initiator(login).build();
	}

	@Override
	protected Page<SelfdataInformationRequestEntity> search(SelfdataInformationRequestSearchCriteria criteria, Pageable pageable) {
		return getAssetDescriptionCustomDao().searchSelfdataInformationRequests(criteria, pageable);
	}

	@Override
	protected String getProcessDefinitionId() {
		return SelfdataMatchingDataHelper.PROCESS_DEFINITION_ID;
	}
}
