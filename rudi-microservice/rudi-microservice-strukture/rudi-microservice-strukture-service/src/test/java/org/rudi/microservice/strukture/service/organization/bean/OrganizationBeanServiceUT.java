package org.rudi.microservice.strukture.service.organization.bean;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.acl.bean.Role;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.datafactory.UserDataFactory;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.kaccess.bean.MetadataFacet;
import org.rudi.facet.kaccess.bean.MetadataFacetValues;
import org.rudi.facet.kaccess.bean.MetadataListFacets;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.projekt.helper.ProjektHelper;
import org.rudi.microservice.projekt.core.bean.ProjectByOwner;
import org.rudi.microservice.strukture.core.bean.OrganizationBean;
import org.rudi.microservice.strukture.core.bean.OrganizationStatus;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationSearchCriteria;
import org.rudi.microservice.strukture.service.StruktureSpringBootTest;
import org.rudi.microservice.strukture.service.datafactory.organization.OrganizationDataFactory;
import org.rudi.microservice.strukture.service.datafactory.organizationmember.OrganizationMemberDataFactory;
import org.rudi.microservice.strukture.service.datafactory.provider.LinkedProducerDataFactory;
import org.rudi.microservice.strukture.service.helper.StruktureAuthorisationHelper;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationMembersHelper;
import org.rudi.microservice.strukture.service.organization.OrganizationService;
import org.rudi.microservice.strukture.service.organization.bean.impl.OrganizationBeanServiceImpl;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@StruktureSpringBootTest
class OrganizationBeanServiceUT {

	// nécessaire pour créer des organizations
	@Autowired
	private OrganizationService organizationService;
	@Autowired
	private OrganizationBeanService organizationBeanService;
	@Autowired
	private OrganizationDataFactory organizationDataFactory;
	@Autowired
	private OrganizationMemberDataFactory organizationMemberDataFactory;
	@Autowired
	private UserDataFactory userDataFactory;

	@MockitoBean
	private ACLHelper aclHelper;
	@MockitoBean
	private UtilContextHelper utilContextHelper;
	@MockitoBean
	private ProjektHelper projektHelper;
	@MockitoBean
	OrganizationMembersHelper organizationMembersHelper;
	@MockitoBean
	DatasetService datasetService;
	@MockitoBean
	StruktureAuthorisationHelper struktureAuthorisationHelper;

	User jean;
	User jacques;

	@Autowired
	private LinkedProducerDataFactory linkedProducerDataFactory;
	@Autowired
	private OrganizationBeanServiceImpl organizationBeanServiceImpl;

	@AfterEach
	void cleanData() {
		linkedProducerDataFactory.deleteAllLinkedProducer();
		organizationDataFactory.deleteAllOrganizationMembers();
		organizationDataFactory.deleteAllOrganizations();
	}

	/**
	 * Permet de créer un lot de 5 organizations pour permettre les tests. Avec des dates de créations et des noms différents pour vérifier les tris.
	 *
	 * @return List<Organization> : La liste des organizations créées pour le test
	 * @throws AppServiceBadRequestException thown si les process "CreateOrganizationProcessor ne passent pas
	 */
	private List<OrganizationEntity> createOrganizations() throws Exception {
		List<OrganizationEntity> organizations = new ArrayList<>();

		// Initialisation des users utilisés comme membre.
		jean = userDataFactory.getOrCreateJean();
		when(aclHelper.getUserByLogin(jean.getLogin())).thenReturn(jean); // Force la récupération du même user quand on repassera dans le get or create.
		jacques = userDataFactory.getOrCreateJacques();
		when(aclHelper.getUserByLogin(jacques.getLogin())).thenReturn(jacques); // Force la récupération du même user quand on repassera dans le get or create.

		OrganizationEntity irisa = organizationDataFactory.createIRISAOrganization(null);
		irisa = organizationMemberDataFactory.createOrganizationMemberJacquesAdministrator(irisa);
		irisa = organizationMemberDataFactory.createOrganizationMemberJeanEditor(irisa);
		organizations.add(irisa);

		OrganizationEntity open = organizationDataFactory.createOpenOrganization(null);
		open = organizationMemberDataFactory.createOrganizationMemberJacquesAdministrator(open);
		open = organizationMemberDataFactory.createOrganizationMemberJeanEditor(open);
		organizations.add(open);

		OrganizationEntity rm = organizationDataFactory.createRMOrganization(null);
		rm = organizationMemberDataFactory.createOrganizationMemberJacquesAdministrator(rm);
		rm = organizationMemberDataFactory.createOrganizationMemberJeanEditor(rm);
		organizations.add(rm);

		OrganizationEntity viaRoma = organizationDataFactory.createViaRomaOrganization(null);
		viaRoma = organizationMemberDataFactory.createOrganizationMemberJeanAdministrator(viaRoma);
		organizations.add(viaRoma);

		OrganizationEntity cookingPot = organizationDataFactory.createCookingPotOrganization(null);
		cookingPot = organizationMemberDataFactory.createOrganizationMemberJeanAdministrator(cookingPot);
		cookingPot = organizationMemberDataFactory.createOrganizationMemberJacquesEditor(cookingPot);
		organizations.add(cookingPot);

		OrganizationEntity block = organizationDataFactory.createBlockOrganization(null);
		block = organizationMemberDataFactory.createOrganizationMemberJeanAdministrator(block);
		block = organizationMemberDataFactory.createOrganizationMemberJacquesEditor(block);
		organizations.add(block);

		return organizations;
	}

