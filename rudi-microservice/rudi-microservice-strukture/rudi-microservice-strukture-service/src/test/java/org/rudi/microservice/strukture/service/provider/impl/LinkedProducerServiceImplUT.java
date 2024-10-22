package org.rudi.microservice.strukture.service.provider.impl;

import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.service.StruktureSpringBootTest;
import org.rudi.microservice.strukture.service.datafactory.organization.OrganizationDataFactory;
import org.rudi.microservice.strukture.service.datafactory.provider.ProviderDataFactory;
import org.rudi.microservice.strukture.service.workflow.LinkedProducerTaskServiceImpl;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.annotation.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
@StruktureSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LinkedProducerServiceImplUT {

	private static final UUID ORGANIZATION_UUID = UUID.fromString("9f575153-0b9a-4ea9-bc8e-e157a0b92b47");
	private static final UUID NODE_PROVIDER_UUID = UUID.fromString("402c0589-bc46-4752-8c88-bcbab26d72b1");
	private static final String PROVIDER_CODE = "CODE";

	private final LinkedProducerServiceImpl linkedProducerService;
	private final LinkedProducerTaskServiceImpl linkedProducerTaskService;
	private final ProviderDataFactory providerDataFactory;
	private final OrganizationDataFactory organizationDataFactory;
	private final UserDataFactory userDataFactory;

	@MockBean
	private ACLHelper aclHelper;
	@MockBean
	private UtilContextHelper utilContextHelper;

	private OrganizationEntity mockOrganization(UUID uuid) {
		if (uuid == null) {
			uuid = ORGANIZATION_UUID;
		}
		return organizationDataFactory.createTestOrganizationLinkedProducer(uuid);
	}

	private ProviderEntity mockProviderEntity(UUID id) {
		if (id == null) {
			id = NODE_PROVIDER_UUID;
		}
		return providerDataFactory.createProviderWithOneNode(PROVIDER_CODE, id);
	}

	private void mockAuthenticatedUserAsProvider(UUID nodeUuid) throws AppServiceUnauthorizedException {
		User user = userDataFactory.createUserProvider(nodeUuid.toString());

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
	@Order(0)
	void createNewLinkedProducerAndInitializeWorkflow() throws AppServiceNotFoundException, AppServiceUnauthorizedException, AppServiceBadRequestException, FormDefinitionException, FormConvertException, InvalidDataException {

		mockAuthenticatedUserAsProvider(NODE_PROVIDER_UUID);
		mockProviderEntity(NODE_PROVIDER_UUID);
//		mockNodeProvider(NODE_PROVIDER_UUID, mockProviderEntity(NODE_PROVIDER_UUID));

		OrganizationEntity organizationEntity = mockOrganization(ORGANIZATION_UUID);
		LinkedProducer linkedProducerCreated = linkedProducerService.createLinkedProducer(ORGANIZATION_UUID);
		LinkedProducer linkedProducer = linkedProducerService.getLinkedProducer(linkedProducerCreated.getUuid());

		assertThat(linkedProducer)
				.as("Le resultat ne doit pas être null")
				.isNotNull()
				.as("L'organization concernée doit être lma bonne")
				.matches(p -> compareEntityAndDto(p.getOrganization(), organizationEntity));

		Task task = linkedProducerTaskService.createDraft(linkedProducer);

		linkedProducerTaskService.startTask(task);
	}

}
