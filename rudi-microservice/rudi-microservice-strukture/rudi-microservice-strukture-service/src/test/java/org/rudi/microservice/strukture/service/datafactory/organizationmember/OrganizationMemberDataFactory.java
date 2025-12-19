package org.rudi.microservice.strukture.service.datafactory.organizationmember;

import java.util.UUID;

import org.rudi.common.service.datafactory.AbstractDataFactory;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.datafactory.UserDataFactory;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationDao;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationMemberEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationRole;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class OrganizationMemberDataFactory extends AbstractDataFactory {

	private final OrganizationDao repository;
	private final UserDataFactory userDataFactory;


	// Jean
	public OrganizationEntity createOrganizationMemberJeanAdministrator(OrganizationEntity organization) {
		User user = userDataFactory.getOrCreateJean();
		return createOrganizationMemberAdministrator(organization, user);
	}

	public OrganizationEntity createOrganizationMemberJeanEditor(OrganizationEntity organization) {
		User user = userDataFactory.getOrCreateJean();
		return createOrganizationMemberEditor(organization, user);
	}

	// Jacques
	public OrganizationEntity createOrganizationMemberJacquesAdministrator(OrganizationEntity organization) {
		User user = userDataFactory.getOrCreateJacques();
		return createOrganizationMemberAdministrator(organization, user);
	}

	public OrganizationEntity createOrganizationMemberJacquesEditor(OrganizationEntity organization) {
		User user = userDataFactory.getOrCreateJacques();
		return createOrganizationMemberEditor(organization, user);
	}


	// Utilitaire
	public OrganizationEntity createOrganizationMemberAdministrator(OrganizationEntity organization, User user) {
		return createOrganizationMember(organization, user.getUuid(), OrganizationRole.ADMINISTRATOR);
	}

	public OrganizationEntity createOrganizationMemberEditor(OrganizationEntity organization, User user) {
		return createOrganizationMember(organization, user.getUuid(), OrganizationRole.EDITOR);
	}

	private OrganizationEntity createOrganizationMember(OrganizationEntity organization, UUID userUuid, OrganizationRole role) {
		OrganizationMemberEntity ome = new OrganizationMemberEntity();
		ome.setUserUuid(userUuid);
		ome.setAddedDate(organization.getCreationDate().plusDays(1));
		ome.setRole(role);

		organization.getMembers().add(ome);

		return repository.save(organization);
	}

}
