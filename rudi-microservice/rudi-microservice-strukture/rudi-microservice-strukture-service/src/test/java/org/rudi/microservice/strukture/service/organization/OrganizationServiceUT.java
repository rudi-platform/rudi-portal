package org.rudi.microservice.strukture.service.organization;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.bpmn.core.bean.Status;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.core.security.RoleCodes;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.acl.bean.Role;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.bean.UserType;
import org.rudi.facet.acl.datafactory.UserDataFactory;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.projekt.helper.ProjektHelper;
import org.rudi.microservice.strukture.core.bean.AbstractAddress;
import org.rudi.microservice.strukture.core.bean.EmailAddress;
import org.rudi.microservice.strukture.core.bean.Feature;
import org.rudi.microservice.strukture.core.bean.LinkedProducer;
import org.rudi.microservice.strukture.core.bean.NodeLinkedProducerStatus;
import org.rudi.microservice.strukture.core.bean.NodeOrganization;
import org.rudi.microservice.strukture.core.bean.NodeOrganizationStatus;
import org.rudi.microservice.strukture.core.bean.NodeProvider;
import org.rudi.microservice.strukture.core.bean.Organization;
import org.rudi.microservice.strukture.core.bean.OrganizationMember;
import org.rudi.microservice.strukture.core.bean.OrganizationRole;
import org.rudi.microservice.strukture.core.bean.OrganizationStatus;
import org.rudi.microservice.strukture.core.bean.Point;
import org.rudi.microservice.strukture.core.bean.TelephoneAddress;
import org.rudi.microservice.strukture.core.bean.WebsiteAddress;
import org.rudi.microservice.strukture.core.bean.criteria.NodeOrganizationSearchCriteria;
import org.rudi.microservice.strukture.core.bean.criteria.OrganizationSearchCriteria;
import org.rudi.microservice.strukture.service.StruktureSpringBootTest;
import org.rudi.microservice.strukture.service.datafactory.abstractaddress.AbstractAddressDataFactory;
import org.rudi.microservice.strukture.service.datafactory.abstractaddress.EmailAddressRoleDataFactory;
import org.rudi.microservice.strukture.service.datafactory.abstractaddress.TelephoneAddressRoleDataFactory;
import org.rudi.microservice.strukture.service.datafactory.abstractaddress.WebsiteAddressRoleDataFactory;
import org.rudi.microservice.strukture.service.datafactory.organization.OrganizationDataFactory;
import org.rudi.microservice.strukture.service.datafactory.organizationmember.OrganizationMemberDataFactory;
import org.rudi.microservice.strukture.service.datafactory.provider.LinkedProducerDataFactory;
import org.rudi.microservice.strukture.service.datafactory.provider.ProviderDataFactory;
import org.rudi.microservice.strukture.service.exception.CannotRemoveLastAdministratorException;
import org.rudi.microservice.strukture.service.helper.LinkedProducerHelper;
import org.rudi.microservice.strukture.service.helper.ProviderHelper;
import org.rudi.microservice.strukture.service.helper.organization.OrganizationMembersHelper;
import org.rudi.microservice.strukture.service.mapper.ProviderMapper;
import org.rudi.microservice.strukture.service.provider.ProviderService;
import org.rudi.microservice.strukture.storage.dao.address.AbstractAddressDao;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationCustomDao;
import org.rudi.microservice.strukture.storage.dao.organization.OrganizationDao;
import org.rudi.microservice.strukture.storage.dao.provider.LinkedProducerDao;
import org.rudi.microservice.strukture.storage.dao.provider.ProviderDao;
import org.rudi.microservice.strukture.storage.entity.address.EmailAddressEntity;
import org.rudi.microservice.strukture.storage.entity.address.TelephoneAddressEntity;
import org.rudi.microservice.strukture.storage.entity.address.WebsiteAddressEntity;
import org.rudi.microservice.strukture.storage.entity.organization.OrganizationEntity;
import org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerEntity;
import org.rudi.microservice.strukture.storage.entity.provider.ProviderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import lombok.val;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@StruktureSpringBootTest
class OrganizationServiceUT {

	public static final String LOGIN = "login@mail.fr";

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private OrganizationDao organizationDao;

	@Autowired
	private OrganizationCustomDao organizationCustomDao;

	@MockitoBean
	private ACLHelper aclHelper;

	@MockitoBean
	private ProjektHelper projektHelper;

	@MockitoBean
	private UtilContextHelper utilContextHelper;

	@MockitoBean
	OrganizationMembersHelper organizationMembersHelper;

	@MockitoBean
	DatasetService datasetService;

	@MockitoBean
	private ProviderHelper providerHelper;

	@MockitoBean
	private ProviderService providerService;

	@Autowired
	private OrganizationDataFactory organizationDataFactory;

	@Autowired
	private OrganizationMemberDataFactory organizationMemberDataFactory;

	@Autowired
	private UserDataFactory userDataFactory;

	@MockitoBean
	private ProviderMapper providerMapper;

	@Autowired
	private AbstractAddressDao abstractAddressDao;

	@Autowired
	private ProviderDao providerDao;

	@Autowired
	private LinkedProducerDao linkedProducerDao;

	@Autowired
	private LinkedProducerHelper linkedProducerHelper;

	@Autowired
	private LinkedProducerDataFactory linkedProducerDataFactory;

	@Autowired
	private ProviderDataFactory providerDataFactory;

	@Autowired
	private WebsiteAddressRoleDataFactory websiteAddressRoleDataFactory;


	@Autowired
	private TelephoneAddressRoleDataFactory telephoneAddressRoleDataFactory;

	@Autowired
	private EmailAddressRoleDataFactory emailAddressRoleDataFactory;
	@Autowired
	private AbstractAddressDataFactory abstractAddressDataFactory;

	@BeforeEach
	void init() {
		doNothing().when(projektHelper).notifyUserHasBeenAdded(any(), any());
		doNothing().when(projektHelper).notifyUserHasBeenRemoved(any(), any());
		// Required by ContactAddressesProcessor: merge/update/remove is keyed on role code CONTACT.
		emailAddressRoleDataFactory.getOrCreateContactRole();
		websiteAddressRoleDataFactory.getOrCreateContactRole();
		telephoneAddressRoleDataFactory.getOrCreateContactRole();
	}

	@AfterEach
	void tearDown() {
		linkedProducerDataFactory.deleteAllLinkedProducer();
		organizationDataFactory.deleteAllOrganizationMembers();
		organizationDataFactory.deleteAllOrganizations();
		providerDataFactory.deleteAllProviders();
	}

	private Organization createOrganizationDto() {
		Organization organization = new Organization();
		organization.setName("Fruity Loops");
		organization.setDescription("Une description OK");
		organization.setInitiator("initiator@mail.fr");

		return organization;
	}

	private void mockExternalCalls() throws AppServiceException {
		when(organizationMembersHelper.isAuthenticatedUserOrganizationAdministrator(any())).thenReturn(true);
		doNothing().when(projektHelper).notifyUserHasBeenAdded(any(), any());
	}

