package org.rudi.microservice.projekt.service.helper;

import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.ListUtils;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Component
@RequiredArgsConstructor
public class MyInformationsHelper {

	private final ACLHelper aclHelper;
	private final OrganizationHelper organizationHelper;

	public List<UUID> getMeAndMyOrganizationsUuids() throws AppServiceUnauthorizedException, GetOrganizationException {
		val user = aclHelper.getAuthenticatedUser();
		// Récupération des UUIDs du connectedUser et de ses organisations.
		return ListUtils.union(ListUtils.emptyIfNull(getMyOrganizationsUuids()), List.of(user.getUuid()));
	}

	public List<UUID> getMyOrganizationsUuids() throws AppServiceUnauthorizedException, GetOrganizationException {
		val user = aclHelper.getAuthenticatedUser();
		if (user == null || user.getLogin() == null) {
			throw new AppServiceUnauthorizedException(
					"Impossible de récupérer les demandes de l'utilisateur connecté sans être authentifié");
		}

		//Récupération des UUIDs du connectedUser et de ses organisations.
		return organizationHelper.getMyOrganizationsUuids(user.getUuid());
	}

}
