package org.rudi.microservice.projekt.service.ownerinfo.impl;

import java.util.Collection;
import java.util.UUID;

import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.facet.organization.bean.Organization;
import org.rudi.facet.organization.bean.OrganizationMember;
import org.rudi.facet.organization.bean.OrganizationRole;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationException;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationMembersException;
import org.rudi.microservice.projekt.core.bean.OwnerInfo;
import org.rudi.microservice.projekt.core.bean.OwnerType;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class OrganizationInfoHelper implements OwnerInfoHelper {
	private final OrganizationHelper organizationHelper;

	@Override
	public boolean isHelperFor(OwnerType ownerType) {
		return ownerType == OwnerType.ORGANIZATION;
	}

	@Override
	public OwnerInfo getOwnerInfo(UUID ownerUuid) throws GetOrganizationException, AppServiceNotFoundException, GetOrganizationMembersException {
		final var organization = organizationHelper.getOrganization(ownerUuid);
		if (organization == null) {
			throw new AppServiceNotFoundException(Organization.class, ownerUuid);
		}
		return toOwnerInfo(ownerUuid, organization);
	}

	private OwnerInfo toOwnerInfo(UUID ownerUuid, Organization organization) throws GetOrganizationMembersException {
		String contact = ownerUuid.toString();
		Collection<OrganizationMember> members = organizationHelper.getOrganizationMembers(ownerUuid);
		if (!members.isEmpty()) {
			contact = members.stream()
					.filter(m -> m.getRole().equals(OrganizationRole.ADMINISTRATOR))
					.findFirst()
					.map(OrganizationMember::getLogin)
					.orElse(contact);
		}

		return new OwnerInfo().name(organization.getName()).contact(contact);
	}
}
