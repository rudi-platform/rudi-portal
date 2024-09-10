package org.rudi.microservice.acl.service.projectkey;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.common.core.json.JsonResourceReader;
import org.rudi.common.service.exception.AppServiceBadRequestException;
import org.rudi.common.service.exception.AppServiceException;
import org.rudi.microservice.acl.core.bean.ProjectKey;
import org.rudi.microservice.acl.core.bean.ProjectKeystore;
import org.rudi.microservice.acl.core.bean.ProjectKeystoreSearchCriteria;
import org.rudi.microservice.acl.service.AclSpringBootTest;
import org.rudi.microservice.acl.storage.dao.projectkey.ProjectKeystoreDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@AclSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProjectKeystoreServiceUT {

	private final ProjectKeystoreService projectKeystoreService;
	private final ProjectKeystoreDao projectKeystoreDao;
	private final JsonResourceReader jsonResourceReader;

	private static final String JSON_CREATE = "projectkey/create-null-uuid.json";

	@AfterEach
	void tearDown() {
		projectKeystoreDao.deleteAll();
	}

	@Test
	@DisplayName("Teste de la création d'un ProjectKeystore avec un UUID")
	void createProjectKeystoreWithUUID() throws IOException, AppServiceException {
		final ProjectKeystore projectKeystoreToCreate = jsonResourceReader.read(JSON_CREATE, ProjectKeystore.class);
		final UUID uuid = UUID.randomUUID();
		projectKeystoreToCreate.setUuid(uuid);
		final ProjectKeystore projectKeystoreCreated = projectKeystoreService.createProjectKeystore(projectKeystoreToCreate);

		assertThat(projectKeystoreCreated.getProjectUuid())
				.as("L'UUID ne doit pas être égal à celui donné lors de la création")
				.isNotEqualTo(uuid);

		assertThat(projectKeystoreCreated.getProjectUuid())
				.as("L'UUID du projet est le même que celui donné à la création")
				.isEqualTo(projectKeystoreToCreate.getProjectUuid());
	}

	@Test
	@DisplayName("Teste de la création d'un ProjectKeystore sans UUID")
	void createProjectKeystoreWithoutUUID() throws IOException, AppServiceException {
		final ProjectKeystore projectKeystoreToCreate = jsonResourceReader.read(JSON_CREATE, ProjectKeystore.class);
		final ProjectKeystore projectKeystoreCreated = projectKeystoreService.createProjectKeystore(projectKeystoreToCreate);

		assertThat(projectKeystoreCreated.getProjectUuid())
				.as("L'UUID du projet est le même que celui donné à la création")
				.isEqualTo(projectKeystoreToCreate.getProjectUuid());
	}

	@Test
	@DisplayName("Teste création vérification unicité du project UUID")
	void createProjectKeystore() throws IOException, AppServiceException {
		final ProjectKeystore projectKeystoreToCreate = jsonResourceReader.read(JSON_CREATE, ProjectKeystore.class);
		final UUID uuid = UUID.randomUUID();
		projectKeystoreToCreate.setProjectUuid(uuid);
		projectKeystoreService.createProjectKeystore(projectKeystoreToCreate);

		assertThatThrownBy(() -> projectKeystoreService.createProjectKeystore(projectKeystoreToCreate))
				.as("On ne peut pas créer un project keystore pour un projet qui en possède déjà un")
				.isInstanceOf(AppServiceException.class)
				.hasMessage("Il existe déjà un Project Keystore pour ce Projet");
	}

	@Test
	@DisplayName("Teste la suppression d'un project keystore")
	void deleteProjectKeystore() throws AppServiceException, IOException {
		final ProjectKeystore projectKeystoreToCreate = jsonResourceReader.read(JSON_CREATE, ProjectKeystore.class);
		final ProjectKeystore projectKeystoreCreated = projectKeystoreService.createProjectKeystore(projectKeystoreToCreate);
		final Pageable pageable = Pageable.unpaged();
		final ProjectKeystoreSearchCriteria searchCriteria = ProjectKeystoreSearchCriteria.builder().projectUuids(List.of(projectKeystoreToCreate.getProjectUuid())).build();
		projectKeystoreCreated.setProjectKeys(List.of());

		assertThat(projectKeystoreService.searchProjectKeys(searchCriteria, pageable))
				.as("Le project keystore est bien créé")
				.containsOnly(projectKeystoreCreated);

		projectKeystoreService.deleteProjectKeystore(projectKeystoreCreated.getUuid());

		assertThat(projectKeystoreService.searchProjectKeys(searchCriteria, pageable))
				.as("Le project keystore n'est plus présent car supprimé")
				.isEmpty();
	}

	@Test
	@DisplayName("Teste la suppression d'un project keystore inexistant")
	void deleteProjectKeystoreThrowException() {
		final UUID uuid = UUID.randomUUID();

		assertThatThrownBy(() -> projectKeystoreService.deleteProjectKeystore(uuid))
				.as("On ne peut pas supprimer un project keystore inexistant")
				.isInstanceOf(AppServiceBadRequestException.class)
				.hasMessage("Invalid projet keystore uuid");
	}

	@Test
	@DisplayName("Teste la récupération d'un project keystore")
	void getProjectKeystoreByUUID() throws IOException, AppServiceException {
		final ProjectKeystore projectKeystoreToCreate = jsonResourceReader.read(JSON_CREATE, ProjectKeystore.class);
		final UUID uuid = UUID.randomUUID();
		projectKeystoreToCreate.setProjectUuid(uuid);
		final ProjectKeystore projectKeystoreCreated = projectKeystoreService.createProjectKeystore(projectKeystoreToCreate);
		projectKeystoreCreated.setProjectKeys(List.of());

		assertThat(projectKeystoreService.getProjectKeystoreByUUID(projectKeystoreCreated.getUuid()))
				.as("On récupère bien le project keystore qui vient d'être créé")
				.isEqualTo(projectKeystoreCreated);
	}


	@Test
	@DisplayName("Teste la récupération d'un project keystore inexistant")
	void getProjectKeystoreByUUIDThrowException() {
		final UUID uuid = UUID.randomUUID();

		assertThatThrownBy(() -> projectKeystoreService.getProjectKeystoreByUUID(uuid))
				.as("Une erreur est levé car project keystore inexistant")
				.isInstanceOf(AppServiceBadRequestException.class)
				.hasMessage("Invalid projet keystore uuid");
	}

	@Test
	@DisplayName("Teste la création d'une clé dans project keystore")
	void createProjectKey() throws AppServiceException, IOException {
		final ProjectKeystore projectKeystoreToCreate = jsonResourceReader.read(JSON_CREATE, ProjectKeystore.class);
		final ProjectKeystore projectKeystore = projectKeystoreService.createProjectKeystore(projectKeystoreToCreate);
		projectKeystore.setProjectKeys(List.of());

		LocalDateTime expirationData = LocalDateTime.now().plusYears(1);
		LocalDateTime beforeCreationData = LocalDateTime.now();
		String name = "name";
		ProjectKey projectKeyToCreate = createKey(name, expirationData);

		ProjectKey projectKeyCreated = projectKeystoreService.createProjectKey(projectKeystore.getUuid(), projectKeyToCreate);

		assertThat(projectKeyCreated.getExpirationDate())
				.as("La date d'expiration est bien celle donné en paramètre")
				.isEqualTo(expirationData);

		assertThat(projectKeyCreated.getCreationDate())
				.as("La date de création est avant la date de maintenant")
				.isAfter(beforeCreationData)
				.isBefore(LocalDateTime.now());

		assertThat(projectKeyCreated.getName())
				.as("Le nom est bien celui passé lors de la création")
				.isEqualTo(name);

		assertThat(projectKeyCreated.getClientId())
				.as("Le client ID n'est pas null")
				.isNotNull()
				.as("Le client ID n'est pas vide")
				.isNotEmpty();


	}


	@Test
	@DisplayName("Teste la recherche des clé dans un project keystore")
	void searchProjectKeys() throws AppServiceException, IOException {
		final ProjectKeystore projectKeystoreToCreate1 = jsonResourceReader.read(JSON_CREATE, ProjectKeystore.class);
		final ProjectKeystore projectKeystore1 = projectKeystoreService.createProjectKeystore(projectKeystoreToCreate1);
		projectKeystore1.setProjectKeys(List.of());

		final ProjectKeystore projectKeystoreToCreate2 = jsonResourceReader.read(JSON_CREATE, ProjectKeystore.class);
		projectKeystoreToCreate2.setProjectUuid(UUID.randomUUID());
		final ProjectKeystore projectKeystore2 = projectKeystoreService.createProjectKeystore(projectKeystoreToCreate2);
		projectKeystore2.setProjectKeys(List.of());

		String name1 = "name1";
		String name2 = "name2";
		String name3 = "name3";
		String name4 = "name4";

		projectKeystoreService.createProjectKey(projectKeystore1.getUuid(), createKey(name1));
		projectKeystoreService.createProjectKey(projectKeystore1.getUuid(), createKey(name2));
		projectKeystoreService.createProjectKey(projectKeystore2.getUuid(), createKey(name3));
		projectKeystoreService.createProjectKey(projectKeystore2.getUuid(), createKey(name4));

		final Pageable pageable = Pageable.unpaged();
		final ProjectKeystoreSearchCriteria searchCriteria = ProjectKeystoreSearchCriteria.builder().projectUuids(List.of(projectKeystore1.getProjectUuid())).build();

		assertThat(projectKeystoreService.searchProjectKeys(searchCriteria, pageable))
				.extracting(ProjectKeystore::getUuid)
				.isEqualTo(List.of(projectKeystore1.getUuid()));

		assertThat(projectKeystoreService.searchProjectKeys(searchCriteria, pageable))
				.as("On ne retrouve que les ProjectKeys qui ont été ajouté au keystore que l'on recherche")
				.flatExtracting(ProjectKeystore::getProjectKeys)
				.extracting(ProjectKey::getName)
				.containsOnly(name1, name2)
				.as("On ne retrouve pas les projectKeys qui ne sont pas présent dans le keystore")
				.doesNotContain(name3, name4);
	}

	@Test
	@DisplayName("Teste la suppression d'une clé dans un project keystore")
	void deleteProjectKey() throws IOException, AppServiceException {
		final ProjectKeystore projectKeystoreToCreate1 = jsonResourceReader.read(JSON_CREATE, ProjectKeystore.class);
		final ProjectKeystore projectKeystore1 = projectKeystoreService.createProjectKeystore(projectKeystoreToCreate1);
		projectKeystore1.setProjectKeys(List.of());

		final Pageable pageable = Pageable.unpaged();
		final ProjectKeystoreSearchCriteria searchCriteria = ProjectKeystoreSearchCriteria.builder().projectUuids(List.of(projectKeystore1.getProjectUuid())).build();

		assertThat(projectKeystoreService.searchProjectKeys(searchCriteria, pageable))
				.as("On ne retrouve que les ProjectKeys qui ont été ajouté au keystore que l'on recherche")
				.flatExtracting(ProjectKeystore::getProjectKeys)
				.extracting(ProjectKey::getName)
				.isEmpty();

		String name1 = "name1";
		String name2 = "name2";

		ProjectKey pk1 = projectKeystoreService.createProjectKey(projectKeystore1.getUuid(), createKey(name1));
		projectKeystoreService.createProjectKey(projectKeystore1.getUuid(), createKey(name2));

		assertThat(projectKeystoreService.searchProjectKeys(searchCriteria, pageable))
				.as("On ne retrouve que les ProjectKeys qui ont été ajouté au keystore que l'on recherche")
				.flatExtracting(ProjectKeystore::getProjectKeys)
				.extracting(ProjectKey::getName)
				.containsOnly(name1, name2);

		projectKeystoreService.deleteProjectKey(projectKeystore1.getUuid(), pk1.getUuid());

		assertThat(projectKeystoreService.searchProjectKeys(searchCriteria, pageable))
				.as("On ne retrouve que les ProjectKeys qui ont été ajouté au keystore que l'on recherche")
				.flatExtracting(ProjectKeystore::getProjectKeys)
				.extracting(ProjectKey::getName)
				.containsOnly(name2)
				.as("On ne retrouve pas le ProjectKeys qui a été supprimé")
				.doesNotContain(name1);

	}

	@Test
	@DisplayName("Teste la suppression d'une clé dans un project keystore inexistant")
	void deleteProjectKeyThrowException() {
		assertThatThrownBy(() -> projectKeystoreService.deleteProjectKey(UUID.randomUUID(), UUID.randomUUID()))
				.as("Une erreur est levé car project keystore inexistant")
				.isInstanceOf(AppServiceBadRequestException.class)
				.hasMessage("Invalid projet keystore uuid");
	}

	private ProjectKey createKey(String name, LocalDateTime expirationData) {
		ProjectKey projectKey = new ProjectKey();
		projectKey.setName(name);
		projectKey.setExpirationDate(expirationData);
		return projectKey;
	}

	private ProjectKey createKey(String name) {
		ProjectKey projectKey = new ProjectKey();
		projectKey.setName(name);
		projectKey.setExpirationDate(LocalDateTime.now().plusYears(1));
		return projectKey;
	}

}
