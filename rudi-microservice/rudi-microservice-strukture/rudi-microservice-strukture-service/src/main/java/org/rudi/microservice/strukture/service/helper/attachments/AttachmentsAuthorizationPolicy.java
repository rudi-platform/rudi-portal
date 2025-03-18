package org.rudi.microservice.strukture.service.helper.attachments;

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
	 * @param authenticatedUser l'utilsiateur connecté
	 * @param uploaderUuid uuid de la personne ayant uploadé le fichier
	 * @return true si l'utlsiateur connecté à le drot de supprimer le fichier
	 */
	@Override
	public boolean isAllowedToDeleteDocument(User authenticatedUser, UUID uploaderUuid) {
		if(rolesHelper.hasAnyRole(authenticatedUser, Role.ADMINISTRATOR, Role.MODERATOR)){
			return true;
		}
		return uploaderUuid.equals(authenticatedUser.getUuid());
	}

	/**
	 * @param authenticatedUser
	 * @param uploaderUuid
	 * @return true : tout le monde peut télécharger le document
	 */
	@Override
	public boolean isAllowedToDownloadDocument(User authenticatedUser, UUID uploaderUuid) {
		return true;
	}
}
