package org.rudi.microservice.strukture.service.helper.provider;

import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.AbstactAssetDescriptionHelper;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.service.mapper.LinkedProducerMapper;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.springframework.stereotype.Component;

@Component
public class LinkedProducerWorkflowHelper extends AbstactAssetDescriptionHelper<LinkedProducerEntity, LinkedProducer, LinkedProducerMapper> {

	protected LinkedProducerWorkflowHelper(UtilContextHelper utilContextHelper, FormHelper formHelper, BpmnHelper bpmnHelper, LinkedProducerMapper assetDescriptionMapper) {
		super(utilContextHelper, formHelper, bpmnHelper, assetDescriptionMapper);
	}

	/**
	 * @return
	 */
	@Override
	protected LinkedProducerEntity createAsset() {
		return new LinkedProducerEntity();
	}
}
