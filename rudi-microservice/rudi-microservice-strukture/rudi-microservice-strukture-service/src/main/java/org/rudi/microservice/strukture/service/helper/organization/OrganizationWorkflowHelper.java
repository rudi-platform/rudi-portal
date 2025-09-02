package org.rudi.microservice.strukture.service.helper.organization;

import java.util.Map;

import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.bpmn.entity.workflow.AssetDescriptionEntity;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.AbstactAssetDescriptionHelper;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.service.mapper.OrganizationMapper;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizationWorkflowHelper extends AbstactAssetDescriptionHelper<OrganizationEntity, Organization, OrganizationMapper> {

	public static final String DRAFT_TYPE_FORM_ARCHIVE_VALUE = "archive";
	private static final String DRAFT_TYPE_FORM_KEY = "draftType";

	public OrganizationWorkflowHelper(UtilContextHelper utilContextHelper, FormHelper formHelper, BpmnHelper bpmnHelper, OrganizationMapper assetDescriptionMapper) {
		super(utilContextHelper, formHelper, bpmnHelper, assetDescriptionMapper);
	}


	@Override
	protected OrganizationEntity createAsset() {
		return new OrganizationEntity();
	}


	public String getDraftType(AssetDescriptionEntity assetDescriptionEntity) throws InvalidDataException {
		FormHelper formHelper = getFormHelper();
		Map<String, Object> hydrate = formHelper.hydrateData(assetDescriptionEntity.getData());
		if (hydrate.containsKey(DRAFT_TYPE_FORM_KEY)) {
			return hydrate.get(DRAFT_TYPE_FORM_KEY).toString();
		}
		return null;
	}

}