	private void mockAuthenticationData() throws AppServiceException {
		Role userRole = new Role();
		userRole.setCode(RoleCodes.USER);

		final List<Role> roles = List.of(userRole);
		AuthenticatedUser authenticatedUser = new AuthenticatedUser();
		authenticatedUser.setLogin(LOGIN);
		User user = new User().login(authenticatedUser.getLogin()).uuid(UUID.randomUUID());
		user.setRoles(roles);
		when(aclHelper.getUserByLogin(any())).thenReturn(user);
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);
		when(aclHelper.getAuthenticatedUser()).thenReturn(user);
		when(organizationMembersHelper.isAuthenticatedUserOrganizationMember(any())).thenReturn(true);
	}

	private User mockNodeAuthenticationData(String login) throws AppServiceException {
		Role userRole = new Role();
		userRole.setCode(RoleCodes.PROVIDER);

		final List<Role> roles = List.of(userRole);
		AuthenticatedUser authenticatedUser = new AuthenticatedUser();
		authenticatedUser.setLogin(login);
		User user = new User().login(authenticatedUser.getLogin()).uuid(UUID.randomUUID());
		user.setRoles(roles);
		user.setType(UserType.ROBOT);
		when(aclHelper.getUserByLogin(login)).thenReturn(user);
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);
		when(aclHelper.getAuthenticatedUser()).thenReturn(user);
		when(organizationMembersHelper.isAuthenticatedUserOrganizationMember(any())).thenReturn(true);
		return user;
	}

	void mockAuthenticationData(User user) throws AppServiceException {
		AuthenticatedUser authenticatedUser = new AuthenticatedUser();
		authenticatedUser.setLogin(user.getLogin());
		authenticatedUser.setFirstname(user.getFirstname());
		authenticatedUser.setLastname(user.getLastname());

		when(aclHelper.getUserByLogin(user.getLogin())).thenReturn(user);
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);
		when(aclHelper.getAuthenticatedUser()).thenReturn(user);
		when(aclHelper.getAuthenticatedUserUuid()).thenReturn(user.getUuid());

	}

	private void mockProviderData(ProviderEntity provider, User user) throws AppServiceException {

		when(providerHelper.getProviderFromUser(user)).thenReturn(provider);
		when(providerHelper.getMyProvider()).thenReturn(provider);
	}

	private ProviderEntity initProvider(String login) {
		ProviderEntity provider = new ProviderEntity();
		provider.setUuid(UUID.randomUUID());
		provider.setCode(login);
		provider.setLinkedProducers(new HashSet<LinkedProducerEntity>());
		LocalDateTime date = LocalDateTime.of(2022, Month.APRIL, 14, 23, 38, 12, 0);
		provider.setOpeningDate(date);
		return providerDao.save(provider);
	}

	private NodeProvider initNodeProvider() {
		NodeProvider nodeProvider = new NodeProvider();
		nodeProvider.setUuid(UUID.randomUUID());
		return nodeProvider;
	}

	private List<AbstractAddress> createTestAddresses() {
		List<AbstractAddress> addresses = new ArrayList<>();
		addresses.add(abstractAddressDataFactory.createWebsiteAddressDto(null));
		addresses.add(abstractAddressDataFactory.createEmailAddressDto(null));
		addresses.add(abstractAddressDataFactory.createTelephoneAddressDto(null));

		return addresses;
	}

	private Organization createTestOrganization() throws AppServiceBadRequestException {

		Organization organization = new Organization();
		organization.setName("Ableton Live");
		organization.setDescription("DAW très bien");
		organization.setAddresses(createTestAddresses());
		organization.setInitiator("initiator@mail.fr");

		LocalDateTime date = LocalDateTime.of(2022, Month.APRIL, 14, 23, 38, 12, 0);
		organization.setOpeningDate(date);

		LocalDateTime date2 = LocalDateTime.of(2025, Month.APRIL, 14, 23, 38, 12, 0);
		organization.setClosingDate(date2);

		return organizationService.createOrganization(organization);
	}

	private OrganizationMember createOrganizationMember(Organization organization, OrganizationRole role)
			throws Exception {
		val adminUuid = UUID.randomUUID();
		val adminOrgaMember = new OrganizationMember();
		adminOrgaMember.setUuid(organization.getUuid());
		adminOrgaMember.setUserUuid(adminUuid);
		adminOrgaMember.setRole(role);
		adminOrgaMember.setAddedDate(LocalDateTime.now());

		User user = new User().login("randomly").uuid(adminUuid);
		when(organizationMembersHelper.getUserByLoginOrByUuid(any(), eq(adminUuid))).thenReturn(user);

		return adminOrgaMember;
	}

	@Test
	@Transactional
	@DisplayName("Création d'une organization - champs minimums")
	void createOrganization() throws AppServiceException {
		Organization organization = new Organization();
		organization.setName("Ableton Live");
		organization.setDescription("DAW très bien");
		organization.setInitiator("initiator@mail.fr");
		organization.setAddresses(createTestAddresses());

		LocalDateTime date = LocalDateTime.of(2022, Month.APRIL, 14, 23, 38, 12, 0);
		organization.setOpeningDate(date);

		LocalDateTime date2 = LocalDateTime.of(2023, Month.APRIL, 14, 23, 38, 12, 0);
		organization.setClosingDate(date2);

		Organization created = organizationService.createOrganization(organization);

		assertNotNull(created);
		assertNotNull(created.getUuid());

		OrganizationEntity inDb = organizationDao.findByUuid(created.getUuid());

		assertThat(inDb).as("Le resultat ne doit pas être null").isNotNull()
				.as("Le nom en BDD doit correspondre au nom saisi")
				.matches(o -> o.getName().equals(organization.getName()))
				.as("L'opening date ne doit pas être celle saisie, mais celle du jour")
				.matches(o -> o.getOpeningDate() != organization.getOpeningDate()
						&& (o.getOpeningDate().truncatedTo(ChronoUnit.MINUTES))
						.equals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)))
				.as("La date de cloture doit être égale à celle saisie")
				.matches(o -> o.getClosingDate().equals(organization.getClosingDate()))
				.as("La description doit correspondre à celle saiaie")
				.matches(o -> o.getDescription().equals(organization.getDescription()))
				.as("Extraction des addresses")
				.extracting(OrganizationEntity::getAddresses)
				.as("La liste doit comporter 3 adresses")
				.matches(addresses -> addresses.size() == 3)
				.as("L'une doit être un site web et contenir le bon URL")
				.matches(addresses -> addresses.stream().anyMatch(a -> a instanceof WebsiteAddressEntity
						&& ((WebsiteAddressEntity) a).getUrl().equals(AbstractAddressDataFactory.WEBSITE_EXPECTED_VALUE)))
				.as("L'une doit être une adresse email et contenir le bon email")
				.matches(o -> o.stream().anyMatch(a -> a instanceof EmailAddressEntity
						&& ((EmailAddressEntity) a).getEmail().equals(AbstractAddressDataFactory.EMAIL_EXPECTED_VALUE)))
				.as("L'une doit être un numéro de téléphone et contenir le bon numéro")
				.matches(o -> o.stream().anyMatch(a -> a instanceof TelephoneAddressEntity
						&& ((TelephoneAddressEntity) a).getPhoneNumber().equals(AbstractAddressDataFactory.TELEPHONE_EXPECTED_VALUE)));


		organizationDao.delete(inDb);
	}

	@Test
	@DisplayName("Création d'une organization - teste sur le nom")
	void createOrganization_name_OK() throws AppServiceBadRequestException {

		Organization organization = new Organization();
		organization.setName("Nom OK");
		organization.setDescription("Une description OK");
		organization.setInitiator("initiator@mail.fr");
		organization.setOpeningDate(LocalDateTime.now());

		Organization created = organizationService.createOrganization(organization);
		assertNotNull(created);
	}

	@Test
	@DisplayName("Test d'une organization avec un nom trop long")
	void createOrganization_name_KO() {

		Organization organization = new Organization();
		organization.setName(
				"Nom trop long Nom trop long Nom trop long Nom trop long Nom trop long Nom trop long Nom trop long " +
						"Nom trop long Nom trop long Nom trop long "
						+ "Nom trop long Nom trop long Nom trop long Nom trop long Nom trop long");
		organization.setOpeningDate(LocalDateTime.now());

		assertThrows(AppServiceBadRequestException.class, () -> organizationService.createOrganization(organization));
	}

	@Test
	@DisplayName("Création organization - opening date uniquement sans end date")
	void createOrganization_openingDate_OK() throws AppServiceBadRequestException {

		Organization organization = new Organization();
		organization.setName("OpeningDate OK");
		organization.setDescription("Une description OK");
		organization.setInitiator("initiator@mail.fr");
		LocalDateTime openingDate = LocalDateTime.now().minus(3, ChronoUnit.MONTHS);
		organization.setOpeningDate(openingDate);

		Organization created = organizationService.createOrganization(organization);
		assertThat(created).as("Le résultat ne doit pas être null").isNotNull()
				.as("Et la date ne doit pas être celle renseignée, mais celle du jour")
				.matches(o -> !o.getOpeningDate().equals(openingDate)
						&& (o.getOpeningDate().truncatedTo(ChronoUnit.MINUTES))
						.equals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)));
	}

	@Test
	@DisplayName("Création d'une organization sans opening date - OK")
	void createOrganization_withoutOpeningDate_OK() throws AppServiceBadRequestException {

		Organization organization = new Organization();
		organization.setName("Opening date is missing");
		organization.setDescription("Une description OK");
		organization.setInitiator("initiator@mail.fr");

		Organization created = organizationService.createOrganization(organization);
		assertThat(created).as("Le résultat ne doit pas être null").isNotNull()
				.as("Et la date doit être égale à celle du jour")
				.matches(o -> (o.getOpeningDate().truncatedTo(ChronoUnit.MINUTES))
						.equals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)));
	}

	@Test
	@DisplayName("Création d'une organzation avec une description OK")
	void createOrganization_description_OK() throws AppServiceBadRequestException {

		Organization organization = new Organization();
		organization.setName("Description OK");
		organization.setDescription("Ceci est ma description, courte.");
		organization.setInitiator("initiator@mail.fr");
		organization.setOpeningDate(LocalDateTime.now());

		Organization created = organizationService.createOrganization(organization);
		assertNotNull(created);
	}

	@Test
	@DisplayName("Création d'une organzation avec une description trop longue")
	void createOrganization_description_KO() {

		Organization organization = new Organization();
		organization.setName("Description KO");
		organization
				.setDescription("Description trop longue de + de 800 caractères ah oui quand même au bout d'un moment"
						+ "j'ai envie de dire enfin voilà quoi après bon. S'il faut meubler autant s'entraîner à la dactilographie n'est-ce pas ? "
						+ "Car ce texte a été écrit à la main pour rendre les tests authentiques de toute manière la façon dont la phrase est tournée"
						+ "n'a que peu d'incidence sur le résultat fonctionnel du TU après tout ? nan je crois que ça se rapproche la ? peut-être pas assez"
						+ "je ne sais pas réellement. J'avoue je m'amuse un peu avec la musique dans les oreilles et tou AAAA quel dommage je devrais"
						+ "écouter du Tenwing ça au moins c'est du lourd, let's go Spotify et Deezer et tout quel artiste de fou et je dis pas ça"
						+ "parce que j'ai un intérêt derrière et tout hehehehehehe. Bon la je fais du padding pour être sûr quoicoubehhhh hein quoi ?"
						+ "hein ? Apagnan enfin ça s'écrit pas comme ça je crois");
		organization.setOpeningDate(LocalDateTime.now());

		assertThrows(AppServiceBadRequestException.class, () -> organizationService.createOrganization(organization));
	}

	@Test
	@DisplayName("Création d'une organzation sans description")
	void createOrganization_no_description_KO() {

		Organization organization = new Organization();
		organization.setName("Description KO");
		organization.setOpeningDate(LocalDateTime.now());

		assertThrows(AppServiceBadRequestException.class, () -> organizationService.createOrganization(organization));
	}

	@Test
	@DisplayName("Création d'une organization avec une URL valide")
	void createOrganization_url_OK() throws AppServiceBadRequestException {

		Organization organization = new Organization();
		organization.setName("Url OK");
		organization.setDescription("Une description OK");
		organization.setInitiator("initiator@mail.fr");
		organization.setAddresses(List.of(abstractAddressDataFactory.createWebsiteAddressDto(null)));
		organization.setOpeningDate(LocalDateTime.now());

		Organization created = organizationService.createOrganization(organization);
		assertNotNull(created);
	}

	@Test
	@DisplayName("Création d'une organization avec une URL trop longue")
	void createOrganization_url_KO() {

		Organization organization = new Organization();
		organization.setName("Url KO");
		// URL dépassant la limite de 1024 caractères

		organization.setAddresses(List.of(abstractAddressDataFactory.createWebsiteAddressDtoTooLong()));
		organization.setOpeningDate(LocalDateTime.now());

		assertThrows(AppServiceBadRequestException.class, () -> organizationService.createOrganization(organization));
	}

	@Test
	@DisplayName("Recherche d'organnization")
	void searchOrganization() throws AppServiceBadRequestException {
		Organization organization = createOrganizationDto();

		Organization created = organizationService.createOrganization(organization);
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().uuids(List.of(created.getUuid())).build();
		Page<Organization> organizations = organizationService.searchOrganizations(criteria, Pageable.unpaged());
		assertTrue(organizations.get().anyMatch(collected -> collected.getName().equals(organization.getName())));
		assertTrue(
				organizations.get().anyMatch(collected -> (collected.getOpeningDate().truncatedTo(ChronoUnit.MINUTES))
						.equals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))));
	}

	@Test
	@DisplayName("Update name d'une organization")
	void updateOrganization() throws AppServiceException {

		Organization organization = new Organization();
		organization.setName("Novation");
		organization.setDescription("Une description OK");
		organization.setInitiator("initiator@mail.fr");
		organization.setOpeningDate(LocalDateTime.now());

		Organization created = organizationService.createOrganization(organization);
		created.setName("Teenage Engineering");
		created.setOpeningDate(LocalDateTime.now());

		organizationService.updateOrganization(created);

		OrganizationEntity updated = organizationDao.findByUuid(created.getUuid());
		assertNotEquals(updated.getName(), organization.getName());
		assertNotEquals(updated.getOpeningDate(), organization.getOpeningDate());
	}

	@Test
	@DisplayName("Suppression d'une organization")
	void deleteOrganization() throws AppServiceException {

		long initialValue = organizationDataFactory.countAll();

		Organization organization = new Organization();
		organization.setName("Sloclap");
		organization.setDescription("Une description OK");
		organization.setInitiator("initiator@mail.fr");
		organization.setOpeningDate(LocalDateTime.now());

		Organization created = organizationService.createOrganization(organization);
		organizationService.deleteOrganization(created.getUuid());

		assertThat(organizationDao.findAll()).as("On doit avoir le même nombre d'organization qu'au début du test")
				.hasSize((int) initialValue);
	}

	@Test
	@DisplayName("Ajout d'un membre à une organisation - personalisation de la date d'ajout")
	void addOrganisationMember_added_date_custom_OK() throws Exception {
		mockAuthenticationData();
		mockExternalCalls();

		val newOrga = createTestOrganization();
		// Création du membre de l'organisation
		val member = createOrganizationMember(newOrga, OrganizationRole.ADMINISTRATOR);
		// Création de la date custom
		LocalDateTime adminAddedDate = LocalDateTime.of(2023, Month.JANUARY, 8, 13, 19, 16, 0);
		member.setAddedDate(adminAddedDate);
		organizationService.addOrganizationMember(newOrga.getUuid(), member);

		// Vérification que le user de l'organisation est bien créé et que sa date est bien set par défaut.
		val members = organizationService.getOrganizationMembers(newOrga.getUuid());
		assertThat(members).as("L'organisation a des membres").isNotEmpty();
		for (OrganizationMember aMember : members) {
			assertThat(aMember.getAddedDate()).as("La date ne doit pas être nulle").isNotNull();

			assertThat(aMember.getAddedDate().truncatedTo(ChronoUnit.HOURS))
					.as("La date d'ajout doit être égale à celle du jour à l'heure près.")
					.isEqualTo(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS));
		}
	}

	@Test
	@DisplayName("Faire evoluer le rôle d'un editeur à administrateur")
	void updateOrganizationMember_set_to_administrator_OK() throws Exception {
		mockAuthenticationData();
		mockExternalCalls();

		val savedOrganization = createTestOrganization();
		val organizationEditor = createOrganizationMember(savedOrganization, OrganizationRole.EDITOR);

		organizationService.addOrganizationMember(savedOrganization.getUuid(), organizationEditor);
		List<OrganizationMember> members = organizationService.getOrganizationMembers(savedOrganization.getUuid());
		assertThat(members).as("Il existe un membre au moins").isNotEmpty();

		OrganizationMember member = members.stream()
				.filter(organizationMember -> organizationMember.getUserUuid().equals(organizationEditor.getUserUuid()))
				.findFirst().orElse(null);

		assertThat(member.getUserUuid()).as("L'editeur fait bien partie de la liste des membres")
				.isEqualByComparingTo(organizationEditor.getUserUuid());

		// Update du rôle EDITOR => ADMIN
		organizationEditor.setRole(OrganizationRole.ADMINISTRATOR);
		val memberUpdated = organizationService.updateOrganizationMember(savedOrganization.getUuid(),
				organizationEditor.getUserUuid(), organizationEditor);

		assertThat(memberUpdated.getUserUuid()).as("C'est bien le membre visé qui a été modifié")
				.isEqualByComparingTo(organizationEditor.getUserUuid());

		assertThat(memberUpdated.getRole()).as("On est passé de EDITOR => ADMIN")
				.isEqualByComparingTo(OrganizationRole.ADMINISTRATOR);
	}

	@Test
	@DisplayName("Baisser les droits d'un admin à editeur à condition qu'il ne soit pas le dernier admin")
	void updateOrganizationMember_set_to_editor_OK() throws Exception {
		mockAuthenticationData();
		mockExternalCalls();

		val savedOrganization = createTestOrganization();
		val admin1 = createOrganizationMember(savedOrganization, OrganizationRole.ADMINISTRATOR);
		val admin2 = createOrganizationMember(savedOrganization, OrganizationRole.ADMINISTRATOR);

		organizationService.addOrganizationMember(savedOrganization.getUuid(), admin1);
		List<OrganizationMember> members = organizationService.getOrganizationMembers(savedOrganization.getUuid());
		assertThat(members).as("Il existe un membre au moins").isNotEmpty();

		OrganizationMember member = members.stream()
				.filter(organizationMember -> organizationMember.getUserUuid().equals(admin1.getUserUuid())).findFirst()
				.orElse(null);

		assertThat(member.getUserUuid()).as("L'editeur fait bien partie de la liste des membres")
				.isEqualByComparingTo(admin1.getUserUuid());

		// Update du rôle EDITOR => ADMIN
		admin1.setRole(OrganizationRole.EDITOR);
		// On tente de faire passer le dernier admin en EDITOR => Exception
		assertThrows(CannotRemoveLastAdministratorException.class, () -> organizationService
				.updateOrganizationMember(savedOrganization.getUuid(), admin1.getUserUuid(), admin1));

		// On ajoute un second admin puis retente de faire passer le 1 en EDITOR => OK
		organizationService.addOrganizationMember(savedOrganization.getUuid(), admin2);
		val memberUpdated = organizationService.updateOrganizationMember(savedOrganization.getUuid(),
				admin1.getUserUuid(), admin1);
		assertThat(memberUpdated.getUserUuid()).as("C'est bien le membre visé qui a été modifié")
				.isEqualByComparingTo(admin1.getUserUuid());

		assertThat(memberUpdated.getRole()).as("On est passé de EDITOR => ADMIN")
				.isEqualByComparingTo(OrganizationRole.EDITOR);
	}

	@Test
	@DisplayName("Supprimer un membre d'organisation, admin et editeur avec les conditions remplies")
	void removeOrganizationMember_OK() throws Exception {
		mockAuthenticationData();
		mockExternalCalls();

		val savedOrganization = createTestOrganization();
		val admin1 = createOrganizationMember(savedOrganization, OrganizationRole.ADMINISTRATOR);
		val admin2 = createOrganizationMember(savedOrganization, OrganizationRole.ADMINISTRATOR);
		val editor1 = createOrganizationMember(savedOrganization, OrganizationRole.EDITOR);
		organizationService.addOrganizationMember(savedOrganization.getUuid(), admin1);
		organizationService.addOrganizationMember(savedOrganization.getUuid(), admin2);
		organizationService.addOrganizationMember(savedOrganization.getUuid(), editor1);

		var oldMembers = organizationService.getOrganizationMembers(savedOrganization.getUuid());
		assertThat(oldMembers.size()).as("Il y a 3 membres : les 3 ajoutés").isEqualTo(3);

		// Suppression d'un ADMINISTRATOR
		organizationService.removeOrganizationMembers(savedOrganization.getUuid(), admin1.getUserUuid());
		var newMembers = organizationService.getOrganizationMembers(savedOrganization.getUuid());

		assertThat(newMembers.size()).as("On ne doit avoir supprimé qu'un seul membre")
				.isEqualTo(oldMembers.size() - 1);

		assertThat(newMembers.stream().map(OrganizationMember::getUserUuid).collect(Collectors.toList()))
				.as("La liste ne doit plus contenir le membre supprimé").doesNotContain(admin1.getUserUuid());

		// Suppression d'un EDITOR
		organizationService.removeOrganizationMembers(savedOrganization.getUuid(), editor1.getUserUuid());
		var membersAfterRemoveEditor = organizationService.getOrganizationMembers(savedOrganization.getUuid());

		assertThat(membersAfterRemoveEditor.stream().map(OrganizationMember::getUserUuid).collect(Collectors.toList()))
				.as("La liste ne doit plus contenir les membres supprimés (1 ADMIN et 1 EDITOR)")
				.doesNotContain(admin1.getUserUuid(), editor1.getUserUuid());
	}

	@Test
	@DisplayName("S'assurer qu'on ne peut pas supprimer le dernier admin")
	void removeOrganizationMember_last_admin_NOK() throws Exception {
		mockAuthenticationData();
		mockExternalCalls();

		val savedOrganization = createTestOrganization();
		val admin1 = createOrganizationMember(savedOrganization, OrganizationRole.ADMINISTRATOR);
		organizationService.addOrganizationMember(savedOrganization.getUuid(), admin1);

		var oldMembers = organizationService.getOrganizationMembers(savedOrganization.getUuid());
		assertThat(oldMembers.size()).as("Il y a 1 membre").isEqualTo(1);

		// Tentative de suppression de l'ADMINISTRATOR
		// On ne peut supprimer le dernier ADMIN
		assertThrows(CannotRemoveLastAdministratorException.class,
				() -> organizationService.removeOrganizationMembers(savedOrganization.getUuid(), admin1.getUserUuid()));

		var newMembers = organizationService.getOrganizationMembers(savedOrganization.getUuid());

		assertThat(newMembers.size()).as("L'admin n'a pas été supprimé et aucun autre membre ne l'a été")
				.isEqualTo(oldMembers.size());
	}

	@Test
	@DisplayName("Update organization member : check member infos - KO")
	void updateOrganizationMember_KO_when_updating_another_member_from_memberDtoInfo() throws AppServiceException {

		Organization organization = createTestOrganization();

		UUID userUuidModified = UUID.randomUUID();
		UUID userUuidFromAnotherUser = UUID.randomUUID();

		OrganizationMember updateDto = new OrganizationMember();
		updateDto.setUuid(organization.getUuid());
		updateDto.setUserUuid(userUuidModified);
		updateDto.setRole(OrganizationRole.ADMINISTRATOR);

		when(organizationMembersHelper.isAuthenticatedUserOrganizationAdministrator(any())).thenReturn(true);

		// J'essaye de MAJ les infos d'un autre membre (membre 1) alors que j'ai un DTO qui concerne quelqu'un d'autre (membre 2)
		assertThrows(AppServiceUnauthorizedException.class, () -> organizationService
				.updateOrganizationMember(organization.getUuid(), userUuidFromAnotherUser, updateDto));
	}

	@Test
	@DisplayName("Update organization member : check orga infos - KO")
	void updateOrganizationMember_KO_when_updating_the_member_to_another_organization() throws AppServiceException {

		Organization organization = createTestOrganization();
		Organization anotherOne = createTestOrganization();

		UUID userUuidModified = UUID.randomUUID();

		OrganizationMember updateDto = new OrganizationMember();
		updateDto.setUuid(organization.getUuid());
		updateDto.setUserUuid(userUuidModified);
		updateDto.setRole(OrganizationRole.ADMINISTRATOR);

		when(organizationMembersHelper.isAuthenticatedUserOrganizationAdministrator(any())).thenReturn(true);

		// J'essaye de MAJ les infos d'un membre d'une autre organisation ( orga 1) alors que j'ai un DTO qui concerne l'orga 2
		assertThrows(AppServiceUnauthorizedException.class,
				() -> organizationService.updateOrganizationMember(anotherOne.getUuid(), userUuidModified, updateDto));
	}

	@Test
	@DisplayName("Update organization member : check access rights - KO")
	void updateOrganizationMember_unauthorized_when_not_administrator_of_organization() throws AppServiceException {

		Organization organization = createTestOrganization();
		UUID userUuidModified = UUID.randomUUID();

		OrganizationMember updateDto = new OrganizationMember();
		updateDto.setUuid(organization.getUuid());
		updateDto.setUserUuid(userUuidModified);
		updateDto.setRole(OrganizationRole.ADMINISTRATOR);

		when(organizationMembersHelper.isAuthenticatedUserOrganizationAdministrator(any())).thenReturn(false);

		// Je suis pas administrateur de l'orga, j'ai pas le droit
		assertThrows(AppServiceUnauthorizedException.class, () -> organizationService
				.updateOrganizationMember(organization.getUuid(), userUuidModified, updateDto));
	}

	@Test
	@DisplayName("Delete organization member : check access rights - KO")
	void removeOrganizationMembers_does_not_delete_members_if_it_fails() throws AppServiceException {
		mockAuthenticationData();
		mockExternalCalls();

		Organization organization = createTestOrganization();
		UUID userRemovedUuid = UUID.randomUUID();

		var oldMembers = organizationService.getOrganizationMembers(organization.getUuid());

		when(organizationMembersHelper.isAuthenticatedUserOrganizationAdministrator(any())).thenReturn(false);
		assertThrows(AppServiceUnauthorizedException.class,
				() -> organizationService.removeOrganizationMembers(organization.getUuid(), userRemovedUuid));

		// On a changé le mock, donc on se "reconnecte" en tant qu'admin de l'organisation
		mockAuthenticationData();
		mockExternalCalls();

		var newMembers = organizationService.getOrganizationMembers(organization.getUuid());

		// On ne doit avoir supprimé aucun membre
		assertEquals(oldMembers, newMembers);
	}

	@Test
	@DisplayName("Création d'une organisation avec une position.")
	void mappingTest() throws AppServiceBadRequestException {
		Organization organization = new Organization();
		organization.setName("Ableton Live");
		organization.setDescription("DAW très bien");

		organization.setInitiator("initiator@mail.fr");
		organization.setAddress("some address");

		Feature feature = new Feature();
		Point point = new Point();

		List<BigDecimal> coordinates = List.of(BigDecimal.valueOf(48.132819955157245),
				BigDecimal.valueOf(-1.6390905740588901));
		point.setType("Point");
		point.setCoordinates(coordinates);

		feature.setType("Feature");
		feature.setGeometry(point);

		organization.setPosition(feature);

		LocalDateTime date = LocalDateTime.of(2022, Month.APRIL, 14, 23, 38, 12, 0);
		organization.setOpeningDate(date);

		LocalDateTime date2 = LocalDateTime.of(2025, Month.APRIL, 14, 23, 38, 12, 0);
		organization.setClosingDate(date2);

		Organization created = organizationService.createOrganization(organization);
		OrganizationEntity organizationEntity = organizationDao.findByUuid(created.getUuid());

		assertThat(organizationEntity).as("L'entité ne doit pas être null").isNotNull()
				.as("La position ne doit pas être null non plus.").matches(o -> o.getPosition() != null)
				.as("La position doit petre la même que celle renseignée")
				.matches(o -> o.getPosition().getCoordinates() != null);
	}

	@Test
	@DisplayName("Noeud - Recherche d'une organisation par nom")
	void searchNodeOrganizationByName() throws AppServiceException {

		// Création de deux organisations validées
		String organisation1Name = "Organisation 1";
		Organization orga1Created = initOrganisation(organisation1Name, OrganizationStatus.VALIDATED, Status.COMPLETED);

		String organisation2Name = "Organisation 2";
		initOrganisation(organisation2Name, OrganizationStatus.VALIDATED, Status.COMPLETED);

		// Création d'une organisation en brouillon
		String organisation3Name = "Organisation 3";
		initOrganisation(organisation3Name, OrganizationStatus.VALIDATED, Status.DRAFT);

		// Création d'un provider et d'un node provider
		ProviderEntity provider1 = initProvider("user-provider-1");
		NodeProvider nodeProvider1 = initNodeProvider();
		assertNotNull(provider1);
		assertNotNull(nodeProvider1);

		// Association de la première organisation au provider avec le statut VALIDATED
		attachWithStatus(orga1Created, provider1, nodeProvider1,
				org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerStatus.VALIDATED);

		// Recherche en tant que user-provider-1
		User u = mockNodeAuthenticationData("user-provider-1");
		mockProviderData(provider1, u);

		NodeOrganizationSearchCriteria criteriaNode = NodeOrganizationSearchCriteria.builder().name("*Organisation*")
				.build();

		Page<NodeOrganization> organizationsNode = organizationService.searchNodeOrganizations(criteriaNode,
				Pageable.unpaged());
		assertEquals(2, organizationsNode.getTotalElements());
		// Seules les organisations 1 et 2 doivent être retournées (orga 3 en brouillon)
		val no1 = organizationsNode.get().filter(collected -> collected.getOrganizationName().equals(organisation1Name))
				.findFirst().orElse(null);
		assertNotNull(no1);
		// seule l'organisation 1 est liée au provider avec le statut VALIDATED
		assertThat(no1.getOrganizationStatus().getValue()).withFailMessage("""
						nodeOrganization Status : %s
						expected OrganizationStatus: %s
						""", no1.getOrganizationStatus(), OrganizationStatus.VALIDATED)
				.isEqualTo(OrganizationStatus.VALIDATED.getValue());

		val no2 = organizationsNode.get().filter(collected -> collected.getOrganizationName().equals(organisation2Name))
				.findFirst().orElse(null);
		assertNotNull(no2);
		assertNull(no2.getLinkedProducerStatus());

	}

	@Test
	@DisplayName("Noeud - Recherche d'une organisation par uuid")
	void searchNodeOrganizationByUuid() throws AppServiceException {

		// Création de deux organisations validées
		String organisation1Name = "Organisation 1";
		Organization orga1Created = initOrganisation(organisation1Name, OrganizationStatus.VALIDATED, Status.COMPLETED);

		String organisation2Name = "Organisation 2";
		Organization orga2Created = initOrganisation(organisation2Name, OrganizationStatus.VALIDATED, Status.COMPLETED);

		// Création d'une organisation en brouillon
		String organisation3Name = "Organisation 3";
		Organization orga3Created = initOrganisation(organisation3Name, OrganizationStatus.VALIDATED, Status.DRAFT);

		// Création d'un provider et d'un node provider
		ProviderEntity provider1 = initProvider("user-provider-1");
		NodeProvider nodeProvider1 = initNodeProvider();
		assertNotNull(provider1);
		assertNotNull(nodeProvider1);

		// Association de la première organisation au provider avec le statut VALIDATED
		attachWithStatus(orga1Created, provider1, nodeProvider1,
				org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerStatus.VALIDATED);

		// Recherche en tant que user-provider-1
		User u = mockNodeAuthenticationData("user-provider-1");
		mockProviderData(provider1, u);

		// Recherche de chaque organisation par son uuid
		// la 1e remonte correctement avec son statut
		NodeOrganizationSearchCriteria criteriaNodeOrga1 = NodeOrganizationSearchCriteria.builder()
				.uuids(List.of(orga1Created.getUuid())).build();

		Page<NodeOrganization> organizationsNode1 = organizationService.searchNodeOrganizations(criteriaNodeOrga1,
				Pageable.unpaged());
		assertEquals(1, organizationsNode1.getTotalElements());
		val no1 = organizationsNode1.get()
				.filter(collected -> collected.getOrganizationName().equals(organisation1Name)).findFirst()
				.orElse(null);
		assertNotNull(no1);
		assertThat(no1.getOrganizationStatus().getValue()).withFailMessage("""
						nodeOrganization Status : %s
						expected OrganizationStatus: %s
						""", no1.getOrganizationStatus(), OrganizationStatus.VALIDATED)
				.isEqualTo(OrganizationStatus.VALIDATED.getValue());

		NodeOrganizationSearchCriteria criteriaNodeOrga2 = NodeOrganizationSearchCriteria.builder()
				.uuids(List.of(orga2Created.getUuid())).build();

		// la 2e remonte correctement sans statut
		Page<NodeOrganization> organizationsNode2 = organizationService.searchNodeOrganizations(criteriaNodeOrga2,
				Pageable.unpaged());
		assertEquals(1, organizationsNode2.getTotalElements());
		val no2 = organizationsNode2.get()
				.filter(collected -> collected.getOrganizationName().equals(organisation2Name)).findFirst()
				.orElse(null);
		assertNotNull(no2);

		// la 3e n'est pas retournée car en brouillon
		NodeOrganizationSearchCriteria criteriaNodeOrga3 = NodeOrganizationSearchCriteria.builder()
				.uuids(List.of(orga3Created.getUuid())).build();

		Page<NodeOrganization> organizationsNode3 = organizationService.searchNodeOrganizations(criteriaNodeOrga3,
				Pageable.unpaged());
		assertEquals(0, organizationsNode3.getTotalElements());
	}

	@Test
	@DisplayName("Noeud - Recherche d'une organisation cancelled")
	void searchNodeOrganizationCancelledByUuid() throws AppServiceException {

		// Création de deux organisations validées
		String organisation1Name = "Organisation 1";
		Organization orga1Created = initOrganisation(organisation1Name, OrganizationStatus.CANCELLED, Status.CANCELLED);

		// Création d'un provider et d'un node provider
		ProviderEntity provider1 = initProvider("user-provider-1");
		NodeProvider nodeProvider1 = initNodeProvider();
		assertNotNull(provider1);
		assertNotNull(nodeProvider1);

		// Association de la première organisation au provider avec le statut VALIDATED
		attachWithStatus(orga1Created, provider1, nodeProvider1,
				org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerStatus.VALIDATED);

		// Recherche en tant que user-provider-1
		User u = mockNodeAuthenticationData("user-provider-1");
		mockProviderData(provider1, u);

		// Recherche de chaque organisation par son uuid
		// la 1e remonte correctement avec son statut
		NodeOrganizationSearchCriteria criteriaNodeOrga1 = NodeOrganizationSearchCriteria.builder()
				.uuids(List.of(orga1Created.getUuid())).build();

		Page<NodeOrganization> organizationsNode1 = organizationService.searchNodeOrganizations(criteriaNodeOrga1,
				Pageable.unpaged());
		assertEquals(0, organizationsNode1.getTotalElements());
	}

	@Test
	@DisplayName("Noeud - Récupération d'une organisation par uuid")
	void getNodeOrganizationByUuid() throws AppServiceException {
		Organization organization = initOrganisation("Organisation getNode", OrganizationStatus.VALIDATED,
				Status.COMPLETED);
		ProviderEntity provider = initProvider("prov-get-node");
		when(providerHelper.getMyProvider()).thenReturn(provider);

		NodeOrganization nodeOrganization = organizationService.getNodeOrganization(organization.getUuid());

		assertNotNull(nodeOrganization);
		assertEquals(organization.getUuid(), nodeOrganization.getOrganizationId());
		assertEquals(organization.getName(), nodeOrganization.getOrganizationName());
	}

	@Test
	@DisplayName("Noeud - getNodeOrganization en erreur quand le provider n'existe pas")
	void getNodeOrganizationWhenNoProvider() throws AppServiceException {
		Organization organization = initOrganisation("Organisation sans provider", OrganizationStatus.VALIDATED,
				Status.COMPLETED);
		when(providerHelper.getMyProvider()).thenReturn(null);

		assertThrows(AppServiceBadRequestException.class,
				() -> organizationService.getNodeOrganization(organization.getUuid()));
	}

	@Test
	@DisplayName("Noeud - getNodeOrganization en erreur quand l'organisation n'existe pas")
	void getNodeOrganizationWhenOrganizationNotFound() throws AppServiceException {
		ProviderEntity provider = initProvider("prov-not-found");
		when(providerHelper.getMyProvider()).thenReturn(provider);

		assertThrows(AppServiceBadRequestException.class,
				() -> organizationService.getNodeOrganization(UUID.randomUUID()));
	}

	@Test
	@DisplayName("Noeud - getNodeOrganization by UUID - VALIDATED COMPLETED")
	void getNodeOrganizationValidatedCompleted() throws AppServiceException {
		ProviderEntity provider = providerDataFactory.createTestProvider();
		UUID nodeProviderUuid = provider.getNodeProviders().iterator().next().getUuid();
		OrganizationEntity rmLinked = organizationDataFactory.getOrCreateRMOrganization();
		LinkedProducerEntity linkedProducer = linkedProducerDataFactory.createValidatedLinkedProducer(provider.getUuid(),
				rmLinked.getUuid(),
				nodeProviderUuid);

		// Rajout du nouveau linkedProducer
		provider.getLinkedProducers().add(linkedProducer);
		providerDao.save(provider);

		when(providerHelper.getMyProvider()).thenReturn(provider);

		NodeOrganization foundLinked = organizationService.getNodeOrganization(rmLinked.getUuid());

		assertThat(foundLinked)
				.as("L'organisation ne doit pas être nulle").withFailMessage("""
						nodeOrganization : %s
						""", foundLinked).isNotNull()
				.as("L'uuid doit bien être celui de l'organisation concernée")
				.matches(o -> o.getOrganizationId().equals(rmLinked.getUuid()))
				.as("Le nom doit bien être celui de l'organisation concernée")
				.matches(o -> o.getOrganizationName().equals(rmLinked.getName()))
				.withFailMessage("""
						Le statut n'est pas correct :
						foundLinked NodeOrganizationStatus : %s
						expected NodeOrganizationStatus: %s
						""", foundLinked.getLinkedProducerStatus(), NodeOrganizationStatus.VALIDATED)
				.matches(o -> NodeOrganizationStatus.VALIDATED.equals(o.getOrganizationStatus()))
				.withFailMessage("""
						Le statut n'est pas correct :
						foundLinked NodeLinkedProducerStatus : %s
						expected NodeLinkedProducerStatusLinkedProducerStatus: %s
						""", foundLinked.getLinkedProducerStatus(), NodeLinkedProducerStatus.VALIDATED)
				.matches(o -> NodeLinkedProducerStatus.VALIDATED.equals(o.getLinkedProducerStatus()))
		;
	}

	@Test
	@DisplayName("Noeud - getNodeOrganization by UUID - Attach IN_PROGRESS")
	void getNodeOrganizationAttachInProgress() throws AppServiceException {
		ProviderEntity provider = providerDataFactory.createTestProvider();
		UUID nodeProviderUuid = provider.getNodeProviders().iterator().next().getUuid();
		OrganizationEntity rmLinked = organizationDataFactory.getOrCreateRMOrganization();
		LinkedProducerEntity linkedProducer = linkedProducerDataFactory.createAttachLinkedProducer(provider.getUuid(),
				rmLinked.getUuid(),
				nodeProviderUuid);

		// Rajout du nouveau linkedProducer
		provider.getLinkedProducers().add(linkedProducer);
		providerDao.save(provider);

		when(providerHelper.getMyProvider()).thenReturn(provider);

		NodeOrganization foundLinked = organizationService.getNodeOrganization(rmLinked.getUuid());

		assertThat(foundLinked)
				.as("L'organisation ne doit pas être nulle").withFailMessage("""
						nodeOrganization : %s
						""", foundLinked).isNotNull()
				.as("L'uuid doit bien être celui de l'organisation concernée")
				.matches(o -> o.getOrganizationId().equals(rmLinked.getUuid()))
				.as("Le nom doit bien être celui de l'organisation concernée")
				.matches(o -> o.getOrganizationName().equals(rmLinked.getName()))
				.withFailMessage("""
						Le statut n'est pas correct :
						foundLinked NodeOrganizzationStatus : %s
						expected OrganizationStatus: %s
						""", foundLinked.getLinkedProducerStatus(), NodeOrganizationStatus.VALIDATED)
				.matches(o -> NodeOrganizationStatus.VALIDATED.equals(o.getOrganizationStatus()))
				.withFailMessage("""
						Le statut n'est pas correct :
						foundLinked NodeLinkedProducerStatus : %s
						expected LinkedProducerStatus: %s
						""", foundLinked.getLinkedProducerStatus(), NodeLinkedProducerStatus.IN_PROGRESS)
				.matches(o -> NodeLinkedProducerStatus.IN_PROGRESS.equals(o.getLinkedProducerStatus()))
		;
	}

	@Test
	@DisplayName("Noeud - getNodeOrganization by UUID - Detach IN_PROGRESS")
	void getNodeOrganizationDetachInProgress() throws AppServiceException {
		ProviderEntity provider = providerDataFactory.createTestProvider();
		UUID nodeProviderUuid = provider.getNodeProviders().iterator().next().getUuid();
		OrganizationEntity rmLinked = organizationDataFactory.getOrCreateRMOrganization();
		LinkedProducerEntity linkedProducer = linkedProducerDataFactory.createDetachLinkedProducer(provider.getUuid(),
				rmLinked.getUuid(),
				nodeProviderUuid);

		// Rajout du nouveau linkedProducer
		provider.getLinkedProducers().add(linkedProducer);
		providerDao.save(provider);

		when(providerHelper.getMyProvider()).thenReturn(provider);

		NodeOrganization foundLinked = organizationService.getNodeOrganization(rmLinked.getUuid());

		assertThat(foundLinked)
				.as("L'organisation ne doit pas être nulle").withFailMessage("""
						nodeOrganization : %s
						""", foundLinked).isNotNull()
				.as("L'uuid doit bien être celui de l'organisation concernée")
				.matches(o -> o.getOrganizationId().equals(rmLinked.getUuid()))
				.as("Le nom doit bien être celui de l'organisation concernée")
				.matches(o -> o.getOrganizationName().equals(rmLinked.getName()))
				.withFailMessage("""
						Le statut n'est pas correct :
						foundLinked NodeOrganizzationStatus : %s
						expected OrganizationStatus: %s
						""", foundLinked.getLinkedProducerStatus(), NodeOrganizationStatus.VALIDATED)
				.matches(o -> NodeOrganizationStatus.VALIDATED.equals(o.getOrganizationStatus()))
				.withFailMessage("""
						Le statut n'est pas correct :
						foundLinked NodeLinkedProducerStatus : %s
						expected NodeLinkedProducerStatus: %s
						""", foundLinked.getLinkedProducerStatus(), NodeLinkedProducerStatus.DETACH_IN_PROGRESS)
				.matches(o -> NodeLinkedProducerStatus.DETACH_IN_PROGRESS.equals(o.getLinkedProducerStatus()))
		;
	}

	@Test
	@DisplayName("MyOrganization - Search organizations liées au user connecté")
	void searchMyOrganizations() throws AppServiceException {
		User jean = userDataFactory.getOrCreateJean();
		User jacques = userDataFactory.getOrCreateJacques();

		mockAuthenticationData(jean);
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().build();
		Pageable pageable = Pageable.unpaged();
		Page<Organization> originPage = organizationService.searchMyOrganizations(criteria, pageable);

		mockAuthenticationData(jacques);
		OrganizationEntity organization = organizationDataFactory.getOrCreateIRISAOrganization();
		organization = organizationMemberDataFactory.createOrganizationMemberJacquesAdministrator(organization);

		mockAuthenticationData(jean);
		// Ces deux là ont été créées avec l'utilisateur connecté en tant qu'initiator
		OrganizationEntity organization2 = organizationDataFactory.getOrCreateOpenOrganization();
		organization2 = organizationMemberDataFactory.createOrganizationMemberJeanAdministrator(organization2);

		OrganizationEntity organization3 = organizationDataFactory.getOrCreateRMOrganization();
		organization3 = organizationMemberDataFactory.createOrganizationMemberJeanAdministrator(organization3);

		List<OrganizationEntity> expectedOrganizations = List.of(organization2, organization3);

		Page<Organization> resultPage = organizationService.searchMyOrganizations(criteria, pageable);

		assertThat(resultPage.getTotalElements())
				.as("La liste des résultats retournés post création doit être plus longue que l'initiale")
				.isGreaterThan(originPage.getTotalElements())
				.as("La liste des résultat doit contenir exactement le même nombre de résultat supplémentaire que la list des résultat attendus.")
				.isEqualTo(originPage.getTotalElements() + expectedOrganizations.size());
		;
		assertThat(resultPage.getContent().stream().map(o -> o.getUuid()).toList())
				.as("Ne doit pas contenir l'organization non concernée").doesNotContain(organization.getUuid())
				.as("Mais doit contenir les deux autres")
				.containsAll(expectedOrganizations.stream().map(o -> o.getUuid()).toList());
		;
	}

	@Test
	@DisplayName("MyOrganization - Search organizations liées au user connecté. Force userUuid")
	void searchMyOrganizationsForceUserUuid() throws AppServiceException {
		User jean = userDataFactory.getOrCreateJean();
		User jacques = userDataFactory.getOrCreateJacques();

		mockAuthenticationData(jean);
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().build();
		Pageable pageable = Pageable.unpaged();
		Page<Organization> originPage = organizationService.searchMyOrganizations(criteria, pageable);

		mockAuthenticationData(jacques);
		OrganizationEntity organization = organizationDataFactory.getOrCreateIRISAOrganization();
		organization = organizationMemberDataFactory.createOrganizationMemberJacquesAdministrator(organization);

		mockAuthenticationData(jean);
		// Ces deux là ont été créées avec l'utilisateur connecté en tant qu'initiator
		OrganizationEntity organization2 = organizationDataFactory.getOrCreateOpenOrganization();
		organization2 = organizationMemberDataFactory.createOrganizationMemberJeanAdministrator(organization2);

		OrganizationEntity organization3 = organizationDataFactory.getOrCreateRMOrganization();
		organization3 = organizationMemberDataFactory.createOrganizationMemberJeanAdministrator(organization3);

		List<OrganizationEntity> expectedOrganizations = List.of(organization2, organization3);

		// On Force la veleur du user.
		criteria = OrganizationSearchCriteria.builder().userUuid(jacques.getUuid()).build();
		Page<Organization> resultPage = organizationService.searchMyOrganizations(criteria, pageable);

		assertThat(resultPage.getContent().stream().map(o -> o.getUuid()).toList())
				.as("Ne doit pas contenir l'organization dont jacques est membre, malgré le paramètre forcé")
				.doesNotContain(organization.getUuid()).as("Mais doit contenir les deux autres")
				.containsAll(expectedOrganizations.stream().map(o -> o.getUuid()).toList());
		;
	}

	@Test
	@DisplayName("MyOrganization - Search organizations liées au user connecté status forcé")
	void searchMyOrganizationsForcedStatus() throws AppServiceException {
		User jean = userDataFactory.getOrCreateJean();
		User jacques = userDataFactory.getOrCreateJacques();

		mockAuthenticationData(jean);
		OrganizationSearchCriteria criteria = OrganizationSearchCriteria.builder().build();
		Pageable pageable = Pageable.unpaged();
		Page<Organization> originPage = organizationService.searchMyOrganizations(criteria, pageable);

		mockAuthenticationData(jacques);
		OrganizationEntity organization = organizationDataFactory.getOrCreateIRISAOrganization();
		organization = organizationMemberDataFactory.createOrganizationMemberJacquesAdministrator(organization);

		mockAuthenticationData(jean);
		// Ces deux là ont été créées avec l'utilisateur connecté en tant qu'initiator
		OrganizationEntity organization2 = organizationDataFactory.getOrCreateOpenOrganization();
		organization2 = organizationMemberDataFactory.createOrganizationMemberJeanAdministrator(organization2);

		OrganizationEntity organization3 = organizationDataFactory.getOrCreateRMOrganization();
		organization3 = organizationMemberDataFactory.createOrganizationMemberJeanAdministrator(organization3);

		// Organization en Draft qui ne doit pas être remontée
		OrganizationEntity organization4 = organizationDataFactory.getOrCreateBlockOrganization();
		organization4 = organizationMemberDataFactory.createOrganizationMemberJeanAdministrator(organization4);

		List<OrganizationEntity> expectedOrganizations = List.of(organization2, organization3);

		OrganizationSearchCriteria.builder().organizationStatus(List.of(OrganizationStatus.DRAFT)).build();
		Page<Organization> resultPage = organizationService.searchMyOrganizations(criteria, pageable);

		assertThat(resultPage.getTotalElements())
				.as("La liste des résultats retournés post création doit être plus longue que l'initiale")
				.isGreaterThan(originPage.getTotalElements())
				.as("La liste des résultat doit contenir exactement le même nombre de résultat supplémentaire que la list des résultat attendus.")
				.isEqualTo(originPage.getTotalElements() + expectedOrganizations.size());
		;
		assertThat(resultPage.getContent().stream().map(o -> o.getUuid()).toList())
				.as("Ne doit pas contenir l'organization non concernée").doesNotContain(organization.getUuid())
				.as("Mais doit contenir les deux autres")
				.containsAll(expectedOrganizations.stream().map(o -> o.getUuid()).toList());
		;
	}


	// ----------------------------------------------------------------
	// Tests portant sur le stockage des coordonnées de contact
	// (url, email, phoneNumber) via AbstractAddressEntity
	// ----------------------------------------------------------------

	@Test
	@DisplayName("Création organisation - URL stockée comme WebsiteAddress")
	void createOrganization_url_stored_as_website_address() throws AppServiceException {
		// Utilise la DataFactory pour créer une organisation avec une URL persistée en BDD
		Organization created = organizationService.getOrganization(
				organizationDataFactory.getOrCreateMinimalOrganizationWithWebsite().getUuid(), true);
		assertThat(created.getAddresses())
				.as("La liste des adresses doit au moins contenir une adresse.")
				.isNotEmpty()
				.as("L'URL doit être remontée dans le DTO")
				.anyMatch(a -> a instanceof WebsiteAddress && ((WebsiteAddress) a).getUrl().equals(AbstractAddressDataFactory.WEBSITE_EXPECTED_VALUE));
	}

	@Test
	@DisplayName("Création organisation - l'email est stocké dans les adresses en EmailAddressEntity")
	void createOrganization_email_stored_as_email_address() throws AppServiceException {
		mockAuthenticationData();
		mockExternalCalls();
		// Crée une organisation avec email via la DataFactory et l'appelle via le service
		Organization dto = organizationDataFactory.getOrCreateMinimalOrganizationWithEmailDto();

		Organization created = organizationService.createOrganization(dto);

		// Vérifie que l'email est présent dans les adresses retournées
		assertThat(created.getAddresses())
				.as("La liste des adresses doit au moins contenir une adresse.")
				.isNotEmpty()
				.as("L'email doit être remonté dans le DTO")
				.anyMatch(a -> a instanceof EmailAddress && ((EmailAddress) a).getEmail().equals(AbstractAddressDataFactory.EMAIL_EXPECTED_VALUE));

	}

	@Test
	@DisplayName("Création organisation - le téléphone est stocké dans les adresses en TelephoneAddressEntity")
	void createOrganization_phoneNumber_stored_as_telephone_address() throws AppServiceException {
		mockAuthenticationData();
		mockExternalCalls();
		// Crée une organisation avec téléphone via la DataFactory et l'appelle via le service
		Organization dto = organizationDataFactory.getOrCreateMinimalOrganizationWithPhoneDto();

		Organization created = organizationService.createOrganization(dto);
		// Vérifie que le téléphone est présent dans les adresses retournées
		assertThat(created.getAddresses())
				.as("La liste des adresses doit au moins contenir une adresse.")
				.isNotEmpty()
				.as("Le téléphone doit être remonté dans le DTO")
				.anyMatch(a -> a instanceof TelephoneAddress && ((TelephoneAddress) a).getPhoneNumber().equals(AbstractAddressDataFactory.TELEPHONE_EXPECTED_VALUE));
	}

	@Test
	@DisplayName("Création organisation - roundtrip url/email/phone : les 3 champs sont retournés dans le DTO")
	void createOrganization_contact_fields_roundtrip() throws AppServiceException {
		mockAuthenticationData();
		mockExternalCalls();
		// Crée une organisation avec les 3 adresses (URL, email, téléphone) via la DataFactory
		Organization dto = organizationDataFactory.getOrCreateOrganizationWithAllAddressesDtoDto();

		Organization created = organizationService.createOrganization(dto);

		// Vérifie que chacune des 3 adresses est bien remontée dans le DTO sans confusion
		assertThat(created.getAddresses())
				.as("La liste des adresses doit au moins contenir une adresse.")
				.isNotEmpty()
				.anyMatch(a -> a instanceof WebsiteAddress && ((WebsiteAddress) a).getUrl().equals(AbstractAddressDataFactory.WEBSITE_EXPECTED_VALUE))
				.anyMatch(a -> a instanceof EmailAddress && ((EmailAddress) a).getEmail().equals(AbstractAddressDataFactory.EMAIL_EXPECTED_VALUE))
				.anyMatch(a -> a instanceof TelephoneAddress && ((TelephoneAddress) a).getPhoneNumber().equals(AbstractAddressDataFactory.TELEPHONE_EXPECTED_VALUE));
	}

	@Test
	@DisplayName("Get organization - mode light : URL/email/téléphone ne sont pas exposés")
	void getOrganization_light_mode_without_contact_fields() throws AppServiceException {
		// Prépare les roles pour les adresses de contact
		websiteAddressRoleDataFactory.getOrCreateContactRole();
		emailAddressRoleDataFactory.getOrCreateContactRole();
		telephoneAddressRoleDataFactory.getOrCreateContactRole();

		// Crée une organisation avec les 3 adresses (URL, email, téléphone)
		OrganizationEntity organizationEntity = organizationDataFactory.getOrCreateMinimalOrganizationWithAllAddresses();
		// Appelle le service en mode LIGHT (full=false)
		Organization found = organizationService.getOrganization(organizationEntity.getUuid(), false);

		// Vérifie qu'en mode light, les adresses de contact ne sont pas chargées/retournées
		assertThat(found.getAddresses())
				.as("""
						On ne charge pas les adresses de contact en mode light
						%s
						""", found.getAddresses() == null ? "found.getAddresses() == null" : found.getAddresses().toString())
				.isNullOrEmpty();
	}


	@Test
	@DisplayName("Get organization - mode full : URL/email/téléphone sont exposés")
	void getOrganization_full_mode_with_contact_fields() throws AppServiceException {
		// Crée une organisation avec les 3 adresses (URL, email, téléphone)
		Organization found = organizationService.getOrganization(
				organizationDataFactory.getOrCreateMinimalOrganizationWithAllAddresses().getUuid(), true);
		// Appelle le service en mode FULL (full=true) et vérifie que toutes les adresses sont retournées
		assertThat(found.getAddresses())
				.as("La liste des adresses doit au moins contenir une adresse.")
				.isNotEmpty()
				.anyMatch(a -> a instanceof WebsiteAddress && ((WebsiteAddress) a).getUrl().equals(AbstractAddressDataFactory.WEBSITE_EXPECTED_VALUE))
				.anyMatch(a -> a instanceof EmailAddress && ((EmailAddress) a).getEmail().equals(AbstractAddressDataFactory.EMAIL_EXPECTED_VALUE))
				.anyMatch(a -> a instanceof TelephoneAddress && ((TelephoneAddress) a).getPhoneNumber().equals(AbstractAddressDataFactory.TELEPHONE_EXPECTED_VALUE));

	}

	@Test
	@DisplayName("Update organisation - l'URL est mise à jour (pas de doublon dans les adresses)")
	@Transactional
	void updateOrganization_url_updated_not_duplicated() throws AppServiceException {
		final String oldUrl = AbstractAddressDataFactory.WEBSITE_EXPECTED_VALUE;
		final String newUrl = "https://new-url.fr/";

		OrganizationEntity createdEntity = organizationDataFactory.getOrCreateMinimalOrganizationWithWebsite();
		Organization created = organizationService.getOrganization(createdEntity.getUuid(), true);

		assertThat(created.getAddresses())
				.as("L'organisation doit contenir au moins une addresse")
				.isNotEmpty()
				.as("La liste doit contenir le site web renseigné")
				.anyMatch(a -> a instanceof WebsiteAddress && ((WebsiteAddress) a).getUrl().equals(oldUrl));

		created.getAddresses().stream().filter(a -> a instanceof WebsiteAddress).findFirst().ifPresent(a -> ((WebsiteAddress) a).setUrl(newUrl));
		assertThat(created.getAddresses())
				.as("Le nouvel URL est set")
				.anyMatch(a -> a instanceof WebsiteAddress && ((WebsiteAddress) a).getUrl().equals(newUrl))
				.as("L'ancien url n'est plus présent")
				.noneMatch(a -> a instanceof WebsiteAddress && ((WebsiteAddress) a).getUrl().equals(oldUrl));
		organizationService.updateOrganization(created);

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria
				.builder()
				.uuids(List.of(created.getUuid()))
				.full(true)
				.build();

		Page<OrganizationEntity> organizations = organizationCustomDao.searchOrganizations(criteria, Pageable.unpaged());
		assertThat(organizations.getContent())
				.as("L'organisation doit exister")
				.isNotEmpty()
				.as("La liste ne doit contenir qu'une seule organization avec cet UUID")
				.hasSize(1);

		assertThat(organizations.getContent().getFirst().getAddresses())
				.as("Il ne doit y avoir qu'une seule WebsiteAddressEntity (pas de doublon)")
				.filteredOn(WebsiteAddressEntity.class::isInstance)
				.hasSize(1)
				.map(WebsiteAddressEntity.class::cast)
				.as("L'ancienne URL ne doit plus être présente en base")
				.noneMatch(a -> a.getUrl().equals(oldUrl))
				.as("La nouvelle URL doit être présente en base")
				.anyMatch(a -> newUrl.equals(a.getUrl()));
	}

	@Test
	@DisplayName("Update organisation - l'URL mise à null supprime la WebsiteAddressEntity")
	@Transactional
	void updateOrganization_url_removed_when_null() throws AppServiceException {
		OrganizationEntity createdEntity = organizationDataFactory.getOrCreateMinimalOrganizationWithWebsite();
		Organization created = organizationService.getOrganization(createdEntity.getUuid(), true);
		assertThat(created.getAddresses()).as("Il y a au moins une addresse de renseignée").isNotEmpty()
				.as("Cette addresse doit être de type WebsiteAddress et valoir " + AbstractAddressDataFactory.WEBSITE_EXPECTED_VALUE)
				.anyMatch(a -> a instanceof WebsiteAddress && ((WebsiteAddress) a).getUrl().equals(AbstractAddressDataFactory.WEBSITE_EXPECTED_VALUE));

		created.setAddresses(null);
		organizationService.updateOrganization(created);

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria
				.builder()
				.uuids(List.of(created.getUuid()))
				.full(true)
				.build();

		Page<OrganizationEntity> organizations = organizationCustomDao.searchOrganizations(criteria, Pageable.unpaged());
		assertThat(organizations.getContent())
				.as("L'organisation doit exister")
				.isNotEmpty()
				.as("La liste ne doit contenir qu'une seule organization avec cet UUID")
				.hasSize(1);

		assertThat(organizations.getContent().getFirst().getAddresses())
				.as("Il ne doit y avoir aucune WebsiteAddressEntity")
				.isEmpty();
	}

	@Test
	@DisplayName("Update organisation - l'email est mis à jour (pas de doublon dans les adresses)")
	@Transactional
	void updateOrganization_email_updated_not_duplicated() throws AppServiceException {
		final String oldEmail = AbstractAddressDataFactory.EMAIL_EXPECTED_VALUE;
		final String newEmail = "new@contact.fr";

		OrganizationEntity createdEntity = organizationDataFactory.getOrCreateMinimalOrganizationWithEmail();
		Organization created = organizationService.getOrganization(createdEntity.getUuid(), true);
		assertThat(created.getAddresses())
				.as("La liste des adresses doit au moins contenir une adresse.")
				.isNotEmpty()
				.as("""
							Et cette addresse doit être de type EmailAddress et valoir
							%s
							""",
						oldEmail)
				.anyMatch(a -> a instanceof EmailAddress && ((EmailAddress) a).getEmail().equals(oldEmail));

		// Changement de l'email
		created.getAddresses().stream().filter(a -> a instanceof EmailAddress).findFirst().ifPresent(a -> ((EmailAddress) a).setEmail(newEmail));
		assertThat(created.getAddresses())
				.as("Le nouvel email est set")
				.anyMatch(a -> a instanceof EmailAddress && ((EmailAddress) a).getEmail().equals(newEmail))
				.as("L'ancien email n'est plus présent")
				.noneMatch(a -> a instanceof EmailAddress && ((EmailAddress) a).getEmail().equals(oldEmail));

		organizationService.updateOrganization(created);

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria
				.builder()
				.uuids(List.of(created.getUuid()))
				.full(true)
				.build();

		Page<OrganizationEntity> organizations = organizationCustomDao.searchOrganizations(criteria, Pageable.unpaged());
		assertThat(organizations.getContent())
				.as("L'organisation doit exister")
				.isNotEmpty()
				.as("La liste ne doit contenir qu'une seule organization avec cet UUID")
				.hasSize(1);

		assertThat(organizations.getContent().getFirst().getAddresses())
				.as("La liste des adresses doit au moins contenir une adresse.")
				.isNotEmpty()
				.as("""
						Et cette addresse doit être de type EmailAddress et valoir
						%s
						""", newEmail)
				.anyMatch(a -> a instanceof EmailAddressEntity && ((EmailAddressEntity) a).getEmail().equals(newEmail)
				)
				.as("""
						Il ne doit plus y l'ancienne addresse
						""")
				.noneMatch(a -> a instanceof EmailAddressEntity && ((EmailAddressEntity) a).getEmail().equals(oldEmail));
	}

	@Test
	@DisplayName("Update organisation - l'email mis à null supprime la EmailAddressEntity")
	@Transactional
	void updateOrganization_email_removed_when_null() throws AppServiceException {
		OrganizationEntity createdEntity = organizationDataFactory.getOrCreateMinimalOrganizationWithEmail();
		Organization created = organizationService.getOrganization(createdEntity.getUuid(), true);

		created.setAddresses(null);
		organizationService.updateOrganization(created);

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria
				.builder()
				.uuids(List.of(created.getUuid()))
				.full(true)
				.build();

		Page<OrganizationEntity> organizations = organizationCustomDao.searchOrganizations(criteria, Pageable.unpaged());
		assertThat(organizations.getContent())
				.as("L'organisation doit exister")
				.isNotEmpty()
				.as("La liste ne doit contenir qu'une seule organization avec cet UUID")
				.hasSize(1);
		assertThat(organizations.getContent().getFirst().getAddresses())
				.as("La liste des adresses doit être vide après mise à null")
				.isEmpty();
	}

	@Test
	@DisplayName("Update organisation - le téléphone est mis à jour (pas de doublon dans les adresses)")
	@Transactional
	void updateOrganization_phoneNumber_updated_not_duplicated() throws AppServiceException {
		final String oldPhone = AbstractAddressDataFactory.TELEPHONE_EXPECTED_VALUE;
		final String newPhone = "0211111111";

		OrganizationEntity createdEntity = organizationDataFactory.getOrCreateMinimalOrganizationWithPhone();
		Organization created = organizationService.getOrganization(createdEntity.getUuid(), true);

		created.setAddresses(List.of(abstractAddressDataFactory.createTelephoneAddressDto(newPhone)));
		organizationService.updateOrganization(created);

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria
				.builder()
				.uuids(List.of(created.getUuid()))
				.full(true)
				.build();

		Page<OrganizationEntity> organizations = organizationCustomDao.searchOrganizations(criteria, Pageable.unpaged());
		assertThat(organizations.getContent())
				.as("L'organisation doit exister")
				.isNotEmpty()
				.as("La liste ne doit contenir qu'une seule organization avec cet UUID")
				.hasSize(1);

		assertThat(organizations.getContent().getFirst().getAddresses())
				.as("La liste des adresses doit au moins contenir une addresse.")
				.isNotEmpty()
				.as("""
						Et cette addresse doit être de type TelephoneAddress et valoir
						%s
						""", newPhone)
				.anyMatch(a -> a instanceof TelephoneAddressEntity && ((TelephoneAddressEntity) a).getPhoneNumber().equals(newPhone))
				.as("""
						Il ne doit plus y l'ancienne addresse :
						%s
						""", oldPhone)
				.noneMatch(a -> a instanceof TelephoneAddressEntity && ((TelephoneAddressEntity) a).getPhoneNumber().equals(oldPhone));

	}


	@Test
	@DisplayName("Update organisation - le téléphone mis à null supprime la TelephoneAddressEntity")
	@Transactional
	void updateOrganization_phoneNumber_removed_when_null() throws AppServiceException {
		OrganizationEntity createdEntity = organizationDataFactory.getOrCreateMinimalOrganizationWithPhone();
		Organization created = organizationService.getOrganization(createdEntity.getUuid(), true);

		assertThat(created.getAddresses()).as("Il y a au moins une addresse de renseignée").isNotEmpty()
				.as("Cette addresse doit être de type TelephoneAddress et valoir " + AbstractAddressDataFactory.TELEPHONE_EXPECTED_VALUE)
				.anyMatch(a -> a instanceof TelephoneAddress && ((TelephoneAddress) a).getPhoneNumber().equals(AbstractAddressDataFactory.TELEPHONE_EXPECTED_VALUE));

		created.setAddresses(null);
		organizationService.updateOrganization(created);

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria
				.builder()
				.uuids(List.of(created.getUuid()))
				.full(true)
				.build();

		Page<OrganizationEntity> organizations = organizationCustomDao.searchOrganizations(criteria, Pageable.unpaged());
		assertThat(organizations.getContent())
				.as("L'organisation doit exister")
				.isNotEmpty()
				.as("La liste ne doit contenir qu'une seule organization avec cet UUID")
				.hasSize(1);

		assertThat(organizations.getContent().getFirst().getAddresses())
				.as("La liste des adresses doit être vide après mise à null")
				.isEmpty();
	}

	@Test
	@DisplayName("Update organisation - suppression URL uniquement, email et téléphone conservés")
	@Transactional
	void updateOrganization_remove_url_keeps_email_and_phone() throws AppServiceException {
		OrganizationEntity createdEntity = organizationDataFactory.getOrCreateMinimalOrganizationWithAllAddresses();
		Organization createdDto = organizationService.getOrganization(createdEntity.getUuid(), true);

		List<AbstractAddress> addressesLight = List.of(
				abstractAddressDataFactory.createEmailAddressDto(null),
				abstractAddressDataFactory.createTelephoneAddressDto(null)
		);

		createdDto.setAddresses(addressesLight);
		organizationService.updateOrganization(createdDto);

		OrganizationSearchCriteria criteria = OrganizationSearchCriteria
						.builder()
						.uuids(List.of(createdEntity.getUuid()))
				.full(true)
				.build();

		Page<OrganizationEntity> organizations = organizationCustomDao.searchOrganizations(criteria, Pageable.unpaged());
		assertThat(organizations.getContent())
				.as("L'organisation doit exister")
				.isNotEmpty()
				.as("La liste ne doit contenir qu'une seule organization avec cet UUID")
				.hasSize(1);

		assertThat(organizations.getContent().getFirst().getAddresses())
				.as("La liste des adresses doit contenir exactement 2 adresses (email et téléphone)")
				.hasSize(2)
				.as("L'URL de contact ne doit pas être présente")
				.noneMatch(a -> a instanceof WebsiteAddressEntity)
				.as("L'email de contact doit être présent")
				.anyMatch(a -> a instanceof EmailAddressEntity && ((EmailAddressEntity) a).getEmail().equals(AbstractAddressDataFactory.EMAIL_EXPECTED_VALUE))
				.as("Le téléphone de contact doit être présent")
				.anyMatch(a -> a instanceof TelephoneAddressEntity && ((TelephoneAddressEntity) a).getPhoneNumber().equals(AbstractAddressDataFactory.TELEPHONE_EXPECTED_VALUE));
	}

	@Test
	@DisplayName("Suppression organisation - les adresses associées sont supprimées de la base")
	void deleteOrganization_addresses_are_deleted() throws AppServiceException {
		// Crée une organisation avec les 3 adresses (URL, email, téléphone)
		OrganizationEntity createdEntity = organizationDataFactory.getOrCreateMinimalOrganizationWithAllAddresses();

		// Mémorise le nombre d'adresses avant la suppression
		final long initialAddressCount = abstractAddressDao.count();
		assertThat(initialAddressCount).as("Des adresses doivent exister après création").isGreaterThan(0L);

		// Supprime l'organisation
		organizationService.deleteOrganization(createdEntity.getUuid());

		// Vérifie que l'organisation est bien supprimée en base
		assertThat(organizationDao.findByUuid(createdEntity.getUuid()))
				.as("L'organisation doit être supprimée")
				.isNull();

		// Vérifie que les adresses associées ont été supprimées avec l'organisation (cascade delete)
		assertThat(abstractAddressDao.count())
				.as("La suppression de l'organisation doit nettoyer les adresses créées pour ce test")
				.isLessThan(initialAddressCount);
	}


	// ----------------------------------------------------------------

	protected void attachWithStatus(Organization organization, ProviderEntity provider, NodeProvider nodeProvider,
			final org.rudi.microservice.strukture.storage.entity.provider.LinkedProducerStatus expectedStatus)
			throws AppServiceException {
		LinkedProducer linkedProducer = linkedProducerHelper
				.createLinkedProducer(organizationDao.findByUuid(organization.getUuid()), provider, nodeProvider);
		LinkedProducerEntity linkedProducerEntity = linkedProducerDao.findByUuid(linkedProducer.getUuid());
		linkedProducerEntity.setLinkedProducerStatus(expectedStatus);
		linkedProducerDao.save(linkedProducerEntity);
	}

	private Organization initOrganisation(String organisationName, OrganizationStatus organisationStatus, Status status)
			throws AppServiceException {
		Organization organization1 = new Organization();
		organization1.setName(organisationName);
		organization1.setDescription("Description " + organisationName);
		organization1.setInitiator("initiator@mail.fr");

		Organization orga1Created = organizationService.createOrganization(organization1);
		orga1Created.setStatus(status);
		orga1Created.setOrganizationStatus(organisationStatus);
		organizationService.updateOrganization(orga1Created);
		return orga1Created;
	}


}

