package org.rudi.microservice.strukture.service.helper;

import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.core.security.UserType;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.acl.bean.Role;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.datafactory.UserDataFactory;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.microservice.strukture.core.bean.OwnerInfo;
import org.rudi.microservice.strukture.core.bean.OwnerType;
import org.rudi.microservice.strukture.service.StruktureSpringBootTest;
import org.rudi.microservice.strukture.service.datafactory.abstractaddress.EmailAddressRoleDataFactory;
import org.rudi.microservice.strukture.service.datafactory.organization.OrganizationDataFactory;
import org.rudi.microservice.strukture.service.datafactory.provider.ProviderDataFactory;
import org.rudi.microservice.strukture.storage.entity.address.AddressRoleEntity;
import org.rudi.microservice.strukture.storage.entity.address.AddressType;
import org.rudi.microservice.strukture.storage.entity.address.EmailAddressEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
@StruktureSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Transactional
class OwnerInfoHelperUT {

	private final OwnerInfoHelper ownerInfoHelper;
	private final ProviderDataFactory providerDataFactory;
	private final OrganizationDataFactory organizationDataFactory;
	private final UserDataFactory userDataFactory;
	private final EmailAddressRoleDataFactory emailAddressRoleDataFactory;

	@MockitoBean
	private ACLHelper aclHelper;
	@MockitoBean
	private UtilContextHelper utilContextHelper;

	private User mockAuthenticatedUserAsProvider(UUID nodeUuid) throws AppServiceUnauthorizedException {
		User user = userDataFactory.createUserNodeProvider(nodeUuid.toString());

		when(aclHelper.getAuthenticatedUser()).thenReturn(user);
		when(aclHelper.getUserByLogin(nodeUuid.toString())).thenReturn(user);
		AuthenticatedUser authenticatedUser = createAuthenticatedUserFromUser(user);
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);

		return user;
	}

	private User mockAuthenticatedUser(UUID nodeUuid) throws AppServiceUnauthorizedException {
		User user = userDataFactory.createUser(nodeUuid.toString());

		when(aclHelper.getAuthenticatedUser()).thenReturn(user);
		when(aclHelper.getUserByLogin(nodeUuid.toString())).thenReturn(user);
		AuthenticatedUser authenticatedUser = createAuthenticatedUserFromUser(user);
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);

		return user;
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

	@Test
	@DisplayName("récupération du mail de contact d'un provider à partir d'un LinkedProducer")
	void getOwnerInfoFromLinkedProducer() throws AppServiceUnauthorizedException {
		UUID nodeProviderUuid = UUID.randomUUID();
		User user = mockAuthenticatedUserAsProvider(nodeProviderUuid);

		ProviderEntity providerEntity = providerDataFactory.createProvider("PROVIDER", nodeProviderUuid);
		AddressRoleEntity addressRoleEntity = emailAddressRoleDataFactory.getOrCreate(ProviderDataFactory.ADDRESS_ROLE_CODE, ProviderDataFactory.ADDRESS_ROLE_CODE.toLowerCase());

		assertThat(providerEntity)
				.as("Le provider doit avoir une addresse mail de contact")
				.matches(p -> p.getAddresses().stream().anyMatch(a -> a.getAddressRole().equals(addressRoleEntity) && a.getType().equals(AddressType.EMAIL)))
				.as("Le providerEntity doit contenir un linkedProducer")
				.matches(p -> !p.getLinkedProducers().isEmpty());

		String email = providerEntity.getAddresses()
				.stream()
				.filter(a -> a.getAddressRole().equals(addressRoleEntity) && a.getType().equals(AddressType.EMAIL))
				.map(a -> ((EmailAddressEntity) a).getEmail())
				.findFirst()
				.orElse(providerEntity.getUuid().toString());
		LinkedProducerEntity linkedProducerEntity = providerEntity.getLinkedProducers().iterator().next();

		OwnerInfo ownerInfo = ownerInfoHelper.getAssetDescriptionOwnerInfo(linkedProducerEntity);

		assertThat(ownerInfo)
				.as("Le contact doit être celui dans le provider.")
				.matches(o -> o.getContact().equals(email))
				.as("Un mail est renseigné, on ne doit pas avoir l'uuid en tant que contact.")
				.matches(o -> !o.getContact().equals(providerEntity.getUuid().toString()))
				.as("Le type doit être organization")
				.matches(o -> OwnerType.ORGANIZATION.equals(o.getOwnerType()))
				.as("Le nom doit être le champ company du user")
				.matches(o -> o.getName().equals(user.getCompany()));
	}

	@Test
	@DisplayName("récupération du mail de contact d'un provider à partir d'une organization")
	void getOwnerInfoFromOrganization() throws AppServiceUnauthorizedException {
		UUID nodeProviderUuid = UUID.randomUUID();
		User user = mockAuthenticatedUserAsProvider(nodeProviderUuid);

		OrganizationEntity organizationEntity = organizationDataFactory.createTestOrganizationLinkedProducer(null);
		organizationEntity.setInitiator(user.getLogin());
		AddressRoleEntity addressRoleEntity = emailAddressRoleDataFactory.getOrCreate(ProviderDataFactory.ADDRESS_ROLE_CODE, ProviderDataFactory.ADDRESS_ROLE_CODE.toLowerCase());
		ProviderEntity providerEntity = providerDataFactory.createProvider("PROVIDER", nodeProviderUuid);

		String email = providerEntity.getAddresses()
				.stream()
				.filter(a -> a.getAddressRole().equals(addressRoleEntity) && a.getType().equals(AddressType.EMAIL))
				.map(a -> ((EmailAddressEntity) a).getEmail())
				.findFirst()
				.orElse(providerEntity.getUuid().toString());

		OwnerInfo ownerInfo = ownerInfoHelper.getAssetDescriptionOwnerInfo(organizationEntity);

		assertThat(ownerInfo)
				.as("Le contact doit être celui dans le provider.")
				.matches(o -> o.getContact().equals(email))
				.as("Un mail est renseigné, on ne doit pas avoir l'uuid en tant que contact.")
				.matches(o -> !o.getContact().equals(providerEntity.getUuid().toString()))
				.as("Le type doit être organization")
				.matches(o -> OwnerType.ORGANIZATION.equals(o.getOwnerType()))
				.as("Le nom doit être le champ company du user")
				.matches(o -> o.getName().equals(user.getCompany()));

	}

	@Test
	@DisplayName("récupération du mail de contact d'un utilisateur à partir d'une organization")
	void getOwnerInfoFromOrganizationUser() throws AppServiceUnauthorizedException {
		UUID userUuid = UUID.randomUUID();
		User user = mockAuthenticatedUser(userUuid);

		OrganizationEntity organizationEntity = organizationDataFactory.createLiksiOrganization();
		organizationEntity.setInitiator(user.getLogin());
		OwnerInfo ownerInfo = ownerInfoHelper.getAssetDescriptionOwnerInfo(organizationEntity);

		assertThat(ownerInfo)
				.as("Le contact doit être celui dans le provider.")
				.matches(o -> o.getContact().equals(userUuid.toString()))
				.as("Le type doit être User")
				.matches(o -> OwnerType.USER.equals(o.getOwnerType()))
				.as("Le nom doit être le prénom et le nom de l'utilisateur")
				.matches(o -> o.getName().equals(user.getFirstname() + " " + user.getLastname()));
	}
}
