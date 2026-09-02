package org.rudi.facet.kaccess.datafactory;

import org.rudi.facet.kaccess.bean.MetadataDatasetSize;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * DataFactory pour créer les objets MetadataDatasetSize.
 * Représente la taille (nombre d'enregistrements et de champs) d'un dataset.
 */
@Component
@RequiredArgsConstructor
public class MetadataDatasetSizeDataFactory {

	/**
	 * Crée une taille de dataset standard.
	 * // Aucun get possible
	 */
	public MetadataDatasetSize createDatasetSizeStandard() {
		MetadataDatasetSize size = new MetadataDatasetSize();
		size.setNumbersOfRecords(50);
		size.setNumberOfFields(15);
		return size;
	}

}

