package org.rudi.microservice.konsult.service.metadata.impl;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.rudi.facet.dataverse.api.exceptions.DataverseAPIException;
import org.rudi.facet.kaccess.bean.Metadata;
import org.rudi.microservice.konsult.service.KonsultSpringBootTest;
import org.rudi.microservice.konsult.service.datafactory.MetadataDataFactory;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.RequiredArgsConstructor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de l'algorithme MetadataWithSameThemeFinder.
 *
 * Ces tests utilisent la MetadataDataFactory pour créer des données de référence stables.
 * Les assertions sont écrites pour rester valides même si d'autres données existent dans Dataverse,
 * ce qui reflète une base de données réelle.
 *
 * On demande un nombre de résultats suffisamment grand (100) pour s'assurer que les datasets
 * créés apparaissent bien dans les résultats, indépendamment des autres données présentes.
 */
@KonsultSpringBootTest
@RequiredArgsConstructor
class MetadataWithSameThemeFinderIT {

	private static final int LARGE_RESULT_COUNT = 100;

	@Autowired
	private MetadataWithSameThemeFinder metadataWithSameThemeFinder;
	@Autowired
	private MetadataDataFactory metadataDataFactory;

	@Test
	void find_theme() throws DataverseAPIException, IOException {
		final String baseDatasetDoi = metadataDataFactory.getOrCreateBase();
		final String datasetWithSameThemeDoi = metadataDataFactory.getOrCreateSameTheme();
		final String datasetWithDifferentThemeDoi = metadataDataFactory.getOrCreateDifferentTheme();

		final List<Metadata> metadataList = metadataWithSameThemeFinder.find(baseDatasetDoi, LARGE_RESULT_COUNT);

		assertThat(metadataList).extracting("dataverseDoi")
				.as("Les jeux de données affichés doivent avoir le même thème que le jeu de donnée de la page")
				.contains(datasetWithSameThemeDoi)
				.doesNotContain(datasetWithDifferentThemeDoi);
	}

	@Test
	void find_producer() throws DataverseAPIException, IOException {
		final String baseDatasetDoi = metadataDataFactory.getOrCreateBase();
		final String datasetWithSameProducer = metadataDataFactory.getOrCreateProducerRudi();
		final String datasetWithDifferentProducer1 = metadataDataFactory.getOrCreateProducerAdeme3Keywords();
		final String datasetWithDifferentProducer2 = metadataDataFactory.getOrCreateProducerIrisa();
		final String datasetWithDifferentProducer3 = metadataDataFactory.getOrCreateProducerRm();

		final List<Metadata> metadataList = metadataWithSameThemeFinder.find(baseDatasetDoi, LARGE_RESULT_COUNT);
 		final List<String> doiList = metadataList.stream().map(Metadata::getDataverseDoi).toList();

		// Les 3 producteurs différents doivent être présents au début, le producteur rudi à la fin (s'il est présent)
		assertThat(doiList)
				.as("Les producteurs différents doivent apparaître dans les résultats")
				.contains(datasetWithDifferentProducer1, datasetWithDifferentProducer2, datasetWithDifferentProducer3);

		if (doiList.contains(datasetWithSameProducer)) {
			final int indexProducerDifferent1 = doiList.indexOf(datasetWithDifferentProducer1);
			final int indexProducerDifferent2 = doiList.indexOf(datasetWithDifferentProducer2);
			final int indexProducerDifferent3 = doiList.indexOf(datasetWithDifferentProducer3);
			final int indexProducerSame = doiList.indexOf(datasetWithSameProducer);

			assertThat(indexProducerSame)
					.as("Le producteur RUDI doit être positionné après les producteurs différents (plus faible priorité)")
					.isGreaterThan(Math.max(Math.max(indexProducerDifferent1, indexProducerDifferent2), indexProducerDifferent3));
		}
	}

	@Test
	void find_producer_keywords() throws DataverseAPIException, IOException {
		final String baseDatasetDoi = metadataDataFactory.getOrCreateBase();
		// On utilise le 3 keyWords car c'est celui qui est retourné en priorité pour le producteur ademe,
		// parce que l'algorithme retourne préférentiellement une metadata avec plus de mots-clefs en commun avec la base.
		final String datasetProducerAWithCommonKeywords = metadataDataFactory.getOrCreateProducerAdeme3Keywords();
		final String datasetProducerAWithoutKeywords = metadataDataFactory.getOrCreateProducerAdeme0Keyword();
		final String datasetProducerBWithCommonKeywords = metadataDataFactory.getOrCreateProducerIrisa();
		final String datasetProducerBWithoutKeywords = metadataDataFactory.getOrCreateProducerIrisa0Keyword();
		final String datasetProducerCWithCommonKeywords = metadataDataFactory.getOrCreateProducerRm();
		final String datasetProducerCWithoutKeywords = metadataDataFactory.getOrCreateProducerRm0Keyword();

		final List<Metadata> metadataList = metadataWithSameThemeFinder.find(baseDatasetDoi, LARGE_RESULT_COUNT);

		assertThat(metadataList).extracting("dataverseDoi")
				.as("Pour un même producteur, on affiche le jdd avec le plus de mots clefs en commun. "
						+ "Les datasets avec mots-clefs communs doivent apparaître, pas les autres.")
				.contains(datasetProducerAWithCommonKeywords, datasetProducerBWithCommonKeywords, datasetProducerCWithCommonKeywords)
				.doesNotContain(datasetProducerAWithoutKeywords, datasetProducerBWithoutKeywords, datasetProducerCWithoutKeywords);
	}

	@Test
	void find_keywords() throws DataverseAPIException, IOException {
		final String baseDatasetDoi = metadataDataFactory.getOrCreateBase();
		final String datasetWith0Keyword = metadataDataFactory.getOrCreateProducerAdeme0Keyword();
		final String datasetWith1Keyword = metadataDataFactory.getOrCreateProducerAdeme1Keyword();
		final String datasetWith2Keywords = metadataDataFactory.getOrCreateProducerAdeme2Keywords();
		final String datasetWith3Keywords = metadataDataFactory.getOrCreateProducerAdeme3Keywords();

		final List<Metadata> metadataList = metadataWithSameThemeFinder.find(baseDatasetDoi, LARGE_RESULT_COUNT);

		assertThat(metadataList).extracting("dataverseDoi")
				.as("Pour le producteur ademe, seul le jdd avec le plus de mots clefs doit apparaître")
				.contains(datasetWith3Keywords)
				.doesNotContain(datasetWith0Keyword, datasetWith1Keyword, datasetWith2Keywords);
	}

	@Test
	void find_minimal() throws DataverseAPIException, IOException {
		final String baseDatasetDoi = metadataDataFactory.getOrCreateBaseChildren();
		final String datasetWith0Keyword = metadataDataFactory.getOrCreateProducerAdemeChildren0Keyword();

		final List<Metadata> metadataList = metadataWithSameThemeFinder.find(baseDatasetDoi, LARGE_RESULT_COUNT);

		assertThat(metadataList).extracting("dataverseDoi")
				.as("On prend en compte les JDD qui n'ont aucun mot-clé en commun, pour avoir un maximum "
						+ "de JDD parmis ceux disponibles")
				.contains(datasetWith0Keyword)
				.as("On doit quand même exclure le JDD de base")
				.doesNotContain(baseDatasetDoi);
	}
}
