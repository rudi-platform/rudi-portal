package org.rudi.microservice.projekt.service.helper.attachment;

import java.util.UUID;

import org.rudi.common.core.security.Role;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.RolesHelper;
import org.rudi.facet.doks.policy.AuthorizationPolicy;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttachmentsAuthorizationPolicy implements AuthorizationPolicy {

	private final RolesHelper rolesHelper;

	/**
	 * @param authenticatedUser l'utilisateur connecté
	 * @param uploaderUuid uuid du propriétaire du document
	 * @return true : tout le monde peut télécharger le document
	 */
	@Override
	public boolean isAllowedToDownloadDocument(User authenticatedUser, UUID uploaderUuid) {
		return true;
	}

	/**
	 * @param authenticatedUser utilisateur connecté
	 * @param uploaderUuid uuid du propriétaire du document
	 * @return true si la personne authentifiée est administrateur, modérateur ou le propriétaire du document
	 */
	@Override
	public boolean isAllowedToDeleteDocument(User authenticatedUser, UUID uploaderUuid) {
		if (rolesHelper.hasAnyRole(authenticatedUser, Role.ADMINISTRATOR, Role.MODERATOR)) {
			return true;
		}

		return uploaderUuid.equals(authenticatedUser.getUuid());
	}
}