	private void mockAuthenticatedUser(User user) throws AppServiceException {
		AuthenticatedUser authenticatedUser = new AuthenticatedUser();
		authenticatedUser.setLogin(user.getLogin());
		authenticatedUser.setFirstname(user.getFirstname());
		authenticatedUser.setLastname(user.getLastname());
		authenticatedUser.setRoles(user.getRoles().stream().map(Role::getCode).toList());

		when(aclHelper.getUserByLogin(user.getLogin())).thenReturn(user);
//		when(aclHelper.getAuthenticatedUser()).thenReturn(user);
		when(aclHelper.getAuthenticatedUserUuid()).thenReturn(user.getUuid());

//		when(utilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);
		when(organizationMembersHelper.getUserByLoginOrByUuid(user.getLogin(), user.getUuid())).thenReturn(user);
	}

	@Test
	@DisplayName("Vérifier que je récupère bien l'ensemble des organization quand je ne met aucun paramètre.")
	void testSearchAllOrganizations() throws Exception {
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().build();
		Pageable pageable = Pageable.unpaged();

		Page<OrganizationBean> initialOrganizations = organizationBeanService.searchOrganizationBeans(criteria,
				pageable);
		List<OrganizationEntity> organizations = createOrganizations();

		Page<OrganizationBean> organizationBeans = organizationBeanService.searchOrganizationBeans(criteria, pageable);
		assertThat(organizationBeans)
				.as("La liste ne doit pas être nullele, mais contenir les organizations créées au préalable.")
				.isNotEmpty().as("Les listes doivent être équivalentes.") // parcours la liste des beans et vérifie que pour chaque bean il y a une organization correspondante
				.allMatch(o -> organizations.stream()
						.anyMatch(organization -> organization.getUuid().equals(o.getUuid())))
				.allMatch(bean -> organizations.stream()
						.anyMatch(organization -> organization.getUuid().equals(bean.getUuid())
								|| initialOrganizations.getContent().contains(bean)));

	}

	@Test
	@DisplayName("Vérifie l'absence que je peux bien remonter les Organizations liées à un utilisateur")
	void testSearchMyOrganizations() throws Exception {
		List<OrganizationEntity> organizations = createOrganizations();
		List<String> jacquesOrgaNames = List.of("IRISA", "Open", "Rennes Métropole", "Block", "Cooking Pot"); // Car on ne tient pas compte ici du statut de l'organisation
		List<OrganizationEntity> expectedOrganizations = organizations.stream()
				.filter(o -> jacquesOrgaNames.contains(o.getName())).toList();

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().userUuid(jacques.getUuid()).build();
		Pageable pageable = Pageable.unpaged();

		Page<OrganizationBean> organizationBeans = organizationBeanService.searchOrganizationBeans(criteria, pageable);
		assertThat(organizationBeans)
				.as("La liste ne doit pas être nulle, mais contenir les organizations créées au préalable.")
				.isNotEmpty().as("La liste ne doit plus que contenir les 3 organizations : IRISA, Open, RM.") // parcours la liste des beans et vérifie que pour chaque bean il y a une organization correspondante
				.allMatch(bean -> expectedOrganizations.stream()
						.anyMatch(organization -> organization.getUuid().equals(bean.getUuid())));
	}

