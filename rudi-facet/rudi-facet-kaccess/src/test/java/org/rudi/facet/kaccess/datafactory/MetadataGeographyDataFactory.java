package org.rudi.facet.kaccess.datafactory;

import java.math.BigDecimal;

import org.rudi.facet.kaccess.bean.MetadataGeography;
import org.rudi.facet.kaccess.bean.MetadataGeographyBoundingBox;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer les objets MetadataGeography.
 * Représente les données géographiques d'un dataset (coordonnées, zone de couverture).
 */
@Component
@RequiredArgsConstructor
public class MetadataGeographyDataFactory {

	/**
	 * Crée une géographie standard (données cohérentes avec les fichiers de test).
	 * IMPORTANT : Les coordonnées [west_longitude=18, east_longitude=10.1] sont volontairement inversées
	 * pour correspondre aux données du fichier agri.json. Cette configuration peut sembler géographiquement
	 * incorrecte mais est maintenue pour la cohérence des tests.
	 * // Aucun get possible
	 */
	public MetadataGeography createGeographyStandard() {
		MetadataGeography geography = new MetadataGeography();

		// Bounding box (NOTE: west_longitude > east_longitude est inversé mais cohérent avec les données de test)
		MetadataGeographyBoundingBox bbox = new MetadataGeographyBoundingBox();
		bbox.setNorthLatitude(BigDecimal.valueOf(71.0));
		bbox.setSouthLatitude(BigDecimal.valueOf(42.0));
		bbox.setWestLongitude(BigDecimal.valueOf(18.0));
		bbox.setEastLongitude(BigDecimal.valueOf(10.1));
		geography.setBoundingBox(bbox);

		// Projection géographique standard
		geography.setProjection("EPSG:4326");

		// Note : Le geographic_distribution (GeoJSON) est optionnel et complexe
		// Il peut être ajouté ultérieurement si nécessaire

		return geography;
	}

}

