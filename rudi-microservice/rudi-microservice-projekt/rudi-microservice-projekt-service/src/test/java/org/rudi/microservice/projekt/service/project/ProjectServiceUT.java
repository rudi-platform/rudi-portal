package org.rudi.microservice.projekt.service.project;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.common.core.DocumentContent;
import org.rudi.common.core.json.JsonResourceReader;
import org.rudi.common.core.security.AuthenticatedUser;
import org.rudi.common.core.security.RoleCodes;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.common.service.exception.AppServiceNotFoundException;
import org.rudi.common.service.exception.AppServiceUnauthorizedException;
import org.rudi.common.service.exception.MissingParameterException;
import org.rudi.common.service.helper.ResourceHelper;
import org.rudi.common.service.helper.UtilContextHelper;
import org.rudi.facet.acl.bean.ProjectKey;
import org.rudi.facet.acl.bean.ProjectKeystore;
import org.rudi.facet.acl.bean.User;
import org.rudi.facet.acl.helper.ACLHelper;
import org.rudi.facet.acl.helper.RolesHelper;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.MetadataAccessCondition;
import org.rudi.facet.kaccess.bean.MetadataAccessConditionConfidentiality;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.rudi.facet.kmedia.bean.KindOfData;
import org.rudi.facet.kmedia.service.MediaService;
import org.rudi.facet.organization.helper.OrganizationHelper;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationException;
import org.rudi.facet.organization.helper.exceptions.GetOrganizationMembersException;
import org.rudi.microservice.projekt.core.bean.DatasetConfidentiality;
import org.rudi.microservice.projekt.core.bean.LinkedDataset;
import org.rudi.microservice.projekt.core.bean.NewDatasetRequest;
import org.rudi.microservice.projekt.core.bean.NewDatasetRequestStatus;
import org.rudi.microservice.projekt.core.bean.OwnerType;
import org.rudi.microservice.projekt.core.bean.Project;
import org.rudi.microservice.projekt.core.bean.ProjectKeyCredential;
import org.rudi.microservice.projekt.core.bean.ProjectKeySearchCriteria;
import org.rudi.microservice.projekt.core.bean.ProjectStatus;
import org.rudi.microservice.projekt.core.bean.ReutilisationStatus;
import org.rudi.microservice.projekt.core.bean.TargetAudience;
import org.rudi.microservice.projekt.core.bean.criteria.ProjectSearchCriteria;
import org.rudi.microservice.projekt.service.ProjectSpringBootTest;
import org.rudi.microservice.projekt.service.confidentiality.impl.ConfidentialityHelper;
import org.rudi.microservice.projekt.service.helper.MyInformationsHelper;
import org.rudi.microservice.projekt.service.mapper.ReutilisationStatusMapper;
import org.rudi.microservice.projekt.service.replacer.TransientDtoReplacerTest;
import org.rudi.microservice.projekt.storage.dao.project.ProjectDao;
import org.rudi.microservice.projekt.storage.dao.reutilisationstatus.ReutilisationStatusDao;
import org.rudi.microservice.projekt.storage.dao.support.SupportDao;
import org.rudi.microservice.projekt.storage.dao.territory.TerritorialScaleDao;
import org.rudi.microservice.projekt.storage.dao.type.ProjectTypeDao;
import org.rudi.microservice.projekt.storage.entity.ReutilisationStatusEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Class de test de la couche service
 */
@ProjectSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class ProjectServiceUT {

	private static final KnownProject PROJET_LAMPADAIRES = new KnownProject("lampadaires",
			"Projet de comptage des lampadaires");
	private static final KnownProject PROJET_POUBELLES = new KnownProject("poubelles",
			"Projet de suivi des poubelles jaunes orangées");
	private static final KnownProject PROJET_LAMPADAIRE_PUBLIC = new KnownProject("lampadaires_public",
			"Projet de comptage des lampadaires - Public");
	private static final KnownProject PROJET_LAMPADAIRE_PRIVATE = new KnownProject("lampadaires_private",
			"Projet de comptage des lampadaires - Privé");
	private static final KnownProject PROJET_POUBELLE_PUBLIC = new KnownProject("poubelles_public",
			"Projet de suivi des poubelles jaunes orangées - Public");
	private static final KnownProject PROJET_POUBELLE_PRIVATE = new KnownProject("poubelles_private",
			"Projet de suivi des poubelles jaunes orangées - Privé");
	private static final KnownProject PROJET_LAMPADAIRE_ORGANISATION_PUBLIC = new KnownProject(
			"lampadaires_orga_public", "Projet de comptage des lampadaires - Public");
	private static final KnownProject PROJET_LAMPADAIRE_ORGANISATION_PRIVATE = new KnownProject(
			"lampadaires_orga_private", "Projet de comptage des lampadaires - Privé");
	private static final KnownProject PROJET_POUBELLE_ORGANISATION_PUBLIC = new KnownProject("poubelles_orga_public",
			"Projet de suivi des poubelles jaunes orangées - Public");
	private static final KnownProject PROJET_POUBELLE_ORGANISATION_PRIVATE = new KnownProject("poubelles_orga_private",
			"Projet de suivi des poubelles jaunes orangées - Privé");

	private static final String DEFAULT_LOGO_FILE_NAME = "media/default-logo.png";
	private static final String SECOND_LOGO_FILE_NAME = "media/project-logo-changed.png";
	private static final String REJECTED_LOGO_TYPE = "media/not_matched_logo_type.svg";

	private final ProjectService projectService;
	private final ProjectDao projectDao;
	private final TerritorialScaleDao territorialScaleDao;
	private final ProjectTypeDao projectTypeDao;
	private final SupportDao supportDao;
	private final ReutilisationStatusDao reutilisationStatusDao;
	private final LinkedDatasetService linkedDatasetService;

	private final JsonResourceReader jsonResourceReader;
	private final List<TransientDtoReplacerTest> transientDtoReplacers;

	private final ConfidentialityHelper confidentialityHelper;
	private final ReutilisationStatusMapper reutilisationStatusMapper;
	private final ResourceHelper resourceHelper;

	@MockitoBean
	private UtilContextHelper utilContextHelper;
	@MockitoBean
	private ACLHelper aclHelper;
	@MockitoBean
	private RolesHelper rolesHelper;
	@MockitoBean
	private MyInformationsHelper myInformationsHelper;
	@MockitoBean
	private OrganizationHelper organizationHelper;
	@MockitoBean
	private MediaService mediaService;
	@MockitoBean
	private final DatasetService datasetService;

	@SuppressWarnings("unused") // mocké pour ACLHelper
	@MockitoBean(name = "rudi_oauth2")
	private WebClient webClient;

	@SuppressWarnings("unused") // mocké pour OrganizationHelper
	@MockitoBean(name = "struktureWebClient")
	private WebClient struktureWebClient;

	List<KnownProject> knownProjects = List.of(PROJET_LAMPADAIRE_PRIVATE, PROJET_LAMPADAIRE_PUBLIC,
			PROJET_POUBELLE_PRIVATE, PROJET_POUBELLE_PUBLIC, PROJET_LAMPADAIRE_ORGANISATION_PRIVATE,
			PROJET_LAMPADAIRE_ORGANISATION_PUBLIC, PROJET_POUBELLE_ORGANISATION_PRIVATE,
			PROJET_POUBELLE_ORGANISATION_PUBLIC);

	@BeforeEach
	void init() throws IOException {
		// Création des ReutilisationStatus nécessaires aux tests
		ReutilisationStatusEntity reutilisationStatus = reutilisationStatusMapper
				.dtoToEntity(jsonResourceReader.read("reutilisationstatus/project.json", ReutilisationStatus.class));
		if (reutilisationStatusDao.findByCode(reutilisationStatus.getCode()) == null) {
			reutilisationStatusDao.save(reutilisationStatus);
		}
	}

	@AfterEach
	void tearDown() {
		projectDao.deleteAll();

		projectTypeDao.deleteAll();
		supportDao.deleteAll();
		territorialScaleDao.deleteAll();
		reutilisationStatusDao.deleteAll();
	}

	private Project createProject(KnownProject knownProject) throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(knownProject.getJsonPath(), Project.class);
		createEntities(project);

		mockAuthenticatedUserToCreateProject(project);

		return projectService.createProject(project);
	}

	private void createEntities(Project project) throws AppServiceException {

		for (final TransientDtoReplacerTest getterOrCreator : transientDtoReplacers) {
			getterOrCreator.replaceDtoFor(project);
		}

	}

	@Test
	void createProject() throws IOException, AppServiceException {
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		mockAuthenticatedUserToCreateProject(projectToCreate);

		projectToCreate.setTargetAudiences(projectToCreate.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		final Project createdProject = projectService.createProject(projectToCreate);
		createdProject.setTargetAudiences(createdProject.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		assertThat(createdProject).as("On retrouve tous les champs du projet à créer").usingRecursiveComparison()
				.ignoringFields("uuid", "creationDate", "updatedDate").isEqualTo(projectToCreate);
	}

	@Test
	void createProjectWithoutAuthentication() throws IOException, AppServiceException {
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		mockUnauthenticatedUser();

		assertThatThrownBy(() -> projectService.createProject(projectToCreate))
				.isInstanceOf(AppServiceUnauthorizedException.class)
				.hasMessage("Accès non autorisé à la fonctionnalité pour l'utilisateur");
	}

	private void mockAuthenticatedUserToCreateProject(Project project)
			throws AppServiceUnauthorizedException, GetOrganizationException, GetOrganizationMembersException {
		mockAuthenticatedUserFromManager(project.getOwnerUuid(), project.getOwnerType().equals(OwnerType.ORGANIZATION));
	}

	private void mockUnauthenticated() throws AppServiceUnauthorizedException {
		when(aclHelper.getUserByLogin(any())).thenReturn(null);
		when(aclHelper.getUserByUUID(any())).thenReturn(null);
		when(aclHelper.getAuthenticatedUser()).thenReturn(null);
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(null);
		when(aclHelper.getUserByLoginAndPassword(any(), any())).thenReturn(null);
	}

	private void mockAuthenticatedWrongPwd() throws AppServiceUnauthorizedException {
		final User user = new User().login("mpokora").uuid(UUID.randomUUID())
				.roles(Collections.singletonList(new org.rudi.facet.acl.bean.Role().code(RoleCodes.USER)));
		when(aclHelper.getUserByLogin(any())).thenReturn(user);
		when(aclHelper.getUserByUUID(any())).thenReturn(user);
		when(aclHelper.getAuthenticatedUser()).thenReturn(user);
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(null);
		when(aclHelper.getUserByLoginAndPassword(any(), any())).thenReturn(null);
	}

	private void mockAuthenticatedUserFromManager(UUID managerUserUuid, boolean isOrganization)
			throws AppServiceUnauthorizedException, GetOrganizationException, GetOrganizationMembersException {
		org.rudi.facet.acl.bean.Role roleUser = new org.rudi.facet.acl.bean.Role().code(RoleCodes.USER);
		final User user = new User().login("mpokora").uuid(managerUserUuid).roles(Collections.singletonList(roleUser));
		// Si le projet est créé par un user et non au nom d'une organization.
		if (!isOrganization) {
			user.setUuid(managerUserUuid);
		} else {
			// Un UUID au hasard pour ne pas avoir le même que celui de l'orga
			user.setUuid(UUID.randomUUID());
			when(myInformationsHelper.getMeAndMyOrganizationsUuids())
					.thenReturn(List.of(user.getUuid(), managerUserUuid));
			when(myInformationsHelper.getMyOrganizationsUuids()).thenReturn(List.of(managerUserUuid));
			when(organizationHelper.organizationContainsUser(managerUserUuid, user.getUuid())).thenReturn(true);
		}
		when(aclHelper.getUserByLogin(user.getLogin())).thenReturn(user);
		when(aclHelper.getUserByUUID(user.getUuid())).thenReturn(user);
		when(aclHelper.getAuthenticatedUser()).thenReturn(user);
		when(aclHelper.getUserByLoginAndPassword(eq(user.getLogin()), any())).thenReturn(user);

		final AuthenticatedUser authenticatedUser = new AuthenticatedUser();
		authenticatedUser.setLogin(user.getLogin());
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);
	}

	private void mockAuthenticatedUserFromModerator(UUID moderatorUuid) throws AppServiceUnauthorizedException {
		org.rudi.facet.acl.bean.Role roleModerator = new org.rudi.facet.acl.bean.Role().code(RoleCodes.MODERATOR);
		final User user = new User().login("PresentMic").uuid(moderatorUuid).roles(List.of(roleModerator));
		when(aclHelper.getUserByLogin(user.getLogin())).thenReturn(user);
		when(aclHelper.getUserByUUID(user.getUuid())).thenReturn(user);
//		when(rolesHelper.hasAnyRole(user, Role.MODERATOR)).thenReturn(true);
		when(aclHelper.getAuthenticatedUser()).thenReturn(user);

		final AuthenticatedUser authenticatedUser = new AuthenticatedUser();
		authenticatedUser.setLogin(user.getLogin());
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);
	}

	private void mockAuthenticatedUserNotOwner(UUID managerUserUuid) throws AppServiceUnauthorizedException {
		org.rudi.facet.acl.bean.Role roleUser = new org.rudi.facet.acl.bean.Role().code(RoleCodes.USER);
		final User user = new User().login("shakira").uuid(managerUserUuid).roles(Collections.singletonList(roleUser));
		when(aclHelper.getUserByLogin(user.getLogin())).thenReturn(user);
		when(aclHelper.getUserByUUID(user.getUuid())).thenReturn(user);
		when(aclHelper.getAuthenticatedUser()).thenReturn(user);

		final AuthenticatedUser authenticatedUser = new AuthenticatedUser();
		authenticatedUser.setLogin(user.getLogin());
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);
	}

	private void mockUnauthenticatedUser() {
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(null);
	}

	private void mockAuthenticatedUserFromAnonymous() throws AppServiceUnauthorizedException {
		org.rudi.facet.acl.bean.Role roleUser = new org.rudi.facet.acl.bean.Role().code(RoleCodes.ANONYMOUS);
		final User user = new User().login("anonymous").uuid(UUID.randomUUID())
				.roles(Collections.singletonList(roleUser));
		when(aclHelper.getUserByLogin(user.getLogin())).thenReturn(user);
		when(aclHelper.getUserByUUID(user.getUuid())).thenReturn(user);
		when(aclHelper.getAuthenticatedUser()).thenReturn(user);

		final AuthenticatedUser authenticatedUser = new AuthenticatedUser();
		authenticatedUser.setLogin(user.getLogin());
		when(utilContextHelper.getAuthenticatedUser()).thenReturn(authenticatedUser);
	}

	@Test
	void createProjectWithoutProjectType() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		project.setType(null);
		createEntities(project);

		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);

		assertThat(createdProject.getType()).as("Le type est facultatif").isNull();
	}

	@Test
	void createProjectWithoutTerritorialScale() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		project.setTerritorialScale(null);
		createEntities(project);

		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);

		assertThat(createdProject.getTerritorialScale()).as("L'échelle de territoire est facultative").isNull();
	}

	@Test
	void createProjectWithoutDesiredSupports() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		project.setDesiredSupports(Collections.emptyList());
		createEntities(project);

		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);
		// On peut créer un projet initialement sans desireSupports
		assertThat(createdProject.getDesiredSupports()).isEmpty();
	}

	// FIXME ce test ne fonctionne que s'il n'est pas lancé avec maven
	@Test
	@Disabled
	void createProjectWithoutConfidentiality() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		project.setConfidentiality(null);
		createEntities(project);

		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);
		assertThat(createdProject).as("Le projet est créé avec la confidentialité par défaut")
				.hasFieldOrPropertyWithValue("confidentiality.uuid",
						confidentialityHelper.getDefaultConfidentiality().getUuid());
	}

	@Test
	void createProjectWithoutManager() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		mockAuthenticatedUserToCreateProject(project);
		project.setOwnerUuid(null);
		createEntities(project);

		assertThatThrownBy(() -> projectService.createProject(project)).isInstanceOf(MissingParameterException.class)
				.hasMessage("owner_uuid manquant");
	}

	@Test
	void createProjectWithProjectTypeWithoutUuid() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		project.getType().setUuid(null);

		mockAuthenticatedUserToCreateProject(project);

		assertThatThrownBy(() -> projectService.createProject(project)).isInstanceOf(MissingParameterException.class)
				.hasMessage("type.uuid manquant");
	}

	@Test
	void createProjectWithTerritorialScaleWithoutUuid() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		project.getTerritorialScale().setUuid(null);

		mockAuthenticatedUserToCreateProject(project);

		assertThatThrownBy(() -> projectService.createProject(project)).isInstanceOf(MissingParameterException.class)
				.hasMessage("territorial_scale.uuid manquant");
	}

	@Test
	void createProjectWithDesiredSupportsWithoutUuid() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		project.getDesiredSupports().get(0).setUuid(null);
		mockAuthenticatedUserToCreateProject(project);

		assertThatThrownBy(() -> projectService.createProject(project)).isInstanceOf(MissingParameterException.class)
				.hasMessage("support.uuid manquant");
	}

	@Test
	void createProjectWithConfidentialityWithoutUuid() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		project.getConfidentiality().setUuid(null);
		mockAuthenticatedUserToCreateProject(project);

		assertThatThrownBy(() -> projectService.createProject(project)).isInstanceOf(MissingParameterException.class)
				.hasMessage("confidentiality.uuid manquant");
	}

	@Test
	void createProjectWithInconsistentPeriod() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);

		// On inverse date de fin et début qui viennent du JSON pour créer une période incohérente
		LocalDateTime endDate = project.getExpectedCompletionEndDate();
		project.setExpectedCompletionEndDate(project.getExpectedCompletionStartDate());
		project.setExpectedCompletionStartDate(endDate);

		mockAuthenticatedUserToCreateProject(project);

		// On vérifie que ça pète bien
		assertThatThrownBy(() -> projectService.createProject(project))
				.isInstanceOf(AppServiceBadRequestException.class);
	}

	@Test
	void createProjectWithUuid() throws IOException, AppServiceException {
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		final UUID forcedUuid = UUID.randomUUID();
		projectToCreate.setUuid(forcedUuid);

		mockAuthenticatedUserToCreateProject(projectToCreate);

		final Project createdProject = projectService.createProject(projectToCreate);

		assertThat(createdProject.getUuid())
				.as("Même si on indique un UUID à la création d'un projet, il n'est pas pris en compte mais regénéré")
				.isNotEqualTo(forcedUuid);
	}

	@Test
	@DisplayName("Je crée le projet pour quelqu'un d'autre")
	void createSomeoneElseSProject() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		val projectManager = new User().login("thebigboss").uuid(project.getOwnerUuid());
		when(aclHelper.getUserByUUID(projectManager.getUuid())).thenReturn(projectManager);
		createEntities(project);

		mockAuthenticatedUserNotOwner(UUID.randomUUID());

		assertThatThrownBy(() -> projectService.createProject(project))
				.as("Je ne peux pas créer le projet pour quelqu'un d'autre")
				.isInstanceOf(AppServiceUnauthorizedException.class)
				.hasMessage("Accès non autorisé à la fonctionnalité pour l'utilisateur");
	}

	@Test
	void updateProject() throws IOException, AppServiceException {
		final Project project = createProject(PROJET_LAMPADAIRES);
		project.setTitle("Projet de comptage des lampadaires revu et corrigé");

		final Project updatedProject = projectService.updateProject(project);

		assertThat(updatedProject).as("Aucun champ n'a été modifié à part le titre").usingRecursiveComparison()
				.ignoringFields("datasetRequests", "linkedDatasets", "creationDate", "updatedDate").isEqualTo(project);
	}

	@Test
	void updateProjectWithNullDatasetRequests() throws IOException, AppServiceException {
		final Project project = createProject(PROJET_LAMPADAIRES);
		final Project updatedProject = projectService.updateProject(project);

		assertThat(updatedProject).isNotNull().hasFieldOrPropertyWithValue("datasetRequests", Collections.emptyList());
	}

	@Test
	@DisplayName("Je modifie un projet qui n'existe pas")
	void updateUnexistingProject() throws IOException, AppServiceException {
		final Project project = createProject(PROJET_LAMPADAIRES);
		project.setUuid(UUID.randomUUID());

		assertThatThrownBy(() -> projectService.updateProject(project))
				.as("On ne peut pas modifier un projet inexistant").isInstanceOf(AppServiceNotFoundException.class)
				.hasMessage("project with UUID = \"%s\" not found", project.getUuid());
	}

	@Test
	@DisplayName("Je modifie le projet créé par quelqu'un d'autre")
	void updateSomeoneElseSProject() throws IOException, AppServiceException {
		final Project project = createProject(PROJET_LAMPADAIRES);
		project.setTitle("Projet de comptage des lampadaires revu et corrigé");

		final UUID otherManager = UUID.randomUUID();
		project.setOwnerUuid(otherManager);
		createEntities(project);
		mockAuthenticatedUserFromManager(otherManager, project.getOwnerType().equals(OwnerType.ORGANIZATION));

		assertThatThrownBy(() -> projectService.updateProject(project))
				.as("Je ne peux pas modifier le projet créé par quelqu'un d'autre")
				.isInstanceOf(AppServiceUnauthorizedException.class)
				.hasMessage("Accès non autorisé à la fonctionnalité pour l'utilisateur");
	}

	@Test
	@DisplayName("Je modifie le projet créé par quelqu'un d'autre en tant qu'animateur")
	void updateSomeoneElseSProjectAsModerator() throws IOException, AppServiceException {
		final Project project = createProject(PROJET_LAMPADAIRES);
		project.setTitle("Projet de comptage des lampadaires revu et corrigé");

		final UUID otherManager = UUID.randomUUID();
		project.setOwnerUuid(otherManager);
		createEntities(project);
		mockAuthenticatedUserFromModerator(otherManager);

		project.setTargetAudiences(project.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		final Project updatedProject = projectService.updateProject(project);

		updatedProject.setTargetAudiences(updatedProject.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		assertThat(updatedProject).as("Aucun champ n'a été modifié à part le titre").usingRecursiveComparison()
				.ignoringFields("datasetRequests", "linkedDatasets", "creationDate", "updatedDate").isEqualTo(project);
	}

	@Test
	void searchProjectThemes() throws IOException, AppServiceException {

		createProject(PROJET_LAMPADAIRES);
		createProject(PROJET_POUBELLES);

		mockAuthenticatedUserFromModerator(UUID.randomUUID());

		val pageable = PageRequest.of(0, 2);
		final Page<Project> projects = projectService.searchProjects(
				ProjectSearchCriteria.builder().themes(Arrays.asList("comptage", "lampadaires")).build(), pageable);

		assertThat(projects).as("On retrouve uniquement le project attendu").extracting("title")
				.containsExactly(PROJET_LAMPADAIRES.getTitle());
	}

	@Test
	void searchProjectKeywords() throws IOException, AppServiceException {

		createProject(PROJET_LAMPADAIRES);
		createProject(PROJET_POUBELLES);

		val pageable = PageRequest.of(0, 2);

		mockAuthenticatedUserFromModerator(UUID.randomUUID());

		final Page<Project> projects = projectService.searchProjects(
				ProjectSearchCriteria.builder().keywords(Arrays.asList("comptage", "lampadaires")).build(), pageable);

		assertThat(projects).as("On retrouve uniquement le project attendu").extracting("title")
				.containsExactly(PROJET_LAMPADAIRES.getTitle());
	}

	@Test
	void deleteProject() throws IOException, AppServiceException {
		final long totalElementsBeforeCreate = countProjects();

		final Project createdProject = createProject(PROJET_LAMPADAIRES);
		final long totalElementsAfterCreate = countProjects();
		assertThat(totalElementsAfterCreate).as("Le projet est bien créé").isEqualTo(totalElementsBeforeCreate + 1);

		projectService.deleteProject(createdProject.getUuid());
		final long totalElementsAfterDelete = countProjects();
		assertThat(totalElementsAfterDelete).as("Le projet est bien supprimé").isEqualTo(totalElementsBeforeCreate);
	}

	@Test
	@DisplayName("Je supprime un projet qui n'existe pas")
	void deleteUnexistingProject() throws IOException, AppServiceException {
		final long totalElementsBeforeCreate = countProjects();

		final Project createdProject = createProject(PROJET_LAMPADAIRES);
		projectService.deleteProject(createdProject.getUuid());
		projectService.deleteProject(createdProject.getUuid());

		final long totalElementsAfterSecondDelete = countProjects();
		assertThat(totalElementsAfterSecondDelete).as("Aucun projet n'a été de nouveau supprimé")
				.isEqualTo(totalElementsBeforeCreate);
	}

	@Test
	@DisplayName("Je supprime le projet créé par quelqu'un d'autre")
	void deleteSomeoneElseSProject() throws IOException, AppServiceException {
		final long totalElementsBeforeCreate = countProjects();

		final Project createdProject = createProject(PROJET_LAMPADAIRES);
		final long totalElementsAfterCreate = countProjects();
		assertThat(totalElementsAfterCreate).as("Le projet est bien créé").isEqualTo(totalElementsBeforeCreate + 1);

		mockAuthenticatedUserNotOwner(UUID.randomUUID());

		assertThatThrownBy(() -> projectService.deleteProject(createdProject.getUuid()))
				.as("Je ne peux pas supprimer le projet créé par quelqu'un d'autre")
				.isInstanceOf(AppServiceUnauthorizedException.class)
				.hasMessage("Accès non autorisé à la fonctionnalité pour l'utilisateur");

		final long totalElementsAfterDelete = countProjects();
		assertThat(totalElementsAfterDelete).as("Aucun projet n'a été supprimé")
				.isEqualTo(totalElementsBeforeCreate + 1);
	}

	@Test
	@DisplayName("Je supprime le projet créé par quelqu'un d'autre en tant qu'animateur")
	void deleteSomeoneElseSProjectAsModerator() throws IOException, AppServiceException {
		final long totalElementsBeforeCreate = countProjects();

		final Project createdProject = createProject(PROJET_LAMPADAIRES);
		final long totalElementsAfterCreate = countProjects();
		assertThat(totalElementsAfterCreate).as("Le projet est bien créé").isEqualTo(totalElementsBeforeCreate + 1);

		final UUID otherManager = UUID.randomUUID();
		mockAuthenticatedUserFromModerator(otherManager);

		projectService.deleteProject(createdProject.getUuid());

		final long totalElementsAfterDelete = countProjects();
		assertThat(totalElementsAfterDelete).as("Le projet est bien supprimé").isEqualTo(totalElementsBeforeCreate);
	}

	@Test
	@DisplayName("Modification du champ ReutilsiationStatus d'un projet via UpdateProject")
	void updateProjectFieldReutilisationStatus() throws AppServiceException, IOException {
		// Creation du projet
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		mockAuthenticatedUserToCreateProject(projectToCreate);

		projectToCreate.setTargetAudiences(projectToCreate.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		final Project createdProject = projectService.createProject(projectToCreate);

		// Récupération puis ajout en BDD du status pour changement
		ReutilisationStatus status = jsonResourceReader.read("reutilisationstatus/reuse.json",
				ReutilisationStatus.class);
		ReutilisationStatusEntity reutilisationStatus = reutilisationStatusMapper.dtoToEntity(status);
		if (reutilisationStatusDao.findByCode(reutilisationStatus.getCode()) == null) {
			reutilisationStatusDao.save(reutilisationStatus);
		}

		createdProject.setReutilisationStatus(status);
		val updatedProject = projectService.updateProject(createdProject);

		assertThat(updatedProject.getReutilisationStatus().getCode()).isEqualTo("REUSE")
				.as("Le code du Reutilsiation Status à ce stade doit être REUSE");

	}

	@Test
	@DisplayName("Vérifie que l'utilisateur connecté est owner du projet - passant")
	void authenticatedUserIsProjectOwner() throws Exception {
		// Creation du projet
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		mockAuthenticatedUserToCreateProject(projectToCreate);

		projectToCreate.setTargetAudiences(projectToCreate.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		final Project createdProject = projectService.createProject(projectToCreate);
		final UUID projectUuid = createdProject.getUuid();

		// On fait en sorte que le helper renvoit un utilisateur connecté
		when(myInformationsHelper.getMeAndMyOrganizationsUuids()).thenReturn(List.of(projectToCreate.getOwnerUuid()));

		assertThat(projectService.isAuthenticatedUserProjectOwner(projectUuid))
				.as("L'utilisateur connecté est celui qui a créé le projet, il doit donc avoir accès.").isTrue();
	}

	@Test
	@DisplayName("Vérifie que l'utilisateur connecté est owner du projet - non passant")
	void authenticatedUserIsNotProjectOwner() throws Exception {
		// Creation du projet
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		mockAuthenticatedUserToCreateProject(projectToCreate);

		projectToCreate.setTargetAudiences(projectToCreate.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		final Project createdProject = projectService.createProject(projectToCreate);
		final UUID projectUuid = createdProject.getUuid();

		// On se connecte avec un autre utilisateur
		mockAuthenticatedUserNotOwner(UUID.randomUUID());

		assertThat(projectService.isAuthenticatedUserProjectOwner(projectUuid))
				.as("L'utilisateur connecté n'est pas celui qui a créé le projet, il ne doit donc pas avoir accès.")
				.isFalse();
	}

	@Test
	@DisplayName("Vérifie que l'utilisateur connecté est owner du projet - projectUuid ne renvoi aucun projet")
	void authenticatedUserISProjectOwnerNoProjectFound() throws Exception {
		// On ne crée pas de projet
		final UUID projectUuid = UUID.randomUUID();

		// on fait en sorte que le helper renvoit un autre utilisateur que celui connecté
		when(myInformationsHelper.getMeAndMyOrganizationsUuids()).thenReturn(List.of(UUID.randomUUID()));

		// Test random UUID (ne renvoit aucun projet)
		assertThrows(AppServiceNotFoundException.class,
				() -> projectService.isAuthenticatedUserProjectOwner(projectUuid));
		// Test UUID du projet est null
		assertThrows(AppServiceNotFoundException.class, () -> projectService.isAuthenticatedUserProjectOwner(null));
	}

	@Test
	@DisplayName("Crée un NewDatasetRequest liée à un projet - Authorized")
	void createNewDatasetRequest() throws AppServiceException, IOException {
		// Creation du projet
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		mockAuthenticatedUserToCreateProject(projectToCreate);

		projectToCreate.setTargetAudiences(projectToCreate.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		final Project createdProject = projectService.createProject(projectToCreate);
		// Récupération du projectUuid pour le lier avec la newDatasetRequest
		final UUID projectUuid = createdProject.getUuid();

		// Création de la newDatasetRequest à lier au projet
		final NewDatasetRequest request = getNewDatasetRequest();

		// Ajout de la newDatasetRequest au projet
		val requestCreated = projectService.createNewDatasetRequest(projectUuid, request);

		assertThat(requestCreated.getTitle()).as("Les champs doivent être égaux - Title").isEqualTo(request.getTitle());
		assertThat(requestCreated.getDescription()).as("Les champs doivent être égaux - Description")
				.isEqualTo(request.getDescription());
		assertThat(requestCreated.getStatus().getValue()).as("Le status doit être renseignée")
				.isEqualTo(NewDatasetRequestStatus.DRAFT.getValue());
		val now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
		assertThat(requestCreated.getCreationDate().truncatedTo(ChronoUnit.MINUTES))
				.as("La demande doit avoir été créée maintenant").isEqualTo(now);

		val newDatasetRequestsFromProject = projectService.getNewDatasetRequests(projectUuid);
		boolean isContained = false;
		for (NewDatasetRequest r : newDatasetRequestsFromProject) {
			if (r.getUuid().equals(requestCreated.getUuid())) {
				isContained = true;
				break;
			}
		}
		assertThat(isContained)
				.as("La liste des NewDatasetRequest du projet doit contenir la NewDatasetRequest créée précédemment.")
				.isTrue();
	}

	@Test
	@DisplayName("Crée une newDatasetRequest et tente de la lier à un projet dont on est pas le owner")
	void createNewDatasetRequestNotAuthorized() throws AppServiceException, IOException {
		// Creation du projet
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		mockAuthenticatedUserToCreateProject(projectToCreate);

		projectToCreate.setTargetAudiences(projectToCreate.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		final Project createdProject = projectService.createProject(projectToCreate);
		// Récupération du projectUuid pour le lier avec la newDatasetRequest
		final UUID projectUuid = createdProject.getUuid();

		// Création de la newDatasetRequest à lier au projet
		final NewDatasetRequest request = getNewDatasetRequest();

		// Changement de personne connectée
		final UUID otherManager = UUID.randomUUID();
		mockAuthenticatedUserFromManager(otherManager, false);

		assertThrows(AppServiceUnauthorizedException.class,
				() -> projectService.createNewDatasetRequest(projectUuid, request));
	}

	@Test
	@DisplayName("Modification d'un newDatasetRequest - Authorized")
	void updateNewDatasetRequest() throws AppServiceException, IOException {
		// Creation du projet
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		mockAuthenticatedUserToCreateProject(projectToCreate);

		projectToCreate.setTargetAudiences(projectToCreate.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		final Project createdProject = projectService.createProject(projectToCreate);
		// Récupération du projectUuid pour le lier avec la newDatasetRequest
		final UUID projectUuid = createdProject.getUuid();

		// Création de la newDatasetRequest à lier au projet
		final NewDatasetRequest requestToCreate = getNewDatasetRequest();

		// Ajout de la newDatasetRequest au projet
		val requestCreated = projectService.createNewDatasetRequest(projectUuid, requestToCreate);

		// On vérifie que le champ que l'on va modifier est bien égal au départ (ndt : la création s'est bien passée)
		assertThat(requestCreated.getDescription()).as("Les champs doivent être égaux - Description")
				.isEqualTo(requestToCreate.getDescription());

		// Création d'un nouvel objet pour l'update avec les champs obligatoires
		val requestToUpdate = new NewDatasetRequest();
		requestToUpdate.setUuid(requestCreated.getUuid());
		requestToUpdate.setDescription("A new description for a newDatasetRequest");
		requestToUpdate.setTitle(requestCreated.getTitle());
		requestToUpdate.setUpdator(createdProject.getInitiator());
		requestToUpdate.setProcessDefinitionKey(requestCreated.getProcessDefinitionKey());
		requestToUpdate.setNewDatasetRequestStatus(requestCreated.getNewDatasetRequestStatus());

		val requestUpdated = projectService.updateNewDatasetRequest(projectUuid, requestToUpdate);

		// On vérifie que le champ que l'on a modifié est bien différent de ce qu'on avait
		assertThat(requestUpdated.getDescription()).as("Les champs ne doivent pas être égaux - Description")
				.isNotEqualTo(requestCreated.getDescription());
		// Mais que les autres champs n'ont pas changé
		assertThat(requestUpdated.getTitle()).as("Les champs non modifiés doivent rester intacte")
				.isEqualTo(requestCreated.getTitle());
	}

	@Test
	@DisplayName("Modification d'un newDatasetRequest d'un projet appartenant à quelqu'un d'autre")
	void updateNewDatasetRequestNotAuthorized() throws AppServiceException, IOException {
		// Creation du projet
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		mockAuthenticatedUserToCreateProject(projectToCreate);

		projectToCreate.setTargetAudiences(projectToCreate.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		final Project createdProject = projectService.createProject(projectToCreate);
		// Récupération du projectUuid pour le lier avec la newDatasetRequest
		final UUID projectUuid = createdProject.getUuid();

		// Création de la newDatasetRequest à lier au projet
		final NewDatasetRequest requestToCreate = getNewDatasetRequest();

		// Ajout de la newDatasetRequest au projet
		val requestCreated = projectService.createNewDatasetRequest(projectUuid, requestToCreate);

		// On vérifie que le champ que l'on va modifier est bien égal au départ (ndt : la création s'est bien passée)
		assertThat(requestCreated.getDescription()).as("Les champs doivent être égaux - Description")
				.isEqualTo(requestToCreate.getDescription());

		// Création d'un nouvel objet pour l'update avec les champs obligatoires
		val requestToUpdate = new NewDatasetRequest();
		requestToUpdate.setUuid(requestCreated.getUuid());
		requestToUpdate.setDescription("A new description for a newDatasetRequest");
		requestToUpdate.setTitle(requestCreated.getTitle());
		requestToUpdate.setUpdator(createdProject.getInitiator());
		requestToUpdate.setProcessDefinitionKey(requestCreated.getProcessDefinitionKey());
		requestToUpdate.setNewDatasetRequestStatus(requestCreated.getNewDatasetRequestStatus());

		// Changement de personne connectée
		final UUID otherManager = UUID.randomUUID();
		mockAuthenticatedUserFromManager(otherManager, false);

		assertThrows(AppServiceUnauthorizedException.class,
				() -> projectService.updateNewDatasetRequest(projectUuid, requestToUpdate));
	}

	@Test
	@DisplayName("Suppression d'une newDatasetRequest - Authorized")
	void deleteNewDatasetRequest() throws AppServiceException, IOException {
		// Creation du projet
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		mockAuthenticatedUserToCreateProject(projectToCreate);

		projectToCreate.setTargetAudiences(projectToCreate.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		final Project createdProject = projectService.createProject(projectToCreate);
		// Récupération du projectUuid pour le lier avec la newDatasetRequest
		final UUID projectUuid = createdProject.getUuid();

		// Création de la newDatasetRequest à lier au projet
		final NewDatasetRequest requestToCreate = getNewDatasetRequest();

		// Ajout de la newDatasetRequest au projet
		val requestCreated = projectService.createNewDatasetRequest(projectUuid, requestToCreate);

		val newDatasetRequestsFromProject = projectService.getNewDatasetRequests(projectUuid);
		boolean isContained = false;
		for (NewDatasetRequest r : newDatasetRequestsFromProject) {
			if (r.getUuid().equals(requestCreated.getUuid())) {
				isContained = true;
				break;
			}
		}
		assertThat(isContained)
				.as("La liste des NewDatasetRequest du projet doit contenir la NewDatasetRequest créée précédemment.")
				.isTrue();

		projectService.deleteNewDatasetRequest(projectUuid, requestCreated.getUuid());

		val newDatasetRequestsFromProjectAfterDelete = projectService.getNewDatasetRequests(projectUuid);
		boolean isStillContained = false;
		for (NewDatasetRequest r : newDatasetRequestsFromProjectAfterDelete) {
			if (r.getUuid().equals(requestCreated.getUuid())) {
				isStillContained = true;
				break;
			}
		}
		assertThat(isStillContained).as(
				"La liste des NewDatasetRequest du projet ne doit plus contenir la NewDatasetRequest créée précédemment.")
				.isFalse();
	}

	@Test
	@DisplayName("Suppression d'une newDatasetRequest d'un projet que je ne possède pas")
	void deleteNewDatasetRequestNotAuthorized() throws AppServiceException, IOException {
		// Creation du projet
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		mockAuthenticatedUserToCreateProject(projectToCreate);

		projectToCreate.setTargetAudiences(projectToCreate.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		final Project createdProject = projectService.createProject(projectToCreate);
		// Récupération du projectUuid pour le lier avec la newDatasetRequest
		final UUID projectUuid = createdProject.getUuid();

		// Création de la newDatasetRequest à lier au projet
		final NewDatasetRequest requestToCreate = getNewDatasetRequest();

		// Ajout de la newDatasetRequest au projet
		val requestCreated = projectService.createNewDatasetRequest(projectUuid, requestToCreate);

		val newDatasetRequestsFromProject = projectService.getNewDatasetRequests(projectUuid);
		boolean isContained = false;
		for (NewDatasetRequest r : newDatasetRequestsFromProject) {
			if (r.getUuid().equals(requestCreated.getUuid())) {
				isContained = true;
				break;
			}
		}
		assertThat(isContained)
				.as("La liste des NewDatasetRequest du projet doit contenir la NewDatasetRequest créée précédemment.")
				.isTrue();

		// Changement de personne connectée
		final UUID otherManager = UUID.randomUUID();
		mockAuthenticatedUserFromManager(otherManager, false);

		assertThrows(AppServiceUnauthorizedException.class,
				() -> projectService.deleteNewDatasetRequest(projectUuid, requestCreated.getUuid()));

	}

	@Test
	@DisplayName("Ajout d'un media a un projet - Authorized and Not Authorized")
	void uploadProjectMedia() throws AppServiceException, IOException {
		// Creation du projet
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		mockAuthenticatedUserToCreateProject(projectToCreate);

		projectToCreate.setTargetAudiences(projectToCreate.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		final Project createdProject = projectService.createProject(projectToCreate);
		// Récupération du projectUuid pour le lier avec la newDatasetRequest
		final UUID projectUuid = createdProject.getUuid();

		val logo = getLogoFromPath(DEFAULT_LOGO_FILE_NAME);
		// Je suis connecté avec le porteur du projet, j'ai donc accès à la modification.
		assertAll(() -> projectService.uploadMedia(projectUuid, KindOfData.LOGO, logo));

		// Changement de personne connectée
		final UUID otherManager = UUID.randomUUID();
		mockAuthenticatedUserFromManager(otherManager, false);

		val newLogo = getLogoFromPath(SECOND_LOGO_FILE_NAME);

		// Une exception est levée, car je ne suis pas connecté avec un utilisateur qui est Owner du projet
		assertThrows(AppServiceUnauthorizedException.class,
				() -> projectService.uploadMedia(projectUuid, KindOfData.LOGO, newLogo));
	}

	@Test
	@DisplayName("Ajout d'un media a un projet - not allowed logo type")
	void uploadProjectMediaWithWrongContentType() throws AppServiceException, IOException {
		// Creation du projet
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		mockAuthenticatedUserToCreateProject(projectToCreate);

		projectToCreate.setTargetAudiences(projectToCreate.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		final Project createdProject = projectService.createProject(projectToCreate);
		// Récupération du projectUuid pour le lier avec la newDatasetRequest
		final UUID projectUuid = createdProject.getUuid();

		val logo = getLogoFromPath(REJECTED_LOGO_TYPE);
		// Je suis connecté avec le porteur du projet, j'ai donc accès à la modification.
		assertThrows(IllegalArgumentException.class,
				() -> projectService.uploadMedia(projectUuid, KindOfData.LOGO, logo));
	}

	@Test
	@DisplayName("Ajout puis suppression d'un media a un projet - Authorized and Not Authorized")
	void deleteProjectMedia() throws AppServiceException, IOException {
		// Creation du projet
		final Project projectToCreate = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(projectToCreate);

		mockAuthenticatedUserToCreateProject(projectToCreate);

		projectToCreate.setTargetAudiences(projectToCreate.getTargetAudiences().stream()
				.sorted(Comparator.comparing(TargetAudience::getUuid)).collect(Collectors.toList()));

		final Project createdProject = projectService.createProject(projectToCreate);
		// Récupération du projectUuid pour le lier avec la newDatasetRequest
		final UUID projectUuid = createdProject.getUuid();

		val logo = getLogoFromPath(DEFAULT_LOGO_FILE_NAME);

		// Je suis connecté avec le porteur du projet, j'ai donc accès à la modification.
		assertAll(() -> projectService.uploadMedia(projectUuid, KindOfData.LOGO, logo));

		// Changement de personne connectée
		final UUID otherManager = UUID.randomUUID();
		mockAuthenticatedUserFromManager(otherManager, false);

		// Une exception est levée, car je ne suis pas connecté avec un utilisateur qui est Owner du projet
		assertThrows(AppServiceUnauthorizedException.class,
				() -> projectService.deleteMedia(projectUuid, KindOfData.LOGO));

		// Je me re connecte en tant qu'Owner du projet
		mockAuthenticatedUserToCreateProject(projectToCreate);
		assertAll(() -> projectService.deleteMedia(projectUuid, KindOfData.LOGO));
	}

	@Test
	@DisplayName("Recherche de projet en anonymous")
	void searchProjectAuthenticatedAsAnonymous() throws AppServiceException, IOException {
		mockAuthenticatedUserFromAnonymous();
		Long baseValue = projectService.searchProjects(new ProjectSearchCriteria(), Pageable.unpaged())
				.getTotalElements();

		for (KnownProject knownProject : knownProjects) {
			createProject(knownProject);
		}

		// On se connecte en tant qu'Anonymous
		mockAuthenticatedUserFromAnonymous();
		Page<Project> projects = projectService.searchProjects(new ProjectSearchCriteria(), Pageable.unpaged());

		assertThat(projects).as("La recherche doit renvoyer au moins un élément, et donc ne pas être null").isNotNull();
		assertThat(projects.getTotalElements()).as("La recherche doit renvoyer un nombre de résultat supérieur à 0")
				.isPositive().as("Le resultat doit comporter quatre nouvelles réutilisations").isEqualTo(baseValue + 4);
		assertThat(projects.get()).as("Le résultat ne doit pas comporter de confidentialités privée")
				.allMatch(p -> !p.getConfidentiality().getPrivateAccess());
	}

	@Test
	@DisplayName("Recherche de projet en animateur")
	void searchProjectAuthenticatedAsModerator() throws AppServiceException, IOException {
		mockAuthenticatedUserFromModerator(UUID.randomUUID());
		Long baseValue = projectService.searchProjects(new ProjectSearchCriteria(), Pageable.unpaged())
				.getTotalElements();

		for (KnownProject knownProject : knownProjects) {
			createProject(knownProject);
		}

		// On se connecte en tant qu'Anonymous
		mockAuthenticatedUserFromModerator(UUID.randomUUID());
		Page<Project> projects = projectService.searchProjects(new ProjectSearchCriteria(), Pageable.unpaged());

		assertThat(projects).as("La recherche doit renvoyer au moins un élément, et donc ne pas être null").isNotNull();
		assertThat(projects.getTotalElements()).as("La recherche doit renvoyer un nombre de résultat supérieur à 0")
				.isPositive().isPositive().as("Le resultat doit comporter huit nouvelles réutilisations")
				.isEqualTo(baseValue + 8);
		assertThat(projects.get()).as("Le résultat doit comporter des confidentialités privée")
				.anyMatch(p -> p.getConfidentiality().getPrivateAccess())
				.as("Le résultat doit comporter des confidentialités public")
				.anyMatch(p -> !p.getConfidentiality().getPrivateAccess());
	}

	@Test
	@DisplayName("Recherche de projet en membre de l'organisation à l'origine du projet")
	void searchProjectAuthenticatedAsOrganizationMember()
			throws AppServiceException, IOException, DataverseAPIException {
		mockAuthenticatedUserFromModerator(UUID.randomUUID());
		Long baseValue = projectService.searchProjects(new ProjectSearchCriteria(), Pageable.unpaged())
				.getTotalElements();

		List<Project> createdPtojects = new ArrayList<>();
		for (KnownProject knownProject : knownProjects) {
			Project project = createProject(knownProject);
			createdPtojects.add(project);
		}

		for (Project p : createdPtojects) {
			UUID projectUuid = p.getUuid();
			// Créations des JDDs de test
			final var ld1Uuid = UUID.randomUUID();
			final var ld1 = createLinkedDataset(ld1Uuid, "link opened", DatasetConfidentiality.OPENED);
			Metadata associated1 = createMetadataAssociated(ld1);
			when(datasetService.getDataset(any(UUID.class))).thenReturn(associated1);
			mockAuthenticatedUserFromManager(p.getOwnerUuid(), p.getOwnerType().equals(OwnerType.ORGANIZATION));
			var ld_openUuid = linkedDatasetService.linkProjectToDataset(projectUuid, ld1).getUuid();
			assertThat(linkedDatasetService.getLinkedDataset(projectUuid, ld_openUuid))
					.as(String.format("La création du JDD %s doit se passer correctement pour le projet %s",
							ld1.getDescription(), p.getTitle()))
					.isNotNull();
		}

		Optional<UUID> organizationUuid = createdPtojects.stream()
				.filter(p -> p.getOwnerType() == OwnerType.ORGANIZATION).map(p -> p.getOwnerUuid()).findFirst();
		assertThat(organizationUuid).as("S'il n'y a pas d'organization, il y a un soucis").isNotEmpty();
		// On se connecte en tant qu'Anonymous
		mockAuthenticatedUserFromManager(organizationUuid.get(), true);
//		mockAuthenticatedUserFromModerator(UUID.randomUUID());
		Page<Project> projects = projectService.searchProjects(new ProjectSearchCriteria(), Pageable.unpaged());

		assertThat(projects).as("La recherche doit renvoyer au moins un élément, et donc ne pas être null").isNotNull();
		assertThat(projects.getTotalElements()).as("La recherche doit renvoyer un nombre de résultat supérieur à 0")
				.isPositive().isPositive()
				.as("Le resultat doit comporter 6 nouvelles réutilisations, car deux sont privées et non attribué à lui")
				.isEqualTo(baseValue + 6);
		assertThat(projects.get()).as("Le résultat doit comporter des confidentialités privée")
				.anyMatch(p -> p.getConfidentiality().getPrivateAccess())
				.as("Le résultat doit comporter des confidentialités public")
				.anyMatch(p -> !p.getConfidentiality().getPrivateAccess())
				.as("Les confidentialités retournées doivent être public ou avoir un JDD appartenant à cette organisation ou avoir été créé au nom de l'oganization de l'utilsiateur connecté")
				.allMatch(p -> !p.getConfidentiality().getPrivateAccess() || (p.getConfidentiality().getPrivateAccess()
						&& (p.getOwnerUuid().equals(organizationUuid.get()) || p.getLinkedDatasets().stream()
								.anyMatch(l -> l.getDatasetOrganizationUuid().equals(organizationUuid.get())))));
	}

	private NewDatasetRequest getNewDatasetRequest() {
		NewDatasetRequest request = new NewDatasetRequest();

		request.setTitle("Demande de données pour un test");
		request.setDescription("Besoin de nouvelles données pour un teste");

		return request;
	}

	private long countProjects() throws AppServiceException {
		val pageable = PageRequest.of(0, 100);

		mockAuthenticatedUserFromModerator(UUID.randomUUID());

		return projectService.searchProjects(new ProjectSearchCriteria(), pageable).getTotalElements();
	}

	private DocumentContent getLogoFromPath(String path) throws IOException {
		val logo = resourceHelper.getResourceFromAdditionalLocationOrFromClasspath(path);
		return DocumentContent.fromResource(logo, false);
	}

	private LinkedDataset createLinkedDataset(UUID uuid, String comment, DatasetConfidentiality dc) {
		LinkedDataset linkedDataset = new LinkedDataset();
		linkedDataset.setComment(comment);
		linkedDataset.setUuid(uuid);
		linkedDataset.setDatasetConfidentiality(dc);
		linkedDataset.setDatasetUuid(UUID.randomUUID());
		return linkedDataset;
	}

	private Metadata createMetadataAssociated(LinkedDataset linkedDataset) {
		Metadata returned = new Metadata();
		returned.setResourceTitle("On s'en moque");
		MetadataAccessCondition accessCondition = new MetadataAccessCondition();
		MetadataAccessConditionConfidentiality confidentiality = new MetadataAccessConditionConfidentiality();
		confidentiality.setGdprSensitive(linkedDataset.getDatasetConfidentiality() == DatasetConfidentiality.SELFDATA);

		confidentiality
				.setRestrictedAccess(linkedDataset.getDatasetConfidentiality() == DatasetConfidentiality.RESTRICTED
						|| linkedDataset.getDatasetConfidentiality() == DatasetConfidentiality.SELFDATA);
		accessCondition.setConfidentiality(confidentiality);
		returned.setAccessCondition(accessCondition);
		return returned;
	}

	@Test
	@DisplayName("Vérification création key avec un project keystore inexsitant")
	void createProjectKey() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		mockAuthenticatedUserToCreateProject(project);
		final Project createdProject = projectService.createProject(project);
		createdProject.setProjectStatus(ProjectStatus.VALIDATED);
		projectService.updateProject(createdProject);

		final ProjectKeystore pks = new ProjectKeystore().projectUuid(createdProject.getUuid()).uuid(UUID.randomUUID());
		when(aclHelper.searchProjectKeystores(any(), any())).thenReturn(new PageImpl<>(List.of()));
		when(aclHelper.createProjectKeyStore(any())).thenReturn(pks);
		final ProjectKeyCredential pkToCreate = createKeyCredential("teste1");
		when(aclHelper.createProjectKey(any(), any())).thenReturn(pkToCreate.getProjectKey());

		projectService.createProjectKey(createdProject.getUuid(), pkToCreate);

		verify(aclHelper).searchProjectKeystores(any(), any());
		verify(aclHelper).createProjectKeyStore(any());
		verify(aclHelper).createProjectKey(any(), any());
	}

	@Test
	@DisplayName("Vérification création key avec un project keystore qui existe déjà")
	void createProjectKeyAlreadyExist() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		mockAuthenticatedUserToCreateProject(project);
		final Project createdProject = projectService.createProject(project);
		createdProject.setProjectStatus(ProjectStatus.VALIDATED);
		projectService.updateProject(createdProject);

		final ProjectKeystore pks = new ProjectKeystore().projectUuid(createdProject.getUuid()).uuid(UUID.randomUUID());
		when(aclHelper.searchProjectKeystores(any(), any())).thenReturn(new PageImpl<>(List.of(pks)));
		final ProjectKeyCredential pkToCreate = createKeyCredential("teste1");
		when(aclHelper.createProjectKey(any(), any())).thenReturn(pkToCreate.getProjectKey());

		projectService.createProjectKey(createdProject.getUuid(), pkToCreate);
		verify(aclHelper).searchProjectKeystores(any(), any());
		verify(aclHelper).createProjectKey(any(), any());
	}

	@Test
	@DisplayName("Vérification de la non création d'une key en ayant seulement les droits Anonymous")
	void createProjectKeyNoRightAnonymous() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);
		final ProjectKeyCredential pkToCreate = createKeyCredential("teste1");

		mockAuthenticatedUserFromAnonymous();
		assertThrows(AppServiceUnauthorizedException.class,
				() -> projectService.createProjectKey(createdProject.getUuid(), pkToCreate));
	}

	@Test
	@DisplayName("Vérification de la non création d'une key en étant non authentifié")
	void createProjectKeyNoRightUnauthenticated() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);
		final ProjectKeyCredential pkToCreate = createKeyCredential("teste1");
		mockAuthenticatedWrongPwd();

		assertThrows(AppServiceUnauthorizedException.class,
				() -> projectService.createProjectKey(createdProject.getUuid(), pkToCreate));
	}

	@Test
	@DisplayName("Vérification de la non création d'une key pour un projet sur lequel on n'est pas owner")
	void createProjectKeyNoRightNotOwner() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);
		final ProjectKeyCredential pkToCreate = createKeyCredential("teste1");

		mockAuthenticatedUserNotOwner(UUID.randomUUID());
		assertThrows(AppServiceUnauthorizedException.class,
				() -> projectService.createProjectKey(createdProject.getUuid(), pkToCreate));
	}

	@Test
	@DisplayName("Vérification de la non suppression d'une key avec un project keystore inexistant")
	void deleteProjectKey() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);
		createdProject.setProjectStatus(ProjectStatus.VALIDATED);
		projectService.updateProject(createdProject);
		final ProjectKeystore pks = new ProjectKeystore().projectUuid(createdProject.getUuid()).uuid(UUID.randomUUID());

		when(aclHelper.searchProjectKeystores(any(), any())).thenReturn(new PageImpl<>(List.of(pks)));
		when(aclHelper.createProjectKeyStore(any())).thenReturn(pks);
		when(aclHelper.createProjectKey(any(), any())).thenReturn(createKeyCredential("teste1").getProjectKey());

		projectService.createProjectKey(createdProject.getUuid(), createKeyCredential("teste1"));

		projectService.deleteProjectKey(createdProject.getUuid(), UUID.randomUUID());
		verify(aclHelper).deleteProjectKey(any(), any());
	}

	@Test
	@DisplayName("Vérification de la non suppression d'une key avec un project keystore inexistant")
	void deleteProjectKeyThrow() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);
		createKeyCredential("teste1");

		assertThrows(AppServiceBadRequestException.class,
				() -> projectService.deleteProjectKey(createdProject.getUuid(), null));
	}

	@Test
	@DisplayName("Vérification de la non suppression d'une key en ayant seulement les droits Anonymous")
	void deleteProjectKeyNoRightAnonymous() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);
		createKeyCredential("teste1");

		mockAuthenticatedUserFromAnonymous();
		assertThrows(AppServiceUnauthorizedException.class,
				() -> projectService.deleteProjectKey(createdProject.getUuid(), UUID.randomUUID()));
	}

	@Test
	@DisplayName("Vérification de la non suppression d'une key en étant non authentifié")
	void deleteProjectKeyNoRightUnauthenticated() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);
		mockUnauthenticated();
		assertThrows(AppServiceUnauthorizedException.class,
				() -> projectService.deleteProjectKey(createdProject.getUuid(), UUID.randomUUID()));
	}

	@Test
	@DisplayName("Vérification de la non suppression d'une key pour un projet sur lequel on n'est pas owner")
	void deleteProjectKeyNoRightNotOwner() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);
		mockAuthenticatedUserNotOwner(UUID.randomUUID());
		assertThrows(AppServiceUnauthorizedException.class,
				() -> projectService.deleteProjectKey(createdProject.getUuid(), UUID.randomUUID()));
	}

	@Test
	@DisplayName("Teste la recherche des ProjectKey sans avoir les droits Anonymous")
	void searchProjectKeyNoRightAnonymous() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);
		createKeyCredential("teste1");

		mockAuthenticatedUserFromAnonymous();
		ProjectKeySearchCriteria projectKeySearchCriteria = new ProjectKeySearchCriteria()
				.projectUuid(createdProject.getUuid());
		assertThrows(AppServiceUnauthorizedException.class,
				() -> projectService.searchProjectKeys(projectKeySearchCriteria));
	}

	@Test
	@DisplayName("Teste la recherche des ProjectKey sans avoir les droits Unauthenticated")
	void searchProjectKeyNoRightUnauthenticated() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);
		createKeyCredential("teste1");

		mockUnauthenticated();
		ProjectKeySearchCriteria projectKeySearchCriteria = new ProjectKeySearchCriteria()
				.projectUuid(createdProject.getUuid());
		assertThrows(AppServiceUnauthorizedException.class,
				() -> projectService.searchProjectKeys(projectKeySearchCriteria));
	}

	@Test
	@DisplayName("Teste la recherche des ProjectKey sans avoir les droits Not Owner")
	void searchProjectKeyNoRightNotOwner() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		mockAuthenticatedUserToCreateProject(project);

		final Project createdProject = projectService.createProject(project);
		createKeyCredential("teste1");

		mockAuthenticatedUserNotOwner(UUID.randomUUID());
		ProjectKeySearchCriteria projectKeySearchCriteria = new ProjectKeySearchCriteria()
				.projectUuid(createdProject.getUuid());
		assertThrows(AppServiceUnauthorizedException.class,
				() -> projectService.searchProjectKeys(projectKeySearchCriteria));
	}

	@Test
	@DisplayName("Teste la recherche des ProjectKey")
	void searchProjectKeyThrow() throws IOException, AppServiceException {
		final Project project = jsonResourceReader.read(PROJET_LAMPADAIRES.getJsonPath(), Project.class);
		createEntities(project);
		mockAuthenticatedUserToCreateProject(project);
		ProjectKeySearchCriteria projectKeySearchCriteria = new ProjectKeySearchCriteria();
		assertThrows(AppServiceBadRequestException.class,
				() -> projectService.searchProjectKeys(projectKeySearchCriteria));
	}

	private ProjectKeyCredential createKeyCredential(String name) {
		ProjectKey projectKey = new ProjectKey();
		projectKey.setName(name);
		projectKey.setExpirationDate(LocalDateTime.now());
		ProjectKeyCredential projectKeyCredential = new ProjectKeyCredential().projectKey(projectKey)
				.password("pwd" + name);
		return projectKeyCredential;
	}

	@Data
	private static class KnownProject {
		private final String file;
		private final String title;

		String getJsonPath() {
			return "projects/" + file + ".json";
		}
	}

}
