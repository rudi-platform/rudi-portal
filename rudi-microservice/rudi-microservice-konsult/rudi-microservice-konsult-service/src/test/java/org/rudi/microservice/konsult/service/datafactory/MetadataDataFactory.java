package org.rudi.microservice.konsult.service.datafactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.rudi.common.core.json.JsonResourceReader;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.Connector;
import org.rudi.facet.kaccess.bean.Contact;
import org.rudi.facet.kaccess.bean.DictionaryEntry;
import org.rudi.facet.kaccess.bean.HashAlgorithm;
import org.rudi.facet.kaccess.bean.Language;
import org.rudi.facet.kaccess.bean.Licence;
import org.rudi.facet.kaccess.bean.LicenceStandard;
import org.rudi.facet.kaccess.bean.Media;
import org.rudi.facet.kaccess.bean.MediaFile;
import org.rudi.facet.kaccess.bean.MediaFileAllOfChecksum;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.MetadataAccessCondition;
import org.rudi.facet.kaccess.bean.MetadataMetadataInfo;
import org.rudi.facet.kaccess.bean.ReferenceDates;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import static org.awaitility.Awaitility.await;

/**
 * DataFactory centralisée pour la création et la réutilisation de métadonnées Dataverse.
 *
 * Principe : chaque basename produit un UUID déterministe.
 * Avant toute création, on vérifie dans Dataverse si le dataset existe déjà.
 * Ainsi, plusieurs classes de tests peuvent s'exécuter en parallèle sans créer de doublons
 * et sans se nuire mutuellement via des cleanups intempestifs.
 *
 * ⚠️ Pas de cleanup automatique : les données persistent entre les runs ce qui est voulu,
 * puisque les UUIDs sont déterministes → pas d'accumulation.
 */
@Component
@RequiredArgsConstructor
public class MetadataDataFactory {

	/** Namespace UUID pour générer des UUIDs déterministes. */
	private static final UUID TEST_NAMESPACE = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

	/** Tous les basenames connus (pour le nettoyage manuel si besoin). */
	private static final List<String> ALL_KNOWN_BASENAMES = List.of(
			"base.json",
			"base_children.json",
			"same-theme.json",
			"different-theme.json",
			"producer-rudi.json",
			"producer-ademe.json",
			"producer-ademe-0keyword.json",
			"producer-ademe-1keyword.json",
			"producer-ademe-2keywords.json",
			"producer-ademe-3keywords.json",
			"producer-ademe-children-0keyword",
			"producer-irisa.json",
			"producer-irisa-0keyword.json",
			"producer-rm.json",
			"producer-rm-0keyword.json"
	);

	private final JsonResourceReader jsonResourceReader = new JsonResourceReader();
	private final DatasetService datasetService;

	/** Cache mémoire : basename -> DOI (premier niveau, évite les appels Dataverse répétés). */
	private final Map<String, String> cache = new ConcurrentHashMap<>();

	/**
	 * Génère un UUID déterministe basé sur le basename.
	 * Même basename = même UUID, sur toutes les machines et tous les runs.
	 */
	private UUID deterministicUuid(String basename) {
		return UUID.nameUUIDFromBytes((TEST_NAMESPACE + ":" + basename).getBytes(StandardCharsets.UTF_8));
	}

	// -------------------------------------------------------------------------
	// API publique : getOrCreate par type de dataset
	// -------------------------------------------------------------------------

	public String getOrCreateBase() throws DataverseAPIException, IOException {
		return getOrCreateDataset("base.json");
	}

	public String getOrCreateBaseChildren() throws DataverseAPIException, IOException {
		return getOrCreateDataset("base_children.json");
	}

	public String getOrCreateSameTheme() throws DataverseAPIException, IOException {
		return getOrCreateDataset("same-theme.json");
	}

	public String getOrCreateDifferentTheme() throws DataverseAPIException, IOException {
		return getOrCreateDataset("different-theme.json");
	}

	public String getOrCreateProducerRudi() throws DataverseAPIException, IOException {
		return getOrCreateDataset("producer-rudi.json");
	}

	public String getOrCreateProducerAdeme() throws DataverseAPIException, IOException {
		return getOrCreateDataset("producer-ademe.json");
	}

	public String getOrCreateProducerAdeme0Keyword() throws DataverseAPIException, IOException {
		return getOrCreateDataset("producer-ademe-0keyword.json");
	}


	public String getOrCreateProducerAdemeChildren0Keyword() throws DataverseAPIException, IOException {
		return getOrCreateDataset("producer-ademe-children-0keyword.json");
	}

	public String getOrCreateProducerAdeme1Keyword() throws DataverseAPIException, IOException {
		return getOrCreateDataset("producer-ademe-1keyword.json");
	}