	@Test
	@DisplayName("Vérifie le tri par date ASC")
	void testSearchOrganizationsOrderByDateASC() throws Exception {
		List<OrganizationEntity> organizations = createOrganizations();
		List<OrganizationEntity> sortedOrganization = organizations.stream()
				.sorted(Comparator.comparing(OrganizationEntity::getOpeningDate)).toList();

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().build();
		Pageable pageable = PageRequest.of(0, 10, Sort.by("openingDate"));

		Page<OrganizationBean> organizationBeans = organizationBeanService.searchOrganizationBeans(criteria, pageable);
		assertThat(organizationBeans)
				.as("La liste ne doit pas être nulle, mais contenir les organizations créées au préalable.")
				.isNotEmpty().as("La liste doit toujours contenir le même nombre d'organisations.") // parcours la liste des beans et vérifie que pour chaque bean il y a une organization correspondante
				.allMatch(o -> organizations.stream()
						.anyMatch(organization -> organization.getUuid().equals(o.getUuid())));
		for (int i = 0; i < organizationBeans.getTotalElements(); i++) {
			OrganizationBean bean = organizationBeans.getContent().get(i);
			OrganizationEntity o = sortedOrganization.get(i);
			assertThat(bean.getUuid())
					.as(String.format("Les listes doivent être dans le même ordre :: %s == %s index=%d", bean.getName(),
							o.getName(), i))
					.isEqualTo(o.getUuid());
		}
	}

	@Test
	@DisplayName("Vérifie le tri par date DESC")
	void testSearchOrganizationsOrderByDateDESC() throws Exception {
		List<OrganizationEntity> organizations = createOrganizations();
		List<OrganizationEntity> sortedOrganization = organizations.stream()
				.sorted(Comparator.comparing(OrganizationEntity::getOpeningDate)).toList();

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().build();
		Pageable pageable = PageRequest.of(0, 10, Sort.by("openingDate").descending());

		Page<OrganizationBean> organizationBeans = organizationBeanService.searchOrganizationBeans(criteria, pageable);
		assertThat(organizationBeans)
				.as("La liste ne doit pas être nulle, mais contenir les organizations créées au préalable.")
				.isNotEmpty().as("La liste doit toujours contenir le même nombre d'organizations.") // parcours la liste des beans et vérifie que pour chaque bean il y a une organization correspondante
				.allMatch(o -> organizations.stream()
						.anyMatch(organization -> organization.getUuid().equals(o.getUuid())));
		for (int i = 0; i < organizationBeans.getTotalElements(); i++) {
			OrganizationBean bean = organizationBeans.getContent().get(i);
			OrganizationEntity o = sortedOrganization.get(sortedOrganization.size() - i - 1);
			assertThat(bean.getUuid())
					.as(String.format("Les listes doivent être dans le même ordre :: %s == %s index=%d", bean.getName(),
							o.getName(), i))
					.isEqualTo(o.getUuid());
		}
	}

