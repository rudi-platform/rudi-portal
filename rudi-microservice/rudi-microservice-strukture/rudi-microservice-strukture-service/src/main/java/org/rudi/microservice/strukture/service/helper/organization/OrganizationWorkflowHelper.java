package org.rudi.microservice.strukture.service.helper.organization;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Strings;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.bpmn.entity.workflow.AssetDescriptionEntity;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.facet.bpmn.helper.form.FormHelper;
import org.rudi.facet.bpmn.helper.workflow.AbstactAssetDescriptionHelper;
import org.rudi.facet.bpmn.helper.workflow.BpmnHelper;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.service.helper.organization.processor.OrganizationTaskUpdateProcessor;
import org.rudi.microservice.strukture.service.mapper.OrganizationFullMapper;
import org.rudi.microservice.strukture.service.mapper.OrganizationSimpleMapper;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizationWorkflowHelper extends AbstactAssetDescriptionHelper<OrganizationEntity, Organization, OrganizationFullMapper> {

	public static final String DRAFT_TYPE_FORM_ARCHIVE_VALUE = "archive";
	public static final String DRAFT_TYPE_FORM_UPDATE_VALUE = "update";
	private static final String DRAFT_TYPE_FORM_KEY = "draftType";

	private final List<OrganizationTaskUpdateProcessor> organizationTaskUpdateProcessors;
	private final OrganizationSimpleMapper assetDescriptionSimpleMapper;

	public OrganizationWorkflowHelper(UtilContextHelper utilContextHelper, FormHelper formHelper, BpmnHelper bpmnHelper,
			OrganizationFullMapper assetDescriptionMapper,
			List<OrganizationTaskUpdateProcessor> organizationTaskUpdateProcessors, OrganizationSimpleMapper assetDescriptionSimpleMapper) {
		super(utilContextHelper, formHelper, bpmnHelper, assetDescriptionMapper);
		this.organizationTaskUpdateProcessors = organizationTaskUpdateProcessors;
		this.assetDescriptionSimpleMapper = assetDescriptionSimpleMapper;
	}


	@Override
	protected OrganizationEntity createAsset() {
		return new OrganizationEntity();
	}


	public String getDraftType(AssetDescriptionEntity assetDescriptionEntity) throws InvalidDataException {
		return getDraftType(assetDescriptionEntity.getData());
	}

	public String getDraftType(String data) throws InvalidDataException {
		FormHelper formHelper = getFormHelper();
		Map<String, Object> hydrate = formHelper.hydrateData(data);
		if (hydrate.containsKey(DRAFT_TYPE_FORM_KEY)) {
			return hydrate.get(DRAFT_TYPE_FORM_KEY).toString();
		}
		return null;
	}

	public boolean isDraftTypeArchive(String draftType) {
		return Strings.CS.equals(DRAFT_TYPE_FORM_ARCHIVE_VALUE, draftType);
	}

	public boolean isDraftTypeUpdate(String draftType) {
		return Strings.CS.equals(DRAFT_TYPE_FORM_UPDATE_VALUE, draftType);
	}

	@Override
	public void updateAssetEntity(Organization assetDescription, OrganizationEntity assetDescriptionEntity) throws InvalidDataException {
		assetDescriptionSimpleMapper.dtoToEntity(assetDescription, assetDescriptionEntity);

		if (CollectionUtils.isNotEmpty(organizationTaskUpdateProcessors)) {
			for (final OrganizationTaskUpdateProcessor processor : organizationTaskUpdateProcessors) {
				processor.process(assetDescription, assetDescriptionEntity);
			}
		}
	}
}