	public String getOrCreateProducerAdeme2Keywords() throws DataverseAPIException, IOException {
		return getOrCreateDataset("producer-ademe-2keywords.json");
	}

	public String getOrCreateProducerAdeme3Keywords() throws DataverseAPIException, IOException {
		return getOrCreateDataset("producer-ademe-3keywords.json");
	}

	public String getOrCreateProducerIrisa() throws DataverseAPIException, IOException {
		return getOrCreateDataset("producer-irisa.json");
	}

	public String getOrCreateProducerIrisa0Keyword() throws DataverseAPIException, IOException {
		return getOrCreateDataset("producer-irisa-0keyword.json");
	}

	public String getOrCreateProducerRm() throws DataverseAPIException, IOException {
		return getOrCreateDataset("producer-rm.json");
	}

	public String getOrCreateProducerRm0Keyword() throws DataverseAPIException, IOException {
		return getOrCreateDataset("producer-rm-0keyword.json");
	}

	// -------------------------------------------------------------------------
	// Logique interne
	// -------------------------------------------------------------------------

	/**
	 * Récupère ou crée un dataset par basename.
	 *
	 * Ordre de priorité :
	 * 1. Cache mémoire (rapide, évite un appel Dataverse)
	 * 2. Dataverse (le dataset existe peut-être d'un run précédent ou d'une autre classe de test)
	 * 3. Création si inexistant
	 */
	private String getOrCreateDataset(String basename) throws DataverseAPIException, IOException {
		// 1. Cache mémoire
		if (cache.containsKey(basename)) {
			return cache.get(basename);
		}

		UUID globalId = deterministicUuid(basename);

		// 2. Vérification dans Dataverse
		try {
			Metadata existing = datasetService.getDataset(globalId);
			String doi = existing.getDataverseDoi();
			cache.put(basename, doi);
			return doi;
		} catch (Exception notFound) {
			// Dataset inexistant dans Dataverse → on le crée
		}

		// 3. Création
		final var metadata = jsonResourceReader.read("metadataWithSameThemeFinder/" + basename, Metadata.class);
		fillRequiredFields(metadata, globalId);

		final var doi = datasetService.createDataset(metadata);

		await().timeout(10, TimeUnit.SECONDS)
				.pollInterval(Duration.of(1, ChronoUnit.SECONDS))
				.until(() -> datasetService.getDataset(doi) != null);

		cache.put(basename, doi);
		return doi;
	}

	private void fillRequiredFields(Metadata metadata, UUID globalId) {
		final var dates = new ReferenceDates()
				.created(OffsetDateTime.now())
				.validated(OffsetDateTime.now())
				.published(OffsetDateTime.now())
				.updated(OffsetDateTime.now());

		metadata
				.globalId(globalId)
				.synopsis(Collections.singletonList(
						new DictionaryEntry().lang(Language.FR_FR).text("Synopsis obligatoire.")))
				.datasetDates(dates)
				.storageStatus(Metadata.StorageStatusEnum.ONLINE)
				.metadataInfo(new MetadataMetadataInfo().apiVersion("1.4.2").metadataDates(dates))
				.availableFormats(Collections.singletonList(
						new MediaFile()
								.fileType("text/csv")
								.fileSize(50L)
								.checksum(new MediaFileAllOfChecksum().algo(HashAlgorithm.MD5).hash("SHA-256"))
								.mediaType(Media.MediaTypeEnum.FILE)
								.mediaId(UUID.randomUUID())
								.mediaCaption("Média obligatoire pour swagger")
								.connector(new Connector()
										.url("www.connector1.org")
										.interfaceContract("interface contrat 1"))))
				.contacts(Collections.singletonList(
						new Contact()
								.contactId(UUID.randomUUID())
								.contactName("Nom du contact obligatoire")
								.email("contact_test@rudi.fr")))
				.accessCondition(new MetadataAccessCondition()
						.licence(new LicenceStandard()
								.licenceLabel(LicenceStandard.LicenceLabelEnum.APACHE_2_0)
								.licenceType(Licence.LicenceTypeEnum.STANDARD)));
	}

	// -------------------------------------------------------------------------
	// Nettoyage MANUEL uniquement (ne jamais appeler depuis @BeforeAll/@AfterAll)
	// -------------------------------------------------------------------------

	/**
	 * Supprime tous les datasets connus de Dataverse.
	 * ⚠️ À appeler MANUELLEMENT uniquement, jamais depuis un @BeforeAll/@AfterAll automatique
	 * car cela briserait les tests s'exécutant en parallèle.
	 */
	public void cleanupAll() {
		for (final var basename : ALL_KNOWN_BASENAMES) {
			try {
				datasetService.deleteDataset(deterministicUuid(basename));
			} catch (Exception ignored) {
				// Dataset inexistant ou déjà supprimé
			}
		}
		cache.clear();
	}
}
