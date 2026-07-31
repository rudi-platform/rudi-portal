package org.rudi.microservice.strukture.service.helper;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.common.core.security.RoleCodes;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.facet.acl.bean.Role;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.bean.UserType;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.service.StruktureSpringBootTest;
import org.rudi.microservice.strukture.service.datafactory.organization.OrganizationDataFactory;
import org.rudi.microservice.strukture.service.datafactory.provider.ProviderDataFactory;
import org.rudi.microservice.strukture.service.mapper.NodeProviderMapper;
import org.rudi.microservice.strukture.storage.dao.provider.ProviderDao;
import org.rudi.microservice.strukture.storage.entity.address.EmailAddressEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.provider.NodeProviderEntity;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@StruktureSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Transactional
class ProviderHelperUT {

	private final ProviderHelper providerHelper;
	private final ProviderDataFactory providerDataFactory;
	private final OrganizationDataFactory organizationDataFactory;
	private final ProviderDao providerDao;
	private final NodeProviderMapper nodeProviderMapper;

	@MockitoBean
	private ACLHelper aclHelper;

	@Test
	@DisplayName("getMyProvider : retourne le provider lié à l'utilisateur authentifié")
	void getMyProvider_returnsProviderLinkedToAuthenticatedUser() throws AppServiceUnauthorizedException {
		UUID nodeUuid = UUID.randomUUID();
		ProviderEntity expectedProvider = providerDataFactory.createProvider("MY_PROVIDER", nodeUuid);
		User user = createProviderUser(nodeUuid);
		when(aclHelper.getAuthenticatedUser()).thenReturn(user);

		ProviderEntity result = providerHelper.getMyProvider();

		assertThat(result.getUuid()).isEqualTo(expectedProvider.getUuid());
	}

	@Test
	@DisplayName("getMyNodeProvider : retourne le node provider de l'utilisateur authentifié")
	void getMyNodeProvider_returnsNodeProviderOfAuthenticatedUser() throws AppServiceUnauthorizedException {
		UUID nodeUuid = UUID.randomUUID();
		providerDataFactory.createProvider("NODE_PROVIDER", nodeUuid);
		User user = createProviderUser(nodeUuid);

		when(aclHelper.getAuthenticatedUser()).thenReturn(user);

		NodeProvider result = providerHelper.getMyNodeProvider();

		assertThat(result.getUuid()).isEqualTo(nodeUuid);
	}

	@Test
	@DisplayName("getProviderFromUser : erreur si l'utilisateur n'est pas un ROBOT provider")
	void getProviderFromUser_throwsUnauthorizedExceptionWhenUserIsNotProviderRobot() {
		User user = createUser(UUID.randomUUID(), UserType.PERSON, List.of(RoleCodes.PROVIDER));

		assertThatThrownBy(() -> providerHelper.getProviderFromUser(user))
				.isInstanceOf(AppServiceUnauthorizedException.class)
				.hasMessageContaining("n'est pas lié au provider");
	}

	@Test
	@DisplayName("getProviderFromUser : erreur si le role PROVIDER est absent")
	void getProviderFromUser_throwsUnauthorizedExceptionWhenProviderRoleMissing() {
		User user = createUser(UUID.randomUUID(), UserType.ROBOT, List.of("OTHER_ROLE"));

		assertThatThrownBy(() -> providerHelper.getProviderFromUser(user))
				.isInstanceOf(AppServiceUnauthorizedException.class)
				.hasMessageContaining("n'est pas lié au provider");
	}

	@Test
	@DisplayName("getProviderFromUser : erreur si le node provider est introuvable")
	void getProviderFromUser_throwsExceptionWhenNodeProviderIsMissing() {
		User user = createProviderUser(UUID.randomUUID());

		assertThatThrownBy(() -> providerHelper.getProviderFromUser(user))
				.isInstanceOf(InvalidParameterException.class)
				.hasMessage("Node introuvable");
	}

	@Test
	@DisplayName("getProviderFromNodeProvider : retourne le provider correspondant")
	void getProviderFromNodeProvider_returnsFirstMatchedProvider() {
		UUID nodeUuid = UUID.randomUUID();
		ProviderEntity expectedProvider = providerDataFactory.createProvider("PROVIDER_FROM_NODE", nodeUuid);
		NodeProvider nodeProvider = nodeProviderMapper.entityToDto(expectedProvider.getNodeProviders().iterator().next());

		ProviderEntity result = providerHelper.getProviderFromNodeProvider(nodeProvider);

		assertThat(result.getUuid()).isEqualTo(expectedProvider.getUuid());
	}

	@Test
	@DisplayName("getProviderFromNodeProvider : erreur quand aucun provider n'est trouvé")
	void getProviderFromNodeProvider_throwsExceptionWhenNoProviderFound() {
		NodeProvider nodeProvider = new NodeProvider();
		nodeProvider.setUuid(UUID.randomUUID());

		assertThatThrownBy(() -> providerHelper.getProviderFromNodeProvider(nodeProvider))
				.isInstanceOf(InvalidParameterException.class)
				.hasMessage("Provider introuvable");
	}

