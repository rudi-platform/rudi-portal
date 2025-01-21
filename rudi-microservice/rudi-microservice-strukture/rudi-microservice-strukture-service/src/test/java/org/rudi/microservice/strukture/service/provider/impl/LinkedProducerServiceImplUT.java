package org.rudi.microservice.strukture.service.provider.impl;

import java.security.InvalidParameterException;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.bpmn.core.bean.Task;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.core.security.UserType;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.acl.bean.Role;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.datafactory.UserDataFactory;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.bpmn.exception.FormConvertException;
import org.rudi.facet.bpmn.exception.FormDefinitionException;
import org.rudi.facet.bpmn.exception.InvalidDataException;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.core.bean.LinkedProducerStatus;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.service.StruktureSpringBootTest;
import org.rudi.microservice.strukture.service.datafactory.organization.OrganizationDataFactory;
import org.rudi.microservice.strukture.service.datafactory.provider.ProviderDataFactory;
import org.rudi.microservice.strukture.service.workflow.LinkedProducerTaskServiceImpl;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.dao.DataIntegrityViolationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
@StruktureSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LinkedProducerServiceImplUT {

	private final LinkedProducerServiceImpl linkedProducerService;
	private final LinkedProducerTaskServiceImpl linkedProducerTaskService;
	private final ProviderDataFactory providerDataFactory;
	private final OrganizationDataFactory organizationDataFactory;
	private final UserDataFactory userDataFactory;

	@MockitoBean
	private ACLHelper aclHelper;
	@MockitoBean
	private UtilContextHelper utilContextHelper;

	private OrganizationEntity mockOrganization(UUID uuid) {
		if (uuid == null) {
			uuid = UUID.randomUUID();
		}
		return organizationDataFactory.createTestOrganizationLinkedProducer(uuid);
	}

	private ProviderEntity mockProviderEntity(UUID id) {
		if (id == null) {
			id = UUID.randomUUID();
		}
		return providerDataFactory.createProvider(null, id);
	}

	private void mockAuthenticatedUserAsProvider(UUID nodeUuid) throws AppServiceUnauthorizedException {
		User user = userDataFactory.createUserNodeProvider(nodeUuid.toString());

		when(aclHelper.getAuthenticatedUser()).thenReturn(user);
		AuthenticatedUser authenticatedUser = createAuthenticatedUserFromUser(user);
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);

	}

	private void mockAuthenticatedUserAsUser(UUID nodeUuid) throws AppServiceUnauthorizedException {
		User user = userDataFactory.createUser(nodeUuid.toString());

		when(aclHelper.getAuthenticatedUser()).thenReturn(user);
		AuthenticatedUser authenticatedUser = createAuthenticatedUserFromUser(user);
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);

	}

	private AuthenticatedUser createAuthenticatedUserFromUser(User user) {
		AuthenticatedUser authenticatedUser = new AuthenticatedUser();
		authenticatedUser.setEmail(user.getLogin());
		authenticatedUser.setFirstname(user.getFirstname());
		authenticatedUser.setLastname(user.getLastname());
		authenticatedUser.setLogin(user.getLogin());
		authenticatedUser.setOrganization(user.getCompany());
		authenticatedUser.setRoles(user.getRoles().stream().map(Role::getCode).collect(Collectors.toList()));
		switch (user.getType()) {
			case API:
				authenticatedUser.setType(UserType.API);
				break;
			case ROBOT:
				authenticatedUser.setType(UserType.ROBOT);
				break;
			case PERSON:
				authenticatedUser.setType(UserType.PERSON);
				break;
			case MICROSERVICE:
				authenticatedUser.setType(UserType.MICROSERVICE);
				break;
			default:
				authenticatedUser.setType(UserType.PERSON);
				break;
		}
		return authenticatedUser;
	}

	private boolean compareEntityAndDto(Organization organziation, OrganizationEntity organziationEtity) {
		if (organziation == null || organziationEtity == null) {
			return false;
		}

		return organziation.getName().equals(organziationEtity.getName())
				&& organziation.getUuid().equals(organziationEtity.getUuid())
				&& organziation.getDescription().equals(organziationEtity.getDescription());
	}

	@Test
	@DisplayName("Création d'un linkedProducer et lancement du workflow")
	void createNewLinkedProducerAndInitializeWorkflow() throws AppServiceNotFoundException, AppServiceUnauthorizedException, AppServiceBadRequestException, FormDefinitionException, FormConvertException, InvalidDataException {
		UUID nodeProviderUuid = UUID.randomUUID();
		UUID organizationUuid = UUID.randomUUID();

		mockAuthenticatedUserAsProvider(nodeProviderUuid);
		mockProviderEntity(nodeProviderUuid);

		OrganizationEntity organizationEntity = mockOrganization(organizationUuid);
		LinkedProducer linkedProducerCreated = linkedProducerService.createLinkedProducer(organizationUuid);
		LinkedProducer linkedProducer = linkedProducerService.getLinkedProducer(linkedProducerCreated.getUuid());

		assertThat(linkedProducer)
				.as("Le resultat ne doit pas être null")
				.isNotNull()
				.as("L'organization concernée doit être la bonne")
				.matches(p -> compareEntityAndDto(p.getOrganization(), organizationEntity));

		Task task = linkedProducerTaskService.createDraft(linkedProducer);

		linkedProducerTaskService.startTask(task);
	}

	@Test
	@DisplayName("User sans les droits pour créer un lien entre un provider et un producer")
	void createNewLinkedProducerNotAuthorized() throws AppServiceUnauthorizedException {
		UUID nodeProviderUuid = UUID.randomUUID();
		UUID organizationUuid = UUID.randomUUID();

		mockAuthenticatedUserAsUser(UUID.randomUUID());
		mockProviderEntity(nodeProviderUuid);

		mockOrganization(organizationUuid);
		assertThrows(AppServiceUnauthorizedException.class, () -> linkedProducerService.createLinkedProducer(organizationUuid));
	}

	@Test
	@DisplayName("Tentative de liaison d'une organization déjà liée.")
	void createLinkedProducerAlreadyExisting() throws AppServiceUnauthorizedException {
		UUID nodeProviderUuid = UUID.randomUUID();

		mockAuthenticatedUserAsProvider(UUID.randomUUID());
		ProviderEntity providerEntity = mockProviderEntity(nodeProviderUuid);

		assertThat(providerEntity)
				.as("Le providerEntity doit contenir au moins un linkedProducer.")
				.matches(p -> !p.getLinkedProducers().isEmpty());

		LinkedProducerEntity linkedProducerEntity = providerEntity.getLinkedProducers().iterator().next();
		UUID organizationUuid = linkedProducerEntity.getOrganization().getUuid();

		assertThrows(InvalidParameterException.class, () -> linkedProducerService.createLinkedProducer(organizationUuid));
	}

	@Test
	@DisplayName("Lancement du workflow de détachement d'une organziation")
	void detachLinkedProducerAndInitializeWorkflow() throws AppServiceUnauthorizedException, FormDefinitionException, FormConvertException, InvalidDataException {
		UUID nodeProviderUuid = UUID.randomUUID();

		mockAuthenticatedUserAsProvider(nodeProviderUuid);

		// Création d'un provider avec au moins un linkedProducer
		ProviderEntity providerEntity = providerDataFactory.createProvider(null,nodeProviderUuid);

		assertThat(providerEntity)
				.as("Le contenu doit avoir été créé")
				.isNotNull()
				.as("La liste des LinkedProducer doit au moins en contenir un")
				.matches(p -> !p.getLinkedProducers().isEmpty());

		LinkedProducerEntity linkedProducerEntity = providerEntity.getLinkedProducers().stream().iterator().next();

		// On appelle la même fonction que celle appelée par le controller.
		UUID organizationUuid = linkedProducerEntity.getOrganization().getUuid();
		LinkedProducer linkedProducer = linkedProducerService.getMyLinkedProducerFromOrganizationUuid(organizationUuid);
		assertThat(linkedProducer)
				.as("Le resultat ne doit pas être null")
				.isNotNull()
				.as("L'organization concernée doit être la bonne")
				.matches(p -> compareEntityAndDto(p.getOrganization(), linkedProducerEntity.getOrganization()));

		Task task = linkedProducerTaskService.createDraft(linkedProducer);

		linkedProducerTaskService.startTask(task);

		LinkedProducer linkedProducerAfterWorkflowStarts = linkedProducerService.getLinkedProducer(linkedProducerEntity.getUuid());
		assertThat(linkedProducerAfterWorkflowStarts)
				.as("Son status doit avoir changé, et doit être à PENDING ")
				.matches(lp -> lp.getStatus() != linkedProducer.getStatus() && lp.getStatus().equals(Status.PENDING))
				.as("Le LinkedProducerStatus ne doit pas avoir changé, il doit donc être à VALIDATED")
				.matches(lp -> lp.getLinkedProducerStatus().equals(linkedProducer.getLinkedProducerStatus())
						&& lp.getLinkedProducerStatus().equals(LinkedProducerStatus.VALIDATED))
				.as("Mais les organization doivent être les mêmes.")
				.matches(lp -> lp.getOrganization().equals(linkedProducer.getOrganization()));
	}

	@Test
	@DisplayName("User sans les droits pour détacher une organziation")
	void detachNewLinkedProducertNotauthorized() throws AppServiceUnauthorizedException {
		UUID nodeProviderUuid = UUID.randomUUID();

		mockAuthenticatedUserAsUser(UUID.randomUUID());

		// Création d'un provider avec au moins un linkedProducer
		ProviderEntity providerEntity = providerDataFactory.createProvider(null,nodeProviderUuid);

		assertThat(providerEntity)
				.as("Le contenu doit avoir été créé")
				.isNotNull()
				.as("La liste des LinkedProducer doit au moins en contenir un")
				.matches(p -> !p.getLinkedProducers().isEmpty());

		LinkedProducerEntity linkedProducerEntity = providerEntity.getLinkedProducers().stream().iterator().next();

		// On appelle la même fonction que celle appelée par le controller.
		UUID organizationUuid = linkedProducerEntity.getOrganization().getUuid();
		assertThrows(AppServiceUnauthorizedException.class, () -> linkedProducerService.getMyLinkedProducerFromOrganizationUuid(organizationUuid));
	}

	@Test
	@DisplayName("Suppression d'un lien d'une organization non liée.")
	void detachInexistingLinkedProducer() throws AppServiceUnauthorizedException {
		UUID nodeProviderUuid = UUID.randomUUID();
		UUID organizationUuid = UUID.randomUUID();

		mockAuthenticatedUserAsProvider(nodeProviderUuid);
		mockOrganization(organizationUuid);

		// Création d'un provider avec au moins un linkedProducer
		ProviderEntity providerEntity = providerDataFactory.createProvider(null,nodeProviderUuid);

		assertThat(providerEntity)
				.as("Le contenu doit avoir été créé")
				.isNotNull()
				.as("La liste des LinkedProducer doit au moins en contenir un")
				.matches(p -> !p.getLinkedProducers().isEmpty());

		assertThrows(DataIntegrityViolationException.class, () -> linkedProducerService.getMyLinkedProducerFromOrganizationUuid(organizationUuid));
	}
}
