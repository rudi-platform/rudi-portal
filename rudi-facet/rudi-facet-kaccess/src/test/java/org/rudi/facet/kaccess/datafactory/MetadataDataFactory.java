package org.rudi.facet.kaccess.datafactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.Contact;
import org.rudi.facet.kaccess.bean.DictionaryEntry;
import org.rudi.facet.kaccess.bean.Language;
import org.rudi.facet.kaccess.bean.Media;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.facet.kaccess.bean.Organization;
import org.rudi.facet.kaccess.bean.RichDictionaryEntry;
import org.rudi.facet.kaccess.service.dataset.DatasetService;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static org.awaitility.Awaitility.await;

/**
 * DataFactory de métadonnées de test pour Dataverse.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetadataDataFactory {
	private static final String DOI_PREFIX = "doi:";

	private static final String LABEL_AGRI = "agri";
	private static final String LABEL_JDD_AVEC_SEPARATEURS = "jdd-avec-separateurs";
	private static final String LABEL_JDD_OUVERT = "jdd-ouvert";
	private static final String LABEL_MINIMAL = "minimal";
	private static final String LABEL_EXISTING_DATASET = "existing_dataset";
	private static final String LABEL_JDD_SANS_MOTS_COMMUNS_AVEC_JDD_AVEC_SEPARATEURS =
			"jdd-sans-mots-communs-avec-jdd-avec-separateurs";
	private static final String LABEL_JDD_PRODUCER_KEOLIS_STAR_THEME_AGRICULTURE =
			"jdd_producer_keolis_star_theme_agriculture";
	private static final String LABEL_JDD_PRODUCER_STAR_THEME_AGRICULTURE_BIOLOGIQUE =
			"jdd_producer_star_theme_agriculture_biologique";
	private static final String LABEL_JDD_TO_UPDATE = "jdd_to_update";
	private static final String LABEL_METADATA_WITHOUT_REFERENCE_DATES_UPDATED =
			"metadata_without_reference_dates_updated";
	private static final String LABEL_TRANSPORT = "transport";
	private static final String LANGUAGE_CS_CZ = "cs-CZ";
	private static final String LOREM_SUMMARY_TEXT =
			"Has autem provincias, quas Orontes ambiens amnis imosque pedes Cassii montis illius celsi praetermeans funditur in Parthenium mare, Gnaeus Pompeius superato Tigrane regnis Armeniorum abstractas dicioni Romanae coniunxit. Batnae municipium in Anthemusia conditum Macedonum manu priscorum ab Euphrate flumine brevi spatio disparatur, refertum mercatoribus opulentis, ubi annua sollemnitate prope Septembris initium mensis ad nundinas magna promiscuae fortunae convenit multitudo ad commercanda quae Indi mittunt et Seres aliaque plurima vehi terra marique consueta. Ardeo, mihi credite, Patres conscripti (id quod vosmet de me existimatis et facitis ipsi) incredibili quodam amore patriae, qui me amor et subvenire olim impendentibus periculis maximis cum dimicatione capitis, et rursum, cum omnia tela undique esse intenta in patriam viderem, subire coegit atque excipere unum pro universis. Hic me meus in rem publicam animus pristinus ac perennis cum C. Caesare reducit, reconciliat, restituit in gratiam. Principium autem unde latius se funditabat, emersit ex negotio tali. Chilo ex vicario et coniux eius Maxima nomine, questi apud Olybrium ea tempestate urbi praefectum, vitamque suam venenis petitam adseverantes inpetrarunt ut hi, quos suspectati sunt, ilico rapti conpingerentur in vincula, organarius Sericus et Asbolius palaestrita et aruspex Campensis. Sed quid est quod in hac causa maxime homines admirentur et reprehendant meum consilium, cum ego idem antea multa decreverim, que magis ad hominis dignitatem quam ad rei publicae necessitatem pertinerent? Supplicationem quindecim dierum decrevi sententia mea. Rei publicae satis erat tot dierum quot C. Mario ; dis immortalibus non erat exigua eadem gratulatio quae ex maximis bellis. Ergo ille cumulus dierum hominis est dignitati tributus.";

	private static final MetadataIdentifiers IDENTIFIERS_AGRI = new MetadataIdentifiers(
			UUID.fromString("f382ec4f-034b-411d-aab7-5b97a15eb92e"),
			"f382ec4f-034b-411d-aab7-5b97a15eb92e",
			"10.5072/FK2/JV9LCO");
	private static final MetadataIdentifiers IDENTIFIERS_JDD_AVEC_SEPARATEURS = new MetadataIdentifiers(
			UUID.fromString("b8d83353-db25-4512-91ca-597b2754ec65"),
			"b8d83353-db25-4512-91ca-597b2754ec65",
			"10.5072/FK2/JV9LCP");
	private static final MetadataIdentifiers IDENTIFIERS_JDD_SANS_MOTS_COMMUNS_AVEC_JDD_AVEC_SEPARATEURS =
			new MetadataIdentifiers(
					UUID.fromString("c1f41d9f-2e8a-4a18-9e76-62dca7c0fe11"),
					"c1f41d9f-2e8a-4a18-9e76-62dca7c0fe11",
					"10.5072/FK2/JV9LCQ");
	private static final MetadataIdentifiers IDENTIFIERS_JDD_OUVERT = new MetadataIdentifiers(
			UUID.fromString("09b737d6-9785-4216-8a1d-478c9c39e2d4"), null, "10.5072/FK2/BNCW2B");
	private static final MetadataIdentifiers IDENTIFIERS_MINIMAL = new MetadataIdentifiers(
			UUID.fromString("fc8f0f43-80e3-458c-a320-f4efca0b24dd"), null, null);
	private static final MetadataIdentifiers IDENTIFIERS_EXISTING_DATASET = new MetadataIdentifiers(
			UUID.fromString("460ace97-81a8-43e2-8916-4a8082fcb79f"),
			"2020.11-Laennec-AQMO-air quality sensors measures",
			"10.1594/PANGAEA.726855");
	private static final MetadataIdentifiers IDENTIFIERS_JDD_PRODUCER_KEOLIS_STAR_THEME_AGRICULTURE =
			new MetadataIdentifiers(UUID.fromString("091944f4-c6ab-43e8-920d-b9c1dd78d889"), null, null);
	private static final MetadataIdentifiers IDENTIFIERS_JDD_PRODUCER_STAR_THEME_AGRICULTURE_BIOLOGIQUE =
			new MetadataIdentifiers(UUID.fromString("f05799e6-6cab-4aaa-a2c0-1cab09b2ae02"), null, null);
	private static final MetadataIdentifiers IDENTIFIERS_JDD_TO_UPDATE =
			new MetadataIdentifiers(UUID.fromString("37b41f6f-20a7-4ecb-9756-2df17e472964"), null, null);
	private static final MetadataIdentifiers IDENTIFIERS_METADATA_WITHOUT_REFERENCE_DATES_UPDATED =
			new MetadataIdentifiers(UUID.fromString("bcff2a86-064d-46d9-8e24-7a4671e38a6d"), null, null);
	private static final MetadataIdentifiers IDENTIFIERS_TRANSPORT = new MetadataIdentifiers(
			UUID.fromString("0a52ec9e-534c-4fa4-9b04-c5e967ccb78e"),
			"0a52ec9e-534c-4fa4-9b04-c5e967ccb78e",
			"10.5072/FK2/NLA4IC");

	private final OrganizationDataFactory organizationDataFactory;
	private final ContactDataFactory contactDataFactory;
	private final DictionaryEntryDataFactory dictionaryEntryDataFactory;
	private final RichDictionaryEntryDataFactory richDictionaryEntryDataFactory;
	private final MetadataTemporalSpreadDataFactory temporalSpreadDataFactory;
	private final MetadataGeographyDataFactory geographyDataFactory;
	private final MetadataDatasetSizeDataFactory datasetSizeDataFactory;
	private final ReferenceDatesDataFactory referenceDatesDataFactory;
	private final MetadataAccessConditionDataFactory accessConditionDataFactory;
	private final MetadataMetadataInfoDataFactory metadataInfoDataFactory;
	private final AvailableFormatsDataFactory availableFormatsDataFactory;
	private final DatasetService datasetService;

	@FunctionalInterface
	private interface MetadataBuilder {
		Metadata build();
	}

	private record MetadataIdentifiers(UUID globalId, String localId, String doi) {
	}

	private Metadata getOrCreate(MetadataIdentifiers identifiers, String label, MetadataBuilder builder)
			throws DataverseAPIException {
		final Metadata existing = findExisting(identifiers, label);
		if (existing != null) {
			return existing;
		}
		return createAndReturnMetadata(builder.build(), label);
	}


	/**
	 * Crée ou retourne la métadonnée 'agri' (dataset test agricole avec biogaz).
	 * Si elle existe déjà via Dataverse, la retourne. Sinon, la crée.
	 * <p>
	 * Cette métadonnée est complète avec tous les champs optionnels.
	 */
	public Metadata getOrCreateMetadataAgri() throws DataverseAPIException {
		return getOrCreate(IDENTIFIERS_AGRI, LABEL_AGRI, this::buildMetadataAgri);
	}

	public Metadata buildMetadataAgri() {
		final Metadata metadata = initializeMetadataBase(
				IDENTIFIERS_AGRI,
				"Recensement des exploitations agricoles avec production de biogaz test unitaire du 2021-04-22T15:26:31.911992100",
				Arrays.asList(
						dictionaryEntryDataFactory.createSynopsisFrench(),
						dictionaryEntryDataFactory.createSynopsisEnglish()
				),
				Arrays.asList(
						richDictionaryEntryDataFactory.createSummaryFrenchAgri(),
						richDictionaryEntryDataFactory.createSummaryEnglishAgri()
				),
				"environment",
				Arrays.asList("biogaz", "agriculture")
		);
		applyMainRelations(metadata,
				organizationDataFactory.createOrganizationRudiProducer(),
				List.of(
						contactDataFactory.createContactPierreDupont(),
						contactDataFactory.createContactRudi()
				),
				availableFormatsDataFactory.createAvailableFormatsAgriDefault());
		metadata.setResourceLanguages(Arrays.asList(Language.FR_FR, Language.EN_US));
		applyStandardMetadataTail(metadata);
		return metadata;
	}


	/**
	 * Crée ou retourne la métadonnée 'jdd-avec-separateurs'.
	 * Dataset utilisé pour tester la recherche avec séparateurs spéciaux.
	 * Si elle existe déjà via Dataverse, la retourne. Sinon, la crée.
	 */
	public Metadata getOrCreateMetadataJddAvecSeparateurs() throws DataverseAPIException {
		return getOrCreate(IDENTIFIERS_JDD_AVEC_SEPARATEURS, LABEL_JDD_AVEC_SEPARATEURS,
				this::buildMetadataJddAvecSeparateurs);
	}

	public Metadata buildMetadataJddAvecSeparateurs() {
		final Metadata metadata = initializeMetadataBase(
				IDENTIFIERS_JDD_AVEC_SEPARATEURS,
				"Ce JDD contient un titre avec des séparateurs : 2021-09-20T15:26:31.911992100 | 09/09/2021 et aussi 31/08+",
				List.of(
						dictionaryEntryDataFactory.createSynopsisFrench(),
						dictionaryEntryDataFactory.createSynopsisEnglish()
				),
				List.of(
						richDictionaryEntryDataFactory.createSummaryFrenchAgri()
				),
				"environment",
				List.of("biogaz")
		);
		applyMainRelations(metadata,
				organizationDataFactory.createOrganizationRudiProducer(),
				List.of(contactDataFactory.createContactPierreDupont()),
				availableFormatsDataFactory.createAvailableFormatsSinglePdf());
		applyStandardMetadataTail(metadata);
		return metadata;
	}


	/**
	 * Crée une métadonnée 'jdd-ouvert'.
	 * Dataset ouvert sans restrictions.
	 * Si elle existe déjà via Dataverse, la retourne. Sinon, la crée.
	 */
	public Metadata getOrCreateMetadataJddOuvert() throws DataverseAPIException {
		return getOrCreate(IDENTIFIERS_JDD_OUVERT, LABEL_JDD_OUVERT, this::buildMetadataJddOuvert);
	}

	public Metadata buildMetadataJddOuvert() {
		final Metadata metadata = initializeMetadataBase(
				IDENTIFIERS_JDD_OUVERT,
				"JDD ouvert 2021-05-27T08:58:58.489Z",
				List.of(dictionaryEntryDataFactory.createDictionaryEntryFrench("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwm")),
				List.of(richDictionaryEntryDataFactory.createSummaryCsCz()),
				"biota",
				List.of("mot-clé_1")
		);
		metadata.setResourceLanguages(List.of(Language.fromValue(LANGUAGE_CS_CZ)));
		applyMainRelations(metadata,
				organizationDataFactory.createOrganizationRudiProducer(),
				List.of(contactDataFactory.createContactPierreDupont()),
				availableFormatsDataFactory.createAvailableFormatsSinglePdf());
		applyMinimalMetadataTail(metadata);
		return metadata;
	}


	/**
	 * Crée ou retourne une métadonnée minimale (propriétés obligatoires uniquement).
	 */
	public Metadata getOrCreateMetadataMinimal() throws DataverseAPIException {
		return getOrCreate(IDENTIFIERS_MINIMAL, LABEL_MINIMAL, this::buildMetadataMinimal);
	}

	public Metadata buildMetadataMinimal() {
		final Metadata metadata = initializeMetadataBase(
				IDENTIFIERS_MINIMAL,
				"Création d'un jeu de données avec uniquement les propriétés minimales",
				List.of(dictionaryEntryDataFactory.createDictionaryEntryFrench("Test rapport version API")),
				List.of(richDictionaryEntryDataFactory.createRichDictionaryEntryFrench(LOREM_SUMMARY_TEXT)),
				"mandatory",
				List.of("localisation", "capteur", "qualité de l'air")
		);
		applyMainRelations(metadata,
				organizationDataFactory.createOrganizationMinimal(),
				List.of(contactDataFactory.createContactMinimal()),
				availableFormatsDataFactory.createAvailableFormatsMinimal());
		applyMinimalMetadataTail(metadata);
		metadata.setAccessCondition(accessConditionDataFactory.createAccessConditionMinimalApache20());
		return metadata;
	}


	/**
	 * Crée ou retourne la métadonnée du fichier existing_dataset.json.
	 */
	public Metadata getOrCreateMetadataExistingDataset() throws DataverseAPIException {
		return getOrCreate(IDENTIFIERS_EXISTING_DATASET, LABEL_EXISTING_DATASET, this::buildMetadataExistingDataset);
	}

	public Metadata buildMetadataExistingDataset() {
		final Metadata metadata = initializeMetadataBase(
				IDENTIFIERS_EXISTING_DATASET,
				"JDD créé le 09/09/2021 pour test sur le DOI",
				List.of(dictionaryEntryDataFactory.createDictionaryEntryFrench(
						"JDD créé le 09/09/2021 pour test sur le DOI = 10.1594/PANGAEA.726855")),
				List.of(richDictionaryEntryDataFactory.createRichDictionaryEntryFrench(
						"Création JDD : mon titre est à modifier. Je vais tenter de le modifier avec une requête PUT.")),
				"transportation",
				List.of("métro", "bus")
		);
		applyMainRelations(metadata,
				organizationDataFactory.createOrganizationRudiProducer(),
				List.of(contactDataFactory.createContactPierreDupont()),
				availableFormatsDataFactory.createAvailableFormatsSinglePdf());
		applyStandardMetadataTail(metadata);
		return metadata;
	}


	/**
	 * Crée ou retourne la métadonnée du cas historique de recherche sans mots communs avec
	 * {@code jdd-avec-separateurs}.
	 */
	public Metadata getOrCreateMetadataJddSansMotsCommunsAvecJddAvecSeparateurs() throws DataverseAPIException {
		return getOrCreate(IDENTIFIERS_JDD_SANS_MOTS_COMMUNS_AVEC_JDD_AVEC_SEPARATEURS,
				LABEL_JDD_SANS_MOTS_COMMUNS_AVEC_JDD_AVEC_SEPARATEURS,
				this::buildMetadataJddSansMotsCommunsAvecJddAvecSeparateurs);
	}

	public Metadata buildMetadataJddSansMotsCommunsAvecJddAvecSeparateurs() {
		final Metadata metadata = initializeMetadataBase(
				IDENTIFIERS_JDD_SANS_MOTS_COMMUNS_AVEC_JDD_AVEC_SEPARATEURS,
				"Rien en commun",
				List.of(
						dictionaryEntryDataFactory.createDictionaryEntryFrench("Rien"),
						dictionaryEntryDataFactory.createDictionaryEntryEnglish("Nothing")
				),
				List.of(richDictionaryEntryDataFactory.createSummaryFrenchAgri()),
				"environment",
				List.of("biogaz")
		);
		applyMainRelations(metadata,
				organizationDataFactory.createOrganizationRudiProducer(),
				List.of(contactDataFactory.createContactPierreDupont()),
				availableFormatsDataFactory.createAvailableFormatsSinglePdf());
		applyStandardMetadataTail(metadata);
		return metadata;
	}


	/**
	 * Crée ou retourne la métadonnée du fichier jdd_producer_keolis-star-theme-agriculture.json.
	 */
	public Metadata getOrCreateMetadataJddProducerKeolisStarThemeAgriculture() throws DataverseAPIException {
		return getOrCreate(IDENTIFIERS_JDD_PRODUCER_KEOLIS_STAR_THEME_AGRICULTURE,
				LABEL_JDD_PRODUCER_KEOLIS_STAR_THEME_AGRICULTURE,
				this::buildMetadataJddProducerKeolisStarThemeAgriculture);
	}

	public Metadata buildMetadataJddProducerKeolisStarThemeAgriculture() {
		final Metadata metadata = initializeMetadataBase(
				IDENTIFIERS_JDD_PRODUCER_KEOLIS_STAR_THEME_AGRICULTURE,
				"JDD producer keolis star avec thème agriculture",
				List.of(dictionaryEntryDataFactory.createDictionaryEntryFrench("jeux de données de kéolis en agriculture")),
				List.of(richDictionaryEntryDataFactory.createRichDictionaryEntryFrench("jeux de données de kéolis en agriculture")),
				"agriculture",
				List.of("mot-clé_1")
		);
		applyMainRelations(metadata,
				organizationDataFactory.createOrganizationKeolis(),
				List.of(contactDataFactory.createContactPierreDupont()),
				availableFormatsDataFactory.createAvailableFormatsForUpdate());
		applyMinimalMetadataTail(metadata);
		return metadata;
	}


	/**
	 * Crée ou retourne la métadonnée du fichier jdd_producer_star_theme_agriculture_biologique.json.
	 */
	public Metadata getOrCreateMetadataJddProducerStarThemeAgricultureBiologique() throws DataverseAPIException {
		return getOrCreate(IDENTIFIERS_JDD_PRODUCER_STAR_THEME_AGRICULTURE_BIOLOGIQUE,
				LABEL_JDD_PRODUCER_STAR_THEME_AGRICULTURE_BIOLOGIQUE,
				this::buildMetadataJddProducerStarThemeAgricultureBiologique);
	}

	public Metadata buildMetadataJddProducerStarThemeAgricultureBiologique() {
		final Metadata metadata = initializeMetadataBase(
				IDENTIFIERS_JDD_PRODUCER_STAR_THEME_AGRICULTURE_BIOLOGIQUE,
				"JDD producer start avec thème agriculture biologique",
				List.of(dictionaryEntryDataFactory.createDictionaryEntryFrench("jeux de données de la start en agriculture biologique")),
				List.of(richDictionaryEntryDataFactory.createRichDictionaryEntryFrench("jeux de données de la start en agriculture biologique")),
				"agriculture biologique",
				List.of("mot-clé_1")
		);
		applyMainRelations(metadata,
				organizationDataFactory.createOrganizationMinimal(),
				List.of(contactDataFactory.createContactPierreDupont()),
				availableFormatsDataFactory.createAvailableFormatsSinglePdf());
		applyMinimalMetadataTail(metadata);
		return metadata;
	}


	/**
	 * Crée ou retourne la métadonnée du fichier jdd_to_update.json.
	 */
	public Metadata getOrCreateMetadataJddToUpdate() throws DataverseAPIException {
		return getOrCreate(IDENTIFIERS_JDD_TO_UPDATE, LABEL_JDD_TO_UPDATE, this::buildMetadataJddToUpdate);
	}

	public Metadata buildMetadataJddToUpdate() {
		final Metadata metadata = initializeMetadataBase(
				IDENTIFIERS_JDD_TO_UPDATE,
				"JDD ouvert à mettre à jour",
				List.of(dictionaryEntryDataFactory.createDictionaryEntryFrench("Test de mise à jour")),
				List.of(richDictionaryEntryDataFactory.createRichDictionaryEntryFrench("Test de mise à jour")),
				"agriculture",
				List.of("biologie")
		);
		applyMainRelations(metadata,
				organizationDataFactory.createOrganizationRudiProducer(),
				List.of(contactDataFactory.createContactPierreDupont()),
				availableFormatsDataFactory.createAvailableFormatsSinglePdf());
		applyMinimalMetadataTail(metadata);
		metadata.getMetadataInfo().setMetadataDates(referenceDatesDataFactory.createReferenceDatesStandard());
		return metadata;
	}

	public void deleteMetadataJddToUpdate() throws DataverseAPIException {
		Metadata metadata = findExisting(IDENTIFIERS_JDD_TO_UPDATE, LABEL_JDD_TO_UPDATE);
		if (metadata != null) {
			deleteDatasetAndWait(metadata.getGlobalId());
		}
	}


	/**
	 * Crée ou retourne la métadonnée du fichier metadata_without_reference_dates_updated.json.
	 * Cette métadonnée est intentionnellement incomplète (sans les dates requises) pour tester la validation.
	 */
	public Metadata getOrCreateMetadataWithoutReferenceDatesUpdated() throws DataverseAPIException {
		return getOrCreate(IDENTIFIERS_METADATA_WITHOUT_REFERENCE_DATES_UPDATED,
				LABEL_METADATA_WITHOUT_REFERENCE_DATES_UPDATED,
				this::buildMetadataWithoutReferenceDatesUpdated);
	}

	public Metadata buildMetadataWithoutReferenceDatesUpdated() {
		final Metadata metadata = initializeMetadataBase(
				IDENTIFIERS_METADATA_WITHOUT_REFERENCE_DATES_UPDATED,
				"Création d'un jeu de données avec les reference dates",
				List.of(dictionaryEntryDataFactory.createDictionaryEntryFrench("Test rapport version API")),
				List.of(richDictionaryEntryDataFactory.createRichDictionaryEntryFrench(LOREM_SUMMARY_TEXT)),
				"dates",
				List.of("references", "dates")
		);
		applyMainRelations(metadata,
				organizationDataFactory.createOrganizationMinimal(),
				List.of(contactDataFactory.createContactMinimal()),
				availableFormatsDataFactory.createAvailableFormatsMinimal());
		applyMinimalMetadataTail(metadata);
		metadata.setAccessCondition(accessConditionDataFactory.createAccessConditionMinimalApache20());

		// Fixture volontairement invalide : updated manquant pour tester la validation.
		metadata.getDatasetDates().setUpdated(null);
		return metadata;
	}

	public void deleteMetadataWithoutReferenceDatesUpdated() throws DataverseAPIException {
		final Metadata metadata = findExisting(IDENTIFIERS_METADATA_WITHOUT_REFERENCE_DATES_UPDATED,
				LABEL_METADATA_WITHOUT_REFERENCE_DATES_UPDATED);
		if (metadata != null) {
			deleteDatasetAndWait(metadata.getGlobalId());
		}
	}


	/**
	 * Crée ou retourne la métadonnée du fichier transport.json.
	 */
	public Metadata getOrCreateMetadataTransport() throws DataverseAPIException {
		return getOrCreate(IDENTIFIERS_TRANSPORT, LABEL_TRANSPORT, this::buildMetadataTransport);
	}

	public Metadata buildMetadataTransport() {
		final Metadata metadata = initializeMetadataBase(
				IDENTIFIERS_TRANSPORT,
				"Réseau de transport intermodal du 2021-04-22T15:26:31.961986900",
				List.of(
						dictionaryEntryDataFactory.createDictionaryEntryFrench("Synopsis : carte du transport intermodal de la métropole"),
						dictionaryEntryDataFactory.createDictionaryEntryEnglish("Intermodal transport network synopsis")
				),
				List.of(
						richDictionaryEntryDataFactory.createRichDictionaryEntryFrench("Résumé long réseau de transport intermodal bus tram train"),
						richDictionaryEntryDataFactory.createRichDictionaryEntryEnglish("Summary intermodal transport network bus tram train ")
				),
				"transport",
				List.of("tram", "intermodalité", "bus")
		);
		applyMainRelations(metadata,
				organizationDataFactory.createOrganizationRudiProducer(),
				List.of(contactDataFactory.createContactPierreDupont()),
				availableFormatsDataFactory.createAvailableFormatsMediaConnectorMetadata());
		metadata.setResourceLanguages(List.of(Language.FR_FR, Language.EN_US));
		applyStandardMetadataTail(metadata);
		return metadata;
	}

	/**
	 * Crée la métadonnée dans Dataverse et renvoie la version persistée si disponible.
	 */
	private Metadata createAndReturnMetadata(Metadata metadata, String label) throws DataverseAPIException {
		final String createdDoi = datasetService.createDataset(metadata);

		if (StringUtils.isEmpty(createdDoi)) {
			log.error("Metadata {} créée dans Dataverse ? Mais pas de DOI : {}", label, createdDoi);
			return null;
		}

		metadata.setDataverseDoi(createdDoi);

		await().timeout(10, TimeUnit.SECONDS).pollInterval(Duration.of(1, ChronoUnit.SECONDS)).until(() -> {
			try {
				boolean trouve = datasetService.getDataset(metadata.getGlobalId()) != null;
				log.info("dataset trouvé ? {} doi {}, globalId {}, label {}", trouve, createdDoi, metadata.getGlobalId(), label);
				return trouve;
			} catch (Exception e) {
				log.info("dataset PAS trouvé ! doi {}, globalId {}, label {}", createdDoi, metadata.getGlobalId(), label);
				return false; // Ignore l'exception et continue les retentatives
			}
		});

		return datasetService.getDataset(metadata.getGlobalId());
	}

	/**
	 * Recherche une métadonnée existante d'abord par DOI puis par globalId.
	 */
	private Metadata findExisting(MetadataIdentifiers identifiers, String label) {
		Metadata existingByDoi = null;
		if (identifiers.doi() != null) {
			existingByDoi = findExistingByDoi(identifiers.doi(), label);
		}

		Metadata existingByGlobalId = null;
		if (identifiers.globalId() != null) {
			existingByGlobalId = findExistingByGlobalId(identifiers.globalId(), label);
		}

		return existingByDoi != null ? existingByDoi : existingByGlobalId;
	}

	/**
	 * Recherche une métadonnée existante par DOI.
	 */
	private Metadata findExistingByDoi(String doi, String label) {
		if (StringUtils.isEmpty(doi)) {
			return null;
		}

		final String persistentId = doi.startsWith(DOI_PREFIX) ? doi : DOI_PREFIX + doi;
		try {
			final Metadata existing = datasetService.getDataset(persistentId);
			if (existing != null) {
				log.info("Metadata {} trouvée dans Dataverse : {}", label, persistentId);
			}
			return existing;
		} catch (DataverseAPIException | RuntimeException e) {
			log.debug("Metadata {} non trouvée dans Dataverse ({}), création en cours", label, persistentId);
			return null;
		}
	}

	/**
	 * Recherche une métadonnée existante par globalId.
	 */
	private Metadata findExistingByGlobalId(UUID globalId, String label) {
		try {
			final Metadata existing = datasetService.getDataset(globalId);
			if (existing != null) {
				log.info("Metadata {} trouvée dans Dataverse avec globalId {}", label, globalId);
			}
			return existing;
		} catch (DataverseAPIException e) {
			log.debug("Metadata {} non trouvée avec globalId {}, création en cours", label, globalId);
			return null;
		}
	}

	/**
	 * Applique les identifiants techniques à la métadonnée.
	 */
	private void applyIdentifiers(Metadata metadata, MetadataIdentifiers identifiers) {
		metadata.setGlobalId(identifiers.globalId());
		metadata.setLocalId(identifiers.localId());
		metadata.setDoi(identifiers.doi());
	}

	/**
	 * Initialise le tronc commun des métadonnées de test.
	 */
	private Metadata initializeMetadataBase(MetadataIdentifiers identifiers, String resourceTitle,
			List<DictionaryEntry> synopsis, List<RichDictionaryEntry> summary, String theme, List<String> keywords) {
		final Metadata metadata = new Metadata();
		applyIdentifiers(metadata, identifiers);
		metadata.setResourceTitle(resourceTitle);
		metadata.setSynopsis(synopsis);
		metadata.setSummary(summary);
		metadata.setTheme(theme);
		metadata.setKeywords(keywords);
		return metadata;
	}

	/**
	 * Applique les relations principales de métadonnée (producteur, contacts, formats).
	 */
	private void applyMainRelations(Metadata metadata, Organization producer, List<Contact> contacts,
			List<Media> availableFormats) {
		metadata.setProducer(producer);
		metadata.setContacts(contacts);
		metadata.setAvailableFormats(availableFormats);
	}

	/**
	 * Applique les champs de fin de métadonnée pour les jeux complets.
	 */
	private void applyStandardMetadataTail(Metadata metadata) {
		metadata.setTemporalSpread(temporalSpreadDataFactory.createTemporalSpreadStandard());
		metadata.setGeography(geographyDataFactory.createGeographyStandard());
		metadata.setDatasetSize(datasetSizeDataFactory.createDatasetSizeStandard());
		metadata.setDatasetDates(referenceDatesDataFactory.createReferenceDatesStandard());
		metadata.setAccessCondition(accessConditionDataFactory.createAccessConditionStandard());
		metadata.setMetadataInfo(metadataInfoDataFactory.createMetadataMetadataInfoStandard());
		metadata.setStorageStatus(Metadata.StorageStatusEnum.ONLINE);
	}

	/**
	 * Applique les champs de fin de métadonnée pour les jeux minimaux.
	 */
	private void applyMinimalMetadataTail(Metadata metadata) {
		metadata.setDatasetDates(referenceDatesDataFactory.createReferenceDatesMinimal());
		metadata.setAccessCondition(accessConditionDataFactory.createAccessConditionMinimal());
		metadata.setMetadataInfo(metadataInfoDataFactory.createMetadataMetadataInfoMinimal());
		metadata.setStorageStatus(Metadata.StorageStatusEnum.ONLINE);
	}

	/**
	 * Supprime tous les datasets créés par cette datafactory.
	 * Utile pour réinitialiser l'état de la base de test.
	 */
	public void cleanupAllCreatedDatasets() {
		final List<MetadataIdentifiers> allDatasets = List.of(
				IDENTIFIERS_AGRI,
				IDENTIFIERS_JDD_AVEC_SEPARATEURS,
				IDENTIFIERS_JDD_SANS_MOTS_COMMUNS_AVEC_JDD_AVEC_SEPARATEURS,
				IDENTIFIERS_JDD_OUVERT,
				IDENTIFIERS_MINIMAL,
				IDENTIFIERS_EXISTING_DATASET,
				IDENTIFIERS_JDD_PRODUCER_KEOLIS_STAR_THEME_AGRICULTURE,
				IDENTIFIERS_JDD_PRODUCER_STAR_THEME_AGRICULTURE_BIOLOGIQUE,
				IDENTIFIERS_JDD_TO_UPDATE,
				IDENTIFIERS_METADATA_WITHOUT_REFERENCE_DATES_UPDATED,
				IDENTIFIERS_TRANSPORT
		);

		allDatasets.forEach(identifiers -> {
			try {
				deleteDatasetAndWait(identifiers.globalId());
				log.info("Dataset supprimé avec globalId: {}", identifiers.globalId());
			} catch (Exception e) {
				log.debug("Dataset {} n'existe pas ou erreur lors de la suppression: {}",
						identifiers.globalId(), e.getMessage());
			}
		});
	}

	private void deleteDatasetAndWait(UUID globalId) throws DataverseAPIException {
		datasetService.deleteDataset(globalId);
		await().timeout(10, TimeUnit.SECONDS).pollInterval(Duration.of(1, ChronoUnit.SECONDS)).until(() -> {
			try {
				return !datasetService.datasetExists(globalId);
			} catch (DataverseAPIException | RuntimeException e) {
				// En cas d'erreur de lecture post-delete, on considère le dataset comme supprimé.
				return true;
			}
		});
	}

}