	@Test
	@DisplayName("Vérifie le tri par nom d'organization ASC")
	void testSearchOrganizationsOrderByNameASC() throws Exception {
		List<OrganizationEntity> organizations = createOrganizations();
		List<OrganizationEntity> sortedOrganization = organizations.stream()
				.sorted(Comparator.comparing(OrganizationEntity::getName)).toList();

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().build();
		Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));

		Page<OrganizationBean> organizationBeans = organizationBeanService.searchOrganizationBeans(criteria, pageable);
		assertThat(organizationBeans)
				.as("La liste ne doit pas être nulle, mais contenir les organizations créées au préalable.")
				.isNotEmpty().as("La liste doit toujours contenir le même nombre d'organizations.") // parcours la liste des beans et vérifie que pour chaque bean il y a une organization correspondante
				.allMatch(o -> organizations.stream()
						.anyMatch(organization -> organization.getUuid().equals(o.getUuid())));
		for (int i = 0; i < organizationBeans.getTotalElements(); i++) {
			OrganizationBean bean = organizationBeans.getContent().get(i);
			OrganizationEntity o = sortedOrganization.get(i);
			assertThat(bean.getUuid())
					.as(String.format("Les listes doivent être dans le même ordre :: %s == %s index=%d", bean.getName(),
							o.getName(), i))
					.isEqualTo(o.getUuid());
		}
	}

	@Test
	@DisplayName("Vérifie le tri par nom d'organization DESC")
	void testSearchOrganizationsOrderByNameDESC() throws Exception {
		List<OrganizationEntity> organizations = createOrganizations();
		List<OrganizationEntity> sortedOrganization = organizations.stream()
				.sorted(Comparator.comparing(OrganizationEntity::getName)).toList();

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().build();
		Pageable pageable = PageRequest.of(0, 10, Sort.by("name").descending());

		Page<OrganizationBean> organizationBeans = organizationBeanService.searchOrganizationBeans(criteria, pageable);
		assertThat(organizationBeans)
				.as("La liste ne doit pas être nulle, mais contenir les organizations créées au préalable.")
				.isNotEmpty().as("La liste doit toujours contenir le même nombre d'organizations.") // parcours la liste des beans et vérifie que pour chaque bean il y a une organization correspondante
				.allMatch(o -> organizations.stream()
						.anyMatch(organization -> organization.getUuid().equals(o.getUuid())));
		for (int i = 0; i < organizationBeans.getTotalElements(); i++) {
			OrganizationBean bean = organizationBeans.getContent().get(i);
			OrganizationEntity o = sortedOrganization.get(sortedOrganization.size() - i - 1);
			assertThat(bean.getUuid())
					.as(String.format("Les listes doivent être dans le même ordre :: %s == %s index=%d", bean.getName(),
							o.getName(), i))
					.isEqualTo(o.getUuid());
		}
	}

	@Test
	@DisplayName("Search public organization")
	void testSearcPublicOrganizations() throws Exception {
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().build();
		Pageable pageable = PageRequest.of(0, 10);

		Page<OrganizationBean> initialOrganizationBeans = organizationBeanService
				.searchPublicOrganizationBeans(criteria, pageable);

		List<OrganizationEntity> organizations = createOrganizations();
		List<OrganizationEntity> filteredOrganization = organizations.stream()
				.filter(o -> o.getOrganizationStatus().equals(
						org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus.VALIDATED))
				.toList();

		Page<OrganizationBean> organizationBeans = organizationBeanService.searchPublicOrganizationBeans(criteria,
				pageable);

		assertThat(organizationBeans)
				.as("La liste ne doit pas être nulle, mais contenir les organizations créées au préalable.")
				.isNotEmpty().as("La liste doit toujours contenir le même nombre d'organizations.") // parcours la liste des beans et vérifie que pour chaque bean il y a une organization correspondante
				.allMatch(o -> filteredOrganization.stream()
						.anyMatch(organization -> organization.getUuid().equals(o.getUuid())));

		assertThat(organizationBeans.getTotalElements()).as(
						"La liste retournée doit contenir exactement le bon nombre d'organisations en plus qu'à l'état initial.")
				.isEqualTo(filteredOrganization.size() + initialOrganizationBeans.getTotalElements());

		// Création de liste avec uniquement les noms des organisations censées être retournées pour effectuer les tests.
		List<String> beanNames = organizationBeans.stream().map(OrganizationBean::getName).toList();
		List<String> filteredNames = filteredOrganization.stream().map(OrganizationEntity::getName).toList();

		assertThat(beanNames).as("Et ces élément supplémentaire doivent être ceux ajoutés lors du test.")
				.containsExactlyElementsOf(filteredNames);
	}

	@Test
	@DisplayName("Search public organization, statut forcé ne change rien")
	void testSearcPublicOrganizationsForcedStatus() throws Exception {
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().build();
		Pageable pageable = PageRequest.of(0, 10);

		Page<OrganizationBean> initialOrganizationBeans = organizationBeanService
				.searchPublicOrganizationBeans(criteria, pageable);

		List<OrganizationEntity> organizations = createOrganizations();
		List<OrganizationEntity> filteredOrganization = organizations.stream()
				.filter(o -> o.getOrganizationStatus().equals(
						org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus.VALIDATED))
				.toList();

		// On force une valeur à draft. Mais cette valeur doit être ignorée
		criteria = OrganizationSearchCriteria.builder().organizationStatus(List.of(OrganizationStatus.DRAFT)).build();
		Page<OrganizationBean> organizationBeans = organizationBeanService.searchPublicOrganizationBeans(criteria,
				pageable);

		assertThat(organizationBeans)
				.as("La liste ne doit pas être nulle, mais contenir les organizations créées au préalable.")
				.isNotEmpty().as("La liste doit toujours contenir le même nombre d'organizations.") // parcours la liste des beans et vérifie que pour chaque bean il y a une organization correspondante
				.allMatch(o -> filteredOrganization.stream()
						.anyMatch(organization -> organization.getUuid().equals(o.getUuid())));

		assertThat(organizationBeans.getTotalElements()).as(
						"La liste retournée doit contenir exactement le bon nombre d'organisations en plus qu'à l'état initial.")
				.isEqualTo(filteredOrganization.size() + initialOrganizationBeans.getTotalElements());

		// Création de liste avec uniquement les noms des organisations censées être retournées pour effectuer les tests.
		List<String> beanNames = organizationBeans.stream().map(OrganizationBean::getName).toList();
		List<String> filteredNames = filteredOrganization.stream().map(OrganizationEntity::getName).toList();

		assertThat(beanNames).as("Et ces élément supplémentaire doivent être ceux ajoutés lors du test.")
				.containsExactlyElementsOf(filteredNames);
	}

	@Test
	@DisplayName("search myOrganizationBeans - Jean")
	void searchJeansOrganizationBeans() throws Exception {
		User jean = userDataFactory.getOrCreateJean();

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().build();
		Pageable pageable = PageRequest.of(0, 10);

		mockAuthenticatedUser(jean);
		Page<OrganizationBean> initialState = organizationBeanService.searchMyOrganizationBeans(criteria, pageable);

		List<OrganizationEntity> organizations = createOrganizations();

		List<String> jeansOrgaNames = List.of("IRISA", "Open", "Rennes Métropole", "Via Roma", "Cooking Pot");
		List<OrganizationEntity> expectedOrganizations = organizations.stream()
				.filter(o -> jeansOrgaNames.contains(o.getName())).toList();

		Page<OrganizationBean> returnedOrganizations = organizationBeanService.searchMyOrganizationBeans(criteria,
				pageable);

		assertThat(returnedOrganizations).as("La liste reçue ne dois pas être vide").isNotEmpty()
				.as("La liste doit toujours contenir le même nombre d'organisations")
				.allMatch(o -> expectedOrganizations.stream()
						.anyMatch(organization -> organization.getUuid().equals(o.getUuid())));

		assertThat(returnedOrganizations.getTotalElements()).as(
						"La liste retournée doit contenir exactement le bon nombre d'organisations en plus qu'à l'état initial.")
				.isEqualTo(initialState.getTotalElements() + expectedOrganizations.size());

		// Création de liste avec uniquement les noms des organisations censées être retournées pour effectuer les tests.
		List<String> beanNames = returnedOrganizations.stream().map(OrganizationBean::getName).toList();
		List<String> filteredNames = expectedOrganizations.stream().map(OrganizationEntity::getName).toList();

		assertThat(beanNames).as("Et ces élément supplémentaire doivent être ceux ajoutés lors du test.")
				.containsExactlyElementsOf(filteredNames);
	}

	@Test
	@DisplayName("search myOrganizationBeans - Jacques")
	void searchJacquesOrganizationBeans() throws Exception {
		User jacques = userDataFactory.getOrCreateJacques();

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().build();
		Pageable pageable = PageRequest.of(0, 10);

		mockAuthenticatedUser(jacques);
		Page<OrganizationBean> initialState = organizationBeanService.searchMyOrganizationBeans(criteria, pageable);

		List<OrganizationEntity> organizations = createOrganizations();

		List<String> jacquesOrgaNames = List.of("IRISA", "Open", "Rennes Métropole");
		List<OrganizationEntity> expectedOrganizations = organizations.stream()
				.filter(o -> jacquesOrgaNames.contains(o.getName())).toList();

		Page<OrganizationBean> returnedOrganizations = organizationBeanService.searchMyOrganizationBeans(criteria,
				pageable);

		assertThat(returnedOrganizations).as("La liste reçue ne dois pas être vide").isNotEmpty()
				.as("La liste doit toujours contenir le même nombre d'organisations")
				.allMatch(o -> expectedOrganizations.stream()
						.anyMatch(organization -> organization.getUuid().equals(o.getUuid())));

		assertThat(returnedOrganizations.getTotalElements()).as(
						"La liste retournée doit contenir exactement le bon nombre d'organisations en plus qu'à l'état initial.")
				.isEqualTo(initialState.getTotalElements() + expectedOrganizations.size());

		// Création de liste avec uniquement les noms des organisations censées être retournées pour effectuer les tests.
		List<String> beanNames = returnedOrganizations.stream().map(OrganizationBean::getName).toList();
		List<String> filteredNames = expectedOrganizations.stream().map(OrganizationEntity::getName).toList();

		assertThat(beanNames).as("Et ces élément supplémentaire doivent être ceux ajoutés lors du test.")
				.containsExactlyElementsOf(filteredNames);
	}

	@Test
	@DisplayName("search public organisation - all data loaded")
	void searchPublicOrganizationBeansFull() throws Exception {
		List<OrganizationEntity> organizations = createOrganizations();
		List<OrganizationEntity> publicOrganizations = organizations.stream()
				.filter(o ->
						o.getStatus().equals(org.rudi.bpmn.core.bean.Status.COMPLETED) &&
								o.getOrganizationStatus().equals(org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus.VALIDATED)
				).toList();

		// On veut charger l'ensemble des données liées aux organisations
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder()
				.loadAllInformations(true)
				.build();
		Pageable pageable = Pageable.unpaged();

		// On crée les fausses donnéezs de sorte à ce que chaque organisation ait 5 projet
		List<ProjectByOwner> projectByOwners = publicOrganizations.stream().map(o -> {
			ProjectByOwner p = new ProjectByOwner();
			p.ownerUUID(o.getUuid());
			p.setProjectCount(5L);
			return p;
		}).toList();

		when(projektHelper.getNumberOfProjectsPerOwners(any()))
				.thenReturn(projectByOwners);

		// Et de sorte à ce que chaque organisation ait 10 jdd
		List<MetadataFacet> metadataFacets = publicOrganizations.stream().map(o -> {
			MetadataFacet f = new MetadataFacet();
			f.setPropertyName(OrganizationBeanServiceImpl.ORGANIZATION_UUID_FACET);
			MetadataFacetValues v = new MetadataFacetValues();
			v.setValue(o.getUuid().toString());
			v.setCount(10);
			f.setValues(List.of(v));
			return f;
		}).toList();

		MetadataListFacets metadataListFacets = new MetadataListFacets();
		metadataListFacets.setFacets(new org.rudi.facet.kaccess.bean.MetadataFacets());
		metadataListFacets.getFacets().setItems(metadataFacets);

		when(datasetService.searchDatasets(any(), any()))
				.thenReturn(metadataListFacets);
		Page<OrganizationBean> returnedOrganizations = organizationBeanService.searchPublicOrganizationBeans(criteria,
				pageable);

		assertThat(returnedOrganizations).as("La liste reçue ne dois pas être vide").isNotEmpty()
				.as("La liste doit toujours contenir le même nombre d'organisations") // parcours la liste des beans et vérifie que pour chaque bean il y a une organization correspondante
				.allMatch(o -> publicOrganizations.stream()
						.anyMatch(organization -> organization.getUuid().equals(o.getUuid())))
				.as("Chaque bean doit avoir le nombre de projet et de jdd renseigné correctement")
				.allMatch(bean -> publicOrganizations.stream()
						.filter(o -> o.getUuid().equals(bean.getUuid()))
						.allMatch(o -> bean.getProjectCount() == 5L && bean.getDatasetCount() == 10))
		;
	}

	@Test
	@DisplayName("search public organisation - only organization loaded")
	void searchPublicOrganizationBeansOnlyOrgaData() throws Exception {
		List<OrganizationEntity> organizations = createOrganizations();
		List<OrganizationEntity> publicOrganizations = organizations.stream()
				.filter(o ->
						o.getStatus().equals(org.rudi.bpmn.core.bean.Status.COMPLETED) &&
								o.getOrganizationStatus().equals(org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus.VALIDATED)
				).toList();

		// On ne veut pas toute la donnée, mais seulement les données de base de l'organisation
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder()
				.loadAllInformations(false)
				.build();
		Pageable pageable = Pageable.unpaged();

		// On crée les fausses donnéezs de sorte à ce que chaque organisation ait 5 projet
		List<ProjectByOwner> projectByOwners = publicOrganizations.stream().map(o -> {
			ProjectByOwner p = new ProjectByOwner();
			p.ownerUUID(o.getUuid());
			p.setProjectCount(5L);
			return p;
		}).toList();

		when(projektHelper.getNumberOfProjectsPerOwners(any()))
				.thenReturn(projectByOwners);

		// Et de sorte à ce que chaque organisation ait 10 jdd
		List<MetadataFacet> metadataFacets = publicOrganizations.stream().map(o -> {
			MetadataFacet f = new MetadataFacet();
			f.setPropertyName(OrganizationBeanServiceImpl.ORGANIZATION_UUID_FACET);
			MetadataFacetValues v = new MetadataFacetValues();
			v.setValue(o.getUuid().toString());
			v.setCount(10);
			f.setValues(List.of(v));
			return f;
		}).toList();

		MetadataListFacets metadataListFacets = new MetadataListFacets();
		metadataListFacets.setFacets(new org.rudi.facet.kaccess.bean.MetadataFacets());
		metadataListFacets.getFacets().setItems(metadataFacets);

		when(datasetService.searchDatasets(any(), any()))
				.thenReturn(metadataListFacets);
		Page<OrganizationBean> returnedOrganizations = organizationBeanService.searchPublicOrganizationBeans(criteria,
				pageable);

		assertThat(returnedOrganizations).as("La liste reçue ne dois pas être vide").isNotEmpty()
				.as("La liste doit toujours contenir le même nombre d'organisations") // parcours la liste des beans et vérifie que pour chaque bean il y a une organization correspondante
				.allMatch(o -> publicOrganizations.stream()
						.anyMatch(organization -> organization.getUuid().equals(o.getUuid())))
				.as("Chaque bean ne doit avoir aucune donnée de renseignée pour le nombre de JDD et le nombre de projets")
				.allMatch(bean -> publicOrganizations.stream()
						.filter(o -> o.getUuid().equals(bean.getUuid()))
						.allMatch(o -> bean.getProjectCount() == 0L && bean.getDatasetCount() == 0))
		;
	}

	@Test
	@DisplayName("search public organisation - only organization loaded - except 1")
	void searchPublicOrganizationBeansOnlyOrgaDataExcept1() throws Exception {
		List<OrganizationEntity> organizations = createOrganizations();

		List<OrganizationEntity> publicOrganizations = organizations.stream()
				.filter(o ->
						o.getStatus().equals(org.rudi.bpmn.core.bean.Status.COMPLETED) &&
								o.getOrganizationStatus().equals(org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus.VALIDATED)
				).toList();

		OrganizationEntity avoidedOrganization = publicOrganizations.getFirst();

		List<OrganizationEntity> expectedOrganizations = publicOrganizations.stream().filter(o -> !o.getUuid().equals(avoidedOrganization.getUuid())).toList();

		// On veut éviter d'avoir une organisation en particulier
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder()
				.loadAllInformations(false)
				.excludeOrganizationUuids(List.of(avoidedOrganization.getUuid()))
				.build();
		Pageable pageable = Pageable.unpaged();

		Page<OrganizationBean> returnedOrganizations = organizationBeanService.searchPublicOrganizationBeans(criteria,
				pageable);

		assertThat(returnedOrganizations).as("La liste reçue ne dois pas être vide").isNotEmpty()
				.as("L'organisation mise dans les excluded, ne doit pas être dans la liste de retour'")
				.noneMatch(o -> o.getUuid().equals(avoidedOrganization.getUuid()))
				.as("La liste doit toujours contenir le même nombre d'organisations") // parcours la liste des beans et vérifie que pour chaque bean il y a une organization correspondante
				.allMatch(o -> expectedOrganizations.stream()
						.anyMatch(organization -> organization.getUuid().equals(o.getUuid())))
		;
	}

	@Test
	@DisplayName("search public organisation - only organization loaded - target 3")
	void searchPublicOrganizationBeansOnlyOrgaDataTarget3() throws Exception {
		List<OrganizationEntity> organizations = createOrganizations();

		List<OrganizationEntity> publicOrganizations = organizations.stream()
				.filter(o ->
						o.getStatus().equals(org.rudi.bpmn.core.bean.Status.COMPLETED) &&
								o.getOrganizationStatus().equals(org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus.VALIDATED)
				).toList();

		OrganizationEntity avoidedOrganization = publicOrganizations.getFirst();

		List<OrganizationEntity> expectedOrganizations = publicOrganizations.stream().filter(o -> !o.getUuid().equals(avoidedOrganization.getUuid())).toList();

		// On veut éviter d'avoir une organisation en particulier
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder()
				.loadAllInformations(false)
				.uuids(expectedOrganizations.stream().map(OrganizationEntity::getUuid).toList())
				.build();
		Pageable pageable = Pageable.unpaged();

		Page<OrganizationBean> returnedOrganizations = organizationBeanService.searchPublicOrganizationBeans(criteria,
				pageable);

		assertThat(returnedOrganizations).as("La liste reçue ne dois pas être vide").isNotEmpty()
				.as("L'organisation mise dans les excluded, ne doit pas être dans la liste de retour'")
				.noneMatch(o -> o.getUuid().equals(avoidedOrganization.getUuid()))
				.as("La liste doit toujours contenir le même nombre d'organisations") // parcours la liste des beans et vérifie que pour chaque bean il y a une organization correspondante
				.allMatch(o -> expectedOrganizations.stream()
						.anyMatch(organization -> organization.getUuid().equals(o.getUuid())))
		;
	}

	@Test
	@DisplayName("search public organisation - filtre par nom exact")
	void searchPublicOrganizationBeansByExactName() throws Exception {
		List<OrganizationEntity> organizations = createOrganizations();

		List<OrganizationEntity> publicOrganizations = organizations.stream()
				.filter(o ->
						o.getStatus().equals(org.rudi.bpmn.core.bean.Status.COMPLETED) &&
								o.getOrganizationStatus().equals(org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus.VALIDATED)
				).toList();

		// On cible la première organisation publique par son nom exact
		OrganizationEntity targetOrganization = publicOrganizations.getFirst();

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder()
				.loadAllInformations(false)
				.name(targetOrganization.getName())
				.build();
		Pageable pageable = Pageable.unpaged();

		Page<OrganizationBean> returnedOrganizations = organizationBeanService.searchPublicOrganizationBeans(criteria,
				pageable);

		assertThat(returnedOrganizations)
				.as("La recherche par nom exact doit retourner exactement une organisation")
				.hasSize(1)
				.as("L'organisation retournée doit correspondre à celle recherchée")
				.allMatch(o -> o.getUuid().equals(targetOrganization.getUuid()));
	}

	@Test
	@DisplayName("search public organisation - filtre par nom avec wildcard (préfixe)")
	void searchPublicOrganizationBeansByWildcardName() throws Exception {
		List<OrganizationEntity> organizations = createOrganizations();

		List<OrganizationEntity> publicOrganizations = organizations.stream()
				.filter(o ->
						o.getStatus().equals(org.rudi.bpmn.core.bean.Status.COMPLETED) &&
								o.getOrganizationStatus().equals(org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus.VALIDATED)
				).toList();

		// On cible la première organisation publique via un préfixe de son nom suivi d'un wildcard
		OrganizationEntity targetOrganization = publicOrganizations.getFirst();
		String namePrefix = targetOrganization.getName().substring(0, 3); // ex : "IRI" pour "IRISA"

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder()
				.loadAllInformations(false)
				.name(namePrefix + "*")
				.build();
		Pageable pageable = Pageable.unpaged();

		Page<OrganizationBean> returnedOrganizations = organizationBeanService.searchPublicOrganizationBeans(criteria,
				pageable);

		assertThat(returnedOrganizations)
				.as("La recherche avec wildcard en suffixe doit retourner au moins une organisation")
				.isNotEmpty()
				.as("L'organisation ciblée doit faire partie des résultats")
				.anyMatch(o -> o.getUuid().equals(targetOrganization.getUuid()));
	}

	@Test
	@DisplayName("search public organisation - filtre par nom avec wildcard des deux côtés (*RI*)")
	void searchPublicOrganizationBeansByWildcardBothSides() throws Exception {
		List<OrganizationEntity> organizations = createOrganizations();

		List<OrganizationEntity> publicOrganizations = organizations.stream()
				.filter(o ->
						o.getStatus().equals(org.rudi.bpmn.core.bean.Status.COMPLETED) &&
								o.getOrganizationStatus().equals(org.rudi.microservice.strukture.storage.entity.organization.OrganizationStatus.VALIDATED)
				).toList();

		// On calcule les organisations dont le nom contient "RI" (insensible à la casse non testée ici)
		List<OrganizationEntity> expectedOrganizations = publicOrganizations.stream()
				.filter(o -> o.getName().contains("RI"))
				.toList();

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder()
				.loadAllInformations(false)
				.name("*RI*")
				.build();
		Pageable pageable = Pageable.unpaged();

		Page<OrganizationBean> returnedOrganizations = organizationBeanService.searchPublicOrganizationBeans(criteria,
				pageable);

		assertThat(returnedOrganizations)
				.as("La recherche avec *RI* doit retourner au moins une organisation")
				.isNotEmpty()
				.as("Seules les organisations dont le nom contient 'RI' doivent être retournées")
				.allMatch(o -> o.getName().contains("RI"))
				.as("Toutes les organisations publiques contenant 'RI' doivent être présentes")
				.hasSize(expectedOrganizations.size());
	}

	@Test
	@DisplayName("search public organisation - filtre par nom inexistant retourne vide")
	void searchPublicOrganizationBeansByUnknownName() throws Exception {
		createOrganizations();

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder()
				.loadAllInformations(false)
				.name("OrganisationQuiNExistePas")
				.build();
		Pageable pageable = Pageable.unpaged();

		Page<OrganizationBean> returnedOrganizations = organizationBeanService.searchPublicOrganizationBeans(criteria,
				pageable);

		assertThat(returnedOrganizations)
				.as("Une recherche par nom inexistant doit retourner une liste vide")
				.isEmpty();
	}
}
