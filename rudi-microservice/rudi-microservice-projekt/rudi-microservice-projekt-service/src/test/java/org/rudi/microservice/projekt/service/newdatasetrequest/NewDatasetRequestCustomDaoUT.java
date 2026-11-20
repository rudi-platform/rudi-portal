package org.rudi.microservice.projekt.service.newdatasetrequest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rudi.microservice.projekt.core.bean.criteria.NewDatasetRequestSearchCriteria;
import org.rudi.microservice.projekt.service.ProjectSpringBootTest;
import org.rudi.microservice.projekt.service.datafactory.NewDatasetRequestDataFactory;
import org.rudi.microservice.projekt.service.datafactory.ProjectDataFactory;
import org.rudi.microservice.projekt.storage.dao.newdatasetrequest.NewDatasetRequestCustomDao;
import org.rudi.microservice.projekt.storage.entity.OwnerType;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestEntity;
import org.rudi.microservice.projekt.storage.entity.newdatasetrequest.NewDatasetRequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;

/**
 * Tests unitaires des critères de recherche de {@code NewDatasetRequestCustomDaoImpl}.
 *
 * <p>
 * Le DAO expose trois prédicats combinables :
 * <ul>
 * <li>{@code titleDescription} : LIKE insensible à la casse sur le titre OU la description ;</li>
 * <li>{@code status} : filtre IN sur le statut métier de la demande ;</li>
 * <li>{@code projectOwnerUuids} : ne conserve que les demandes rattachées à un projet dont le propriétaire figure dans
 * la liste.</li>
 * </ul>
 * Chaque prédicat est couvert isolément (branche renseignée) et le cas « aucun critère » valide la branche vide.
 *
 * <p>
 * Tests réentrants : aucune donnée partagée n'est recréée ni purgée entre les tests (pas de {@code @BeforeEach} /
 * {@code @AfterEach} destructeur). Chaque test crée ses propres données avec des valeurs déterministes et filtre ses
 * assertions sur les identifiants qu'il a lui-même créés, pour rester indépendant de l'ordre d'exécution et des
 * éventuelles données résiduelles.
 */
@ProjectSpringBootTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class NewDatasetRequestCustomDaoUT {

	private final NewDatasetRequestCustomDao newDatasetRequestCustomDao;
	private final NewDatasetRequestDataFactory newDatasetRequestDataFactory;
	private final ProjectDataFactory projectDataFactory;

	private NewDatasetRequestSearchCriteria emptyCriteria() {
		return new NewDatasetRequestSearchCriteria();
	}

	/**
	 * Convertit les statuts entité en statuts du bean de recherche (OpenAPI), les deux énumérations partageant les
	 * mêmes constantes.
	 */
	private List<org.rudi.microservice.projekt.core.bean.NewDatasetRequestStatus> toCriteriaStatus(
			NewDatasetRequestStatus... statuses) {
		return java.util.Arrays.stream(statuses)
				.map(status -> org.rudi.microservice.projekt.core.bean.NewDatasetRequestStatus.valueOf(status.name()))
				.collect(java.util.stream.Collectors.toList());
	}

	private List<NewDatasetRequestEntity> search(NewDatasetRequestSearchCriteria criteria) {
		final Page<NewDatasetRequestEntity> page = newDatasetRequestCustomDao.searchNewDatasetRequest(criteria,
				Pageable.unpaged());
		return page.getContent();
	}

	@Test
	@DisplayName("Aucun critère : la branche vide ne filtre pas et retourne les demandes créées")
	void test_searchNewDatasetRequest_withoutCriteria_returnsCreated() {
		final NewDatasetRequestEntity first = newDatasetRequestDataFactory.createPersisted("Capteurs qualité air",
				"Mesures de pollution", NewDatasetRequestStatus.DRAFT);
		final NewDatasetRequestEntity second = newDatasetRequestDataFactory.createPersisted("Pistes cyclables",
				"Tracés vélo", NewDatasetRequestStatus.IN_PROGRESS);

		final List<NewDatasetRequestEntity> results = search(emptyCriteria());

		// Aucun critère : on ne compte pas le total global (données résiduelles possibles),
		// on vérifie seulement que les demandes créées par ce test sont bien présentes.
		assertThat(results).extracting(NewDatasetRequestEntity::getUuid)
				.contains(first.getUuid(), second.getUuid());
	}

	@Test
	@DisplayName("Critère titleDescription : correspondance sur le titre (via wildcard, sensible à la casse)")
	void test_searchNewDatasetRequest_byTitle_matchesOnTitle() {
		// Jeton unique dans le libellé : garantit qu'aucune donnée résiduelle ne matche le critère.
		final String token = "CAPTEURS-" + UUID.randomUUID();
		final NewDatasetRequestEntity expected = newDatasetRequestDataFactory.createPersisted(token + " qualité air",
				"Mesures de pollution", NewDatasetRequestStatus.DRAFT);
		newDatasetRequestDataFactory.createPersisted("Pistes cyclables", "Tracés vélo",
				NewDatasetRequestStatus.DRAFT);

		final NewDatasetRequestSearchCriteria criteria = emptyCriteria();
		criteria.setTitleDescription("*" + token + "*");

		final List<NewDatasetRequestEntity> results = search(criteria);

		assertThat(results).extracting(NewDatasetRequestEntity::getUuid).containsExactly(expected.getUuid());
	}

	@Test
	@DisplayName("Critère titleDescription : correspondance sur la description")
	void test_searchNewDatasetRequest_byDescription_matchesOnDescription() {
		final String token = "MOBILITE-" + UUID.randomUUID();
		final NewDatasetRequestEntity expected = newDatasetRequestDataFactory.createPersisted("Pistes cyclables",
				"Données relatives à la " + token, NewDatasetRequestStatus.DRAFT);
		newDatasetRequestDataFactory.createPersisted("Capteurs qualité air", "Mesures de pollution",
				NewDatasetRequestStatus.DRAFT);

		final NewDatasetRequestSearchCriteria criteria = emptyCriteria();
		criteria.setTitleDescription("*" + token + "*");

		final List<NewDatasetRequestEntity> results = search(criteria);

		assertThat(results).extracting(NewDatasetRequestEntity::getUuid).containsExactly(expected.getUuid());
	}

	@Test
	@DisplayName("Critère titleDescription : aucune correspondance renvoie une liste vide")
	void test_searchNewDatasetRequest_byTitleDescription_noMatch() {
		final String token = "INEXISTANT-" + UUID.randomUUID();
		newDatasetRequestDataFactory.createPersisted("Capteurs qualité air", "Mesures de pollution",
				NewDatasetRequestStatus.DRAFT);

		final NewDatasetRequestSearchCriteria criteria = emptyCriteria();
		criteria.setTitleDescription("*" + token + "*");

		final List<NewDatasetRequestEntity> results = search(criteria);

		assertThat(results).isEmpty();
	}

	@Test
	@DisplayName("Critère status : filtre sur un unique statut")
	void test_searchNewDatasetRequest_byStatus_singleStatus() {
		// Un propriétaire local unique isole les demandes du test : le filtre statut est combiné à projectOwnerUuids.
		final UUID ownerUuid = UUID.randomUUID();
		final NewDatasetRequestEntity validated = newDatasetRequestDataFactory.createNotPersisted("Demande validée",
				"Description", NewDatasetRequestStatus.VALIDATED);
		final NewDatasetRequestEntity draft = newDatasetRequestDataFactory.createNotPersisted("Demande brouillon",
				"Description", NewDatasetRequestStatus.DRAFT);
		projectDataFactory.createProjectWithRequests(ownerUuid, OwnerType.USER, validated, draft);

		final NewDatasetRequestSearchCriteria criteria = emptyCriteria();
		criteria.setStatus(toCriteriaStatus(NewDatasetRequestStatus.VALIDATED));
		criteria.setProjectOwnerUuids(List.of(ownerUuid));

		final List<NewDatasetRequestEntity> results = search(criteria);

		assertThat(results).extracting(NewDatasetRequestEntity::getUuid).containsExactly(validated.getUuid());
	}

	@Test
	@DisplayName("Critère status : filtre sur plusieurs statuts")
	void test_searchNewDatasetRequest_byStatus_multipleStatuses() {
		final UUID ownerUuid = UUID.randomUUID();
		final NewDatasetRequestEntity draft = newDatasetRequestDataFactory.createNotPersisted("Demande brouillon",
				"Description", NewDatasetRequestStatus.DRAFT);
		final NewDatasetRequestEntity inProgress = newDatasetRequestDataFactory.createNotPersisted("Demande en cours",
				"Description", NewDatasetRequestStatus.IN_PROGRESS);
		final NewDatasetRequestEntity refused = newDatasetRequestDataFactory.createNotPersisted("Demande refusée",
				"Description", NewDatasetRequestStatus.REFUSED);
		projectDataFactory.createProjectWithRequests(ownerUuid, OwnerType.USER, draft, inProgress, refused);

		final NewDatasetRequestSearchCriteria criteria = emptyCriteria();
		criteria.setStatus(toCriteriaStatus(NewDatasetRequestStatus.DRAFT, NewDatasetRequestStatus.IN_PROGRESS));
		criteria.setProjectOwnerUuids(List.of(ownerUuid));

		final List<NewDatasetRequestEntity> results = search(criteria);

		assertThat(results).extracting(NewDatasetRequestEntity::getUuid)
				.containsExactlyInAnyOrder(draft.getUuid(), inProgress.getUuid());
	}

	@Test
	@DisplayName("Critère status : liste vide ne filtre pas")
	void test_searchNewDatasetRequest_byEmptyStatusList_returnsCreated() {
		final NewDatasetRequestEntity draft = newDatasetRequestDataFactory.createPersisted("Demande brouillon",
				"Description", NewDatasetRequestStatus.DRAFT);
		final NewDatasetRequestEntity inProgress = newDatasetRequestDataFactory.createPersisted("Demande en cours",
				"Description", NewDatasetRequestStatus.IN_PROGRESS);

		final NewDatasetRequestSearchCriteria criteria = emptyCriteria();
		criteria.setStatus(java.util.Collections.emptyList());

		final List<NewDatasetRequestEntity> results = search(criteria);

		// Liste de statuts vide = pas de filtre : on vérifie la présence des demandes créées, sans comptage global.
		assertThat(results).extracting(NewDatasetRequestEntity::getUuid)
				.contains(draft.getUuid(), inProgress.getUuid());
	}

	@Test
	@DisplayName("Critère projectOwnerUuids : ne conserve que les demandes rattachées aux projets du propriétaire")
	void test_searchNewDatasetRequest_byProjectOwnerUuids_filtersByOwner() {
		// UUIDs propriétaires locaux : valeurs uniques (non assertées) qui isolent les données de ce test.
		final UUID owner1Uuid = UUID.randomUUID();
		final UUID owner2Uuid = UUID.randomUUID();
		final NewDatasetRequestEntity requestOwner1 = newDatasetRequestDataFactory
				.createNotPersisted("Demande owner 1", "Description owner 1", NewDatasetRequestStatus.DRAFT);
		final NewDatasetRequestEntity requestOwner2 = newDatasetRequestDataFactory
				.createNotPersisted("Demande owner 2", "Description owner 2", NewDatasetRequestStatus.DRAFT);

		projectDataFactory.createProjectWithRequests(owner1Uuid, OwnerType.USER, requestOwner1);
		projectDataFactory.createProjectWithRequests(owner2Uuid, OwnerType.USER, requestOwner2);

		final NewDatasetRequestSearchCriteria criteria = emptyCriteria();
		criteria.setProjectOwnerUuids(List.of(owner1Uuid));

		final List<NewDatasetRequestEntity> results = search(criteria);

		assertThat(results).extracting(NewDatasetRequestEntity::getTitle).containsExactly("Demande owner 1");
	}

	@Test
	@DisplayName("Critère projectOwnerUuids : plusieurs propriétaires")
	void test_searchNewDatasetRequest_byProjectOwnerUuids_multipleOwners() {
		final UUID owner1Uuid = UUID.randomUUID();
		final UUID owner2Uuid = UUID.randomUUID();
		final NewDatasetRequestEntity requestOwner1 = newDatasetRequestDataFactory
				.createNotPersisted("Demande owner 1", "Description owner 1", NewDatasetRequestStatus.DRAFT);
		final NewDatasetRequestEntity requestOwner2 = newDatasetRequestDataFactory
				.createNotPersisted("Demande owner 2", "Description owner 2", NewDatasetRequestStatus.DRAFT);

		projectDataFactory.createProjectWithRequests(owner1Uuid, OwnerType.USER, requestOwner1);
		projectDataFactory.createProjectWithRequests(owner2Uuid, OwnerType.USER, requestOwner2);

		final NewDatasetRequestSearchCriteria criteria = emptyCriteria();
		criteria.setProjectOwnerUuids(List.of(owner1Uuid, owner2Uuid));

		final List<NewDatasetRequestEntity> results = search(criteria);

		assertThat(results).extracting(NewDatasetRequestEntity::getTitle)
				.containsExactlyInAnyOrder("Demande owner 1", "Demande owner 2");
	}

	@Test
	@DisplayName("Critère projectOwnerUuids : propriétaire inconnu renvoie une liste vide")
	void test_searchNewDatasetRequest_byProjectOwnerUuids_unknownOwner() {
		final UUID owner1Uuid = UUID.randomUUID();
		final UUID unknownOwnerUuid = UUID.randomUUID();
		final NewDatasetRequestEntity request = newDatasetRequestDataFactory.createNotPersisted("Demande owner 1",
				"Description owner 1", NewDatasetRequestStatus.DRAFT);
		projectDataFactory.createProjectWithRequests(owner1Uuid, OwnerType.USER, request);

		final NewDatasetRequestSearchCriteria criteria = emptyCriteria();
		criteria.setProjectOwnerUuids(List.of(unknownOwnerUuid));

		final List<NewDatasetRequestEntity> results = search(criteria);

		assertThat(results).isEmpty();
	}

	@Test
	@DisplayName("Combinaison des critères : titleDescription + status + projectOwnerUuids")
	void test_searchNewDatasetRequest_allCriteriaCombined() {
		final UUID owner1Uuid = UUID.randomUUID();
		final UUID owner2Uuid = UUID.randomUUID();
		final NewDatasetRequestEntity expected = newDatasetRequestDataFactory
				.createNotPersisted("Capteurs qualité air", "Mesures de pollution",
						NewDatasetRequestStatus.IN_PROGRESS);
		// même propriétaire mais statut différent → exclu par le critère status
		final NewDatasetRequestEntity wrongStatus = newDatasetRequestDataFactory
				.createNotPersisted("Capteurs qualité air", "Mesures de pollution", NewDatasetRequestStatus.DRAFT);
		// bon statut et bon titre mais autre propriétaire → exclu par projectOwnerUuids
		final NewDatasetRequestEntity wrongOwner = newDatasetRequestDataFactory
				.createNotPersisted("Capteurs qualité air", "Mesures de pollution",
						NewDatasetRequestStatus.IN_PROGRESS);

		projectDataFactory.createProjectWithRequests(owner1Uuid, OwnerType.USER, expected, wrongStatus);
		projectDataFactory.createProjectWithRequests(owner2Uuid, OwnerType.USER, wrongOwner);

		final NewDatasetRequestSearchCriteria criteria = emptyCriteria();
		criteria.setTitleDescription("*Capteurs*");
		criteria.setStatus(toCriteriaStatus(NewDatasetRequestStatus.IN_PROGRESS));
		criteria.setProjectOwnerUuids(List.of(owner1Uuid));

		final List<NewDatasetRequestEntity> results = search(criteria);

		assertThat(results).extracting(NewDatasetRequestEntity::getUuid).containsExactly(expected.getUuid());
	}
}
