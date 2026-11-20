package org.rudi.microservice.projekt.service.notifications;

import java.util.UUID;

import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;

public interface NotifyService {
	/**
	 * @param organizationUuid
	 * @param userUUid
	 * Traiter l'ajout d'un nouveau membre dans une organisation : il s'agit de lui affecter correctement
	 * les tasks auxquelles il a droit après cet ajout
	 */
	void handleAddOrganizationMember(UUID organizationUuid, UUID userUUid);

	/**
	 * @param organizationUuid
	 * @param userUUid
	 * Traiter la suppression d'un membre dans une organisation : il s'agit de lui retirer les droits
	 * qu'il avait grâce à cette organisation
	 */
	void handleRemoveOrganizationMember(UUID organizationUuid, UUID userUUid);

	void handleUserTasks(String login, UUID userUuid) throws FormDefinitionException, InvalidDataException;
}
