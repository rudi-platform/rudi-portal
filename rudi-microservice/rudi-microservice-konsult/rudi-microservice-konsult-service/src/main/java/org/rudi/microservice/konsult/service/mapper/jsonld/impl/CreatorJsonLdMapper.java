package org.rudi.microservice.konsult.service.mapper.jsonld.impl;

import java.util.UUID;

import com.google.gson.JsonObject;
import io.micrometer.common.util.StringUtils;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.facet.kaccess.bean.Organization;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.microservice.konsult.core.harvest.DcatJsonLdContext;
import org.rudi.microservice.konsult.service.helper.media.MediaUrlHelper;
import org.rudi.microservice.konsult.service.mapper.jsonld.AbstractKindJsonLdMapper;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CreatorJsonLdMapper extends AbstractKindJsonLdMapper<Organization> {

	private final OrganizationHelper organizationHelper;

	public CreatorJsonLdMapper(MediaUrlHelper mediaUrlHelper, OrganizationHelper organizationHelper) {
		super(mediaUrlHelper);
		this.organizationHelper = organizationHelper;
	}

	@Override
	public JsonObject toJsonLd(Organization organization, DcatJsonLdContext context) throws AppServiceException {
		UUID organizationUuid = organization.getOrganizationId();

		return toJsonLd(organizationUuid, extractValue(organization),
				null, extractUrl(organizationHelper.getOrganization(organizationUuid)));
	}

	protected String extractValue(Organization item) {
		if (StringUtils.isNotEmpty(item.getOrganizationCaption())) {
			return item.getOrganizationCaption();
		}

		if (StringUtils.isNotEmpty(item.getOrganizationName())) {
			return item.getOrganizationName();
		}

		return "Creator";
	}


	private String extractUrl(org.rudi.facet.organization.bean.Organization organization) {
		if (organization != null && StringUtils.isNotEmpty(organization.getUrl())) {
			return organization.getUrl();
		}
		return null;
	}
}
