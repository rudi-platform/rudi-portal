package org.rudi.microservice.strukture.service.helper.anonymize;

import org.rudi.facet.bpmn.helper.AbstractAnonymizeUserProcessor;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationSearchCriteria;
import org.rudi.microservice.strukture.service.workflow.OrganizationTaskServiceImpl;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationDao;
import org.rudi.microservice.strukture.storage.dao.organization.impl.OrganizationCustomDaoImpl;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class OrganizationAnonymizeUserProcessor extends AbstractAnonymizeUserProcessor<OrganizationEntity, OrganizationSearchCriteria, OrganizationCustomDaoImpl, OrganizationDao> {

	protected OrganizationAnonymizeUserProcessor(OrganizationCustomDaoImpl assetDescriptionCustomDao, OrganizationDao assetDescriptionDao, BpmnHelper bpmnHelper) {
		super(assetDescriptionCustomDao, assetDescriptionDao, bpmnHelper);
	}

	@Override
	protected OrganizationSearchCriteria createAssetDescriptionCriteria(String login) {
		return OrganizationSearchCriteria.builder().initiator(login).build();
	}

	@Override
	protected Page<OrganizationEntity> search(OrganizationSearchCriteria criteria, Pageable pageable) {
		return getAssetDescriptionCustomDao().searchOrganizations(criteria, pageable);
	}

	@Override
	protected String getProcessDefinitionId() {
		return OrganizationTaskServiceImpl.PROCESS_DEFINITION_ID;
	}
}
