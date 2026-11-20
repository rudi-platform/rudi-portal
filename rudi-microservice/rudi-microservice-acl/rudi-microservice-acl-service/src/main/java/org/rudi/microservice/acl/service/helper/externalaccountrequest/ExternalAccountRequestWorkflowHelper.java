package org.rudi.microservice.acl.service.helper.externalaccountrequest;

import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.AbstactAssetDescriptionHelper;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.microservice.acl.core.bean.ExternalAccountRequest;
import org.rudi.microservice.acl.service.mapper.externalaccountrequest.ExternalAccountRequestMapper;
import org.rudi.microservice.acl.storage.entity.externalaccountrequest.ExternalAccountRequestEntity;
import org.springframework.stereotype.Component;

/**
 * Helper de construction/mise à jour de l'entité ExternalAccountRequest à partir du DTO et inversement
 */
@Component
public class ExternalAccountRequestWorkflowHelper extends
		AbstactAssetDescriptionHelper<ExternalAccountRequestEntity, ExternalAccountRequest, ExternalAccountRequestMapper> {

	public ExternalAccountRequestWorkflowHelper(UtilContextHelper utilContextHelper, FormHelper formHelper,
			BpmnHelper bpmnHelper, ExternalAccountRequestMapper assetDescriptionMapper) {
		super(utilContextHelper, formHelper, bpmnHelper, assetDescriptionMapper);
	}

	@Override
	protected ExternalAccountRequestEntity createAsset() {
		return new ExternalAccountRequestEntity();
	}

}