	@Test
	@DisplayName("getContactEmail(provider) : retourne l'email de contact")
	void getContactEmail_returnsEmailWhenContactAddressExists() {
		ProviderEntity providerEntity = providerDataFactory.createProvider("CONTACT_PROVIDER", UUID.randomUUID());
		String expectedEmail = providerEntity.getAddresses().stream()
				.filter(address -> ProviderDataFactory.ADDRESS_ROLE_CODE.equals(address.getAddressRole().getCode()))
				.map(address -> ((EmailAddressEntity) address).getEmail())
				.findFirst()
				.orElse(null);

		String result = providerHelper.getContactEmail(providerEntity);

		assertThat(result).isEqualTo(expectedEmail);
	}

	@Test
	@DisplayName("getContactEmail(provider) : retourne null si aucun contact")
	void getContactEmail_returnsNullWhenNoContactAddressMatches() {
		ProviderEntity providerEntity = providerDataFactory.createProviderWithoutAddress("NO_CONTACT", UUID.randomUUID());

		String result = providerHelper.getContactEmail(providerEntity);

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("getContactEmail(node) : résout le provider puis retourne l'email de contact")
	void getContactEmail_fromNodeProvider_resolvesProviderThenReturnsContactEmail() {
		UUID nodeUuid = UUID.randomUUID();
		ProviderEntity providerEntity = providerDataFactory.createProvider("CONTACT_FROM_NODE", nodeUuid);
		NodeProvider nodeProvider = nodeProviderMapper.entityToDto(providerEntity.getNodeProviders().iterator().next());
		String expectedEmail = providerEntity.getAddresses().stream()
				.filter(address -> ProviderDataFactory.ADDRESS_ROLE_CODE.equals(address.getAddressRole().getCode()))
				.map(address -> ((EmailAddressEntity) address).getEmail())
				.findFirst()
				.orElse(null);

		String result = providerHelper.getContactEmail(nodeProvider);

		assertThat(result).isEqualTo(expectedEmail);
	}

	@Test
	@DisplayName("searchAllOrganizationsProviders : retourne les providers de l'organisation")
	void searchAllOrganizationsProviders_returnsProvidersForOrganization() {
		UUID organizationUuid = UUID.randomUUID();
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(organizationUuid);
		ProviderEntity provider1 = providerDataFactory.createProvider("ORG_PROVIDER_1", UUID.randomUUID());
		ProviderEntity provider2 = providerDataFactory.createProvider("ORG_PROVIDER_2", UUID.randomUUID());
		attachProviderToOrganization(provider1, organization);
		attachProviderToOrganization(provider2, organization);

		List<ProviderEntity> result = providerHelper.searchAllOrganizationsProviders(organizationUuid, true);

		assertThat(result.stream().map(ProviderEntity::getUuid).collect(Collectors.toSet()))
				.contains(provider1.getUuid(), provider2.getUuid());
	}

	@Test
	@DisplayName("getOrganizationsNodeProviders : retourne les node providers de l'organisation")
	void getOrganizationsNodeProviders_returnsNodeProvidersForOrganization() {
		UUID organizationUuid = UUID.randomUUID();
		OrganizationEntity organization = organizationDataFactory.getOrCreateOrganization(organizationUuid);
		ProviderEntity provider1 = providerDataFactory.createProvider("ORG_NODE_PROVIDER_1", UUID.randomUUID());
		ProviderEntity provider2 = providerDataFactory.createProvider("ORG_NODE_PROVIDER_2", UUID.randomUUID());
		attachProviderToOrganization(provider1, organization);
		attachProviderToOrganization(provider2, organization);

		NodeProviderEntity nodeProviderEntity1 = provider1.getNodeProviders().iterator().next();
		NodeProviderEntity nodeProviderEntity2 = provider2.getNodeProviders().iterator().next();

		List<NodeProvider> result = providerHelper.getOrganizationsNodeProviders(organizationUuid);

		assertThat(result.stream().map(NodeProvider::getUuid).collect(Collectors.toSet()))
				.contains(nodeProviderEntity1.getUuid(), nodeProviderEntity2.getUuid());
	}

	private User createProviderUser(UUID nodeUuid) {
		return createUser(nodeUuid, UserType.ROBOT, List.of(RoleCodes.PROVIDER));
	}

	private User createUser(UUID userUuid, UserType type, List<String> roleCodes) {
		User user = new User();
		user.setUuid(UUID.randomUUID());
		user.setType(type);
		user.setLogin(userUuid.toString());
		user.setRoles(roleCodes.stream().map(roleCode -> {
			Role role = new Role();
			role.setCode(roleCode);
			return role;
		}).collect(Collectors.toList()));
		return user;
	}

	private void attachProviderToOrganization(ProviderEntity provider, OrganizationEntity organization) {
		provider.getLinkedProducers().forEach(linkedProducer -> linkedProducer.setOrganization(organization));
		providerDao.save(provider);
	}
}
